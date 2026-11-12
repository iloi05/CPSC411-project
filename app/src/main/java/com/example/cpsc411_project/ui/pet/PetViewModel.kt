package com.example.cpsc411_project.ui.pet

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cpsc411_project.data.PetRepository
import com.example.cpsc411_project.data.Stat
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import  kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import  kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

data class PetUiState(
    val petType: String,
    val happiness: Int,
    val hunger: Int,
    val clean: Int,
    val isEating: Boolean = false,
    val isPlaying: Boolean = false,
    val isCleaning: Boolean = false
)

class PetViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = PetRepository(app)
    private val _state = MutableStateFlow(
        PetUiState(
            petType = repo.petType,
            happiness = repo.read(Stat.HAPPINESS),
            hunger = repo.read(Stat.HUNGER),
            clean = repo.read(Stat.CLEAN)
        )
    )
    val state: StateFlow<PetUiState> = _state.asStateFlow()
    private val pulseJobs = mutableMapOf<String, Job>()

    init {
        viewModelScope.launch {
            while (true) {
                _state.update {
                    it.copy(
                        happiness = repo.applyDecay(Stat.HAPPINESS),
                        hunger = repo.applyDecay(Stat.HUNGER),
                        clean = repo.applyDecay(Stat.CLEAN)
                    )
                }
                delay(10.seconds)
            }
        }
    }
    fun play(): PetUiState {
        val s = _state.updateAndGet { it.copy(happiness = repo.change(Stat.HAPPINESS,5)) }
        pulse("play") { st, on -> st.copy(isPlaying = on) }
        return s
    }
    fun pet(): PetUiState =
        _state.updateAndGet { it.copy(happiness = repo.change(Stat.HAPPINESS, 5)) }

    fun feed(): PetUiState {
        val s = _state.updateAndGet { it.copy(clean = repo.change(Stat.HUNGER, 5)) }
        pulse("feed") { st, on -> st.copy(isEating = on) }
        return s
    }

    fun scrub(): PetUiState {
        val s = _state.updateAndGet { it.copy(clean = repo.change(Stat.CLEAN, 2)) }
        pulse("clean") { st, on -> st.copy(isCleaning = on) }
        return s
    }
    private fun pulse(key: String, set: (PetUiState, Boolean) -> PetUiState) {
        pulseJobs[key]?.cancel()
        pulseJobs[key] = viewModelScope.launch {
            _state.update { set(it, true) }
            delay(2.seconds)
            _state.update { set(it, false) }
        }
    }
}