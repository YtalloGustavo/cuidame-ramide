package com.example.medapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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

data class Caregiver(
    val id: String,
    val name: String,
    val relationship: String,
    val phone: String,
    val initials: String,
    val avatarBg: Color,
    val avatarFg: Color,
    val isActive: Boolean
)

@Composable
fun CuidadorScreen(
    caregivers: List<Caregiver>,
    onToggleCaregiver: (String) -> Unit,
    onAddCaregiver: () -> Unit,
    onAddCaregiverData: (String, String, String) -> Unit = { _, _, _ -> },
    onLogout: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showAddForm by remember { mutableStateOf(false) }
    var caregiverName by remember { mutableStateOf("") }
    var caregiverRelationship by remember { mutableStateOf("") }
    var caregiverPhone by remember { mutableStateOf("") }
    var formError by remember { mutableStateOf<String?>(null) }

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
                    text = "Quem acompanha você",
                    color = Ink3,
                    fontSize = 12.sp,
                    fontFamily = EpilogueFontFamily
                )
                Text(
                    text = "Cuidadores",
                    color = Ink,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = SyneFontFamily
                )
            }
        }

        // Section label
        item {
            Text(
                text = "FAMILIARES ATIVOS",
                color = Ink3,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                fontFamily = EpilogueFontFamily,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        // Caregiver Cards list
        items(caregivers, key = { it.id }) { caregiver ->
            CaregiverRow(
                caregiver = caregiver,
                onToggle = { onToggleCaregiver(caregiver.id) }
            )
        }

        // Notification Preview Section
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "PRÉVIA DA NOTIFICAÇÃO (WHATSAPP)",
                color = Ink3,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                fontFamily = EpilogueFontFamily,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        // Mock Whatsapp Notification Bubbles
        item {
            WhatsappNotificationCard(
                icon = "💊",
                appName = "MedApp via WhatsApp",
                timeLabel = "agora",
                messageContent = "👤 João tomou Omeprazol 20mg às 12:04 ✅"
            )
        }

        item {
            WhatsappNotificationCard(
                icon = "⚠️",
                appName = "MedApp via WhatsApp",
                timeLabel = "20:35",
                messageContent = "⚠️ João ainda não confirmou Losartana 50mg (20:00). Verifique com ele."
            )
        }

        // Add Caregiver button
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    onAddCaregiver()
                    showAddForm = !showAddForm
                    formError = null
                },
                colors = ButtonDefaults.buttonColors(containerColor = Surface),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, GreenPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = "+ Adicionar familiar",
                    color = GreenPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = SyneFontFamily
                )
            }
        }

        if (showAddForm) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Surface),
                    border = BorderStroke(1.dp, Border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Novo cuidador",
                            color = Ink,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = SyneFontFamily
                        )

                        CaregiverInput(
                            value = caregiverName,
                            onValueChange = { caregiverName = it; formError = null },
                            placeholder = "Nome do familiar"
                        )
                        CaregiverInput(
                            value = caregiverRelationship,
                            onValueChange = { caregiverRelationship = it; formError = null },
                            placeholder = "Parentesco"
                        )
                        CaregiverInput(
                            value = caregiverPhone,
                            onValueChange = { caregiverPhone = it; formError = null },
                            placeholder = "Telefone"
                        )

                        formError?.let {
                            Text(
                                text = it,
                                color = Red,
                                fontSize = 11.sp,
                                fontFamily = EpilogueFontFamily
                            )
                        }

                        Button(
                            onClick = {
                                val name = caregiverName.trim()
                                val relationship = caregiverRelationship.trim()
                                val phone = caregiverPhone.trim()

                                if (name.isEmpty() || relationship.isEmpty() || phone.isEmpty()) {
                                    formError = "Preencha nome, parentesco e telefone."
                                } else {
                                    onAddCaregiverData(name, relationship, phone)
                                    caregiverName = ""
                                    caregiverRelationship = ""
                                    caregiverPhone = ""
                                    showAddForm = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                        ) {
                            Text(
                                text = "Salvar cuidador",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = SyneFontFamily
                            )
                        }
                    }
                }
            }
        }

        // Logout button
        if (onLogout != null) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = onLogout,
                    colors = ButtonDefaults.buttonColors(containerColor = RedLight),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = "Sair da Conta",
                        color = Red,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = SyneFontFamily
                    )
                }
            }
        }
    }
}

@Composable
private fun CaregiverInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(text = placeholder, color = Ink3, fontSize = 13.sp) },
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
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
fun CaregiverRow(
    caregiver: Caregiver,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Surface)
            .border(1.dp, Border, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar Box
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(caregiver.avatarBg),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = caregiver.initials,
                color = caregiver.avatarFg,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = SyneFontFamily
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Text Info
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = caregiver.name,
                color = Ink,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = EpilogueFontFamily
            )
            Text(
                text = "${caregiver.relationship} · ${caregiver.phone}",
                color = Ink3,
                fontSize = 11.sp,
                fontFamily = EpilogueFontFamily
            )
        }

        // Switch
        Switch(
            checked = caregiver.isActive,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = White,
                checkedTrackColor = GreenPrimary,
                uncheckedThumbColor = Ink3,
                uncheckedTrackColor = Background,
                uncheckedBorderColor = Border
            )
        )
    }
}

@Composable
fun WhatsappNotificationCard(
    icon: String,
    appName: String,
    timeLabel: String,
    messageContent: String
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x081A1812)), // ~3% opacity
        border = BorderStroke(1.dp, Border),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Mini App Icon Box
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(GreenLight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = icon, fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = appName,
                    color = Ink2,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = EpilogueFontFamily
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = timeLabel,
                    color = Ink3,
                    fontSize = 10.sp,
                    fontFamily = EpilogueFontFamily
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = messageContent,
                color = Ink,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontFamily = EpilogueFontFamily
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CuidadorScreenPreview() {
    MedAppTheme {
        CuidadorScreen(
            caregivers = listOf(
                Caregiver("1", "Maria Silva", "Mãe", "+55 81 99999-0001", "MA", GreenLight, GreenPrimary, true),
                Caregiver("2", "Carlos Rocha", "Filho", "+55 81 99999-0002", "CR", BlueLight, Blue, true)
            ),
            onToggleCaregiver = {},
            onAddCaregiver = {}
        )
    }
}
