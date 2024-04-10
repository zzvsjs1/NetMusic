package com.github.tartaricacid.netmusic.compat.tlm.init;

import com.github.tartaricacid.netmusic.compat.tlm.message.MaidMusicToClientMessage;
import com.github.tartaricacid.netmusic.compat.tlm.message.MaidStopMusicMessage;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

public class NetworkInit {
    public static void init(SimpleChannel channel) {
        channel.registerMessage(99, MaidMusicToClientMessage.class, MaidMusicToClientMessage::encode, MaidMusicToClientMessage::decode, MaidMusicToClientMessage::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        channel.registerMessage(100, MaidStopMusicMessage.class, MaidStopMusicMessage::encode, MaidStopMusicMessage::decode, MaidStopMusicMessage::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }
}
