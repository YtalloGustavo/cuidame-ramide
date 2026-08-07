package com.example.medapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.medapp.ui.screens.*
import com.example.medapp.ui.theme.MedAppTheme
import com.example.medapp.ui.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

enum class AppFlow {
    LOGIN, PATIENT, CAREGIVER
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MedAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var currentFlow by remember { mutableStateOf(AppFlow.LOGIN) }

                    when (currentFlow) {
                        AppFlow.LOGIN -> {
                            LoginScreen(
                                onLoginAsPatient = { currentFlow = AppFlow.PATIENT },
                                onLoginAsCaregiver = { currentFlow = AppFlow.CAREGIVER }
                            )
                        }
                        AppFlow.PATIENT -> {
                            val viewModel: MainViewModel = hiltViewModel()
                            MainScreen(
                                viewModel = viewModel,
                                onLogout = { currentFlow = AppFlow.LOGIN }
                            )
                        }
                        AppFlow.CAREGIVER -> {
                            val viewModel: MainViewModel = hiltViewModel()
                            val doses by viewModel.doses.collectAsState()
                            val adherenceRate by viewModel.caregiverAdherenceRate.collectAsState()

                            CaregiverDashboardScreen(
                                adherenceRate = adherenceRate,
                                doses = doses,
                                onConfirmDose = { viewModel.confirmDose(it) },
                                onLogout = { currentFlow = AppFlow.LOGIN }
                            )
                        }
                    }
                }
            }
        }
    }
}
