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
import kotlin.properties.Delegates

/**
 * @author SQwatermark
 */
class Mp3AudioStream @Throws(IOException::class, UnsupportedAudioFileException::class) constructor(
    private val url: URL?
) : AudioStream {

    private var stream: AudioInputStream? = null

    private lateinit var array: ByteArray

    private var offset by Delegates.notNull<Int>()

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

        val targetInputStream = AudioSystem.getAudioInputStream(targetFormat, originalInputStream)
        if (GeneralConfig.ENABLE_STEREO!!.get()) {
            targetFormat = AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                originalFormat.sampleRate,
                16,
                1,
                2,
                originalFormat.sampleRate,
                false
            )

            this.stream = AudioSystem.getAudioInputStream(targetFormat, targetInputStream)
        } else {
            this.stream = targetInputStream
        }

        this.array = IOUtils.toByteArray(stream)
        this.offset = 0
    }

    override fun getFormat(): AudioFormat {
        return stream!!.format
    }

    override fun read(size: Int): ByteBuffer {
        val byteBuffer = BufferUtils.createByteBuffer(size)
        if (array.size >= offset + size) {
            byteBuffer.put(array, offset, size)
        } else {
            byteBuffer.put(ByteArray(size))
        }

        offset += size
        byteBuffer.flip()
        return byteBuffer
    }

    @Throws(IOException::class)
    override fun close() {
        stream?.close()
    }

}