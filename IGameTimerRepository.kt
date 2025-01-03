package com.ta_da.android.arch_new.domain.repository

import kotlinx.coroutines.flow.StateFlow

interface IGameTimerRepository {

    fun startTimer(milliseconds: Long)

    fun stopTimer()

    fun pauseTimer()

    fun resumeTimer()

    fun addTime(milliseconds: Long)

    fun subtractTime(milliseconds: Long)

    fun clear()

    val timeRemainingMillis: StateFlow<Long>

    val timeRemainingSeconds: StateFlow<Long>

    val elapsedTimeMillis: StateFlow<Long>

}

