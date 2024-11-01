package com.github.tartaricacid.netmusic.compat.tlm.client.audio;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.client.audio.Mp3AudioStream;
import com.github.tartaricacid.netmusic.init.InitSounds;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

public class MaidNetMusicSound extends AbstractTickableSoundInstance {
    private final URL songUrl;
    private final int tickTimes;
    private int tick;

    public MaidNetMusicSound(URL songUrl, int timeSecond) {
        super(InitSounds.NET_MUSIC.get(), SoundSource.RECORDS, SoundInstance.createUnseededRandom());

        this.songUrl = songUrl;

        this.tickTimes = timeSecond * 20;
        this.volume = 4.0f;
        this.tick = 0;
    }

    @Override
    public void tick() {
        Level world = Minecraft.getInstance().level;
        if (world == null) {
            this.stop();
            return;
        }

        tick++;
        if (tick > tickTimes + 50) {
            this.stop();
        } else {
            if (world.getGameTime() % 8 == 0) {
                for (int i = 0; i < 2; i++) {
                    world.addParticle(ParticleTypes.NOTE,
                            x - 0.5 + world.random.nextDouble(),
                            y + 1.5 + world.random.nextDouble(),
                            z - 0.5 + world.random.nextDouble(),
                            world.random.nextGaussian(), world.random.nextGaussian(), world.random.nextInt(3));
                }
            }
        }
    }

    @Override
    public @NotNull CompletableFuture<AudioStream> getStream(@NotNull SoundBufferLibrary soundBuffers, @NotNull Sound sound, boolean looping) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return new Mp3AudioStream(this.songUrl);
            } catch (IOException | UnsupportedAudioFileException e) {
                NetMusic.LOGGER.error("Error to create stream", e);
            }

            return null;
        }, Util.backgroundExecutor());
    }

    public void setStop() {
        this.stop();
    }
}
