package com.ta_da.android.arch_new.presentation.game.game

import com.ta_da.android.arch_new.domain.repository.IGameTimerRepository
import com.ta_da.android.arch_new.presentation.base.BaseViewModel
import com.ta_da.android.enums.GameCategory
import com.ta_da.android.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val gameTimerRepository: IGameTimerRepository
) : BaseViewModel() {

    val timeRemainingSeconds = gameTimerRepository.timeRemainingSeconds
    val elapsedTimeMillis = gameTimerRepository.elapsedTimeMillis

    private val _correctCategories = MutableStateFlow<List<GameCategory>>(emptyList())
    val correctCategories = _correctCategories.asStateFlow()

    private val _incorrectCategories = MutableStateFlow<List<GameCategory>>(emptyList())
    val incorrectCategories = _incorrectCategories.asStateFlow()

    private val categories = GameCategory.entries

    private val _addedTime = MutableStateFlow<Long>(Constants.ADDED_GAME_TIME)
    val addedTime = _addedTime.asStateFlow()

    private val _subtractedTime = MutableStateFlow<Long>(Constants.SUBTRACTED_GAME_TIME)
    val subtractedTime = _subtractedTime.asStateFlow()


    fun startTimer(milliseconds: Long) {
        gameTimerRepository.startTimer(milliseconds)
    }

    fun stopTimer() {
        gameTimerRepository.stopTimer()
    }

    fun pauseTimer() {
        gameTimerRepository.pauseTimer()
    }

    fun resumeTimer() {
        gameTimerRepository.resumeTimer()
    }

    fun addTime(milliseconds: Long) {
        gameTimerRepository.addTime(milliseconds)
    }

    fun subtractTime(milliseconds: Long) {
        gameTimerRepository.subtractTime(milliseconds)
    }

    fun setAddedTime(value: Long){
        _addedTime.value = value
    }

    fun setSubtractedTime(value: Long){
        _subtractedTime.value = value
    }


    fun generateCategories() {
        val shuffledCategories = categories.shuffled()

        val selectedCorrectCategories = shuffledCategories.take(2)
        val selectedIncorrectCategories = shuffledCategories - selectedCorrectCategories

        _correctCategories.value = selectedCorrectCategories
        _incorrectCategories.value = selectedIncorrectCategories
    }


    override fun onCleared() {
        super.onCleared()
        gameTimerRepository.clear()
    }
}
