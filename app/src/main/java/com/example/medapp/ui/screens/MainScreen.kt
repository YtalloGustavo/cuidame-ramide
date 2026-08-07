package com.example.medapp.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medapp.ui.theme.*
import com.example.medapp.ui.viewmodel.MainViewModel
import androidx.core.content.ContextCompat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onLogout: () -> Unit = {}
) {
    val doses by viewModel.doses.collectAsState()
    val caregivers by viewModel.caregivers.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()
    val adherenceRate by viewModel.adherenceRate.collectAsState()
    val dosesDelayed by viewModel.dosesDelayed.collectAsState()
    val dosesMissed by viewModel.dosesMissed.collectAsState()
    val historyItems by viewModel.historyItems.collectAsState()
    val medicineSearchState by viewModel.medicineSearch.collectAsState()
    val nameSearchResults by viewModel.nameSearchResults.collectAsState()
    val interactions by viewModel.interactions.collectAsState()
    val isSearchingName by viewModel.isSearchingName.collectAsState()
    val medicineList by viewModel.medicineList.collectAsState()

    var showScanner by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.loadMedicineList() }

    val context = LocalContext.current
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        // Permission result handled silently — notifications will work or be silently dropped
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                CustomBottomBar(
                    currentTab = currentTab,
                    onTabSelected = { viewModel.selectTab(it) }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentTab) {
                    0 -> HomeScreen(
                        userName = "João Silva",
                        doses = doses,
                        onConfirmDose = { viewModel.confirmDose(it) },
                        onDelayDose = { viewModel.delayDose(it) },
                        onMissDose = { viewModel.missDose(it) },
                        onDeleteDose = { viewModel.deleteDose(it) },
                        onUpdateDose = { viewModel.updateDose(it) }
                    )
                    1 -> BulaScreen(viewModel = viewModel)
                    2 -> CadastroScreen(
                        onAddMedicine = { name, doseDescription, time ->
                            viewModel.addMedicine(name, doseDescription, time)
                        },
                        onNavigateToHome = { viewModel.navigateToHome() },
                        onScanRequested = { showScanner = true },
                        onSearchByEan = { code -> viewModel.searchByEan(code) },
                        medicineSearchState = medicineSearchState,
                        onSearchByName = { viewModel.searchByName(it) },
                        nameSearchResults = nameSearchResults,
                        interactions = interactions,
                        isSearchingName = isSearchingName,
                        popularMedicines = medicineList,
                        onClearSearch = { viewModel.clearMedicineSearch() }
                    )
                    3 -> HistoricoScreen(
                        adherenceRate = adherenceRate,
                        dosesDelayed = dosesDelayed,
                        dosesMissed = dosesMissed,
                        historyItems = historyItems
                    )
                    4 -> CuidadorScreen(
                        caregivers = caregivers,
                        onToggleCaregiver = { viewModel.toggleCaregiver(it) },
                        onAddCaregiver = {},
                        onAddCaregiverData = { name, relationship, phone ->
                            viewModel.addCaregiver(name, relationship, phone)
                        },
                        onLogout = onLogout
                    )
                }
            }
        }

        if (showScanner) {
            BarcodeScannerScreen(
                onBarcodeScanned = { code ->
                    showScanner = false
                    viewModel.searchByEan(code)
                },
                onBack = { showScanner = false }
            )
        }
    }
}

@Composable
fun CustomBottomBar(
    currentTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .shadow(16.dp, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
        color = Surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                icon = "🏠",
                label = "Hoje",
                isActive = currentTab == 0,
                onClick = { onTabSelected(0) }
            )

            BottomNavItem(
                icon = "📋",
                label = "Meds",
                isActive = currentTab == 1,
                onClick = { onTabSelected(1) }
            )

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(GreenPrimary)
                    .clickable { onTabSelected(2) }
                    .shadow(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+",
                    color = White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            BottomNavItem(
                icon = "📊",
                label = "Histórico",
                isActive = currentTab == 3,
                onClick = { onTabSelected(3) }
            )

            BottomNavItem(
                icon = "👤",
                label = "Perfil",
                isActive = currentTab == 4,
                onClick = { onTabSelected(4) }
            )
        }
    }
}

@Composable
fun BottomNavItem(
    icon: String,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (isActive) GreenLight else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = icon,
                fontSize = 14.sp
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = if (isActive) GreenPrimary else Ink3,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = EpilogueFontFamily
        )
    }
}

@Preview
@Composable
fun MainScreenPreview() {
    MedAppTheme {
        MainScreen(
            viewModel = androidx.hilt.navigation.compose.hiltViewModel()
        )
    }
}