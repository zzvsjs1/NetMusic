package com.github.tartaricacid.netmusic.init

import com.github.tartaricacid.netmusic.network.NetworkHandler
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
object CommonRegistry {
    @SubscribeEvent
    fun onSetupEvent(event: FMLCommonSetupEvent) {
        event.enqueueWork { NetworkHandler.init() }
    }
}