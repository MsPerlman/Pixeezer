package com.lostf1sh.pixelplayeross.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.abs

private val flowGradientPairs = listOf(
    listOf(Color(0xFFFF6B9D), Color(0xFF9B5DE5)),
    listOf(Color(0xFF00C2CB), Color(0xFF3A86FF)),
    listOf(Color(0xFFFF9E00), Color(0xFFFF5C5C)),
    listOf(Color(0xFF9B5DE5), Color(0xFF3A86FF)),
)

fun gradientForFlowConfig(id: String): Brush {
    val pair = flowGradientPairs[abs(id.hashCode()) % flowGradientPairs.size]
    return Brush.linearGradient(pair)
}

/** Deezer's own Flow branding: a fixed purple→pink gradient, not a per-item hashed one. */
fun deezerFlowBrandGradient(): Brush = Brush.linearGradient(
    listOf(Color(0xFF8E2DE2), Color(0xFFE8397B))
)
