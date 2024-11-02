package com.github.tartaricacid.netmusic.client.audio

import com.github.tartaricacid.netmusic.init.InitSounds
import com.github.tartaricacid.netmusic.tileentity.TileEntityMusicPlayer
import net.minecraft.client.Minecraft
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance
import net.minecraft.client.resources.sounds.SoundInstance
import net.minecraft.core.BlockPos
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.sounds.SoundSource
import java.net.URL

abstract class NetMusicSound(pos: BlockPos, private val songUrl: URL, timeSecond: Int)
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
                        world.random.nextGaussian(),
                        world.random.nextGaussian(),
                        world.random.nextInt(3).toDouble()
                    )
                }
            }
        }

        val blockEntity = world.getBlockEntity(pos)
        if (blockEntity is TileEntityMusicPlayer) {
            if (!blockEntity.isPlay) {
                this.stop()
            }
        } else {
            this.stop()
        }
    }

}