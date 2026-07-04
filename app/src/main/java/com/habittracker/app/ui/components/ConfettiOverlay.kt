package com.habittracker.app.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit

@Composable
fun ConfettiOverlay(
    isVisible: Boolean,
    onAnimationEnd: () -> Unit
) {
    if (!isVisible) return

    val party = Party(
        speed = 0f,
        maxSpeed = 30f,
        damping = 0.9f,
        spread = 360,
        colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
        position = Position.Relative(0.5, 0.3),
        emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(100)
    )

    KonfettiView(
        modifier = Modifier.fillMaxSize(),
        parties = listOf(party),
        updateListener = object : nl.dionsegijn.konfetti.compose.OnParticleSystemUpdateListener {
            override fun onParticleSystemEnded(system: nl.dionsegijn.konfetti.core.ParticleSystem, activeSystems: Int) {
                if (activeSystems == 0) {
                    onAnimationEnd()
                }
            }
        }
    )
}
