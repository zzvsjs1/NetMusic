package com.github.tartaricacid.netmusic.client.event

import com.github.tartaricacid.netmusic.NetMusic
import com.github.tartaricacid.netmusic.client.config.MusicListManage
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import java.io.IOException


@EventBusSubscriber(modid = NetMusic.MOD_ID, value = [Dist.CLIENT], bus = EventBusSubscriber.Bus.MOD)
object ClientEvent {

    @SubscribeEvent
    fun onClientSetup(event: FMLClientSetupEvent) {
        event.enqueueWork {
            try {
                MusicListManage.loadConfigSongs()
            } catch (e: IOException) {
                NetMusic.LOGGER.error("Error", e)
            }
        }
    }

}