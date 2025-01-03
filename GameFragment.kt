package com.ta_da.android.arch_new.presentation.game.game

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.ta_da.android.R
import com.ta_da.android.arch_new.presentation.base.BaseMVVMFragment
import com.ta_da.android.databinding.FragmentGameBinding
import com.ta_da.android.extensions.collectWithLifecycle
import com.ta_da.android.utils.Constants
import com.ta_da.android.utils.GameFormatter

class GameFragment : BaseMVVMFragment<GameViewModel, FragmentGameBinding>(
    R.layout.fragment_game
) {

    override val viewModel: GameViewModel by viewModels()
    override val binding: FragmentGameBinding by viewBinding(FragmentGameBinding::bind)

    override fun initView(view: View, savedInstanceState: Bundle?) {
        setListeners()
        startGame()
    }

    private fun setListeners() = with(binding) {
        start.setOnClickListener {
            startGame()
        }
        resume.setOnClickListener {
            resumeGame()
        }
        pause.setOnClickListener {
            pauseGame()
        }
        stop.setOnClickListener {
            stopGame()
        }
        add.setOnClickListener {
            viewModel.addTime(viewModel.addedTime.value)
        }
        subtract.setOnClickListener {
            viewModel.subtractTime(viewModel.subtractedTime.value)
        }

        binding.gvGameGameLayout.setListener {
            if (it){
                viewModel.addTime(viewModel.addedTime.value)
            } else {
                viewModel.subtractTime(viewModel.subtractedTime.value)
            }
        }
    }

    override fun initViewModel() {
        viewModel.addedTime.collectWithLifecycle(viewLifecycleOwner) {
            val a = (it/1000).toString()
            binding.add.text = "+$a"
        }
        viewModel.subtractedTime.collectWithLifecycle(viewLifecycleOwner) {
            val a = (it/1000).toString()
            binding.subtract.text = "-$a"
        }


        viewModel.timeRemainingSeconds.collectWithLifecycle(viewLifecycleOwner) {
            if (it == 0L){
                stopGame()
            }

            binding.tvGameTimer.text = GameFormatter.formatTime(it)
        }

        viewModel.elapsedTimeMillis.collectWithLifecycle(viewLifecycleOwner) {
            binding.stopwatchTextView.text = it.toString()
            setGameDifficulty(it)
        }

        viewModel.correctCategories.collectWithLifecycle(viewLifecycleOwner) {
            with(binding) {
                ivGameCategory1.setImageResource(it[0].iconId)
                ivGameCategory2.setImageResource(it[1].iconId)
            }
        }

        viewModel.incorrectCategories.collectWithLifecycle(viewLifecycleOwner) {
            binding.gvGameGameLayout.setCategories(
                viewModel.correctCategories.value,
                viewModel.incorrectCategories.value
            )
        }
    }

    private fun startGame() {
        viewModel.startTimer(Constants.INITIAL_GAME_TIME)
        viewModel.generateCategories()
        binding.gvGameGameLayout.startGame()
    }

    private fun resumeGame() {
        viewModel.resumeTimer()
        binding.gvGameGameLayout.resumeGame()
    }

    private fun pauseGame() {
        viewModel.pauseTimer()
        binding.gvGameGameLayout.pauseGame()
    }

    private fun stopGame() {
        viewModel.stopTimer()
        binding.gvGameGameLayout.stopGame()
    }

    private fun setGameDifficulty(milliseconds: Long) {
        if (milliseconds % Constants.DIFFICULTY_INTERVAL == 0L || milliseconds == 0L) {
            val multiplier = (milliseconds / Constants.DIFFICULTY_INTERVAL).toInt()

            binding.gvGameGameLayout.spawnInterval = calculateDifficulty(
                Constants.MIN_SPAWN_INTERVAL,
                Constants.SPAWN_INTERVAL,
                multiplier,
                Constants.DIFFICULTY_INTERVAL_SPAWN_INTERVAL
            )
            binding.gvGameGameLayout.displayDuration = calculateDifficulty(
                Constants.MIN_DISPLAY_DURATION,
                Constants.DISPLAY_DURATION,
                multiplier,
                Constants.DIFFICULTY_INTERVAL_DISPLAY_DURATION
            )

            viewModel.setAddedTime(
                calculateDifficulty(
                    Constants.MIN_ADDED_GAME_TIME,
                    Constants.ADDED_GAME_TIME,
                    multiplier,
                    Constants.DIFFICULTY_INTERVAL_ADDED_GAME_TIME
            ))
            viewModel.setSubtractedTime(
                calculateDifficulty(
                    Constants.MIN_SUBTRACTED_GAME_TIME,
                    Constants.SUBTRACTED_GAME_TIME,
                    multiplier,
                    Constants.DIFFICULTY_INTERVAL_SUBTRACTED_GAME_TIME
                ))
        }
    }

    private fun calculateDifficulty(minValue: Long, value: Long, multiplier: Int, interval: Long): Long {
        return maxOf(
            minValue,
            value - (multiplier * interval)
        )
    }
}
