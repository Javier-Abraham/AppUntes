package com.appuntes

import com.appuntes.data.local.dao.MateriaDao
import com.appuntes.data.local.dao.TareaDao
import com.appuntes.data.remote.RetrofitInstance
import com.appuntes.data.remote.SupabaseApiService
import com.appuntes.data.repository.MateriaRepository
import com.appuntes.data.repository.TareaRepository
import com.appuntes.domain.model.EstadoTarea
import com.appuntes.domain.model.Materia
import com.appuntes.domain.model.Tarea
import com.appuntes.presentation.home.HomeViewModel
import com.appuntes.presentation.materias.MateriasViewModel
import com.appuntes.presentation.tareas.TareasViewModel
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class ViewModelTests {

    private val testDispatcher = UnconfinedTestDispatcher()

    // ── Fechas de referencia ──────────────────────────────────────────────
    private val hoy        = LocalDate.now()
    private val manana     = hoy.plusDays(1)
    private val enSeisDias = hoy.plusDays(6) // dentro de la ventana próxima (< 7d)
    private val enOchoDias = hoy.plusDays(8) // fuera de la ventana próxima (> 7d)

    // ── Datos de prueba ───────────────────────────────────────────────────
    private val materia1 = Materia(id = 1, nombre = "Matemáticas", profesor = "Pérez", color = "#6750A4", icono = "book")
    private val materia2 = Materia(id = 2, nombre = "Historia",    profesor = "López", color = "#006874", icono = "school")

    private val tareaHoy       = Tarea(id = 1, titulo = "Entrega hoy",     materiaId = 1, fechaEntrega = hoy)
    private val tareaManana    = Tarea(id = 2, titulo = "Entrega mañana",  materiaId = 1, fechaEntrega = manana)
    private val tareaProxima   = Tarea(id = 3, titulo = "Entrega en 6d",   materiaId = 1, fechaEntrega = enSeisDias)
    private val tareaLejana    = Tarea(id = 4, titulo = "Entrega en 8d",   materiaId = 1, fechaEntrega = enOchoDias)
    private val tareaCompletada = Tarea(id = 5, titulo = "Ya terminé",     materiaId = 1, fechaEntrega = manana, estado = EstadoTarea.COMPLETADA)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        // Evita llamadas HTTP reales durante syncWithSupabase() del HomeViewModel
        mockkObject(RetrofitInstance)
        val mockApi = mockk<SupabaseApiService>(relaxed = true)
        every { RetrofitInstance.supabaseApi } returns mockApi
        coEvery { mockApi.getMaterias() } throws Exception("test — sin red")
        coEvery { mockApi.getTareas()   } throws Exception("test — sin red")
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    // ── Helpers de construcción de mocks ─────────────────────────────────

    private fun mockMateriaRepo(materias: List<Materia> = emptyList()): MateriaRepository = mockk {
        every { this@mockk.materias } returns flowOf(materias)
        every { dao } returns mockk<MateriaDao>(relaxed = true)
    }

    private fun mockTareaRepo(
        tareas: List<Tarea> = emptyList(),
        pendientes: List<Tarea> = emptyList()
    ): TareaRepository = mockk {
        every { this@mockk.tareas }          returns flowOf(tareas)
        every { tareasPendientes }            returns flowOf(pendientes)
        every { dao }                         returns mockk<TareaDao>(relaxed = true)
    }

    // ══════════════════════════════════════════════════════════════════════
    // HomeViewModel
    // ══════════════════════════════════════════════════════════════════════

    @Test
    fun `homeViewModel al iniciar carga materias y tareas correctamente`() = runTest {
        val pendientes = listOf(tareaHoy, tareaManana)

        val vm = HomeViewModel(
            mockMateriaRepo(listOf(materia1, materia2)),
            mockTareaRepo(pendientes = pendientes)
        )
        advanceUntilIdle()

        with(vm.uiState.value) {
            assertEquals(listOf(materia1, materia2), materias)
            assertEquals(pendientes, tareasPendientes)
            assertFalse(isLoading)
            assertNull(error)
        }
    }

    @Test
    fun `homeViewModel tareasHoy solo incluye las que vencen hoy`() = runTest {
        val pendientes = listOf(tareaHoy, tareaManana, tareaProxima)

        val vm = HomeViewModel(
            mockMateriaRepo(),
            mockTareaRepo(pendientes = pendientes)
        )
        advanceUntilIdle()

        val tareasDeHoy = vm.uiState.value.tareasHoy
        assertEquals("Solo debe haber 1 tarea para hoy", 1, tareasDeHoy.size)
        assertEquals(tareaHoy, tareasDeHoy.first())
    }

    @Test
    fun `homeViewModel tareasProximas no incluye las de hoy ni las de mas de 7 dias`() = runTest {
        val pendientes = listOf(tareaHoy, tareaManana, tareaProxima, tareaLejana)

        val vm = HomeViewModel(
            mockMateriaRepo(),
            mockTareaRepo(pendientes = pendientes)
        )
        advanceUntilIdle()

        val proximas = vm.uiState.value.tareasProximas
        assertFalse("hoy no debe estar en próximas",      tareaHoy     in proximas)
        assertTrue("mañana sí debe estar en próximas",    tareaManana  in proximas)
        assertTrue("en 6 días sí debe estar en próximas", tareaProxima in proximas)
        assertFalse("en 8 días no debe estar en próximas", tareaLejana in proximas)
    }

    // ══════════════════════════════════════════════════════════════════════
    // MateriasViewModel
    // ══════════════════════════════════════════════════════════════════════

    @Test
    fun `materiasViewModel crearMateria con nombre vacio setea error`() = runTest {
        val vm = MateriasViewModel(mockMateriaRepo())

        vm.crearMateria(nombre = "", profesor = "Pérez", aula = "A1", horario = "08:00")

        val error = vm.uiState.value.error
        assertNotNull("Debe haber un mensaje de error", error)
        assertTrue("El error debe mencionar 'vacío'", error!!.contains("vacío", ignoreCase = true))
    }

    @Test
    fun `materiasViewModel crearMateria con nombre en blanco setea error`() = runTest {
        val vm = MateriasViewModel(mockMateriaRepo())

        vm.crearMateria(nombre = "   ", profesor = "Pérez", aula = "A1", horario = "08:00")

        assertNotNull(
            "Nombre en blanco también debe producir error",
            vm.uiState.value.error
        )
    }

    @Test
    fun `materiasViewModel getMateriaById retorna null si no existe`() = runTest {
        val vm = MateriasViewModel(mockMateriaRepo(listOf(materia1, materia2)))
        advanceUntilIdle()

        assertNull(
            "ID inexistente debe retornar null",
            vm.getMateriaById(99L)
        )
    }

    @Test
    fun `materiasViewModel getMateriaById retorna la materia correcta`() = runTest {
        val vm = MateriasViewModel(mockMateriaRepo(listOf(materia1, materia2)))
        advanceUntilIdle()

        assertEquals(materia2, vm.getMateriaById(2L))
        assertEquals(materia1, vm.getMateriaById(1L))
    }

    // ══════════════════════════════════════════════════════════════════════
    // TareasViewModel
    // ══════════════════════════════════════════════════════════════════════

    @Test
    fun `tareasViewModel al cargar muestra todas las tareas`() = runTest {
        val todas = listOf(tareaHoy, tareaManana, tareaCompletada)
        val vm = TareasViewModel(mockTareaRepo(tareas = todas))
        advanceUntilIdle()

        with(vm.uiState.value) {
            assertEquals(todas, tareas)
            assertFalse(isLoading)
        }
    }

    @Test
    fun `tareasViewModel setFiltro COMPLETADA muestra solo completadas`() = runTest {
        val todas = listOf(tareaHoy, tareaManana, tareaCompletada)
        val vm = TareasViewModel(mockTareaRepo(tareas = todas))

        // Necesario para activar SharingStarted.WhileSubscribed en tareasFiltradas
        val collectJob = backgroundScope.launch { vm.tareasFiltradas.collect {} }
        advanceUntilIdle()

        vm.setFiltro(EstadoTarea.COMPLETADA)
        advanceUntilIdle()

        val filtradas = vm.tareasFiltradas.value
        assertEquals("Solo debe haber 1 tarea completada", 1, filtradas.size)
        assertEquals(tareaCompletada, filtradas.first())
        assertTrue(filtradas.all { it.estado == EstadoTarea.COMPLETADA })

        collectJob.cancel()
    }

    @Test
    fun `tareasViewModel setFiltro null muestra todas`() = runTest {
        val todas = listOf(tareaHoy, tareaManana, tareaCompletada)
        val vm = TareasViewModel(mockTareaRepo(tareas = todas))

        val collectJob = backgroundScope.launch { vm.tareasFiltradas.collect {} }
        advanceUntilIdle()

        vm.setFiltro(EstadoTarea.COMPLETADA)
        advanceUntilIdle()
        vm.setFiltro(null)
        advanceUntilIdle()

        assertEquals(
            "Sin filtro debe mostrar todas las tareas",
            todas,
            vm.tareasFiltradas.value
        )

        collectJob.cancel()
    }

    @Test
    fun `tareasViewModel crearTarea con titulo vacio setea error`() = runTest {
        val vm = TareasViewModel(mockTareaRepo())
        val tareaInvalida = Tarea(titulo = "", materiaId = 1, fechaEntrega = hoy)

        vm.crearTarea(tareaInvalida)

        val error = vm.uiState.value.error
        assertNotNull("Debe haber un error con título vacío", error)
        assertTrue("El error debe mencionar 'título'", error!!.contains("título", ignoreCase = true))
    }

    @Test
    fun `tareasViewModel crearTarea con titulo en blanco setea error`() = runTest {
        val vm = TareasViewModel(mockTareaRepo())
        val tareaInvalida = Tarea(titulo = "    ", materiaId = 1, fechaEntrega = hoy)

        vm.crearTarea(tareaInvalida)

        assertNotNull(
            "Título en blanco también debe producir error",
            vm.uiState.value.error
        )
    }
}
