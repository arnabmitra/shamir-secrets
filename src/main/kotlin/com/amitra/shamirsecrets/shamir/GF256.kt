package com.amitra.shamirsecrets.shamir

import java.security.SecureRandom
import kotlin.experimental.xor


/**
 * An implementation of polynomials over `GF(256)`. Uses the same field polynomial (`0x11b`) and generator (`0x03`) as AES. Internally, uses lookup tables for performance.
 *
 * @see [Finite Field Arithmetic and Reed-Solomon
 * Coding](https://research.swtch.com/field)
 */
internal object GF256 {
    private val LOG = byteArrayOf(
            0xff.toByte(), 0x00.toByte(), 0x19.toByte(), 0x01.toByte(), 0x32.toByte(), 0x02.toByte(), 0x1a.toByte(),
            0xc6.toByte(), 0x4b.toByte(), 0xc7.toByte(), 0x1b.toByte(), 0x68.toByte(), 0x33.toByte(), 0xee.toByte(),
            0xdf.toByte(), 0x03.toByte(), 0x64.toByte(), 0x04.toByte(), 0xe0.toByte(), 0x0e.toByte(), 0x34.toByte(),
            0x8d.toByte(), 0x81.toByte(), 0xef.toByte(), 0x4c.toByte(), 0x71.toByte(), 0x08.toByte(), 0xc8.toByte(),
            0xf8.toByte(), 0x69.toByte(), 0x1c.toByte(), 0xc1.toByte(), 0x7d.toByte(), 0xc2.toByte(), 0x1d.toByte(),
            0xb5.toByte(), 0xf9.toByte(), 0xb9.toByte(), 0x27.toByte(), 0x6a.toByte(), 0x4d.toByte(), 0xe4.toByte(),
            0xa6.toByte(), 0x72.toByte(), 0x9a.toByte(), 0xc9.toByte(), 0x09.toByte(), 0x78.toByte(), 0x65.toByte(),
            0x2f.toByte(), 0x8a.toByte(), 0x05.toByte(), 0x21.toByte(), 0x0f.toByte(), 0xe1.toByte(), 0x24.toByte(),
            0x12.toByte(), 0xf0.toByte(), 0x82.toByte(), 0x45.toByte(), 0x35.toByte(), 0x93.toByte(), 0xda.toByte(),
            0x8e.toByte(), 0x96.toByte(), 0x8f.toByte(), 0xdb.toByte(), 0xbd.toByte(), 0x36.toByte(), 0xd0.toByte(),
            0xce.toByte(), 0x94.toByte(), 0x13.toByte(), 0x5c.toByte(), 0xd2.toByte(), 0xf1.toByte(), 0x40.toByte(),
            0x46.toByte(), 0x83.toByte(), 0x38.toByte(), 0x66.toByte(), 0xdd.toByte(), 0xfd.toByte(), 0x30.toByte(),
            0xbf.toByte(), 0x06.toByte(), 0x8b.toByte(), 0x62.toByte(), 0xb3.toByte(), 0x25.toByte(), 0xe2.toByte(),
            0x98.toByte(), 0x22.toByte(), 0x88.toByte(), 0x91.toByte(), 0x10.toByte(), 0x7e.toByte(), 0x6e.toByte(),
            0x48.toByte(), 0xc3.toByte(), 0xa3.toByte(), 0xb6.toByte(), 0x1e.toByte(), 0x42.toByte(), 0x3a.toByte(),
            0x6b.toByte(), 0x28.toByte(), 0x54.toByte(), 0xfa.toByte(), 0x85.toByte(), 0x3d.toByte(), 0xba.toByte(),
            0x2b.toByte(), 0x79.toByte(), 0x0a.toByte(), 0x15.toByte(), 0x9b.toByte(), 0x9f.toByte(), 0x5e.toByte(),
            0xca.toByte(), 0x4e.toByte(), 0xd4.toByte(), 0xac.toByte(), 0xe5.toByte(), 0xf3.toByte(), 0x73.toByte(),
            0xa7.toByte(), 0x57.toByte(), 0xaf.toByte(), 0x58.toByte(), 0xa8.toByte(), 0x50.toByte(), 0xf4.toByte(),
            0xea.toByte(), 0xd6.toByte(), 0x74.toByte(), 0x4f.toByte(), 0xae.toByte(), 0xe9.toByte(), 0xd5.toByte(),
            0xe7.toByte(), 0xe6.toByte(), 0xad.toByte(), 0xe8.toByte(), 0x2c.toByte(), 0xd7.toByte(), 0x75.toByte(),
            0x7a.toByte(), 0xeb.toByte(), 0x16.toByte(), 0x0b.toByte(), 0xf5.toByte(), 0x59.toByte(), 0xcb.toByte(),
            0x5f.toByte(), 0xb0.toByte(), 0x9c.toByte(), 0xa9.toByte(), 0x51.toByte(), 0xa0.toByte(), 0x7f.toByte(),
            0x0c.toByte(), 0xf6.toByte(), 0x6f.toByte(), 0x17.toByte(), 0xc4.toByte(), 0x49.toByte(), 0xec.toByte(),
            0xd8.toByte(), 0x43.toByte(), 0x1f.toByte(), 0x2d.toByte(), 0xa4.toByte(), 0x76.toByte(), 0x7b.toByte(),
            0xb7.toByte(), 0xcc.toByte(), 0xbb.toByte(), 0x3e.toByte(), 0x5a.toByte(), 0xfb.toByte(), 0x60.toByte(),
            0xb1.toByte(), 0x86.toByte(), 0x3b.toByte(), 0x52.toByte(), 0xa1.toByte(), 0x6c.toByte(), 0xaa.toByte(),
            0x55.toByte(), 0x29.toByte(), 0x9d.toByte(), 0x97.toByte(), 0xb2.toByte(), 0x87.toByte(), 0x90.toByte(),
            0x61.toByte(), 0xbe.toByte(), 0xdc.toByte(), 0xfc.toByte(), 0xbc.toByte(), 0x95.toByte(), 0xcf.toByte(),
            0xcd.toByte(), 0x37.toByte(), 0x3f.toByte(), 0x5b.toByte(), 0xd1.toByte(), 0x53.toByte(), 0x39.toByte(),
            0x84.toByte(), 0x3c.toByte(), 0x41.toByte(), 0xa2.toByte(), 0x6d.toByte(), 0x47.toByte(), 0x14.toByte(),
            0x2a.toByte(), 0x9e.toByte(), 0x5d.toByte(), 0x56.toByte(), 0xf2.toByte(), 0xd3.toByte(), 0xab.toByte(),
            0x44.toByte(), 0x11.toByte(), 0x92.toByte(), 0xd9.toByte(), 0x23.toByte(), 0x20.toByte(), 0x2e.toByte(),
            0x89.toByte(), 0xb4.toByte(), 0x7c.toByte(), 0xb8.toByte(), 0x26.toByte(), 0x77.toByte(), 0x99.toByte(),
            0xe3.toByte(), 0xa5.toByte(), 0x67.toByte(), 0x4a.toByte(), 0xed.toByte(), 0xde.toByte(), 0xc5.toByte(),
            0x31.toByte(), 0xfe.toByte(), 0x18.toByte(), 0x0d.toByte(), 0x63.toByte(), 0x8c.toByte(), 0x80.toByte(),
            0xc0.toByte(), 0xf7.toByte(), 0x70.toByte(), 0x07.toByte())
    private val EXP = byteArrayOf(
            0x01.toByte(), 0x03.toByte(), 0x05.toByte(), 0x0f.toByte(), 0x11.toByte(), 0x33.toByte(), 0x55.toByte(),
            0xff.toByte(), 0x1a.toByte(), 0x2e.toByte(), 0x72.toByte(), 0x96.toByte(), 0xa1.toByte(), 0xf8.toByte(),
            0x13.toByte(), 0x35.toByte(), 0x5f.toByte(), 0xe1.toByte(), 0x38.toByte(), 0x48.toByte(), 0xd8.toByte(),
            0x73.toByte(), 0x95.toByte(), 0xa4.toByte(), 0xf7.toByte(), 0x02.toByte(), 0x06.toByte(), 0x0a.toByte(),
            0x1e.toByte(), 0x22.toByte(), 0x66.toByte(), 0xaa.toByte(), 0xe5.toByte(), 0x34.toByte(), 0x5c.toByte(),
            0xe4.toByte(), 0x37.toByte(), 0x59.toByte(), 0xeb.toByte(), 0x26.toByte(), 0x6a.toByte(), 0xbe.toByte(),
            0xd9.toByte(), 0x70.toByte(), 0x90.toByte(), 0xab.toByte(), 0xe6.toByte(), 0x31.toByte(), 0x53.toByte(),
            0xf5.toByte(), 0x04.toByte(), 0x0c.toByte(), 0x14.toByte(), 0x3c.toByte(), 0x44.toByte(), 0xcc.toByte(),
            0x4f.toByte(), 0xd1.toByte(), 0x68.toByte(), 0xb8.toByte(), 0xd3.toByte(), 0x6e.toByte(), 0xb2.toByte(),
            0xcd.toByte(), 0x4c.toByte(), 0xd4.toByte(), 0x67.toByte(), 0xa9.toByte(), 0xe0.toByte(), 0x3b.toByte(),
            0x4d.toByte(), 0xd7.toByte(), 0x62.toByte(), 0xa6.toByte(), 0xf1.toByte(), 0x08.toByte(), 0x18.toByte(),
            0x28.toByte(), 0x78.toByte(), 0x88.toByte(), 0x83.toByte(), 0x9e.toByte(), 0xb9.toByte(), 0xd0.toByte(),
            0x6b.toByte(), 0xbd.toByte(), 0xdc.toByte(), 0x7f.toByte(), 0x81.toByte(), 0x98.toByte(), 0xb3.toByte(),
            0xce.toByte(), 0x49.toByte(), 0xdb.toByte(), 0x76.toByte(), 0x9a.toByte(), 0xb5.toByte(), 0xc4.toByte(),
            0x57.toByte(), 0xf9.toByte(), 0x10.toByte(), 0x30.toByte(), 0x50.toByte(), 0xf0.toByte(), 0x0b.toByte(),
            0x1d.toByte(), 0x27.toByte(), 0x69.toByte(), 0xbb.toByte(), 0xd6.toByte(), 0x61.toByte(), 0xa3.toByte(),
            0xfe.toByte(), 0x19.toByte(), 0x2b.toByte(), 0x7d.toByte(), 0x87.toByte(), 0x92.toByte(), 0xad.toByte(),
            0xec.toByte(), 0x2f.toByte(), 0x71.toByte(), 0x93.toByte(), 0xae.toByte(), 0xe9.toByte(), 0x20.toByte(),
            0x60.toByte(), 0xa0.toByte(), 0xfb.toByte(), 0x16.toByte(), 0x3a.toByte(), 0x4e.toByte(), 0xd2.toByte(),
            0x6d.toByte(), 0xb7.toByte(), 0xc2.toByte(), 0x5d.toByte(), 0xe7.toByte(), 0x32.toByte(), 0x56.toByte(),
            0xfa.toByte(), 0x15.toByte(), 0x3f.toByte(), 0x41.toByte(), 0xc3.toByte(), 0x5e.toByte(), 0xe2.toByte(),
            0x3d.toByte(), 0x47.toByte(), 0xc9.toByte(), 0x40.toByte(), 0xc0.toByte(), 0x5b.toByte(), 0xed.toByte(),
            0x2c.toByte(), 0x74.toByte(), 0x9c.toByte(), 0xbf.toByte(), 0xda.toByte(), 0x75.toByte(), 0x9f.toByte(),
            0xba.toByte(), 0xd5.toByte(), 0x64.toByte(), 0xac.toByte(), 0xef.toByte(), 0x2a.toByte(), 0x7e.toByte(),
            0x82.toByte(), 0x9d.toByte(), 0xbc.toByte(), 0xdf.toByte(), 0x7a.toByte(), 0x8e.toByte(), 0x89.toByte(),
            0x80.toByte(), 0x9b.toByte(), 0xb6.toByte(), 0xc1.toByte(), 0x58.toByte(), 0xe8.toByte(), 0x23.toByte(),
            0x65.toByte(), 0xaf.toByte(), 0xea.toByte(), 0x25.toByte(), 0x6f.toByte(), 0xb1.toByte(), 0xc8.toByte(),
            0x43.toByte(), 0xc5.toByte(), 0x54.toByte(), 0xfc.toByte(), 0x1f.toByte(), 0x21.toByte(), 0x63.toByte(),
            0xa5.toByte(), 0xf4.toByte(), 0x07.toByte(), 0x09.toByte(), 0x1b.toByte(), 0x2d.toByte(), 0x77.toByte(),
            0x99.toByte(), 0xb0.toByte(), 0xcb.toByte(), 0x46.toByte(), 0xca.toByte(), 0x45.toByte(), 0xcf.toByte(),
            0x4a.toByte(), 0xde.toByte(), 0x79.toByte(), 0x8b.toByte(), 0x86.toByte(), 0x91.toByte(), 0xa8.toByte(),
            0xe3.toByte(), 0x3e.toByte(), 0x42.toByte(), 0xc6.toByte(), 0x51.toByte(), 0xf3.toByte(), 0x0e.toByte(),
            0x12.toByte(), 0x36.toByte(), 0x5a.toByte(), 0xee.toByte(), 0x29.toByte(), 0x7b.toByte(), 0x8d.toByte(),
            0x8c.toByte(), 0x8f.toByte(), 0x8a.toByte(), 0x85.toByte(), 0x94.toByte(), 0xa7.toByte(), 0xf2.toByte(),
            0x0d.toByte(), 0x17.toByte(), 0x39.toByte(), 0x4b.toByte(), 0xdd.toByte(), 0x7c.toByte(), 0x84.toByte(),
            0x97.toByte(), 0xa2.toByte(), 0xfd.toByte(), 0x1c.toByte(), 0x24.toByte(), 0x6c.toByte(), 0xb4.toByte(),
            0xc7.toByte(), 0x52.toByte(), 0xf6.toByte(), 0x01.toByte(), 0x03.toByte(), 0x05.toByte(), 0x0f.toByte(),
            0x11.toByte(), 0x33.toByte(), 0x55.toByte(), 0xff.toByte(), 0x1a.toByte(), 0x2e.toByte(), 0x72.toByte(),
            0x96.toByte(), 0xa1.toByte(), 0xf8.toByte(), 0x13.toByte(), 0x35.toByte(), 0x5f.toByte(), 0xe1.toByte(),
            0x38.toByte(), 0x48.toByte(), 0xd8.toByte(), 0x73.toByte(), 0x95.toByte(), 0xa4.toByte(), 0xf7.toByte(),
            0x02.toByte(), 0x06.toByte(), 0x0a.toByte(), 0x1e.toByte(), 0x22.toByte(), 0x66.toByte(), 0xaa.toByte(),
            0xe5.toByte(), 0x34.toByte(), 0x5c.toByte(), 0xe4.toByte(), 0x37.toByte(), 0x59.toByte(), 0xeb.toByte(),
            0x26.toByte(), 0x6a.toByte(), 0xbe.toByte(), 0xd9.toByte(), 0x70.toByte(), 0x90.toByte(), 0xab.toByte(),
            0xe6.toByte(), 0x31.toByte(), 0x53.toByte(), 0xf5.toByte(), 0x04.toByte(), 0x0c.toByte(), 0x14.toByte(),
            0x3c.toByte(), 0x44.toByte(), 0xcc.toByte(), 0x4f.toByte(), 0xd1.toByte(), 0x68.toByte(), 0xb8.toByte(),
            0xd3.toByte(), 0x6e.toByte(), 0xb2.toByte(), 0xcd.toByte(), 0x4c.toByte(), 0xd4.toByte(), 0x67.toByte(),
            0xa9.toByte(), 0xe0.toByte(), 0x3b.toByte(), 0x4d.toByte(), 0xd7.toByte(), 0x62.toByte(), 0xa6.toByte(),
            0xf1.toByte(), 0x08.toByte(), 0x18.toByte(), 0x28.toByte(), 0x78.toByte(), 0x88.toByte(), 0x83.toByte(),
            0x9e.toByte(), 0xb9.toByte(), 0xd0.toByte(), 0x6b.toByte(), 0xbd.toByte(), 0xdc.toByte(), 0x7f.toByte(),
            0x81.toByte(), 0x98.toByte(), 0xb3.toByte(), 0xce.toByte(), 0x49.toByte(), 0xdb.toByte(), 0x76.toByte(),
            0x9a.toByte(), 0xb5.toByte(), 0xc4.toByte(), 0x57.toByte(), 0xf9.toByte(), 0x10.toByte(), 0x30.toByte(),
            0x50.toByte(), 0xf0.toByte(), 0x0b.toByte(), 0x1d.toByte(), 0x27.toByte(), 0x69.toByte(), 0xbb.toByte(),
            0xd6.toByte(), 0x61.toByte(), 0xa3.toByte(), 0xfe.toByte(), 0x19.toByte(), 0x2b.toByte(), 0x7d.toByte(),
            0x87.toByte(), 0x92.toByte(), 0xad.toByte(), 0xec.toByte(), 0x2f.toByte(), 0x71.toByte(), 0x93.toByte(),
            0xae.toByte(), 0xe9.toByte(), 0x20.toByte(), 0x60.toByte(), 0xa0.toByte(), 0xfb.toByte(), 0x16.toByte(),
            0x3a.toByte(), 0x4e.toByte(), 0xd2.toByte(), 0x6d.toByte(), 0xb7.toByte(), 0xc2.toByte(), 0x5d.toByte(),
            0xe7.toByte(), 0x32.toByte(), 0x56.toByte(), 0xfa.toByte(), 0x15.toByte(), 0x3f.toByte(), 0x41.toByte(),
            0xc3.toByte(), 0x5e.toByte(), 0xe2.toByte(), 0x3d.toByte(), 0x47.toByte(), 0xc9.toByte(), 0x40.toByte(),
            0xc0.toByte(), 0x5b.toByte(), 0xed.toByte(), 0x2c.toByte(), 0x74.toByte(), 0x9c.toByte(), 0xbf.toByte(),
            0xda.toByte(), 0x75.toByte(), 0x9f.toByte(), 0xba.toByte(), 0xd5.toByte(), 0x64.toByte(), 0xac.toByte(),
            0xef.toByte(), 0x2a.toByte(), 0x7e.toByte(), 0x82.toByte(), 0x9d.toByte(), 0xbc.toByte(), 0xdf.toByte(),
            0x7a.toByte(), 0x8e.toByte(), 0x89.toByte(), 0x80.toByte(), 0x9b.toByte(), 0xb6.toByte(), 0xc1.toByte(),
            0x58.toByte(), 0xe8.toByte(), 0x23.toByte(), 0x65.toByte(), 0xaf.toByte(), 0xea.toByte(), 0x25.toByte(),
            0x6f.toByte(), 0xb1.toByte(), 0xc8.toByte(), 0x43.toByte(), 0xc5.toByte(), 0x54.toByte(), 0xfc.toByte(),
            0x1f.toByte(), 0x21.toByte(), 0x63.toByte(), 0xa5.toByte(), 0xf4.toByte(), 0x07.toByte(), 0x09.toByte(),
            0x1b.toByte(), 0x2d.toByte(), 0x77.toByte(), 0x99.toByte(), 0xb0.toByte(), 0xcb.toByte(), 0x46.toByte(),
            0xca.toByte(), 0x45.toByte(), 0xcf.toByte(), 0x4a.toByte(), 0xde.toByte(), 0x79.toByte(), 0x8b.toByte(),
            0x86.toByte(), 0x91.toByte(), 0xa8.toByte(), 0xe3.toByte(), 0x3e.toByte(), 0x42.toByte(), 0xc6.toByte(),
            0x51.toByte(), 0xf3.toByte(), 0x0e.toByte(), 0x12.toByte(), 0x36.toByte(), 0x5a.toByte(), 0xee.toByte(),
            0x29.toByte(), 0x7b.toByte(), 0x8d.toByte(), 0x8c.toByte(), 0x8f.toByte(), 0x8a.toByte(), 0x85.toByte(),
            0x94.toByte(), 0xa7.toByte(), 0xf2.toByte(), 0x0d.toByte(), 0x17.toByte(), 0x39.toByte(), 0x4b.toByte(),
            0xdd.toByte(), 0x7c.toByte(), 0x84.toByte(), 0x97.toByte(), 0xa2.toByte(), 0xfd.toByte(), 0x1c.toByte(),
            0x24.toByte(), 0x6c.toByte(), 0xb4.toByte(), 0xc7.toByte(), 0x52.toByte(), 0xf6.toByte(), 0x00.toByte(),
            0x00.toByte()
    )

