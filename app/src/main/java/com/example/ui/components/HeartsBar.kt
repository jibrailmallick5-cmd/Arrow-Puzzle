package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.HeartCrimson
import com.example.ui.theme.MutedSlate

@Composable
fun HeartsBar(
    hearts: Int,
    maxHearts: Int = 3,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until maxHearts) {
            val isFull = i < hearts
            val scale = remember { Animatable(1f) }

            LaunchedEffect(isFull) {
                if (!isFull) {
                    scale.animateTo(1.32f, tween(110))
                    scale.animateTo(0.88f, tween(130))
                    scale.animateTo(1f, spring(stiffness = Spring.StiffnessMedium))
                } else {
                    scale.animateTo(1.1f, tween(120))
                    scale.animateTo(1f, tween(120))
                }
            }

            val tint by animateColorAsState(
                targetValue = if (isFull) HeartCrimson else MutedSlate.copy(alpha = 0.45f),
                animationSpec = tween(240),
                label = "heart_tint_$i"
            )

            Icon(
                imageVector = if (isFull) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = if (isFull) "Active heart" else "Lost heart",
                tint = tint,
                modifier = Modifier
                    .size(26.dp)
                    .scale(scale.value)
                    .testTag("heart_$i")
            )
        }
    }
}
