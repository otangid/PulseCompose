package otang.id.lib.pulse

import kotlin.math.abs
import kotlin.math.hypot

object FftUtils {
    fun calculateMagnitudes(fft: ByteArray, outMagnitudes: FloatArray): Int {
        if (fft.isEmpty()) return 0
        
        val n = fft.size
        val count = minOf(n / 2, outMagnitudes.size)
        
        // DC component
        outMagnitudes[0] = abs(fft[0].toInt()).toFloat()
        
        for (k in 1 until count) {
            val i = k * 2
            val real = fft[i].toInt()
            val imag = fft[i + 1].toInt()
            outMagnitudes[k] = hypot(real.toDouble(), imag.toDouble()).toFloat()
        }
        
        return count
    }
}
