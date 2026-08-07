package com.example.medapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medapp.ui.theme.*

@Composable
fun LoginScreen(
    onLoginAsPatient: () -> Unit,
    onLoginAsCaregiver: () -> Unit,
    modifier: Modifier = Modifier
) {
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var identifierError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    fun validateLogin(onSuccess: () -> Unit) {
        val trimmedIdentifier = identifier.trim()
        val trimmedPassword = password.trim()

        identifierError = when {
            trimmedIdentifier.isEmpty() -> "Informe seu e-mail ou celular."
            trimmedIdentifier.length < 5 -> "Informe um e-mail ou celular valido."
            else -> null
        }

        passwordError = when {
            trimmedPassword.isEmpty() -> "Informe sua senha."
            trimmedPassword.length < 4 -> "A senha precisa ter pelo menos 4 caracteres."
            else -> null
        }

        if (identifierError == null && passwordError == null) {
            onSuccess()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Background)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Logo
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(GreenPrimary),
            contentAlignment = Alignment.Center
        ) {
            // Draw stylized white medical cross inside logo
            Text(
                text = "✚",
                color = White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // App Brand Name
        Text(
            text = "MedApp",
            color = GreenPrimary,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = SyneFontFamily
        )
        Text(
            text = "Lembrete inteligente de medicamentos",
            color = Ink2,
            fontSize = 13.sp,
            fontFamily = EpilogueFontFamily,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Identifier input (email/phone)
        OutlinedTextField(
            value = identifier,
            onValueChange = {
                identifier = it
                identifierError = null
            },
            placeholder = { Text(text = "E-mail ou celular", color = Ink3, fontSize = 14.sp) },
            leadingIcon = { Text(text = "👤", fontSize = 14.sp) },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp)),
            isError = identifierError != null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Surface,
                unfocusedContainerColor = Surface,
                focusedBorderColor = GreenPrimary,
                unfocusedBorderColor = Border,
                errorBorderColor = Red,
                errorCursorColor = Red,
                cursorColor = GreenPrimary
            ),
            singleLine = true,
            supportingText = {
                identifierError?.let {
                    Text(text = it, color = Red, fontSize = 11.sp)
                }
            }
        )

        Spacer(modifier = Modifier.height(if (identifierError == null) 12.dp else 4.dp))

        // Password input
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                passwordError = null
            },
            placeholder = { Text(text = "Senha de acesso", color = Ink3, fontSize = 14.sp) },
            leadingIcon = { Text(text = "🔒", fontSize = 14.sp) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp)),
            isError = passwordError != null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Surface,
                unfocusedContainerColor = Surface,
                focusedBorderColor = GreenPrimary,
                unfocusedBorderColor = Border,
                errorBorderColor = Red,
                errorCursorColor = Red,
                cursorColor = GreenPrimary
            ),
            singleLine = true,
            supportingText = {
                passwordError?.let {
                    Text(text = it, color = Red, fontSize = 11.sp)
                }
            }
        )

        Spacer(modifier = Modifier.height(if (passwordError == null) 24.dp else 8.dp))

        // Patient Login Button
        Button(
            onClick = { validateLogin(onLoginAsPatient) },
            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(
                text = "Entrar como Paciente",
                color = White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = SyneFontFamily
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Caregiver Login Button
        Button(
            onClick = { validateLogin(onLoginAsCaregiver) },
            colors = ButtonDefaults.buttonColors(containerColor = Surface),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, GreenPrimary),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(
                text = "Entrar como Cuidador",
                color = GreenPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = SyneFontFamily
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Helper presentation tip
        Text(
            text = "Acesso demonstrativo acadêmico.\nEscolha o perfil para navegar.",
            color = Ink3,
            fontSize = 11.sp,
            fontFamily = EpilogueFontFamily,
            textAlign = TextAlign.Center,
            lineHeight = 15.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    MedAppTheme {
        LoginScreen(onLoginAsPatient = {}, onLoginAsCaregiver = {})
    }
}
