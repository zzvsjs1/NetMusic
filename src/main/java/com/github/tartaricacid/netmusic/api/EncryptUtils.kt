package com.github.tartaricacid.netmusic.api

import org.apache.commons.codec.binary.Base64
import java.math.BigInteger
import java.net.URLEncoder
import java.util.*
import java.util.random.RandomGenerator
import java.util.random.RandomGeneratorFactory
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * @author 内个球
 */
object EncryptUtils {

    private val random: RandomGenerator = RandomGeneratorFactory.getDefault().create()

    private val randomString: String
        get() = buildString {
            for (i in 0..15) {
                append(Integer.toHexString(random.nextInt(16)))
            }
        }

    @JvmStatic
    @Throws(Exception::class)
    fun encryptedParam(text: String?): String {
        if (text == null) {
            return "params=null&encSecKey=null"
        }

        val modulus = "00e0b509f6259df8642dbc35662901477df22677ec152b5ff68ace615bb7" +
                "b725152b3ab17a876aea8a5aa76d2e417629ec4ee341f56135fccf695280" +
                "104e0312ecbda92557c93870114af6c9d05c4f7f0c3685b7a46bee255932" +
                "575cce10b424d813cfe4875d3e82047b97ddef52741d546b8e289dc6935b" +
                "3ece0462db0a22b8e7"
        val nonce = "0CoJUm6Qyw8W8jud"
        val pubKey = "010001"
        val secKey = randomString
        val encText = aesEncrypt(aesEncrypt(text, nonce), secKey)
        val encSecKey = rsaEncrypt(secKey, pubKey, modulus)

        return "params=" +
                URLEncoder.encode(encText, Charsets.UTF_8) +
                "&encSecKey=" + URLEncoder.encode(encSecKey, Charsets.UTF_8)
    }

    @JvmStatic
    @Throws(Exception::class)
    private fun aesEncrypt(text: String, key: String): String {
        val ivParameterSpec = IvParameterSpec("0102030405060708".toByteArray(Charsets.UTF_8))
        val secretKeySpec = SecretKeySpec(key.toByteArray(Charsets.UTF_8), "AES")
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec)
        val encrypted = cipher.doFinal(text.toByteArray())
        return Base64.encodeBase64String(encrypted)
    }

    private fun rsaEncrypt(text: String, pubKey: String, modulus: String): String {
        val reverseText = text.reversed()
        val bigIntVal = BigInteger(1, reverseText.toByteArray())
        val exp = BigInteger(pubKey, 16)
        val mod = BigInteger(modulus, 16)
        val hexString = StringBuilder(bigIntVal.modPow(exp, mod).toString(16))

        if (hexString.length >= 256) {
            return hexString.substring(hexString.length - 256)
        }

        while (hexString.length < 256) {
            hexString.insert(0, "0")
        }

        return hexString.toString()
    }

}