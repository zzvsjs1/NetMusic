package com.github.tartaricacid.netmusic.network

import com.github.tartaricacid.netmusic.NetMusic
import com.github.tartaricacid.netmusic.network.message.GetMusicListMessage
import com.github.tartaricacid.netmusic.network.message.MusicToClientMessage
import com.github.tartaricacid.netmusic.network.message.SetMusicIDMessage
import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.Level
import net.minecraftforge.network.NetworkDirection
import net.minecraftforge.network.NetworkRegistry
import net.minecraftforge.network.PacketDistributor
import net.minecraftforge.network.simple.SimpleChannel
import java.util.*

object NetworkHandler {

    private const val VERSION = "1.0.0"

    @JvmField
    val CHANNEL: SimpleChannel = NetworkRegistry.newSimpleChannel(ResourceLocation(
        NetMusic.MOD_ID,
        "network"
    ),
        { VERSION },
        { it: String -> it == VERSION },
        { it: String -> it == VERSION }
    )

    fun doInit() {
        CHANNEL.registerMessage(
            0,
            MusicToClientMessage::class.java,
            MusicToClientMessage::encode,
            MusicToClientMessage::decode,
            MusicToClientMessage::handle,
            Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        )

        CHANNEL.registerMessage(
            1,
            GetMusicListMessage::class.java,
            GetMusicListMessage::encode,
            GetMusicListMessage::decode,
            GetMusicListMessage::handle,
            Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        )

        CHANNEL.registerMessage(
            2,
            SetMusicIDMessage::class.java,
            SetMusicIDMessage::encode,
            SetMusicIDMessage::decode,
            SetMusicIDMessage::handle,
            Optional.of(NetworkDirection.PLAY_TO_SERVER)
        )
    }

    @JvmStatic
    fun sendToNearby(world: Level, pos: BlockPos, toSend: Any) {
        if (world is ServerLevel) {
            world.chunkSource.chunkMap.getPlayers(ChunkPos(pos), false)
                .stream()
                .filter { p: ServerPlayer ->
                    p.distanceToSqr(
                        pos.x.toDouble(),
                        pos.y.toDouble(),
                        pos.z.toDouble()
                    ) < 96 * 96
                }
                .forEach { p: ServerPlayer? -> CHANNEL.send(PacketDistributor.PLAYER.with { p }, toSend) }
        }
    }

    fun sendToClientPlayer(message: Any, player: ServerPlayer?) {
        CHANNEL.send(PacketDistributor.PLAYER.with { player }, message)
    }
}