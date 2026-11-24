
package lol.peng.midnightlibchanger

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.SystemClock
import kotlin.random.Random

class NightDrawable(
    context: Context,
    private val showStars: () -> Boolean,
    private val motion: () -> Boolean
) : Drawable() {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val stars = List(110) {
        Star(
            Random.nextFloat(),
            Random.nextFloat(),
            Random.nextFloat() * 2f + 0.5f,
            Random.nextFloat() * 6.28f
        )
    }

    private val start = SystemClock.uptimeMillis()

    data class Star(
        val x: Float,
        val y: Float,
        val radius: Float,
        val phase: Float
    )

    override fun draw(canvas: Canvas) {
        val b = bounds

        paint.color = Color.WHITE
        canvas.drawRect(b, paint)

        if (showStars()) {
            val time = (SystemClock.uptimeMillis() - start) / 1000f

            stars.forEach { star ->
                val twinkle = if (motion()) {
                    0.25f + 0.75f *
                        ((kotlin.math.sin(time * 1.3f + star.phase) + 1f) / 2f)
                } else {
                    0.8f
                }

                paint.color = Color.argb(
                    (255 * twinkle).toInt(),
                    0,
                    0,
                    0
                )

                canvas.drawCircle(
                    b.left + star.x * b.width(),
                    b.top + star.y * b.height(),
                    star.radius,
                    paint
                )
            }
        }

        if (motion()) {
            invalidateSelf()
        }
    }

    override fun setAlpha(alpha: Int) {}

    override fun setColorFilter(colorFilter: ColorFilter?) {}

    @Deprecated("Deprecated in Android")
    override fun getOpacity() = android.graphics.PixelFormat.OPAQUE
}
