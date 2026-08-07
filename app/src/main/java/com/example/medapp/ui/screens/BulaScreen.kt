package com.example.medapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medapp.data.BulaUiState
import com.example.medapp.data.MedicineInfo
import com.example.medapp.ui.theme.*
import com.example.medapp.ui.viewmodel.MainViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BulaScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val medicineList by viewModel.medicineList.collectAsState()
    val bulaState by viewModel.bulaState.collectAsState()
    val selectedEan by viewModel.selectedBulaEan.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.loadMedicineList() }
    LaunchedEffect(medicineList) {
        if (medicineList.isNotEmpty() && selectedEan == null) {
            viewModel.selectMedicineForBula(medicineList.first().ean)
        }
    }

    val selectedMedicine = medicineList.find { it.ean == selectedEan } ?: medicineList.firstOrNull()
    val filteredMedicines = medicineList.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
            it.category.contains(searchQuery, ignoreCase = true)
    }
    val tabs = listOf("Resumo", "Bula completa", "Interações")

    Column(modifier = modifier.fillMaxSize().background(Background)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush = Brush.linearGradient(colors = listOf(Color(0xFF1A3A2A), GreenPrimary)))
                .padding(top = 44.dp, start = 20.dp, end = 20.dp, bottom = 20.dp)
        ) {
            Column {
                Text(
                    text = selectedMedicine?.name ?: "Carregando...",
                    color = White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = SyneFontFamily
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = selectedMedicine?.let { "${it.category} - ${it.laboratory}" } ?: "",
                    color = GreenLight.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    fontFamily = EpilogueFontFamily
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().background(Surface).padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                val isActive = selectedTab == index
                Box(
                    modifier = Modifier.weight(1f).clickable { selectedTab = index }.padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        color = if (isActive) GreenPrimary else Ink3,
                        fontSize = 12.sp,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                        fontFamily = EpilogueFontFamily
                    )
                    if (isActive) {
                        Box(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(0.6f).height(2.dp).background(GreenPrimary))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(1.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(text = "Buscar medicamento...", color = Ink3, fontSize = 13.sp) },
                    leadingIcon = { Text(text = "?", fontSize = 14.sp) },
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
            }

            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    filteredMedicines.forEach { medicine ->
                        val isSelected = medicine.ean == selectedEan
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) GreenPrimary else Surface)
                                .border(1.dp, if (isSelected) GreenPrimary else Border, RoundedCornerShape(8.dp))
                                .clickable { viewModel.selectMedicineForBula(medicine.ean) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = medicine.name,
                                color = if (isSelected) White else GreenPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = EpilogueFontFamily
                            )
                        }
                    }
                }
            }

            when (bulaState) {
                is BulaUiState.Loading -> {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = GreenPrimary)
                        }
                    }
                }
                is BulaUiState.NotAvailable -> {
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = AmberLight),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Bula nao disponivel para este medicamento.",
                                color = Amber,
                                fontSize = 13.sp,
                                fontFamily = EpilogueFontFamily,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
                is BulaUiState.Error -> {
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = RedLight),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = (bulaState as BulaUiState.Error).message,
                                color = Red,
                                fontSize = 13.sp,
                                fontFamily = EpilogueFontFamily,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
                is BulaUiState.Success -> {
                    val bula = (bulaState as BulaUiState.Success).bula
                    when (selectedTab) {
                        0 -> {
                            item {
                                InfoBadge(text = "Guia Simplificado de Bula", bg = PurpleLight, fg = Purple)
                                SectionHeader(title = "Para que serve")
                                Text(text = bula.paraQueServe, color = Ink, fontSize = 13.sp, lineHeight = 18.sp, fontFamily = EpilogueFontFamily)
                            }
                            item {
                                SectionHeader(title = "Efeitos colaterais comuns")
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                                    bula.efeitosColaterais.forEachIndexed { index, effect ->
                                        EffectPill(text = effect, isMild = index != bula.efeitosColaterais.lastIndex)
                                    }
                                }
                            }
                            item {
                                SectionHeader(title = "Atencao")
                                Text(text = bula.atencao, color = Ink, fontSize = 13.sp, lineHeight = 18.sp, fontFamily = EpilogueFontFamily)
                            }
                        }
                        1 -> {
                            item {
                                InfoBadge(text = "Texto Oficial", bg = BlueLight, fg = Blue)
                                SectionHeader(title = "Informacoes ao Paciente")
                                Text(text = bula.infoPaciente, color = Ink, fontSize = 13.sp, lineHeight = 18.sp, fontFamily = EpilogueFontFamily)
                            }
                        }
                        2 -> {
                            item {
                                InfoBadge(text = "Cruzamento de Formulas", bg = RedLight, fg = Red)
                                SectionHeader(title = "Interacoes Criticas")
                                Text(text = bula.interacoes, color = Ink, fontSize = 13.sp, lineHeight = 18.sp, fontFamily = EpilogueFontFamily)
                            }
                            item {
                                SectionHeader(title = "Interacoes Alimentares")
                                Text(text = bula.interacoesAlimentares, color = Ink, fontSize = 13.sp, lineHeight = 18.sp, fontFamily = EpilogueFontFamily)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        color = Ink3,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp,
        fontFamily = EpilogueFontFamily,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
fun EffectPill(text: String, isMild: Boolean) {
    val (bg, fg) = if (isMild) Pair(AmberLight, Amber) else Pair(RedLight, Red)
    Box(
        modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(bg).padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text = text, color = fg, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = EpilogueFontFamily)
    }
}

@Composable
private fun InfoBadge(text: String, bg: Color, fg: Color) {
    Box(
        modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(bg).padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text = text, color = fg, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = EpilogueFontFamily)
    }
}

@Preview(showBackground = true)
@Composable
fun BulaScreenPreview() {
    MedAppTheme {
        BulaScreen(viewModel = androidx.hilt.navigation.compose.hiltViewModel())
    }
}
