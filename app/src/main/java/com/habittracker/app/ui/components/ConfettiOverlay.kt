package com.habittracker.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay
import kotlin.random.Random

data class ConfettiParticle(
    var x: Float,
    var y: Float,
    var velocityX: Float,
    var velocityY: Float,
    var color: Color,
    var rotation: Float,
    var rotationSpeed: Float,
    var size: Float
)

@Composable
fun ConfettiOverlay(
    isVisible: Boolean,
    onAnimationEnd: () -> Unit
) {
    if (!isVisible) return

    val particles = remember { mutableStateListOf<ConfettiParticle>() }
    val colors = listOf(
        Color(0xFF8B5CF6), Color(0xFF06B6D4), Color(0xFFF59E0B),
        Color(0xFF10B981), Color(0xFFEC4899)
    )

    // Fire confetti when it becomes visible
    LaunchedEffect(isVisible) {
        if (isVisible) {
            particles.clear()
            // generate 100 particles from the center/top
            for (i in 0..100) {
                particles.add(
                    ConfettiParticle(
                        x = 500f, // Will be overridden by canvas width later
                        y = 100f,
                        velocityX = Random.nextFloat() * 20f - 10f,
                        velocityY = Random.nextFloat() * 10f - 15f,
                        color = colors.random(),
                        rotation = Random.nextFloat() * 360f,
                        rotationSpeed = Random.nextFloat() * 10f - 5f,
                        size = Random.nextFloat() * 15f + 10f
                    )
                )
            }

            // Animation loop for ~3 seconds
            var timeElapsed = 0
            while (timeElapsed < 3000) {
                withFrameNanos {
                    for (i in particles.indices) {
                        val p = particles[i]
                        p.x += p.velocityX
                        p.y += p.velocityY
                        p.velocityY += 0.5f // Gravity
                        p.rotation += p.rotationSpeed
                    }
                }
                timeElapsed += 16
                delay(16)
            }
            onAnimationEnd()
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Update initial x based on screen width
        if (particles.isNotEmpty() && particles[0].x == 500f) {
            particles.forEach { 
                it.x = width / 2f + (Random.nextFloat() * 200f - 100f)
                it.y = height * 0.2f
            }
        }

        particles.forEach { p ->
            drawCircle(
                color = p.color,
                radius = p.size,
                center = Offset(p.x, p.y)
            )
        }
    }
}
