package com.lostf1sh.pixelplayeross.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistPlay
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.HapticFeedbackConstantsCompat
import com.lostf1sh.pixelplayeross.presentation.utils.AppHapticsConfig
import com.lostf1sh.pixelplayeross.presentation.utils.LocalAppHapticsConfig
import com.lostf1sh.pixelplayeross.presentation.utils.performAppCompatHapticFeedback
import kotlin.math.abs
import kotlinx.coroutines.launch

/**
 * Wraps a song row with Spotify-style horizontal swipe actions:
 *  - swipe right  → [onSwipeRight]  (play next)
 *  - swipe left   → [onSwipeLeft]   (e.g. remove from queue; null disables that direction)
 *
 * A resisted drag past ~35% of the row width commits the action; anything shorter springs back.
 * Uses [detectHorizontalDragGestures], which claims horizontal drags while leaving the parent's
 * vertical scroll untouched — so it coexists with a LazyColumn. Do NOT enable it inside a
 * horizontal pager (it would swallow the page swipe); pass [enabled] = false there.
 */
@Composable
fun SongSwipeActionsBox(
    onSwipeRight: () -> Unit,
    onSwipeLeft: (() -> Unit)? = null,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    if (!enabled) {
        content()
        return
    }

    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val hapticView = LocalView.current
    val haptics: AppHapticsConfig = LocalAppHapticsConfig.current
    val offsetX = remember { Animatable(0f) }
    val latestRight by rememberUpdatedState(onSwipeRight)
    val latestLeft by rememberUpdatedState(onSwipeLeft)

    Box(modifier = Modifier.fillMaxWidth()) {
        // Revealed background action, colored + iconed by swipe direction.
        val offset = offsetX.value
        if (offset != 0f) {
            val swipingRight = offset > 0f
            val bg = if (swipingRight) MaterialTheme.colorScheme.primary else Color(0xFFB3261E)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(bg)
                    .padding(horizontal = 24.dp),
                contentAlignment = if (swipingRight) Alignment.CenterStart else Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = if (swipingRight) Icons.AutoMirrored.Rounded.PlaylistPlay else Icons.Rounded.Delete,
                    contentDescription = null,
                    tint = if (swipingRight) MaterialTheme.colorScheme.onPrimary else Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { translationX = offsetX.value }
                .pointerInput(latestLeft != null) {
                    val widthPx = size.width.toFloat()
                    val commitThreshold = widthPx * 0.35f
                    val tensionPx = 24f * density.density
                    var accumulated = 0f
                    var armed = false
                    detectHorizontalDragGestures(
                        onDragStart = {
                            accumulated = 0f
                            armed = false
                            scope.launch { offsetX.stop() }
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            accumulated += dragAmount
                            // Block the left direction entirely when no left action is wired.
                            if (latestLeft == null && accumulated < 0f) accumulated = 0f
                            // Resist the first few dp so scrolling doesn't nudge rows sideways.
                            val effective = if (abs(accumulated) < tensionPx) {
                                accumulated * 0.35f
                            } else {
                                accumulated
                            }
                            val crossed = abs(accumulated) > commitThreshold
                            if (crossed != armed) {
                                armed = crossed
                                performAppCompatHapticFeedback(
                                    hapticView,
                                    haptics,
                                    if (crossed) HapticFeedbackConstantsCompat.GESTURE_THRESHOLD_ACTIVATE
                                    else HapticFeedbackConstantsCompat.GESTURE_THRESHOLD_DEACTIVATE
                                )
                            }
                            scope.launch { offsetX.snapTo(effective) }
                        },
                        onDragEnd = {
                            val committed = abs(accumulated) > commitThreshold
                            val right = accumulated > 0f
                            scope.launch {
                                offsetX.animateTo(
                                    0f,
                                    spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    )
                                )
                            }
                            if (committed) {
                                performAppCompatHapticFeedback(
                                    hapticView, haptics, HapticFeedbackConstantsCompat.GESTURE_END
                                )
                                if (right) latestRight() else latestLeft?.invoke()
                            }
                            accumulated = 0f
                            armed = false
                        },
                        onDragCancel = {
                            accumulated = 0f
                            armed = false
                            scope.launch {
                                offsetX.animateTo(
                                    0f,
                                    spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    )
                                )
                            }
                        }
                    )
                }
        ) {
            content()
        }
    }
}
