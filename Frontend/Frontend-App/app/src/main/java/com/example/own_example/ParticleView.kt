package com.example.own_example

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

class ParticleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val particles = mutableListOf<Particle>()
    private val paint = Paint().apply {
        color = context.getColor(R.color.cardinal_red)
        alpha = 50
    }
    private val random = Random()

    init {
        repeat(50) {
            particles.add(createParticle())
        }
    }

    private fun createParticle() = Particle(
        x = random.nextFloat() * width,
        y = random.nextFloat() * height,
        radius = 4f + random.nextFloat() * 4,
        speed = 1f + random.nextFloat() * 2,
        angle = random.nextFloat() * 360
    )

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        particles.forEach { particle ->
            canvas.drawCircle(particle.x, particle.y, particle.radius, paint)
            particle.update()
            if (particle.isOutOfBounds(width.toFloat(), height.toFloat())) {
                particles[particles.indexOf(particle)] = createParticle()
            }
        }
        invalidate()
    }

    data class Particle(
        var x: Float,
        var y: Float,
        val radius: Float,
        val speed: Float,
        var angle: Float
    ) {
        fun update() {
            x += cos(Math.toRadians(angle.toDouble())).toFloat() * speed
            y += sin(Math.toRadians(angle.toDouble())).toFloat() * speed
            angle += 0.5f
        }

        fun isOutOfBounds(width: Float, height: Float): Boolean =
            x < -radius || x > width + radius || y < -radius || y > height + radius
    }
}