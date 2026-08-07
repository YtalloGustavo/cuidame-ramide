package com.example.medapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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

// Data structures for state
data class Dose(
    val id: String,
    val name: String,
    val doseDescription: String,
    val time: String,
    val status: DoseStatus,
    val ean: String? = null,
    val laboratory: String? = null,
    val activeIngredient: String? = null
)

enum class DoseStatus {
    Tomado, Pendente, MaisTarde, Perdido
}

@Composable
fun HomeScreen(
    userName: String,
    doses: List<Dose>,
    onConfirmDose: (String) -> Unit,
    onDelayDose: (String) -> Unit = {},
    onMissDose: (String) -> Unit = {},
    onDeleteDose: (String) -> Unit = {},
    onUpdateDose: (Dose) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Find next pending dose to highlight in the hero card
    val nextPendingDose = doses.firstOrNull { it.status == DoseStatus.Pendente }
    var selectedDose by remember { mutableStateOf<Dose?>(null) }
    val totalDoses = doses.size
    val takenDoses = doses.count { it.status == DoseStatus.Tomado }
    val pendingDoses = doses.count { it.status == DoseStatus.Pendente }
    val adherence = if (totalDoses == 0) 0 else (takenDoses * 100) / totalDoses

    selectedDose?.let { dose ->
        DoseDetailsDialog(
            dose = dose,
            onDismiss = { selectedDose = null },
            onConfirmDose = {
                onConfirmDose(dose.id)
                selectedDose = null
            },
            onDelayDose = {
                onDelayDose(dose.id)
                selectedDose = null
            },
            onMissDose = {
                onMissDose(dose.id)
                selectedDose = null
            },
            onDeleteDose = {
                onDeleteDose(dose.id)
                selectedDose = null
            },
            onUpdateDose = {
                onUpdateDose(it)
                selectedDose = null
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Background)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp)
    ) {
        // Topbar
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = "Bom dia,",
                    color = Ink3,
                    fontSize = 13.sp,
                    fontFamily = EpilogueFontFamily
                )
                Text(
                    text = userName,
                    color = Ink,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = SyneFontFamily
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DailySummaryBox("Total", totalDoses.toString(), GreenPrimary, Modifier.weight(1f))
                DailySummaryBox("Tomadas", takenDoses.toString(), GreenMid, Modifier.weight(1f))
                DailySummaryBox("Pendentes", pendingDoses.toString(), Amber, Modifier.weight(1f))
                DailySummaryBox("Adesão", "$adherence%", Blue, Modifier.weight(1f))
            }
        }

        // Hero Card (Next Pending Dose)
        item {
            AnimatedVisibility(
                visible = nextPendingDose != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                if (nextPendingDose != null) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = GreenPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Text(
                                text = "PRÓXIMA DOSE — ${nextPendingDose.time}",
                                color = GreenLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                                fontFamily = SyneFontFamily
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = nextPendingDose.name,
                                color = White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = SyneFontFamily
                            )
                            Text(
                                text = nextPendingDose.doseDescription,
                                color = GreenLight.copy(alpha = 0.8f),
                                fontSize = 13.sp,
                                fontFamily = EpilogueFontFamily
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { onConfirmDose(nextPendingDose.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = White),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
                                modifier = Modifier.align(Alignment.Start)
                            ) {
                                Text(
                                    text = "Confirmar dose",
                                    color = GreenPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = SyneFontFamily
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section Label
        item {
            Text(
                text = "HOJE",
                color = Ink3,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                fontFamily = EpilogueFontFamily,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        // Medicine Rows
        items(doses, key = { it.id }) { dose ->
                DoseRow(
                    dose = dose,
                    onRowClick = {
                        selectedDose = dose
                    }
                )
            }
    }
}

@Composable
private fun DailySummaryBox(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, Border),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                color = color,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = SyneFontFamily
            )
            Text(
                text = label,
                color = Ink3,
                fontSize = 9.sp,
                fontFamily = EpilogueFontFamily
            )
        }
    }
}

@Composable
private fun DoseDetailsDialog(
    dose: Dose,
    onDismiss: () -> Unit,
    onConfirmDose: () -> Unit,
    onDelayDose: () -> Unit,
    onMissDose: () -> Unit,
    onDeleteDose: () -> Unit,
    onUpdateDose: (Dose) -> Unit
) {
    var isEditing by remember(dose.id) { mutableStateOf(false) }
    var name by remember(dose.id) { mutableStateOf(dose.name) }
    var doseDescription by remember(dose.id) { mutableStateOf(dose.doseDescription) }
    var time by remember(dose.id) { mutableStateOf(dose.time) }
    var errorMessage by remember(dose.id) { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        title = {
            Text(
                text = if (isEditing) "Editar medicamento" else "Detalhes da dose",
                color = Ink,
                fontWeight = FontWeight.Bold,
                fontFamily = SyneFontFamily
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (isEditing) {
                    DoseEditField("Medicamento", name) { name = it; errorMessage = null }
                    DoseEditField("Dose", doseDescription) { doseDescription = it; errorMessage = null }
                    DoseEditField("Horário", time) { time = it; errorMessage = null }
                    errorMessage?.let {
                        Text(text = it, color = Red, fontSize = 11.sp, fontFamily = EpilogueFontFamily)
                    }
                } else {
                    DoseDetailLine("Medicamento", dose.name)
                    DoseDetailLine("Dose", dose.doseDescription)
                    DoseDetailLine("Horário", dose.time)
                    DoseDetailLine("Status", dose.status.name)
                }

                if (isEditing) {
                    Button(
                        onClick = {
                            val editedName = name.trim()
                            val editedDose = doseDescription.trim()
                            val editedTime = time.trim()
                            if (editedName.isEmpty() || editedDose.isEmpty() || editedTime.isEmpty()) {
                                errorMessage = "Preencha nome, dose e horário."
                            } else {
                                onUpdateDose(
                                    dose.copy(
                                        name = editedName,
                                        doseDescription = editedDose,
                                        time = editedTime
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Salvar alterações", fontFamily = SyneFontFamily, fontWeight = FontWeight.Bold) }
                } else {
                    Button(
                        onClick = onConfirmDose,
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Confirmar dose", fontFamily = SyneFontFamily, fontWeight = FontWeight.Bold) }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = onDelayDose,
                            colors = ButtonDefaults.buttonColors(containerColor = BlueLight),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) { Text("Mais tarde", color = Blue, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        Button(
                            onClick = onMissDose,
                            colors = ButtonDefaults.buttonColors(containerColor = RedLight),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) { Text("Perdida", color = Red, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { isEditing = !isEditing },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) { Text(if (isEditing) "Cancelar" else "Editar", color = GreenPrimary) }
                    OutlinedButton(
                        onClick = onDeleteDose,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) { Text("Excluir", color = Red) }
                }
            }
        },
        containerColor = Surface,
        shape = RoundedCornerShape(18.dp)
    )
}

@Composable
private fun DoseDetailLine(label: String, value: String) {
    Column {
        Text(text = label, color = Ink3, fontSize = 10.sp, fontFamily = EpilogueFontFamily)
        Text(text = value, color = Ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, fontFamily = EpilogueFontFamily)
    }
}

@Composable
private fun DoseEditField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Background,
            unfocusedContainerColor = Background,
            focusedBorderColor = GreenPrimary,
            unfocusedBorderColor = Border,
            cursorColor = GreenPrimary
        ),
        singleLine = true
    )
}

@Composable
fun DoseRow(
    dose: Dose,
    onRowClick: () -> Unit
) {
    val (iconBgColor, badgeText, badgeBgColor, badgeTextColor) = when (dose.status) {
        DoseStatus.Tomado -> Quadruple(GreenLight, "Tomado", GreenLight, GreenPrimary)
        DoseStatus.Pendente -> Quadruple(AmberLight, "Pendente", AmberLight, Amber)
        DoseStatus.MaisTarde -> Quadruple(BlueLight, "Mais tarde", BlueLight, Blue)
        DoseStatus.Perdido -> Quadruple(RedLight, "Perdido", RedLight, Red)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Surface)
            .border(1.dp, Border, RoundedCornerShape(14.dp))
            .clickable { onRowClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon Container
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "💊", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Info Column
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = dose.name,
                color = Ink,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = EpilogueFontFamily
            )
            Text(
                text = "${dose.time} · ${dose.doseDescription}",
                color = Ink3,
                fontSize = 11.sp,
                fontFamily = EpilogueFontFamily
            )
        }

        // Status Badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(badgeBgColor)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = badgeText,
                color = badgeTextColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = EpilogueFontFamily
            )
        }
    }
}

// Simple Helper class since Kotlin doesn't have standard Quadruple
data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    MedAppTheme {
        HomeScreen(
            userName = "João Silva",
            doses = listOf(
                Dose("1", "Dipirona 500mg", "1 comprimido", "08:00", DoseStatus.Tomado),
                Dose("2", "Omeprazol 20mg", "1 cápsula", "12:00", DoseStatus.Pendente),
                Dose("3", "Losartana 50mg", "1 comprimido", "20:00", DoseStatus.MaisTarde)
            ),
            onConfirmDose = {}
        )
    }
}
