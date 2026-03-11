package com.example.playlistmaker.player.ui.views

import android.content.Context
import android.content.res.Configuration
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import com.example.playlistmaker.R
import kotlin.math.min

class PlaybackButtonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Состояния кнопки
    enum class State {
        PLAY, PAUSE
    }

    private var currentState: State = State.PLAY

    // Ресурсы изображений (сохраняем ID)
    private var playIconResId: Int = 0
    private var pauseIconResId: Int = 0

    // Храним Drawable как поля класса - создаются один раз
    private var playDrawable: Drawable? = null
    private var pauseDrawable: Drawable? = null

    // Прямоугольник для отрисовки
    private val drawingRect = RectF()

    // Краска для отрисовки
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Слушатель для обработки нажатий
    private var onClickListener: (() -> Unit)? = null

    init {
        // Обрабатываем атрибуты
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.PlaybackButtonView,
            0, 0
        ).apply {
            try {
                // Получаем ресурсы изображений
                playIconResId = getResourceId(R.styleable.PlaybackButtonView_playIcon, 0)
                pauseIconResId = getResourceId(R.styleable.PlaybackButtonView_pauseIcon, 0)

                // Загружаем Drawable один раз при создании
                loadDrawables()
            } finally {
                recycle()
            }
        }
    }

    private fun loadDrawables() {
        playDrawable = if (playIconResId != 0) {
            AppCompatResources.getDrawable(context, playIconResId)
        } else null

        pauseDrawable = if (pauseIconResId != 0) {
            AppCompatResources.getDrawable(context, pauseIconResId)
        } else null
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Перезагружаем Drawable при смене темы
        loadDrawables()
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // Определяем прямоугольник для отрисовки (квадрат по центру)
        val size = min(w, h).toFloat()
        val left = (w - size) / 2f
        val top = (h - size) / 2f
        drawingRect.set(left, top, left + size, top + size)

        // Обновляем bounds для Drawable при изменении размера
        updateDrawableBounds()
    }

    private fun updateDrawableBounds() {
        playDrawable?.setBounds(
            drawingRect.left.toInt(),
            drawingRect.top.toInt(),
            drawingRect.right.toInt(),
            drawingRect.bottom.toInt()
        )
        pauseDrawable?.setBounds(
            drawingRect.left.toInt(),
            drawingRect.top.toInt(),
            drawingRect.right.toInt(),
            drawingRect.bottom.toInt()
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Выбираем нужный Drawable (уже загруженный, с установленными bounds)
        val drawable = when (currentState) {
            State.PLAY -> playDrawable
            State.PAUSE -> pauseDrawable
        }

        // Рисуем Drawable
        drawable?.draw(canvas)
    }
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // Говорим, что будем отслеживать все последующие события
                return true
            }
            MotionEvent.ACTION_UP -> {
                // Проверяем, что палец поднят в пределах View
                if (event.x in 0f..width.toFloat() && event.y in 0f..height.toFloat()) {
                    // Меняем состояние кнопки (требование критерия)
                    toggleState()
                    // Вызываем колбэк для уведомления фрагмента
                    onClickListener?.invoke()
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    fun toggleState() {
        currentState = if (currentState == State.PLAY) State.PAUSE else State.PLAY
        invalidate()
    }

    fun setState(state: State) {
        if (currentState != state) {
            currentState = state
            invalidate()
        }
    }

    fun getState(): State = currentState

    fun setOnPlaybackClickListener(listener: () -> Unit) {
        this.onClickListener = listener
    }

    /**
     * Очистка ресурсов
     */
    fun release() {
        playDrawable = null
        pauseDrawable = null
    }
}