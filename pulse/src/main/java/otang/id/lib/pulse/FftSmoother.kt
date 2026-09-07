package otang.id.lib.pulse

import java.util.ArrayDeque

internal class FftSmoother {
    private var windows: Array<ArrayDeque<Float>> = emptyArray()
    private var sums: FloatArray = FloatArray(0)
    private var currentWindowSize = 0

    fun smooth(magnitudes: FloatArray, windowSize: Int): FloatArray {
        if (windowSize <= 1) return magnitudes

        val barCount = magnitudes.size
        if (windows.size != barCount || currentWindowSize != windowSize) {
            windows = Array(barCount) { ArrayDeque<Float>(windowSize) }
            sums = FloatArray(barCount)
            currentWindowSize = windowSize
        }

        val result = FloatArray(barCount)
        for (i in 0 until barCount) {
            val window = windows[i]
            if (window.size >= windowSize) {
                sums[i] -= window.pollFirst() ?: 0f
            }

            val newValue = magnitudes[i]
            window.offerLast(newValue)
            sums[i] += newValue

            result[i] = sums[i] / window.size
        }

        return result
    }
}
