package com.github.tartaricacid.netmusic.compat.tlm.message

import com.github.tartaricacid.netmusic.client.audio.MusicPlayManager.play
import com.github.tartaricacid.netmusic.compat.tlm.client.audio.MaidNetMusicSound
import net.minecraft.Util
import net.minecraft.client.Minecraft
import net.minecraft.network.FriendlyByteBuf
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import net.minecraftforge.network.NetworkEvent
import java.net.URL
import java.util.concurrent.CompletableFuture
import java.util.function.Supplier

class MaidMusicToClientMessage(
    private val entityId: Int,
    private val url: String,
    private val timeSecond: Int,
    private val songName: String
) {
    companion object {

        fun decode(buf: FriendlyByteBuf): MaidMusicToClientMessage {
            return MaidMusicToClientMessage(buf.readInt(), buf.readUtf(), buf.readInt(), buf.readUtf())
        }

        fun encode(message: MaidMusicToClientMessage, buf: FriendlyByteBuf) {
            buf.writeInt(message.entityId)
            buf.writeUtf(message.url)
            buf.writeInt(message.timeSecond)
            buf.writeUtf(message.songName)
        }

        fun handle(message: MaidMusicToClientMessage, contextSupplier: Supplier<NetworkEvent.Context>) {
            val context = contextSupplier.get()
            if (context.direction.receptionSide.isClient) {
                context.enqueueWork {
                    CompletableFuture.runAsync({
                        onHandle(
                            message
                        )
                    }, Util.backgroundExecutor())
                }
            }
            context.packetHandled = true
        }

        @OnlyIn(Dist.CLIENT)
        private fun onHandle(message: MaidMusicToClientMessage) {
            if (Minecraft.getInstance().level == null) {
                return
            }

            play(message.url, message.songName) { url: URL? -> MaidNetMusicSound(url, message.timeSecond) }
        }
    }
}