package com.example.medapp.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medapp.data.MedicineInfo
import com.example.medapp.data.MedicineSearchUiState
import com.example.medapp.data.isValidEan13
import com.example.medapp.ui.theme.*

@Composable
fun CadastroScreen(
    onAddMedicine: (String, String, String) -> Unit = { _, _, _ -> },
    onNavigateToHome: () -> Unit,
    onScanRequested: () -> Unit = {},
    onSearchByEan: (String) -> Unit = {},
    medicineSearchState: MedicineSearchUiState = MedicineSearchUiState.Idle,
    onSearchByName: (String) -> Unit = {},
    nameSearchResults: List<MedicineInfo> = emptyList(),
    interactions: List<com.example.medapp.data.InteractionInfo> = emptyList(),
    isSearchingName: Boolean = false,
    popularMedicines: List<MedicineInfo> = emptyList(),
    onClearSearch: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var doseDescription by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var lastFilledEan by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(medicineSearchState) {
        when (medicineSearchState) {
            is MedicineSearchUiState.Success -> {
                if (lastFilledEan != medicineSearchState.medicine.ean) {
                    lastFilledEan = medicineSearchState.medicine.ean
                    searchQuery = medicineSearchState.medicine.name
                    errorMessage = null
                }
            }
            is MedicineSearchUiState.NotFound -> {
                searchQuery = ""
                errorMessage = null
            }
            else -> {}
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Background)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Novo medicamento",
                color = Ink3,
                fontSize = 12.sp,
                fontFamily = EpilogueFontFamily
            )
            Text(
                text = "Adicionar",
                color = Ink,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = SyneFontFamily
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFF1A1812))
                .clickable { onScanRequested() },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(width = 160.dp, height = 90.dp)
                    .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            ) {
                Box(modifier = Modifier.align(Alignment.TopStart).size(12.dp).border(2.dp, GreenMid, RoundedCornerShape(topStart = 4.dp)))
                Box(modifier = Modifier.align(Alignment.TopEnd).size(12.dp).border(2.dp, GreenMid, RoundedCornerShape(topEnd = 4.dp)))
                Box(modifier = Modifier.align(Alignment.BottomStart).size(12.dp).border(2.dp, GreenMid, RoundedCornerShape(bottomStart = 4.dp)))
                Box(modifier = Modifier.align(Alignment.BottomEnd).size(12.dp).border(2.dp, GreenMid, RoundedCornerShape(bottomEnd = 4.dp)))
            }

            val infiniteTransition = rememberInfiniteTransition(label = "scanner")
            val animatedOffsetY by infiniteTransition.animateFloat(
                initialValue = -40f,
                targetValue = 40f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scannerLine"
            )

            Box(
                modifier = Modifier
                    .size(width = 150.dp, height = 2.dp)
                    .offset(y = animatedOffsetY.dp)
                    .background(GreenMid)
            )

            Text(
                text = "Toque para escanear o código de barras",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 10.sp,
                fontFamily = EpilogueFontFamily,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                errorMessage = null
                if (!isValidEan13(it.trim())) {
                    onSearchByName(it.trim())
                }
            },
            placeholder = { Text(text = "Buscar por nome ou codigo...", color = Ink3, fontSize = 13.sp) },
            leadingIcon = { Text(text = "🔍", fontSize = 14.sp) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    Text(
                        text = "✕",
                        color = Ink3,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(horizontal = 12.dp).clickable {
                            searchQuery = ""
                            onClearSearch()
                            onSearchByName("")
                        }
                    )
                }
            },
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Surface,
                unfocusedContainerColor = Surface,
                focusedBorderColor = GreenPrimary,
                unfocusedBorderColor = Border,
                cursorColor = GreenPrimary
            ),
            singleLine = true
        )

        if (!isValidEan13(searchQuery.trim()) && medicineSearchState is MedicineSearchUiState.Idle) {
            if (isSearchingName) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = GreenPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Buscando...", color = Ink3, fontSize = 12.sp, fontFamily = EpilogueFontFamily)
                }
            } else if (searchQuery.trim().length >= 2 && nameSearchResults.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "RESULTADOS",
                    color = Ink3,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    fontFamily = EpilogueFontFamily
                )
                Spacer(modifier = Modifier.height(6.dp))
                nameSearchResults.take(5).forEach { medicine ->
                    MedicineSearchCard(medicine) {
                        searchQuery = medicine.name
                        onSearchByEan(medicine.ean)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }
            } else if (searchQuery.isBlank() && popularMedicines.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "MEDICAMENTOS POPULARES",
                    color = Ink3,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    fontFamily = EpilogueFontFamily
                )
                Spacer(modifier = Modifier.height(6.dp))
                popularMedicines.take(5).forEach { medicine ->
                    MedicineSearchCard(medicine) {
                        searchQuery = medicine.name
                        onSearchByEan(medicine.ean)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }

        if (isValidEan13(searchQuery.trim())) {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { onSearchByEan(searchQuery.trim()) },
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(42.dp)
            ) {
                Text(
                    text = "Buscar medicamento",
                    color = White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = EpilogueFontFamily
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = doseDescription,
            onValueChange = {
                doseDescription = it
                errorMessage = null
            },
            placeholder = { Text(text = "Dose, ex: 1 comprimido", color = Ink3, fontSize = 13.sp) },
            leadingIcon = { Text(text = "💊", fontSize = 14.sp) },
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Surface,
                unfocusedContainerColor = Surface,
                focusedBorderColor = GreenPrimary,
                unfocusedBorderColor = Border,
                cursorColor = GreenPrimary
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = time,
            onValueChange = {
                time = it
                errorMessage = null
            },
            placeholder = { Text(text = "Horário, ex: 08:00", color = Ink3, fontSize = 13.sp) },
            leadingIcon = { Text(text = "⏰", fontSize = 14.sp) },
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Surface,
                unfocusedContainerColor = Surface,
                focusedBorderColor = GreenPrimary,
                unfocusedBorderColor = Border,
                cursorColor = GreenPrimary
            ),
            singleLine = true
        )

        errorMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = it,
                color = Red,
                fontSize = 11.sp,
                fontFamily = EpilogueFontFamily,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.TopCenter
        ) {
            when (val state = medicineSearchState) {
                MedicineSearchUiState.Idle -> {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = GreenLight),
                        border = BorderStroke(1.dp, GreenPrimary.copy(alpha = 0.15f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                            Text(text = "💡", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Como cadastrar",
                                    color = GreenPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = SyneFontFamily
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "1. Escaneie o codigo de barras do medicamento\n2. Ou busque pelo nome acima\n3. Preencha dose e horario\n4. Toque em Continuar",
                                    color = Ink2,
                                    fontSize = 11.sp,
                                    fontFamily = EpilogueFontFamily,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
                MedicineSearchUiState.Loading -> {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = GreenLight),
                        border = BorderStroke(1.dp, GreenPrimary.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = GreenPrimary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Consultando base da ANVISA...",
                                color = GreenPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = EpilogueFontFamily
                            )
                        }
                    }
                }
                is MedicineSearchUiState.Success -> MedicineFoundCard(state.medicine)
                is MedicineSearchUiState.NotFound -> {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = AmberLight),
                        border = BorderStroke(1.dp, Amber.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "EAN ${state.ean} nao cadastrado",
                                color = Amber,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = SyneFontFamily
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Digite o nome do medicamento acima e toque em Continuar. Ele sera cadastrado automaticamente na base.",
                                color = Amber,
                                fontSize = 11.sp,
                                lineHeight = 15.sp,
                                fontFamily = EpilogueFontFamily
                            )
                        }
                    }
                }
                is MedicineSearchUiState.Error -> {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = RedLight),
                        border = BorderStroke(1.dp, Red.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(text = "⚠️", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Não encontrado",
                                    color = Red,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = SyneFontFamily
                                )
                                Text(
                                    text = state.message,
                                    color = Red,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    fontFamily = EpilogueFontFamily
                                )
                            }
                        }
                    }
                }
            }
            // Interaction warnings (shown when medicine found + interactions exist)
            if (interactions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                interactions.forEach { interaction ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (interaction.severidade == "grave") RedLight else AmberLight
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (interaction.severidade == "grave") Red.copy(alpha = 0.2f) else Amber.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                            Text(text = if (interaction.severidade == "grave") "!!" else "?", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Interacao ${interaction.severidade}",
                                    color = if (interaction.severidade == "grave") Red else Amber,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = SyneFontFamily
                                )
                                Text(
                                    text = interaction.descricao,
                                    color = if (interaction.severidade == "grave") Red else Amber,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    fontFamily = EpilogueFontFamily
                                )
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = {
                val medicineName = searchQuery.trim()
                val doseInfo = doseDescription.trim()
                val schedule = time.trim()

                if (medicineName.isEmpty() || doseInfo.isEmpty() || schedule.isEmpty()) {
                    errorMessage = "Preencha medicamento, dose e horário."
                } else {
                    onAddMedicine(medicineName, doseInfo, schedule)
                    onNavigateToHome()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text(
                text = "Continuar para horários →",
                color = White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = SyneFontFamily
            )
        }
        Spacer(modifier = Modifier.height(90.dp))
    }
}

@Composable
private fun MedicineSearchCard(
    medicine: MedicineInfo,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Surface)
            .border(1.dp, Border, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(GreenLight),
            contentAlignment = Alignment.Center
        ) {
            Text("💊", fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = medicine.name,
                color = Ink,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = EpilogueFontFamily
            )
            Text(
                text = "${medicine.laboratory} · ${medicine.category}",
                color = Ink3,
                fontSize = 10.sp,
                fontFamily = EpilogueFontFamily
            )
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(GreenLight)
                .padding(horizontal = 6.dp, vertical = 3.dp)
        ) {
            Text(
                text = "...${medicine.ean.takeLast(4)}",
                color = GreenPrimary,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = EpilogueFontFamily
            )
        }
    }
}

@Composable
private fun MedicineFoundCard(medicine: MedicineInfo) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = GreenLight),
        border = BorderStroke(1.dp, GreenPrimary.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "✓ ENCONTRADO NA ANVISA",
                color = GreenPrimary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                fontFamily = EpilogueFontFamily
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = medicine.name,
                color = Ink,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = SyneFontFamily
            )
            Text(
                text = "${medicine.laboratory} · ${medicine.category} · Reg. ANVISA ${medicine.anvisaRegistration}",
                color = Ink2,
                fontSize = 11.sp,
                fontFamily = EpilogueFontFamily
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Princípio ativo: ${medicine.activeIngredient}",
                color = Ink2,
                fontSize = 11.sp,
                fontFamily = EpilogueFontFamily
            )
            medicine.presentation?.let {
                Text(
                    text = it,
                    color = Ink3,
                    fontSize = 10.sp,
                    fontFamily = EpilogueFontFamily
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CadastroScreenPreview() {
    MedAppTheme {
        CadastroScreen(onNavigateToHome = {})
    }
}