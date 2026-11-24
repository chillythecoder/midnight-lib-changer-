package lol.peng.midnightlibchanger

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.SystemClock
import android.view.View
import kotlin.random.Random

class NightDrawable(context:Context, private val showStars:()->Boolean, private val motion:()->Boolean): Drawable() {
    private val paint=Paint(Paint.ANTI_ALIAS_FLAG)
    private val stars=List(85) { Star(Random.nextFloat(),Random.nextFloat(),Random.nextFloat()*1.8f+.4f,Random.nextFloat()*6.28f) }
    private val start=SystemClock.uptimeMillis()
    data class Star(val x:Float,val y:Float,val r:Float,val phase:Float)
    override fun draw(canvas:Canvas) {
        val b=bounds
        val shader=LinearGradient(0f,0f,b.width().toFloat(),b.height().toFloat(),intArrayOf(0xff080c1b.toInt(),0xff11152c.toInt(),0xff090d1c.toInt()),null,Shader.TileMode.CLAMP)
        paint.shader=shader; canvas.drawRect(b,paint); paint.shader=null
        if(showStars()) {
            val t=(SystemClock.uptimeMillis()-start)/1000f
            stars.forEach { s ->
                val twinkle=if(motion()) .35f+.65f*((kotlin.math.sin(t*1.2f+s.phase)+1f)/2f) else .8f
                paint.color=Color.argb((255*twinkle).toInt(),210,220,255)
                canvas.drawCircle(b.left+s.x*b.width(),b.top+s.y*b.height(),s.r,paint)
            }
            paint.color=0x225f69ff
            canvas.drawCircle(b.width()*.83f,b.height()*.13f,b.width()*.23f,paint)
        }
        paint.color=Color.WHITE
        if(motion()) invalidateSelf()
    }
    override fun setAlpha(alpha:Int) {}
    override fun setColorFilter(colorFilter:ColorFilter?) {}
    @Deprecated("Deprecated in Android")
    override fun getOpacity()=android.graphics.PixelFormat.OPAQUE
}
