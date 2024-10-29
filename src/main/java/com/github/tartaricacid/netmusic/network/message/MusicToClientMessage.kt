package com.github.tartaricacid.netmusic.network.message

import com.github.tartaricacid.netmusic.client.audio.MusicPlayManager.play
import com.github.tartaricacid.netmusic.client.audio.NetMusicSound
import net.minecraft.Util
import net.minecraft.core.BlockPos
import net.minecraft.network.FriendlyByteBuf
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import net.minecraftforge.network.NetworkEvent
import java.net.URL
import java.util.concurrent.CompletableFuture
import java.util.function.Supplier

class MusicToClientMessage(
    private val pos: BlockPos,
    private val url: String,
    private val timeSecond: Int,
    private val songName: String
) {
    companion object {

        fun decode(buf: FriendlyByteBuf): MusicToClientMessage {
            return MusicToClientMessage(BlockPos.of(buf.readLong()), buf.readUtf(), buf.readInt(), buf.readUtf())
        }

        fun encode(message: MusicToClientMessage, buf: FriendlyByteBuf) {
            buf.writeLong(message.pos.asLong())
            buf.writeUtf(message.url)
            buf.writeInt(message.timeSecond)
            buf.writeUtf(message.songName)
        }

        fun handle(message: MusicToClientMessage, contextSupplier: Supplier<NetworkEvent.Context>) {
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
        private fun onHandle(message: MusicToClientMessage) {
            play(
                message.url,
                message.songName
            ) { url: URL? -> NetMusicSound(message.pos, url!!, message.timeSecond) }
        }
    }
}