    fun add(a: Byte, b: Byte): Byte {
        return (a xor b) as Byte
    }

    fun sub(a: Byte, b: Byte): Byte {
        return add(a, b)
    }

    fun mul(a: Byte, b: Byte): Byte {
        return if (a.toInt() == 0 || b.toInt() == 0) {
            0
        } else EXP[java.lang.Byte.toUnsignedInt(LOG[java.lang.Byte.toUnsignedInt(a)]) + java.lang.Byte.toUnsignedInt(LOG[java.lang.Byte.toUnsignedInt(b)])]
    }

    fun div(a: Byte, b: Byte): Byte {
        // multiply by the inverse of b
        return mul(a, EXP[255 - java.lang.Byte.toUnsignedInt(LOG[java.lang.Byte.toUnsignedInt(b)])])
    }

    fun eval(p: ByteArray, x: Byte): Byte {
        // Horner's method
        var result: Byte = 0
        for (i in p.indices.reversed()) {
            result = add(mul(result, x), p[i])
        }
        return result
    }

    fun degree(p: ByteArray): Int {
        for (i in p.size - 1 downTo 1) {
            if (p[i].toUnsignedValue() != 0) {
                return i
            }
        }
        return 0
    }

    fun Byte.toUnsignedValue(): Int = this.toInt() and 0xFF

    fun generate(random: SecureRandom, degree: Int, x: Byte): ByteArray {
        val p = ByteArray(degree + 1)

        // generate random polynomials until we find one of the given degree
        do {
            random.nextBytes(p)
        } while (degree(p) != degree)

        // set y intercept
        p[0] = x
        return p
    }

    fun interpolate(points: Array<ByteArray>): Byte {
        // calculate f(0) of the given points using Lagrangian interpolation
        val x: Byte = 0
        var y: Byte = 0
        for (i in points.indices) {
            val aX = points[i][0]
            val aY = points[i][1]
            var li: Byte = 1
            for (j in points.indices) {
                val bX = points[j][0]
                if (i != j) {
                    li = mul(li, div(sub(x, bX), sub(aX, bX)))
                }
            }
            y = add(y, mul(li, aY))
        }
        return y
    }
}
