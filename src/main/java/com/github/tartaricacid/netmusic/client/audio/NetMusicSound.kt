package com.github.tartaricacid.netmusic.client.audio

import com.github.tartaricacid.netmusic.init.InitSounds
import com.github.tartaricacid.netmusic.tileentity.TileEntityMusicPlayer
import net.minecraft.Util
import net.minecraft.client.Minecraft
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance
import net.minecraft.client.resources.sounds.Sound
import net.minecraft.client.resources.sounds.SoundInstance
import net.minecraft.client.sounds.AudioStream
import net.minecraft.client.sounds.SoundBufferLibrary
import net.minecraft.core.BlockPos
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.sounds.SoundSource
import java.io.IOException
import java.net.URL
import java.util.concurrent.CompletableFuture
import javax.sound.sampled.UnsupportedAudioFileException

class NetMusicSound(pos: BlockPos, private val songUrl: URL, timeSecond: Int)
    : AbstractTickableSoundInstance(
        InitSounds.NET_MUSIC.get(),
        SoundSource.RECORDS,
        SoundInstance.createUnseededRandom()
    ) {
    private val tickTimes: Int
    private val pos: BlockPos
    private var tick: Int

    init {
        this.x = (pos.x + 0.5f).toDouble()
        this.y = (pos.y + 0.5f).toDouble()
        this.z = (pos.z + 0.5f).toDouble()
        this.tickTimes = timeSecond * 20
        this.volume = 4.0f
        this.tick = 0
        this.pos = pos
    }

    override fun tick() {
        val world = Minecraft.getInstance().level ?: return
        tick++
        if (tick > tickTimes + 50) {
            this.stop()
        } else {
            if (world.gameTime % 8 == 0L) {
                for (i in 0..1) {
                    world.addParticle(
                        ParticleTypes.NOTE,
                        x - 0.5f + world.random.nextDouble(),
                        y + world.random.nextDouble() + 1,
                        z - 0.5f + world.random.nextDouble(),
                        world.random.nextGaussian(), world.random.nextGaussian(), world.random.nextInt(3).toDouble()
                    )
                }
            }
        }

        val te = world.getBlockEntity(pos)
        if (te is TileEntityMusicPlayer) {
            if (!te.isPlay) {
                this.stop()
            }
        } else {
            this.stop()
        }
    }

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