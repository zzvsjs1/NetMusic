package com.github.tartaricacid.netmusic.init

import com.github.tartaricacid.netmusic.command.NetMusicCommand
import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber

@EventBusSubscriber
object CommandRegistry {
    @SubscribeEvent
    fun onServerStaring(event: RegisterCommandsEvent) {
        event.dispatcher.register(NetMusicCommand.get())
    }
}