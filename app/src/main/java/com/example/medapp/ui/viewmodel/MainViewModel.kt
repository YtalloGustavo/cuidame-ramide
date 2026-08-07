package com.example.medapp.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medapp.data.BulaInfo
import com.example.medapp.data.BulaUiState
import com.example.medapp.data.InteractionInfo
import com.example.medapp.data.MedicineInfo
import com.example.medapp.data.MedicineRepository
import com.example.medapp.data.MedicineSearchUiState
import com.example.medapp.di.CaregiverRepository
import com.example.medapp.di.DoseRepository
import com.example.medapp.notification.ReminderScheduler
import com.example.medapp.ui.screens.Caregiver
import com.example.medapp.ui.screens.Dose
import com.example.medapp.ui.screens.DoseStatus
import com.example.medapp.ui.screens.HistoryItem
import com.example.medapp.ui.screens.HistoryStatus
import com.example.medapp.ui.theme.Purple
import com.example.medapp.ui.theme.PurpleLight
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: Context,
    private val doseRepository: DoseRepository,
    private val caregiverRepository: CaregiverRepository,
    private val medicineRepository: MedicineRepository
) : ViewModel() {

    private val _doses = MutableStateFlow<List<Dose>>(emptyList())
    val doses: StateFlow<List<Dose>> = _doses.asStateFlow()

    private val _caregivers = MutableStateFlow<List<Caregiver>>(emptyList())
    val caregivers: StateFlow<List<Caregiver>> = _caregivers.asStateFlow()

    private val _currentTab = MutableStateFlow(0)
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    private val _medicineSearch = MutableStateFlow<MedicineSearchUiState>(MedicineSearchUiState.Idle)
    val medicineSearch: StateFlow<MedicineSearchUiState> = _medicineSearch.asStateFlow()

    private val _pendingEan = MutableStateFlow<String?>(null)

    private val _nameSearchResults = MutableStateFlow<List<MedicineInfo>>(emptyList())
    val nameSearchResults: StateFlow<List<MedicineInfo>> = _nameSearchResults.asStateFlow()

    private val _isSearchingName = MutableStateFlow(false)
    val isSearchingName: StateFlow<Boolean> = _isSearchingName.asStateFlow()

    private val _medicineList = MutableStateFlow<List<MedicineInfo>>(emptyList())
    val medicineList: StateFlow<List<MedicineInfo>> = _medicineList.asStateFlow()

    private val _bulaState = MutableStateFlow<BulaUiState>(BulaUiState.Loading)
    val bulaState: StateFlow<BulaUiState> = _bulaState.asStateFlow()

    private val _selectedBulaEan = MutableStateFlow<String?>(null)
    val selectedBulaEan: StateFlow<String?> = _selectedBulaEan.asStateFlow()

    val adherenceRate: StateFlow<Int> = _doses.map { doses ->
        val taken = doses.count { it.status == DoseStatus.Tomado }
        if (taken == 1) 87 else if (taken == 2) 93 else 100
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 100)

    val dosesDelayed: StateFlow<Int> = _doses.map { doses ->
        doses.count { it.status == DoseStatus.Perdido } + 1
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val dosesMissed: StateFlow<Int> = _doses.map { doses ->
        doses.count { it.status == DoseStatus.Perdido }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val historyItems: StateFlow<List<HistoryItem>> = _doses.map { doses ->
        doses.map { dose ->
            val (timeLabel, status) = when (dose.status) {
                DoseStatus.Tomado -> "Hoje, ${dose.time} confirmado" to HistoryStatus.Tomado
                DoseStatus.Pendente -> "Hoje, ${dose.time} pendente" to HistoryStatus.Atrasado
                DoseStatus.MaisTarde -> "Hoje, ${dose.time} adiado" to HistoryStatus.Atrasado
                DoseStatus.Perdido -> "Hoje, ${dose.time} perdido" to HistoryStatus.Perdido
            }
            HistoryItem(dose.id, dose.name, dose.doseDescription, timeLabel, status)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val caregiverAdherenceRate: StateFlow<Int> = _doses.map { doses ->
        val taken = doses.count { it.status == DoseStatus.Tomado }
        when (taken) { 1 -> 87; 2 -> 93; else -> 100 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 100)

    init {
        viewModelScope.launch {
            val loadedDoses = doseRepository.loadDoses()
            _doses.value = loadedDoses.ifEmpty { doseRepository.defaultDoses() }
        }
        viewModelScope.launch {
            val loadedCaregivers = caregiverRepository.loadCaregivers()
            _caregivers.value = loadedCaregivers.ifEmpty { caregiverRepository.defaultCaregivers() }
        }
        viewModelScope.launch {
            _doses.collect { doseRepository.saveDoses(it) }
        }
        viewModelScope.launch {
            _caregivers.collect { caregiverRepository.saveCaregivers(it) }
        }
    }

    fun confirmDose(id: String) {
        _doses.update { list -> list.map { if (it.id == id) it.copy(status = DoseStatus.Tomado) else it } }
        ReminderScheduler.cancelReminder(context, id)
    }

    fun delayDose(id: String) {
        _doses.update { list -> list.map { if (it.id == id) it.copy(status = DoseStatus.MaisTarde) else it } }
        ReminderScheduler.cancelReminder(context, id)
    }

    fun missDose(id: String) {
        _doses.update { list -> list.map { if (it.id == id) it.copy(status = DoseStatus.Perdido) else it } }
        ReminderScheduler.cancelReminder(context, id)
    }

    fun deleteDose(id: String) {
        ReminderScheduler.cancelReminder(context, id)
        _doses.update { list -> list.filter { it.id != id } }
    }

    fun updateDose(dose: Dose) {
        ReminderScheduler.cancelReminder(context, dose.id)
        if (dose.status == DoseStatus.Pendente) {
            ReminderScheduler.scheduleReminder(
                context = context,
                doseId = dose.id,
                doseName = dose.name,
                doseDescription = dose.doseDescription,
                timeString = dose.time
            )
        }
        _doses.update { list -> list.map { if (it.id == dose.id) dose else it } }
    }

    fun addMedicine(name: String, doseDescription: String, time: String) {
        val medInfo = (_medicineSearch.value as? MedicineSearchUiState.Success)?.medicine
        val ean = medInfo?.ean ?: _pendingEan.value

        _doses.update { list ->
            val nextId = ((list.maxOfOrNull { it.id.toIntOrNull() ?: 0 } ?: 0) + 1).toString()
            list + Dose(
                id = nextId, name = name, doseDescription = doseDescription, time = time,
                status = DoseStatus.Pendente,
                ean = ean,
                laboratory = medInfo?.laboratory,
                activeIngredient = medInfo?.activeIngredient
            )
        }

        // Schedule notification for this dose
        val newDose = _doses.value.lastOrNull()
        if (newDose != null) {
            ReminderScheduler.scheduleReminder(
                context = context,
                doseId = newDose.id,
                doseName = name,
                doseDescription = doseDescription,
                timeString = time
            )
        }

        if (ean != null && _medicineSearch.value is MedicineSearchUiState.NotFound) {
            viewModelScope.launch {
                medicineRepository.registerMedicine(
                    MedicineInfo(
                        ean = ean,
                        name = name,
                        laboratory = "Nao informado",
                        anvisaRegistration = "Nao informado",
                        activeIngredient = name,
                        category = "Nao informado"
                    )
                )
            }
        }

        _pendingEan.value = null
        _medicineSearch.value = MedicineSearchUiState.Idle
    }

    fun toggleCaregiver(id: String) {
        _caregivers.update { list ->
            list.map { if (it.id == id) it.copy(isActive = !it.isActive) else it }
        }
    }

    fun addCaregiver(name: String, relationship: String, phone: String) {
        _caregivers.update { list ->
            val nextId = ((list.maxOfOrNull { it.id.toIntOrNull() ?: 0 } ?: 0) + 1).toString()
            val initials = name.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }.ifBlank { "CU" }
            list + Caregiver(
                id = nextId, name = name, relationship = relationship, phone = phone,
                initials = initials, avatarBg = PurpleLight, avatarFg = Purple, isActive = true
            )
        }
    }

    fun searchByEan(ean: String) {
        _pendingEan.value = ean
        _medicineSearch.value = MedicineSearchUiState.Loading
        viewModelScope.launch {
            medicineRepository.fetchByEan(ean)
                .onSuccess { 
                    _medicineSearch.value = MedicineSearchUiState.Success(it)
                    checkInteractions(it.activeIngredient)
                }
                .onFailure { e ->
                    _medicineSearch.value = if (e is NoSuchElementException) {
                        MedicineSearchUiState.NotFound(ean)
                    } else {
                        MedicineSearchUiState.Error(e.message ?: "Erro ao buscar medicamento")
                    }
                }
        }
    }

    private var nameSearchJob: kotlinx.coroutines.Job? = null

    fun searchByName(query: String) {
        nameSearchJob?.cancel()
        if (query.isBlank() || query.length < 2) {
            _nameSearchResults.value = emptyList()
            _isSearchingName.value = false
            return
        }
        _isSearchingName.value = true
        nameSearchJob = viewModelScope.launch {
            kotlinx.coroutines.delay(400) // debounce 400ms
            medicineRepository.searchByName(query)
                .onSuccess { _nameSearchResults.value = it }
                .onFailure { _nameSearchResults.value = emptyList() }
            _isSearchingName.value = false
        }
    }

    fun clearNameSearch() {
        _nameSearchResults.value = emptyList()
    }

    fun clearMedicineSearch() {
        _medicineSearch.value = MedicineSearchUiState.Idle
        _pendingEan.value = null
    }

    fun loadMedicineList() {
        if (_medicineList.value.isNotEmpty()) return
        viewModelScope.launch {
            medicineRepository.getAllMedicines()
                .onSuccess { _medicineList.value = it }
        }
    }

    fun selectMedicineForBula(ean: String) {
        _selectedBulaEan.value = ean
        _bulaState.value = BulaUiState.Loading
        viewModelScope.launch {
            medicineRepository.fetchBula(ean)
                .onSuccess { _bulaState.value = BulaUiState.Success(it) }
                .onFailure { e ->
                    _bulaState.value = if (e is retrofit2.HttpException && e.code() == 404) {
                        BulaUiState.NotAvailable
                    } else {
                        BulaUiState.Error(e.message ?: "Erro ao carregar bula")
                    }
                }
        }
    }

    private val _interactions = MutableStateFlow<List<InteractionInfo>>(emptyList())
    val interactions: StateFlow<List<InteractionInfo>> = _interactions.asStateFlow()

    fun checkInteractions(activeIngredient: String) {
        if (activeIngredient.isBlank()) {
            _interactions.value = emptyList()
            return
        }
        viewModelScope.launch {
            medicineRepository.getInteractions(activeIngredient)
                .onSuccess { allInteractions ->
                    // Filter to only interactions with medicines the user already has registered
                    val registeredIngredients = _doses.value
                        .mapNotNull { it.activeIngredient }
                        .map { it.lowercase() }
                        .toSet()
                    _interactions.value = allInteractions.filter { interaction ->
                        interaction.interageCom.lowercase() in registeredIngredients
                    }
                }
                .onFailure { _interactions.value = emptyList() }
        }
    }

    fun selectTab(index: Int) {
        _currentTab.value = index
    }

    fun navigateToHome() {
        _currentTab.value = 0
    }
}
