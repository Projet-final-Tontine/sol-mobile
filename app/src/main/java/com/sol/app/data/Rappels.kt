package com.sol.app.data

import android.annotation.SuppressLint
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.sol.app.MainActivity
import java.util.concurrent.TimeUnit

// ============================================================================
//  RAPPELS DE COTISATION  —  notifications locales, meme quand l'app est fermee.
//
//  Un travail de fond (WorkManager) verifie ~1x/jour les cotisations de
//  l'utilisateur connecte. Si l'une est due bientot ou en retard et pas encore
//  payee, une notification s'affiche sur le telephone — sans ouvrir l'app,
//  sans serveur allume, sans compte externe (Firebase). 100 % hors-ligne cote
//  declenchement (une simple requete reseau sert a lire les echeances).
// ============================================================================

private const val CANAL_RAPPELS = "rappels_cotisation"
private const val TRAVAIL_PERIODIQUE = "rappel_cotisation_periodique"
private const val TRAVAIL_IMMEDIAT = "rappel_cotisation_immediat"
private const val ID_NOTIFICATION = 1001

object Rappels {

    /** Cree le canal de notification (obligatoire depuis Android 8 / Oreo). */
    fun creerCanal(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                CANAL_RAPPELS,
                "Rappels de cotisation",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "Rappels quand une cotisation approche ou est en retard."
            }
            context.getSystemService(NotificationManager::class.java)
                ?.createNotificationChannel(canal)
        }
    }

    /** Programme la verification quotidienne en tache de fond (meme app fermee). */
    fun planifier(context: Context) {
        creerCanal(context)
        val requete = PeriodicWorkRequestBuilder<RappelWorker>(1, TimeUnit.DAYS).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            TRAVAIL_PERIODIQUE,
            ExistingPeriodicWorkPolicy.KEEP,
            requete,
        )
    }

    /** Verifie tout de suite (a l'ouverture de l'app, ou pour tester le rappel). */
    fun verifierMaintenant(context: Context) {
        creerCanal(context)
        val requete = OneTimeWorkRequestBuilder<RappelWorker>().build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            TRAVAIL_IMMEDIAT,
            ExistingWorkPolicy.REPLACE,
            requete,
        )
    }

    /** Arrete les rappels (ex : a la deconnexion). */
    fun annuler(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(TRAVAIL_PERIODIQUE)
    }
}

/**
 * Travail de fond execute par WorkManager : lit les cotisations de l'utilisateur
 * et affiche une notification si l'une est due sous 1 jour ou en retard.
 */
class RappelWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        // Personne de connecte : rien a rappeler.
        if (!Session.estConnecte) return Result.success()

        return try {
            val cotisations = Network.api.mesCotisations()
            val sols = Network.api.mesSols().associateBy { it.id }
            val aujourdHui = java.time.LocalDate.now()
            val limite = aujourdHui.plusDays(1)

            // Cotisations non reglees, echeance passee ou dans <= 1 jour.
            val aRappeler = cotisations.mapNotNull { c ->
                if (c.statut.equals("VALIDE", true)) return@mapNotNull null
                val date = try {
                    c.dateEcheance?.take(10)?.let { java.time.LocalDate.parse(it) }
                } catch (_: Throwable) {
                    null
                } ?: return@mapNotNull null
                if (date.isAfter(limite)) null else c to date
            }.sortedBy { it.second }

            if (aRappeler.isEmpty()) return Result.success()

            val (cotisation, date) = aRappeler.first()
            val nomSol = sols[cotisation.solId]?.nom ?: tr("un de tes Sols", "youn nan Sòl ou yo")
            val montant = (cotisation.montantPaye ?: cotisation.montantAttendu).toLong()
            val quand = when {
                date.isBefore(aujourdHui) -> tr("est en retard", "an reta")
                date.isEqual(aujourdHui) -> tr("est due aujourd'hui", "dwe jodi a")
                else -> tr("est due demain", "dwe demen")
            }

            val titre = if (aRappeler.size > 1) {
                tr(
                    "Tu as ${aRappeler.size} cotisations à payer",
                    "Ou gen ${aRappeler.size} kotizasyon pou peye",
                )
            } else {
                tr("Rappel de cotisation", "Rapèl kotizasyon")
            }
            val texte = tr(
                "Ta cotisation de $montant HTG pour « $nomSol » $quand. 💰",
                "Kotizasyon $montant HTG pou « $nomSol » $quand. 💰",
            )

            afficherNotification(applicationContext, titre, texte)
            Result.success()
        } catch (_: Throwable) {
            // Reseau indisponible : on retentera plus tard.
            Result.retry()
        }
    }
}

/** Affiche la notification de rappel (respecte la permission Android 13+). */
@SuppressLint("MissingPermission")
private fun afficherNotification(context: Context, titre: String, texte: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
        != PackageManager.PERMISSION_GRANTED
    ) {
        return
    }

    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    val pending = PendingIntent.getActivity(
        context, 0, intent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )

    val notification = NotificationCompat.Builder(context, CANAL_RAPPELS)
        .setSmallIcon(android.R.drawable.ic_popup_reminder)
        .setContentTitle(titre)
        .setContentText(texte)
        .setStyle(NotificationCompat.BigTextStyle().bigText(texte))
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .setContentIntent(pending)
        .build()

    NotificationManagerCompat.from(context).notify(ID_NOTIFICATION, notification)
}
