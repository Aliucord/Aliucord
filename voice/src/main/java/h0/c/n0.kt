package h0.c

import org.webrtc.EglBase
import org.webrtc.EglBaseFactory

@Suppress("unused", "ClassName")
object n0 {
    @JvmStatic
    fun a(): EglBase {
        return b(null, EglBase.CONFIG_PLAIN)
    }

    // This is the main change, as EglBase..Impls are package-private
    @JvmStatic
    fun b(context: EglBase.Context?, iArr: IntArray) = EglBaseFactory.create(context, iArr)

    @JvmStatic
    fun c(iArr: IntArray): Int {
        var i = 0
        while (i < iArr.size - 1) {
            if (iArr[i] == 12352) {
                val i2 = iArr[i + 1]
                if (i2 != 4) {
                    return if (i2 != 64) 1 else 3
                }
                return 2
            }
            i++
        }
        return 1
    }
}
