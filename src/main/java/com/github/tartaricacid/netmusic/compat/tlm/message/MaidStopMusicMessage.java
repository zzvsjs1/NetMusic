package com.github.tartaricacid.netmusic.compat.tlm.message;

import com.github.tartaricacid.netmusic.compat.tlm.client.audio.MaidNetMusicSound;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.Map;
import java.util.function.Supplier;

public class MaidStopMusicMessage {
    private final int entityId;

    public MaidStopMusicMessage(int entityId) {
        this.entityId = entityId;
    }

    public static MaidStopMusicMessage decode(FriendlyByteBuf buffer) {
        return new MaidStopMusicMessage(buffer.readInt());
    }

    public static void encode(MaidStopMusicMessage message, FriendlyByteBuf buf) {
        buf.writeInt(message.entityId);
    }

    public static void handle(MaidStopMusicMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isClient()) {
            context.enqueueWork(() -> onHandle(message));
        }
        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void onHandle(MaidStopMusicMessage message) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        Map<SoundInstance, ChannelAccess.ChannelHandle> sounds = Minecraft.getInstance().getSoundManager().soundEngine.instanceToChannel;
        for (SoundInstance instance : sounds.keySet()) {
            if (!(instance instanceof MaidNetMusicSound sound)) {
                continue;
            }
            if (sound.getMaidId() == message.entityId) {
                sound.setStop();
            }
        }
    }
}
