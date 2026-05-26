package com.appuntes.presentation.tareas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appuntes.data.repository.TareaRepository
import com.appuntes.domain.model.EstadoTarea
import com.appuntes.domain.model.Tarea
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class TareasUiState(
    val isLoading: Boolean = false,
    val tareas: List<Tarea> = emptyList(),
    val filtroActivo: EstadoTarea? = null,
    val error: String? = null,
    val success: String? = null
)

class TareasViewModel(private val repository: TareaRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(TareasUiState(isLoading = true))
    val uiState: StateFlow<TareasUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.tareas
                .catch { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
                .collect { tareas -> _uiState.update { it.copy(isLoading = false, tareas = tareas) } }
        }
    }

    val tareasFiltradas: StateFlow<List<Tarea>> = uiState.map { state ->
        state.filtroActivo?.let { filtro -> state.tareas.filter { it.estado == filtro } } ?: state.tareas
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setFiltro(estado: EstadoTarea?) { _uiState.update { it.copy(filtroActivo = estado) } }

    suspend fun getTareaById(id: Long): Tarea? = repository.getTareaById(id)

    fun crearTarea(tarea: Tarea) {
        if (tarea.titulo.isBlank()) { _uiState.update { it.copy(error = "El título no puede estar vacío") }; return }
        viewModelScope.launch { repository.insertTarea(tarea); _uiState.update { it.copy(success = "Tarea creada") } }
    }

    fun editarTarea(tarea: Tarea) {
        viewModelScope.launch { repository.updateTarea(tarea); _uiState.update { it.copy(success = "Tarea actualizada") } }
    }

    fun eliminarTarea(id: Long) {
        viewModelScope.launch { repository.deleteTarea(id); _uiState.update { it.copy(success = "Tarea eliminada") } }
    }

    fun marcarCompletada(id: Long) { viewModelScope.launch { repository.marcarCompletada(id) } }

    fun marcarEnProgreso(id: Long) { viewModelScope.launch { repository.marcarEnProgreso(id) } }

    fun clearMessages() { _uiState.update { it.copy(error = null, success = null) } }
}
