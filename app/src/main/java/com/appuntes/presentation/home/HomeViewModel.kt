package com.appuntes.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appuntes.data.remote.SupabaseRepository
import com.appuntes.data.repository.MateriaRepository
import com.appuntes.data.repository.TareaRepository
import com.appuntes.domain.model.Materia
import com.appuntes.domain.model.Tarea
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

data class HomeUiState(
    val isLoading: Boolean = false,
    val materias: List<Materia> = emptyList(),
    val tareasPendientes: List<Tarea> = emptyList(),
    val tareasHoy: List<Tarea> = emptyList(),
    val tareasProximas: List<Tarea> = emptyList(),
    val error: String? = null
)

class HomeViewModel(
    private val materiaRepository: MateriaRepository,
    private val tareaRepository: TareaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
        syncWithSupabase()
    }

    private fun syncWithSupabase() {
        viewModelScope.launch {
            try {
                val supabaseRepo = SupabaseRepository(materiaRepository.dao, tareaRepository.dao)
                supabaseRepo.syncFromSupabase()
            } catch (e: Exception) {
                // Falla silenciosamente — la app funciona offline con Room
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(materiaRepository.materias, tareaRepository.tareasPendientes)
            { materias, tareasPendientes ->
                val hoy = LocalDate.now()
                val en7dias = hoy.plusDays(7)
                HomeUiState(
                    isLoading = false,
                    materias = materias,
                    tareasPendientes = tareasPendientes,
                    tareasHoy = tareasPendientes.filter { it.fechaEntrega == hoy },
                    tareasProximas = tareasPendientes.filter {
                        it.fechaEntrega.isAfter(hoy) && it.fechaEntrega.isBefore(en7dias)
                    }
                )
            }.catch { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun marcarTareaCompletada(tareaId: Long) {
        viewModelScope.launch { tareaRepository.marcarCompletada(tareaId) }
    }

    fun clearError() { _uiState.update { it.copy(error = null) } }
}
