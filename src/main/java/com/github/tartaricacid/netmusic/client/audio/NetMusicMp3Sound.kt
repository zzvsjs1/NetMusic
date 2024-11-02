package com.github.tartaricacid.netmusic.client.audio

import net.minecraft.Util
import net.minecraft.client.resources.sounds.Sound
import net.minecraft.client.sounds.AudioStream
import net.minecraft.client.sounds.SoundBufferLibrary
import net.minecraft.core.BlockPos
import java.io.IOException
import java.net.URL
import java.util.concurrent.CompletableFuture
import javax.sound.sampled.UnsupportedAudioFileException

class NetMusicMp3Sound(
    pos: BlockPos,
    private val songUrl: URL,
    timeSecond: Int
) : NetMusicSound(pos, songUrl, timeSecond) {

    override fun getStream(
        soundBuffers: SoundBufferLibrary,
        sound: Sound,
        looping: Boolean
    ): CompletableFuture<AudioStream> {
        return CompletableFuture.supplyAsync({
            try {
                return@supplyAsync Mp3AudioStream(this.songUrl)
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: UnsupportedAudioFileException) {
                e.printStackTrace()
            }

            null
        }, Util.backgroundExecutor())
    }

}