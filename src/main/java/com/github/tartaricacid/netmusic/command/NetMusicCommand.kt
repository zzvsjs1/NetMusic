package com.github.tartaricacid.netmusic.command

import com.github.tartaricacid.netmusic.NetMusic
import com.github.tartaricacid.netmusic.client.config.MusicListManage
import com.github.tartaricacid.netmusic.init.InitItems
import com.github.tartaricacid.netmusic.item.ItemMusicCD.Companion.setSongInfo
import com.github.tartaricacid.netmusic.network.NetworkHandler
import com.github.tartaricacid.netmusic.network.message.GetMusicListMessage
import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.LongArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource

object NetMusicCommand {

    private const val ROOT_NAME = "netmusic"
    private const val RELOAD_NAME = "reload"
    private const val GET_163_NAME = "get163"
    private const val GET_163_CD_NAME = "get163cd"
    private const val SONG_LIST_ID = "song_list_id"
    private const val SONG_ID = "song_id"

    fun get(): LiteralArgumentBuilder<CommandSourceStack?> {
        val root = Commands.literal(ROOT_NAME)
        val get163List = Commands.literal(GET_163_NAME)
        val get163Song = Commands.literal(GET_163_CD_NAME)
        val reload = Commands.literal(RELOAD_NAME)
        val songListId = Commands.argument(
            SONG_LIST_ID,
            LongArgumentType.longArg()
        )

        val songId = Commands.argument(
            SONG_ID,
            LongArgumentType.longArg()
        )

        root.then(get163List.then(songListId.executes(NetMusicCommand::getSongList)))
        root.then(get163Song.then(songId.executes(NetMusicCommand::getSong)))
        root.then(reload.executes(NetMusicCommand::reload))
        return root
    }

    private fun getSong(context: CommandContext<CommandSourceStack>): Int {
        try {
            val songId = LongArgumentType.getLong(context, SONG_ID)
            val songInfo = MusicListManage.get163Song(songId)
            val musicDisc = setSongInfo(songInfo, InitItems.MUSIC_CD.get().defaultInstance)
            val serverPlayer = context.source.playerOrException
            val canPlaceIn = serverPlayer.inventory.add(musicDisc)
            if (canPlaceIn && musicDisc.isEmpty) {
                musicDisc.count = 1
                val dropItem = serverPlayer.drop(musicDisc, false)
                dropItem?.makeFakeItem()

                serverPlayer.level()
                    .playSound(
                        null,
                        serverPlayer.x,
                        serverPlayer.y,
                        serverPlayer.z,
                        SoundEvents.ITEM_PICKUP,
                        SoundSource.PLAYERS,
                        0.2f,
                        ((serverPlayer.random.nextFloat() - serverPlayer.random.nextFloat()) * 0.7f
                                + 1.0f) * 2.0f
                    )

                serverPlayer.inventoryMenu.broadcastChanges()
            } else {
                val dropItem = serverPlayer.drop(musicDisc, false)
                if (dropItem != null) {
                    dropItem.setNoPickUpDelay()
                    dropItem.setThrower(serverPlayer.uuid)
                }
            }

            context.source
                .sendSuccess({ Component.translatable("command.netmusic.music_cd.add163cd.success") }, false)
        } catch (e: Exception) {
            NetMusic.LOGGER.error("Error: ", e)
            context.source.sendFailure(Component.translatable("command.netmusic.music_cd.add163cd.fail"))
        }

        return Command.SINGLE_SUCCESS
    }

    private fun getSongList(context: CommandContext<CommandSourceStack>): Int {
        try {
            val listId = LongArgumentType.getLong(context, SONG_LIST_ID)
            val serverPlayer = context.source.playerOrException
            NetworkHandler.sendToClientPlayer(GetMusicListMessage(listId), serverPlayer)
        } catch (e: Exception) {
            NetMusic.LOGGER.error("Error: ", e)
        }

        return Command.SINGLE_SUCCESS
    }

    private fun reload(context: CommandContext<CommandSourceStack>): Int {
        try {
            val serverPlayer = context.source.playerOrException
            NetworkHandler.sendToClientPlayer(
                GetMusicListMessage(GetMusicListMessage.RELOAD_MESSAGE),
                serverPlayer
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return Command.SINGLE_SUCCESS
    }
}