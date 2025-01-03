package com.ta_da.android.arch_new.data.repository

import com.ta_da.android.arch_new.domain.repository.IGameTimerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class GameTimerRepository @Inject constructor() : IGameTimerRepository {

    private val _timeRemainingMillis = MutableStateFlow(0L)
    override val timeRemainingMillis = _timeRemainingMillis.asStateFlow()

    private val _timeRemainingSeconds = MutableStateFlow(0L)
    override val timeRemainingSeconds = _timeRemainingSeconds.asStateFlow()

    private val _elapsedTimeMillis = MutableStateFlow(0L)
    override val elapsedTimeMillis = _elapsedTimeMillis.asStateFlow()

    private var isPaused = false
    private var isRunning = false

    private var job: Job? = null

//    private var runnable: Runnable = Runnable {
//        run {
//
//        }
//    }

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun startTimer(milliseconds: Long) {
        isPaused = false
        isRunning = true

        job?.cancel()
        job = null

        _timeRemainingMillis.value = milliseconds
        _timeRemainingSeconds.value = milliseconds / 1000
        _elapsedTimeMillis.value = 0L

        startLogic()
    }

    override fun stopTimer() {
        isRunning = false

        job?.cancel()
        job = null
    }

    override fun pauseTimer() {
        if (!isPaused && isRunning){
            isPaused = true

            job?.cancel()
            job = null
        }
    }

    override fun resumeTimer() {
        if (isPaused && isRunning) {
            isPaused = false

            startLogic()
        }
    }

    override fun addTime(milliseconds: Long) {
        _timeRemainingMillis.value += milliseconds
        _timeRemainingSeconds.value = _timeRemainingMillis.value / 1000
    }

    override fun subtractTime(milliseconds: Long) {
        _timeRemainingMillis.value = (_timeRemainingMillis.value - milliseconds).coerceAtLeast(0)
        _timeRemainingSeconds.value = _timeRemainingMillis.value / 1000
    }

    private fun startLogic() {
        val interval = 1L

        if (job == null) {
            job = coroutineScope.launch {
                while (_timeRemainingMillis.value > 0) {
                    if (isPaused) break
                    delay(interval)

                    _timeRemainingMillis.value = (_timeRemainingMillis.value - interval).coerceAtLeast(0)
                    _elapsedTimeMillis.value += interval

                    if (_timeRemainingMillis.value % 1000 == 0L) {
                        _timeRemainingSeconds.value = _timeRemainingMillis.value / 1000
                    }
                }

                if (_timeRemainingMillis.value == 0L) {
                    stopTimer()
                }
            }
        }
    }

    override fun clear() {
        coroutineScope.cancel()
    }
}
