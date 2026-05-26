package com.appuntes.presentation.materias

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appuntes.data.repository.MateriaRepository
import com.appuntes.domain.model.Materia
import com.appuntes.presentation.theme.MateriaColorsHex
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class MateriasUiState(
    val isLoading: Boolean = false,
    val materias: List<Materia> = emptyList(),
    val error: String? = null,
    val success: String? = null
)

class MateriasViewModel(private val repository: MateriaRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(MateriasUiState(isLoading = true))
    val uiState: StateFlow<MateriasUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.materias
                .catch { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
                .collect { materias -> _uiState.update { it.copy(isLoading = false, materias = materias) } }
        }
    }

    fun getMateriaById(id: Long): Materia? = uiState.value.materias.firstOrNull { it.id == id }

    fun crearMateria(nombre: String, profesor: String, aula: String, horario: String) {
        if (nombre.isBlank()) { _uiState.update { it.copy(error = "El nombre no puede estar vacío") }; return }
        viewModelScope.launch {
            val colorIndex = uiState.value.materias.size % MateriaColorsHex.size
            repository.insertMateria(Materia(nombre = nombre.trim(), profesor = profesor.trim(),
                color = MateriaColorsHex[colorIndex], icono = "book",
                aula = aula.trim(), horario = horario.trim()))
            _uiState.update { it.copy(success = "Materia creada correctamente") }
        }
    }

    fun editarMateria(id: Long, nombre: String, profesor: String, aula: String, horario: String) {
        if (nombre.isBlank()) { _uiState.update { it.copy(error = "El nombre no puede estar vacío") }; return }
        viewModelScope.launch {
            val existing = repository.getMateriaById(id) ?: return@launch
            repository.updateMateria(existing.copy(nombre = nombre.trim(), profesor = profesor.trim(),
                aula = aula.trim(), horario = horario.trim()))
            _uiState.update { it.copy(success = "Materia actualizada") }
        }
    }

    fun eliminarMateria(id: Long) {
        viewModelScope.launch {
            repository.deleteMateria(id)
            _uiState.update { it.copy(success = "Materia eliminada") }
        }
    }

    fun clearMessages() { _uiState.update { it.copy(error = null, success = null) } }
}
