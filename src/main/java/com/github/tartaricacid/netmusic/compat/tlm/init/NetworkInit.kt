package com.github.tartaricacid.netmusic.compat.tlm.init

import com.github.tartaricacid.netmusic.compat.tlm.message.MaidMusicToClientMessage
import com.github.tartaricacid.netmusic.compat.tlm.message.MaidStopMusicMessage
import net.minecraft.network.FriendlyByteBuf
import net.minecraftforge.network.NetworkDirection
import net.minecraftforge.network.NetworkEvent
import net.minecraftforge.network.simple.SimpleChannel
import java.util.*
import java.util.function.Supplier

object NetworkInit {

    fun init(channel: SimpleChannel) {
        channel.registerMessage(
            99,
            MaidMusicToClientMessage::class.java,
            MaidMusicToClientMessage::encode,
            MaidMusicToClientMessage::decode,
            MaidMusicToClientMessage::handle,
            Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        )

        channel.registerMessage(
            100,
            MaidStopMusicMessage::class.java,
            { message: MaidStopMusicMessage?, buf: FriendlyByteBuf? -> MaidStopMusicMessage.encode(message, buf) },
            { buffer: FriendlyByteBuf? -> MaidStopMusicMessage.decode(buffer) },
            { message: MaidStopMusicMessage?, contextSupplier: Supplier<NetworkEvent.Context?>? ->
                MaidStopMusicMessage.handle(
                    message,
                    contextSupplier
                )
            },
            Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        )
    }

}