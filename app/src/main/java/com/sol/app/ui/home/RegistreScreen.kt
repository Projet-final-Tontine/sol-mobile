package com.sol.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sol.app.data.BlocRegistreResponse
import com.sol.app.data.Network
import com.sol.app.data.VerificationRegistreResponse
import com.sol.app.data.messageErreur
import com.sol.app.data.tr

private val VERT = Color(0xFF1B8A4E)
private val ROUGE = Color(0xFFC62828)

/**
 * Écran « Registre Inviolable ».
 * Affiche l'attestation d'intégrité (chaîne à hash chaîné vérifiée par le
 * serveur) puis la liste des blocs scellés — chaque mouvement d'argent de la
 * plateforme y laisse une trace impossible à falsifier.
 */
@Composable
fun EcranRegistre(onFermer: () -> Unit) {
    var verif by remember { mutableStateOf<VerificationRegistreResponse?>(null) }
    var blocs by remember { mutableStateOf<List<BlocRegistreResponse>>(emptyList()) }
    var erreur by remember { mutableStateOf<String?>(null) }
    var enCours by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            verif = Network.api.verifierRegistre()
            blocs = Network.api.registre()
        } catch (e: Throwable) {
            erreur = messageErreur(e)
        } finally {
            enCours = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // Barre du haut
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onFermer) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = tr("Retour", "Tounen"))
            }
            Text(
                tr("Registre Inviolable", "Rejis Enfalsifyab"),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        }

        when {
            enCours -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            erreur != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(erreur!!, color = ROUGE, modifier = Modifier.padding(24.dp))
            }

            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item { verif?.let { CarteIntegrite(it) } }
                item {
                    Text(
                        tr(
                            "Chaque mouvement d'argent est scellé par une empreinte reliée à la précédente. Modifier une seule ligne briserait toute la chaîne.",
                            "Chak deplasman lajan sele ak yon anprent ki makònen ak sa anvan an. Chanje yon sèl liy ap kraze tout chèn nan.",
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp),
                    )
                }
                if (blocs.isEmpty()) {
                    item {
                        Text(
                            tr("Aucune transaction scellée pour l'instant.", "Poko gen okenn tranzaksyon sele."),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 12.dp),
                        )
                    }
                } else {
                    items(blocs) { bloc -> CarteBloc(bloc) }
                }
                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
private fun CarteIntegrite(v: VerificationRegistreResponse) {
    val ok = v.intacte
    val accent = if (ok) VERT else ROUGE
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(accent.copy(alpha = 0.14f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (ok) Icons.Default.Lock else Icons.Default.Warning,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(32.dp),
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                if (ok) tr("Registre inviolable", "Rejis enfalsifyab")
                else tr("Falsification détectée", "Yo detekte yon falsifikasyon"),
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = accent,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                if (ok) tr(
                    "${v.nombreBlocs} écritures authentiques et non modifiées.",
                    "${v.nombreBlocs} ekriti otantik ki pa chanje.",
                ) else tr(
                    "Rupture au bloc #${v.positionRupture}. La comptabilité a été altérée.",
                    "Kase nan blòk #${v.positionRupture}. Kontablite a chanje.",
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (ok && !v.empreinteGlobale.isNullOrBlank()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    tr("Empreinte globale", "Anprent global"),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    v.empreinteGlobale.take(24) + "…",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun CarteBloc(bloc: BlocRegistreResponse) {
    val credit = bloc.sens.equals("CREDIT", ignoreCase = true)
    val couleurMontant = if (credit) VERT else ROUGE
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.Gavel,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "#${bloc.position} · ${libelleType(bloc.type)}",
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (!bloc.description.isNullOrBlank()) {
                    Text(
                        bloc.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    "🔗 " + bloc.hash.take(16) + "…",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                (if (credit) "+" else "−") + formaterMontant(bloc.montant) + " HTG",
                color = couleurMontant,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

private fun libelleType(type: String): String = when (type.uppercase()) {
    "DEPOT" -> tr("Dépôt", "Depo")
    "COTISATION" -> tr("Cotisation", "Kotizasyon")
    "GAIN_MAIN" -> tr("Réception de la main", "Resepsyon men an")
    else -> type.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }
}

private fun formaterMontant(montant: Double): String {
    return "%,.2f".format(montant).replace(',', ' ')
}
