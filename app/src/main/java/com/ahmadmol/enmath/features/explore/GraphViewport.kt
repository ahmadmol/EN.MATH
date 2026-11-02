package com.ahmadmol.enmath.features.explore

import kotlin.math.ceil
import kotlin.math.floor

/** Screen pixels, independent of interface layout direction. Positive math Y points up. */
internal data class GraphViewport(
    val width: Float, val height: Float, val zoom: Float,
    val panX: Float = 0f, val panY: Float = 0f,
) {
    val scale = 38f * zoom
    val originX = width / 2f + panX
    val originY = height / 2f + panY
    fun screenX(x: Float) = originX + x * scale
    fun screenY(y: Float) = originY - y * scale
    fun mathX(pixel: Float) = (pixel - originX) / scale
    fun mathY(pixel: Float) = (originY - pixel) / scale
    val xTicks: IntRange get() = floor(mathX(0f)).toInt()..ceil(mathX(width)).toInt()
    val yTicks: IntRange get() = floor(mathY(height)).toInt()..ceil(mathY(0f)).toInt()
}

/** Whole-unit 1/2/5 steps with at least the measured label separation. */
internal fun graphTickStep(unitPixels: Float, minimumPixels: Float): Int {
    require(unitPixels > 0f && unitPixels.isFinite())
    val required = ceil((minimumPixels / unitPixels).coerceAtLeast(1f)).toInt()
    var magnitude = 1
    while (magnitude <= Int.MAX_VALUE / 10) {
        for (factor in listOf(1, 2, 5)) {
            val candidate = magnitude.toLong() * factor
            if (candidate >= required) return candidate.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
        }
        magnitude *= 10
    }
    return Int.MAX_VALUE
}
