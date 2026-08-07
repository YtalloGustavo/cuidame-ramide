package com.example.medapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medapp.ui.theme.*

data class HistoryItem(
    val id: String,
    val name: String,
    val doseInfo: String,
    val timeLabel: String,
    val status: HistoryStatus
)

enum class HistoryStatus {
    Tomado, Atrasado, Perdido
}

@Composable
fun HistoricoScreen(
    adherenceRate: Int,
    dosesDelayed: Int,
    dosesMissed: Int,
    historyItems: List<HistoryItem>,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf(HistoryFilter.Todos) }
    val filteredHistoryItems = when (selectedFilter) {
        HistoryFilter.Todos -> historyItems
        HistoryFilter.Tomados -> historyItems.filter { it.status == HistoryStatus.Tomado }
        HistoryFilter.Atrasados -> historyItems.filter { it.status == HistoryStatus.Atrasado }
        HistoryFilter.Perdidos -> historyItems.filter { it.status == HistoryStatus.Perdido }
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
                    text = "Últimos 7 dias",
                    color = Ink3,
                    fontSize = 12.sp,
                    fontFamily = EpilogueFontFamily
                )
                Text(
                    text = "Histórico",
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
                HistoryFilter.entries.forEach { filter ->
                    val isSelected = selectedFilter == filter
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) GreenPrimary else Surface)
                            .border(1.dp, if (isSelected) GreenPrimary else Border, RoundedCornerShape(10.dp))
                            .clickable { selectedFilter = filter }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = filter.label,
                            color = if (isSelected) White else GreenPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = EpilogueFontFamily
                        )
                    }
                }
            }
        }

        // Stats Cards Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatBox(value = "$adherenceRate%", label = "Adesão", color = GreenPrimary, modifier = Modifier.weight(1f))
                StatBox(value = "$dosesDelayed", label = "Atrasadas", color = Amber, modifier = Modifier.weight(1f))
                StatBox(value = "$dosesMissed", label = "Perdidas", color = Red, modifier = Modifier.weight(1f))
            }
        }

        // Weekly Grid View
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Surface),
                border = BorderStroke(1.dp, Border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp)
                ) {
                    Text(
                        text = "VISTA SEMANAL",
                        color = Ink3,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        fontFamily = EpilogueFontFamily
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val days = listOf("Seg", "Ter", "Qua", "Qui", "Sex", "Sáb", "Dom")
                        val results = listOf("✓", "✓", "✗", "✓", "✓", "✓", "●")
                        
                        days.zip(results).forEach { (day, result) ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = day,
                                    color = Ink3,
                                    fontSize = 9.sp,
                                    fontFamily = EpilogueFontFamily
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                
                                val (bg, fg) = when (result) {
                                    "✓" -> Pair(GreenLight, GreenPrimary)
                                    "✗" -> Pair(RedLight, Red)
                                    else -> Pair(GreenPrimary, White) // "●" (today)
                                }
                                
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(bg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = result,
                                        color = fg,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
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
                text = "REGISTROS",
                color = Ink3,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                fontFamily = EpilogueFontFamily,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        // History items
        if (filteredHistoryItems.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Surface),
                    border = BorderStroke(1.dp, Border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Nenhum registro para este filtro.",
                        color = Ink3,
                        fontSize = 12.sp,
                        fontFamily = EpilogueFontFamily,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(filteredHistoryItems) { item ->
                HistoryRow(item = item)
            }
        }
    }
}

private enum class HistoryFilter(val label: String) {
    Todos("Todos"),
    Tomados("Tomados"),
    Atrasados("Atrasos"),
    Perdidos("Perdidos")
}

@Composable
fun StatBox(
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        border = BorderStroke(1.dp, Border),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                color = color,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = SyneFontFamily
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                color = Ink3,
                fontSize = 10.sp,
                fontFamily = EpilogueFontFamily
            )
        }
    }
}

@Composable
fun HistoryRow(item: HistoryItem) {
    val (lineColor, badgeText, badgeBgColor, badgeTextColor) = when (item.status) {
        HistoryStatus.Tomado -> Quadruple(GreenMid, "Tomado", GreenLight, GreenPrimary)
        HistoryStatus.Atrasado -> Quadruple(Amber, "Atrasado", AmberLight, Amber)
        HistoryStatus.Perdido -> Quadruple(Red, "Perdido", RedLight, Red)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Surface)
            .border(1.dp, Border, RoundedCornerShape(14.dp))
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Vertical Indicator Line
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(36.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(lineColor)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Text Info
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = item.name,
                color = Ink,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = EpilogueFontFamily
            )
            Text(
                text = item.timeLabel,
                color = Ink3,
                fontSize = 11.sp,
                fontFamily = EpilogueFontFamily
            )
        }

        // Status Badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(5.dp))
                .background(badgeBgColor)
                .padding(horizontal = 7.dp, vertical = 2.dp)
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

@Preview(showBackground = true)
@Composable
fun HistoricoScreenPreview() {
    MedAppTheme {
        HistoricoScreen(
            adherenceRate = 87,
            dosesDelayed = 2,
            dosesMissed = 1,
            historyItems = listOf(
                HistoryItem("1", "Dipirona 500mg", "1 comprimido", "Hoje, 08:03", HistoryStatus.Tomado),
                HistoryItem("2", "Losartana 50mg", "1 comprimido", "Ontem, 20:18 (+18 min)", HistoryStatus.Atrasado),
                HistoryItem("3", "Omeprazol 20mg", "1 cápsula", "Qua, 12:00", HistoryStatus.Perdido)
            )
        )
    }
}
