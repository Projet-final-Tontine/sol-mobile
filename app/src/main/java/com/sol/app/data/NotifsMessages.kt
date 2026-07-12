package com.sol.app.data

import android.Manifest
import android.annotation.SuppressLint
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
//  NOTIFICATIONS DE MESSAGES  —  chat de groupe et prive, meme app fermee.
//
//  Un travail de fond (WorkManager) interroge ~toutes les 15 min l'endpoint
//  /api/messages/recents pour savoir s'il y a de nouveaux messages destines a
//  l'utilisateur depuis le dernier controle, et affiche une notification.
//  Sans Firebase : le delai peut donc atteindre ~15 min (limite Android).
// ============================================================================

private const val CANAL_MESSAGES = "messages_chat"
private const val TRAVAIL_PERIODIQUE = "notif_messages_periodique"
private const val TRAVAIL_IMMEDIAT = "notif_messages_immediat"
private const val ID_NOTIFICATION = 2001

object NotifsMessages {

    /** Cree le canal de notification (obligatoire depuis Android 8 / Oreo). */
    fun creerCanal(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                CANAL_MESSAGES,
                "Messages",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "Notifications des nouveaux messages (groupe et privé)."
            }
            context.getSystemService(NotificationManager::class.java)
                ?.createNotificationChannel(canal)
        }
    }

    /** Programme la verification periodique en tache de fond (meme app fermee). */
    fun planifier(context: Context) {
        creerCanal(context)
        val requete = PeriodicWorkRequestBuilder<MessagesWorker>(15, TimeUnit.MINUTES).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            TRAVAIL_PERIODIQUE,
            ExistingPeriodicWorkPolicy.KEEP,
            requete,
        )
    }

    /** Verifie tout de suite (a l'ouverture de l'app). */
    fun verifierMaintenant(context: Context) {
        creerCanal(context)
        val requete = OneTimeWorkRequestBuilder<MessagesWorker>().build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            TRAVAIL_IMMEDIAT,
            ExistingWorkPolicy.REPLACE,
            requete,
        )
    }

    /** Arrete les notifications de messages (ex : a la deconnexion). */
    fun annuler(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(TRAVAIL_PERIODIQUE)
    }
}

/**
 * Travail de fond : lit les messages recents destines a l'utilisateur et
 * affiche une notification s'il y en a de nouveaux depuis le dernier controle.
 * La borne temporelle est celle du dernier message recu (heure serveur) pour
 * eviter tout decalage d'horloge entre le telephone et le serveur.
 */
class MessagesWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (!Session.estConnecte) return Result.success()

        return try {
            val premierControle = Session.dernierCheckMessages == null
            val messages = Network.api.messagesRecents(Session.dernierCheckMessages)

            // Avance la borne : dernier message recu (heure serveur), sinon maintenant.
            Session.dernierCheckMessages =
                messages.lastOrNull()?.dateEnvoi ?: maintenantIso()

            // Au tout premier passage, on ne notifie pas les messages deja anciens.
            if (premierControle || messages.isEmpty()) return Result.success()

            val titre: String
            val texte: String
            if (messages.size == 1) {
                val m = messages.first()
                titre = (m.expediteurNom ?: tr("Nouveau message", "Nouvo mesaj")) +
                    (m.solNom?.let { " · $it" } ?: "")
                texte = m.apercu ?: tr("Nouveau message", "Nouvo mesaj")
            } else {
                titre = tr("${messages.size} nouveaux messages", "${messages.size} nouvo mesaj")
                texte = messages.takeLast(4).joinToString("\n") {
                    "• " + (it.expediteurNom ?: "") + " : " + (it.apercu ?: "")
                }
            }

            afficherNotification(applicationContext, titre, texte)
            Result.success()
        } catch (_: Throwable) {
            // Reseau indisponible : on retentera plus tard.
            Result.retry()
        }
    }

    private fun maintenantIso(): String =
        java.time.LocalDateTime.now()
            .format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME)
}

/** Affiche la notification de message (respecte la permission Android 13+). */
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

    val notification = NotificationCompat.Builder(context, CANAL_MESSAGES)
        .setSmallIcon(android.R.drawable.ic_dialog_email)
        .setContentTitle(titre)
        .setContentText(texte)
        .setStyle(NotificationCompat.BigTextStyle().bigText(texte))
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .setContentIntent(pending)
        .build()

    NotificationManagerCompat.from(context).notify(ID_NOTIFICATION, notification)
}
