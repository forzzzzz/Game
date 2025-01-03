package com.ta_da.android.ui.custom.views

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import com.ta_da.android.R
import android.content.Context
import android.graphics.PointF
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.ta_da.android.databinding.GameViewBinding
import com.ta_da.android.enums.GameCategory
import com.ta_da.android.enums.IconProvider
import com.ta_da.android.utils.Constants
import java.util.Random
import java.util.concurrent.atomic.AtomicInteger
import kotlin.Int

typealias OnImageActionListener = (Boolean) -> Unit

class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.gameStyle,
    defStyleRes: Int = R.style.DefaultGameViewStyle
) : ConstraintLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val binding: GameViewBinding
    private var listener: OnImageActionListener? = null

    var characterWidth: Int = LayoutParams.WRAP_CONTENT
        set(value) = updateLayoutParams(binding.ivGameViewCharacter, value, characterHeight)

    var characterHeight: Int = LayoutParams.WRAP_CONTENT
        set(value) = updateLayoutParams(binding.ivGameViewCharacter, characterWidth, value)

    var imageViewSize: Int = LayoutParams.WRAP_CONTENT
    var spacing: Int = 0

    var spawnInterval: Long = Constants.SPAWN_INTERVAL
    var displayDuration: Long = Constants.DISPLAY_DURATION

    private var correctCategories = listOf<GameCategory>()
    private var incorrectCategories = listOf<GameCategory>()
    private val gameObjects = mutableListOf<GameObject>()

    private var isPaused = false
    private var isRunning = false

    private val random = Random()
    private val handler = Handler(Looper.getMainLooper())
    private val idGenerator = AtomicInteger(0)

    init {
        val inflater = LayoutInflater.from(context)
        inflater.inflate(R.layout.game_view, this, true)
        binding = GameViewBinding.bind(this)
        initializeAttributes(attrs, defStyleAttr, defStyleRes)
    }

    private fun initializeAttributes(attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        attrs ?: return

        context.obtainStyledAttributes(attrs, R.styleable.GameView, defStyleAttr, defStyleRes).apply {
            setCharacterImage(getResourceId(R.styleable.GameView_srcCharacter, 0))
            characterWidth = getLayoutDimension(
                R.styleable.GameView_characterWidth,
                LayoutParams.WRAP_CONTENT
            )
            characterHeight = getLayoutDimension(
                R.styleable.GameView_characterHeight,
                LayoutParams.WRAP_CONTENT
            )
            imageViewSize = getLayoutDimension(
                R.styleable.GameView_imageViewSize,
                LayoutParams.WRAP_CONTENT
            )
            spacing = getLayoutDimension(R.styleable.GameView_spacing, 0)
            spawnInterval = getInt(
                R.styleable.GameView_spawnInterval,
                Constants.SPAWN_INTERVAL.toInt()
            ).toLong()
            displayDuration = getInt(
                R.styleable.GameView_displayDuration,
                Constants.DISPLAY_DURATION.toInt()
            ).toLong()
            recycle()
        }
    }

    fun setListener(listener: OnImageActionListener) {
        this.listener = listener
    }

    fun setCharacterImage(resId: Int?) {
        resId?.let {
            binding.ivGameViewCharacter.setImageResource(it)
        }
    }

    fun setCategories(correct: List<GameCategory>, incorrect: List<GameCategory>) {
        correctCategories = correct
        incorrectCategories = incorrect
    }

    fun startGame() {
        isPaused = false
        isRunning = true

        handler.removeCallbacksAndMessages(null)
        clearGameObjects()
        idGenerator.set(0)

        spawnInterval = Constants.SPAWN_INTERVAL
        displayDuration = Constants.DISPLAY_DURATION

        scheduleSpawn()
    }

    fun resumeGame() {
        if (isPaused && isRunning) {
            isPaused = false

            resumeAnimations()
            scheduleSpawn()
        }
    }

    fun pauseGame() {
        if (!isPaused && isRunning) {
            isPaused = true

            pauseAnimations()
        }
    }

    fun stopGame() {
        isRunning = false

        handler.removeCallbacksAndMessages(null)
        clearGameObjects()
        idGenerator.set(0)
    }

    private fun resumeAnimations() {
        gameObjects.forEach {
            it.animator.resume()
        }
    }

    private fun pauseAnimations() {
        gameObjects.forEach {
            it.animator.pause()
        }
    }

    private fun clearGameObjects() {
        gameObjects.forEach {
            removeView(it.imageView)
        }
        gameObjects.clear()
    }

    private fun scheduleSpawn() {
        if (isPaused) return

        handler.postDelayed({
            spawnImageView()
        }, spawnInterval)
    }

    private fun spawnImageView() {
        if (isPaused || (correctCategories.isEmpty() && incorrectCategories.isEmpty())) return

        val category = getRandomCategory() ?: return scheduleSpawn()
        val randomItem = category.items.randomOrNull() ?: return

        if (randomItem !is IconProvider) return

        val position = generateRandomPosition(imageViewSize) ?: return scheduleSpawn()
        val imageView = createImageView(randomItem.iconId, randomItem.name, position)

        val animationSet = createAnimationSet(imageView)
        addGameObject(randomItem.name, imageView, animationSet, position)

        animationSet.start()
        scheduleSpawn()
    }

    private fun getRandomCategory(): GameCategory? {
        return (correctCategories + incorrectCategories).randomOrNull()
    }

    private fun createImageView(iconId: Int, name: String, position: Rect): AppCompatImageView {
        return AppCompatImageView(context).apply {
            isSoundEffectsEnabled = false
            isHapticFeedbackEnabled = false
            layoutParams = LayoutParams(imageViewSize, imageViewSize)
            setImageResource(iconId)
            translationX = position.left.toFloat()
            translationY = position.top.toFloat()

            var isClicked = false
            setOnClickListener {
                if (isClicked && isPaused) return@setOnClickListener

                isClicked = true
                handleImageClick(name, this)
            }

            addView(this)
        }
    }

    private fun createAnimationSet(targetView: View): AnimatorSet {
        val fadeIn = AnimationUtils.createFadeInAnimation(targetView)
        val fadeOut = AnimationUtils.createFadeOutAnimation(targetView, startDelay = displayDuration)

        val rotationAnimation = when (RotationAnimation.getRandom()){
            RotationAnimation.COUNTER_CLOCKWISE -> {
                AnimationUtils.createCounterClockwiseRotationAnimation(targetView)
            }
            else -> {
                AnimationUtils.createClockwiseRotationAnimation(targetView)
            }
        }

        return AnimatorSet().apply {
//            playTogether(fadeIn, rotationAnimation)
            playSequentially(fadeIn, fadeOut)
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    removeView(targetView)
                    gameObjects.removeIf { it.imageView == targetView }
                }
            })
        }
    }

    private fun addGameObject(
        name: String,
        imageView: AppCompatImageView,
        animator: Animator,
        area: Rect
    ) {
        gameObjects.add(
            GameObject(
                idGenerator.incrementAndGet(),
                name,
                imageView,
                animator,
                area
            )
        )
    }

    private fun handleImageClick(imageName: String, targetView: View) {
        val isCorrect = correctCategories.any {
            it.items.any { item ->
                item.name == imageName
            }
        }

        listener?.invoke(isCorrect)

        gameObjects.find { it.imageView == targetView }?.animator?.pause()

        animateToCharacter(targetView, imageName)
    }

    private fun animateToCharacter(targetView: View, imageName: String) {
        val characterCenter = binding.ivGameViewCharacter.getCenter()
        val shrinkAndMoveAnimation = AnimationUtils.createShrinkAndMoveAnimation(targetView, characterCenter)

        gameObjects.add(
            GameObject(
                idGenerator.incrementAndGet(),
                imageName,
                targetView as AppCompatImageView,
                shrinkAndMoveAnimation,
                Rect()
            )
        )

        shrinkAndMoveAnimation.start()
    }

    private fun generateRandomPosition(size: Int): Rect? {
        repeat(100) {
            val left = random.nextInt(width - size)
            val top = random.nextInt(height - size)

            val rect = Rect(left, top, left + size, top + size)

            if (!isOverlapping(rect)) {
                return rect
            }
        }

        return null
    }

    private fun isOverlapping(rect: Rect): Boolean {
        return rect.intersect(binding.ivGameViewCharacter.expandRect(spacing)) ||
                gameObjects.any {
                    it.area.expand(spacing).intersect(rect)
                }
    }

    private fun Rect.expand(amount: Int) = Rect(
        left - amount,
        top - amount,
        right + amount,
        bottom + amount
    )

    private fun View.expandRect(amount: Int) = Rect(
        left - amount,
        top - amount,
        right + amount,
        bottom + amount
    )

    private fun View.getCenter() = PointF(
        x + width / 2,
        y + height / 2
    )

    private fun updateLayoutParams(view: View, width: Int, height: Int) {
        view.layoutParams = view.layoutParams.apply {
            this.width = width
            this.height = height
        }
    }

    data class GameObject(
        val id: Int,
        val name: String,
        val imageView: AppCompatImageView,
        val animator: Animator,
        val area: Rect
    )

    object AnimationUtils {
        fun createFadeInAnimation(
            target: View,
            duration: Long = 200
        ): ObjectAnimator {
            return ObjectAnimator.ofFloat(target, "alpha", 0f, 1f).apply {
                this.duration = duration
            }
        }

        fun createFadeOutAnimation(
            target: View,
            duration: Long = 200,
            startDelay: Long = 0
        ): ObjectAnimator {
            return ObjectAnimator.ofFloat(target, "alpha", 1f, 0f).apply {
                this.duration = duration
                this.startDelay = startDelay
            }
        }

        fun createShrinkAndMoveAnimation(
            target: View,
            targetCenter: PointF
        ): AnimatorSet {
            return AnimatorSet().apply {
                playTogether(
                    ObjectAnimator.ofFloat(target, "scaleX", 1f, 0.5f),
                    ObjectAnimator.ofFloat(target, "scaleY", 1f, 0.5f),
                    ObjectAnimator.ofFloat(target, "translationX", target.x, targetCenter.x - target.width / 2),
                    ObjectAnimator.ofFloat(target, "translationY", target.y, targetCenter.y - target.height / 2)
                )
                duration = 200
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        (target.parent as? ViewGroup)?.removeView(target)
                    }
                })
            }
        }

        fun createClockwiseRotationAnimation(target: View, duration: Long = 10000): ObjectAnimator {
            return ObjectAnimator.ofFloat(target, "rotation", 0f, 360f).apply {
                this.duration = duration
                repeatCount = ObjectAnimator.INFINITE
                repeatMode = ObjectAnimator.RESTART
            }
        }

        fun createCounterClockwiseRotationAnimation(target: View, duration: Long = 10000): ObjectAnimator {
            return ObjectAnimator.ofFloat(target, "rotation", 0f, -360f).apply {
                this.duration = duration
                repeatCount = ObjectAnimator.INFINITE
                repeatMode = ObjectAnimator.RESTART
            }
        }
    }

    enum class RotationAnimation{
        CLOCKWISE,
        COUNTER_CLOCKWISE;

        companion object {
            fun getRandom(): RotationAnimation {
                return RotationAnimation.entries.toTypedArray().random()
            }
        }
    }
}
