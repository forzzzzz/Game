package com.ta_da.android.utils

object GameFormatter {

    fun formatTime(seconds: Long): String {
        val minutes = calculateMinutes(seconds)
        val remainingSeconds = calculateRemainingSeconds(seconds)
        return formatToMMSS(minutes, remainingSeconds)
    }

    private fun calculateMinutes(seconds: Long): Long {
        return seconds / 60
    }

    private fun calculateRemainingSeconds(seconds: Long): Long {
        return seconds % 60
    }

    private fun formatToMMSS(minutes: Long, seconds: Long): String {
        return String.format("%02d:%02d", minutes, seconds)
    }

}
