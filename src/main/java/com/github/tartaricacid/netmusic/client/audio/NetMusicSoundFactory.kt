package com.github.tartaricacid.netmusic.client.audio

import net.minecraft.core.BlockPos
import java.io.BufferedInputStream
import java.net.HttpURLConnection
import java.net.URL

object NetMusicSoundFactory {

    private fun getFileExtension(url: URL): String? {
        val path = url.path
        val dotIndex = path.lastIndexOf('.')
        return if (dotIndex != -1 && dotIndex < path.length - 1) {
            path.substring(dotIndex + 1).lowercase()
        } else {
            null
        }
    }


    fun createSound(pos: BlockPos, songUrl: URL, timeSecond: Int): NetMusicSound? {
        // Option A: Using file extension
        return when (val extension = getFileExtension(songUrl)) {
            "mp3" -> NetMusicMp3Sound(pos, songUrl, timeSecond)
            // Add more cases for other formats
            else -> {
                println("Unsupported audio format: $extension")
                null
            }
        }
    }

    fun detectFileFormat(url: URL): String? {
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 5000
        connection.readTimeout = 5000
        connection.doInput = true
        connection.connect()

        BufferedInputStream(connection.inputStream).use { inputStream ->
            val header = ByteArray(12)
            val bytesRead = inputStream.read(header, 0, header.size)
            if (bytesRead < 12) return null

            byteArrayOf('I'.code.toByte(), 'D'.code.toByte(), '3'.code.toByte())

            return when {
                // MP3 files typically start with "ID3" or frame sync bits
                header[0].toInt() and 0xFF == 0xFF && (header[1].toInt() and 0xE0) == 0xE0 -> "mp3"

                // Add more formats as needed
                else -> null
            }
        }
    }
}
