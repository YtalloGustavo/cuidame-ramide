package com.example.medapp

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.medapp.data.AnvisaApiService
import com.example.medapp.data.MedicineInfo
import com.example.medapp.data.MedicineRepository
import com.example.medapp.data.MedicineSearchUiState
import com.example.medapp.di.CaregiverRepository
import com.example.medapp.di.DoseRepository
import com.example.medapp.data.local.MedAppDatabase
import com.example.medapp.ui.screens.DoseStatus
import com.example.medapp.ui.viewmodel.MainViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MainViewModelTest {

    private lateinit var database: MedAppDatabase
    private lateinit var doseRepo: DoseRepository
    private lateinit var caregiverRepo: CaregiverRepository
    private lateinit var medicineRepo: MedicineRepository
    private lateinit var apiService: AnvisaApiService
    private lateinit var viewModel: MainViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, MedAppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        doseRepo = DoseRepository(database.doseDao())
        caregiverRepo = CaregiverRepository(database.caregiverDao())
        apiService = mockk()
        medicineRepo = MedicineRepository(apiService)
        viewModel = MainViewModel(
            context = context,
            doseRepository = doseRepo,
            caregiverRepository = caregiverRepo,
            medicineRepository = medicineRepo
        )
    }

    @After
    fun tearDown() {
        database.close()
        Dispatchers.resetMain()
    }

    @Test
    fun selectTab_updates_currentTab() = runTest {
        viewModel.selectTab(3)
        assertEquals(3, viewModel.currentTab.value)
    }

    @Test
    fun navigateToHome_sets_tab_zero() = runTest {
        viewModel.selectTab(4)
        viewModel.navigateToHome()
        assertEquals(0, viewModel.currentTab.value)
    }

    @Test
    fun confirmDose_changes_status_to_Tomado() = runTest {
        viewModel.addMedicine("Test", "1 pill", "08:00")
        val added = viewModel.doses.value.first { it.name == "Test" }
        viewModel.confirmDose(added.id)
        val updated = viewModel.doses.value.first { it.id == added.id }
        assertEquals(DoseStatus.Tomado, updated.status)
    }

    @Test
    fun delayDose_changes_status_to_MaisTarde() = runTest {
        viewModel.addMedicine("Test", "1 pill", "08:00")
        val added = viewModel.doses.value.first { it.name == "Test" }
        viewModel.delayDose(added.id)
        val updated = viewModel.doses.value.first { it.id == added.id }
        assertEquals(DoseStatus.MaisTarde, updated.status)
    }

    @Test
    fun missDose_changes_status_to_Perdido() = runTest {
        viewModel.addMedicine("Test", "1 pill", "08:00")
        val added = viewModel.doses.value.first { it.name == "Test" }
        viewModel.missDose(added.id)
        val updated = viewModel.doses.value.first { it.id == added.id }
        assertEquals(DoseStatus.Perdido, updated.status)
    }

    @Test
    fun deleteDose_removes_from_list() = runTest {
        val initialSize = viewModel.doses.value.size
        viewModel.addMedicine("Test", "1 pill", "08:00")
        assertEquals(initialSize + 1, viewModel.doses.value.size)
        val added = viewModel.doses.value.first { it.name == "Test" }
        viewModel.deleteDose(added.id)
        assertEquals(initialSize, viewModel.doses.value.size)
    }

    @Test
    fun addMedicine_adds_dose_with_correct_data() = runTest {
        viewModel.addMedicine("Paracetamol", "1 comp", "14:00")
        val added = viewModel.doses.value.first { it.name == "Paracetamol" }
        assertEquals("Paracetamol", added.name)
        assertEquals("1 comp", added.doseDescription)
        assertEquals("14:00", added.time)
        assertEquals(DoseStatus.Pendente, added.status)
    }

    @Test
    fun searchByEan_sets_Success_when_api_returns_medicine() = runTest {
        val mockMedicine = MedicineInfo(
            ean = "7891000100011",
            name = "Losartana",
            laboratory = "EMS",
            anvisaRegistration = "123",
            activeIngredient = "Losartana",
            category = "Anti-hipertensivo"
        )
        coEvery { apiService.findMedicineByEan(any()) } returns mockMedicine

        viewModel.searchByEan("7891000100011")
        val state = viewModel.medicineSearch.value
        assertTrue(state is MedicineSearchUiState.Success)
        assertEquals("Losartana", (state as MedicineSearchUiState.Success).medicine.name)
    }
}
