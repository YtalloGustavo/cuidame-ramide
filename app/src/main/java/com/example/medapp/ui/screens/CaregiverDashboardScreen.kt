package com.example.medapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medapp.ui.theme.*

@Composable
fun CaregiverDashboardScreen(
    adherenceRate: Int,
    doses: List<Dose>,
    onConfirmDose: (String) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var reminderMessage by remember { mutableStateOf("") }
    val nextPendingDose = doses.firstOrNull { it.status == DoseStatus.Pendente }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Background)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp)
    ) {
        // Topbar / Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Painel do Cuidador",
                        color = Ink3,
                        fontSize = 12.sp,
                        fontFamily = EpilogueFontFamily
                    )
                    Text(
                        text = "Acompanhando: João",
                        color = Ink,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = SyneFontFamily
                    )
                }

                // Logout Button
                IconButton(
                    onClick = onLogout,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(RedLight)
                ) {
                    Text(text = "🚪", fontSize = 16.sp)
                }
            }
        }

        // Feedback toast notification mockup
        item {
            AnimatedVisibility(visible = reminderMessage.isNotEmpty()) {
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = GreenLight),
                    border = BorderStroke(1.dp, GreenPrimary.copy(alpha = 0.2f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { reminderMessage = "" }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "📲", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = reminderMessage,
                            color = GreenPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = EpilogueFontFamily
                        )
                    }
                }
            }
        }

        // Metrics Summary Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Surface),
                border = BorderStroke(1.dp, Border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ADESÃO DO PACIENTE",
                            color = Ink3,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            fontFamily = EpilogueFontFamily
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "João Silva",
                            color = Ink,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = SyneFontFamily
                        )
                        Text(
                            text = "Últimos 7 dias de tratamento",
                            color = Ink3,
                            fontSize = 11.sp,
                            fontFamily = EpilogueFontFamily
                        )
                    }
                    
                    // Adhesion Ring/Metric representation
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(30.dp))
                            .background(GreenLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$adherenceRate%",
                            color = GreenPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = SyneFontFamily
                        )
                    }
                }
            }
        }

        // Action Card (WhatsApp reminder/Confirm remote)
        item {
            AnimatedVisibility(visible = nextPendingDose != null) {
                if (nextPendingDose != null) {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Surface),
                        border = BorderStroke(1.dp, Border),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp)
                        ) {
                            Text(
                                text = "ALERTA: PENDENTE HÁ 30 MIN",
                                color = Amber,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                                fontFamily = EpilogueFontFamily
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = nextPendingDose.name,
                                color = Ink,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = SyneFontFamily
                            )
                            Text(
                                text = "Dose: ${nextPendingDose.doseDescription} às ${nextPendingDose.time}",
                                color = Ink2,
                                fontSize = 12.sp,
                                fontFamily = EpilogueFontFamily
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { 
                                        reminderMessage = "Disparo efetuado via WhatsApp para +55 81 99999-0000!" 
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "Cobrar Paciente",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = SyneFontFamily
                                    )
                                }

                                Button(
                                    onClick = { 
                                        onConfirmDose(nextPendingDose.id)
                                        reminderMessage = "Você confirmou a dose do João com sucesso!"
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = GreenLight),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "Confirmar Dose",
                                        color = GreenPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = SyneFontFamily
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section label
        item {
            Text(
                text = "STATUS DE HOJE",
                color = Ink3,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                fontFamily = EpilogueFontFamily,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        // Patient Doses Rows (Caregiver view)
        items(doses, key = { it.id }) { dose ->
            DoseRow(
                dose = dose,
                onRowClick = {
                    if (dose.status == DoseStatus.Pendente) {
                        onConfirmDose(dose.id)
                        reminderMessage = "Você confirmou a dose do João com sucesso!"
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CaregiverDashboardScreenPreview() {
    MedAppTheme {
        CaregiverDashboardScreen(
            adherenceRate = 87,
            doses = listOf(
                Dose("1", "Dipirona 500mg", "1 comprimido", "08:00", DoseStatus.Tomado),
                Dose("2", "Omeprazol 20mg", "1 cápsula", "12:00", DoseStatus.Pendente),
                Dose("3", "Losartana 50mg", "1 comprimido", "20:00", DoseStatus.MaisTarde)
            ),
            onConfirmDose = {},
            onLogout = {}
        )
    }
}
