package com.github.tartaricacid.netmusic.client.audio

import com.github.tartaricacid.netmusic.config.GeneralConfig
import javazoom.spi.mpeg.sampled.file.MpegAudioFileReader
import net.minecraft.client.sounds.AudioStream
import org.apache.commons.compress.utils.IOUtils
import org.lwjgl.BufferUtils
import java.io.IOException
import java.net.URL
import java.nio.ByteBuffer
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.UnsupportedAudioFileException

/**
 * @author SQwatermark
 */
class Mp3AudioStream @Throws(IOException::class, UnsupportedAudioFileException::class) constructor(
    private val url: URL?
) : AudioStream {

    private lateinit var format: AudioFormat

    private lateinit var audioStream: AudioInputStream

    private val bufferSize = 4096

    private val buffer = ByteArray(bufferSize)

    init {
        doInit()
    }

    private fun doInit() {
        val originalInputStream = MpegAudioFileReader().getAudioInputStream(url)
        val originalFormat = originalInputStream.format
        var targetFormat = AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            originalFormat.sampleRate,
            16,
            originalFormat.channels,
            originalFormat.channels * 2,
            originalFormat.sampleRate,
            false
        )

        var targetInputStream = AudioSystem.getAudioInputStream(targetFormat, originalInputStream)

        // Transfer to target format.
        if (GeneralConfig.ENABLE_STEREO?.get() == true) {
            targetFormat = AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                originalFormat.sampleRate,
                16,
                1,
                2,
                originalFormat.sampleRate,
                false
            )

            targetInputStream = AudioSystem.getAudioInputStream(targetFormat, targetInputStream)
        }

        this.format = targetInputStream.format
        this.audioStream = targetInputStream
    }

    override fun getFormat(): AudioFormat {
        return format
    }

    /**
     * Reads up to [size] bytes from the audio stream.
     *
     * @param size The maximum number of bytes to read.
     * @return A ByteBuffer containing the read bytes.
     * @throws IOException If an I/O error occurs.
     */
    @Throws(IOException::class)
    override fun read(size: Int): ByteBuffer {
        val ret = BufferUtils.createByteBuffer(size)
        var needToRead = size

        while (needToRead > 0) {
            // Determine the number of bytes to read in this iteration
            val bytesToRead = minOf(needToRead, buffer.size)

            // Read bytes from the audioStream into the buffer starting at offset 0
            val numOfBytesRead = audioStream.read(buffer, 0, bytesToRead)

            if (numOfBytesRead == -1) {
                // End of stream reached; pad the remaining bytes with zeros
                ret.put(ByteArray(needToRead))
                break
            }

            // Put the read bytes into the ByteBuffer
            ret.put(buffer, 0, numOfBytesRead)

            needToRead -= numOfBytesRead
        }

        ret.flip()
        return ret
    }

    @Throws(IOException::class)
    override fun close() {
        audioStream.close()
    }

}