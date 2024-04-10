package com.github.tartaricacid.netmusic.compat.tlm.message;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.api.NetWorker;
import com.github.tartaricacid.netmusic.compat.tlm.client.audio.MaidNetMusicSound;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class MaidMusicToClientMessage {
    private static final String ERROR_404 = "http://music.163.com/404";
    private static final String MUSIC_163_URL = "https://music.163.com/";
    private final int entityId;
    private final String url;
    private final int timeSecond;
    private final String songName;

    public MaidMusicToClientMessage(int entityId, String url, int timeSecond, String songName) {
        this.entityId = entityId;
        this.url = url;
        this.timeSecond = timeSecond;
        this.songName = songName;
    }

    public static MaidMusicToClientMessage decode(FriendlyByteBuf buf) {
        return new MaidMusicToClientMessage(buf.readInt(), buf.readUtf(), buf.readInt(), buf.readUtf());
    }

    public static void encode(MaidMusicToClientMessage message, FriendlyByteBuf buf) {
        buf.writeInt(message.entityId);
        buf.writeUtf(message.url);
        buf.writeInt(message.timeSecond);
        buf.writeUtf(message.songName);
    }

    public static void handle(MaidMusicToClientMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isClient()) {
            context.enqueueWork(() -> CompletableFuture.runAsync(() -> onHandle(message), Util.backgroundExecutor()));
        }
        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void onHandle(MaidMusicToClientMessage message) {
        String url = message.url;
        if (message.url.startsWith(MUSIC_163_URL)) {
            try {
                url = NetWorker.getRedirectUrl(message.url, NetMusic.NET_EASE_WEB_API.getRequestPropertyData());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (url != null && !url.equals(ERROR_404)) {
            playMusic(message, url);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void playMusic(MaidMusicToClientMessage message, String url) {
        final URL urlFinal;
        try {
            if (Minecraft.getInstance().level == null) {
                return;
            }
            Entity entity = Minecraft.getInstance().level.getEntity(message.entityId);
            if (!(entity instanceof EntityMaid maid)) {
                return;
            }
            urlFinal = new URL(url);
            MaidNetMusicSound sound = new MaidNetMusicSound(maid, urlFinal, message.timeSecond);
            Minecraft.getInstance().submitAsync(() -> {
                Minecraft.getInstance().getSoundManager().play(sound);
                Minecraft.getInstance().gui.setNowPlaying(Component.literal(message.songName));
            });
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
