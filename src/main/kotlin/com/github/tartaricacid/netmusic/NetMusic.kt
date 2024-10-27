package com.github.tartaricacid.netmusic

import com.github.tartaricacid.netmusic.api.NetEaseMusic
import com.github.tartaricacid.netmusic.api.WebApi
import com.github.tartaricacid.netmusic.config.GeneralConfig
import com.github.tartaricacid.netmusic.init.InitBlocks
import com.github.tartaricacid.netmusic.init.InitContainer
import com.github.tartaricacid.netmusic.init.InitItems
import com.github.tartaricacid.netmusic.init.InitSounds
import net.minecraftforge.fml.ModLoadingContext
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.config.ModConfig
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import thedarkcolour.kotlinforforge.forge.MOD_CONTEXT

@Mod(NetMusic.MOD_ID)
object NetMusic {

    @JvmField
    val NET_EASE_WEB_API: WebApi = NetEaseMusic().api

    const val MOD_ID: String = "netmusic"

    @JvmField
    val LOGGER: Logger = LogManager.getLogger(MOD_ID)

    init {
        InitBlocks.BLOCKS.register(MOD_CONTEXT.getKEventBus())
        InitBlocks.TILE_ENTITIES.register(MOD_CONTEXT.getKEventBus())
        InitItems.ITEMS.register(MOD_CONTEXT.getKEventBus())
        InitItems.TABS.register(MOD_CONTEXT.getKEventBus())
        InitSounds.SOUND_EVENTS.register(MOD_CONTEXT.getKEventBus())
        InitContainer.CONTAINER_TYPE.register(MOD_CONTEXT.getKEventBus())
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, GeneralConfig.init())
    }

}