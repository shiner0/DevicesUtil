package com.rn.devicesinfo


import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


object DisposeTools {

    /**
     * SHA1加密
     *
     * @param data 明文字节数组
     * @return 16进制密文
     */
    @JvmStatic
    fun encryptSHA1ToString(type:String,data: ByteArray?): String {
        return bytes2HexString(encryptSHA1(type,data))
    }

    private val HEX_DIGITS = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        'A', 'B', 'C', 'D', 'E', 'F')

    @JvmStatic
    fun bytes2HexString(bytes: ByteArray): String {
        val ret = CharArray(bytes.size shl 1)
        var i = 0
        var j = 0
        while (i < bytes.size) {
            ret[j++] = HEX_DIGITS[bytes[i].toInt() ushr 4 and 0x0f]
            ret[j++] = HEX_DIGITS[bytes[i].toInt() and 0x0f]
            i++
        }
        return String(ret)
    }

    /**
     * SHA1加密
     *
     * @param data 明文字节数组
     * @return 密文字节数组
     */
    @JvmStatic
    fun encryptSHA1(type:String,data: ByteArray?): ByteArray {
        return encryptAlgorithm(data, type)
    }

    /**
     * 对data进行algorithm算法加密
     *
     * @param data      明文字节数组
     * @param algorithm 加密算法
     * @return 密文字节数组
     */
    private fun encryptAlgorithm(data: ByteArray?, algorithm: String): ByteArray {
        try {
            val md = MessageDigest.getInstance(algorithm)
            md.update(data)
            return md.digest()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return ByteArray(0)
    }

}