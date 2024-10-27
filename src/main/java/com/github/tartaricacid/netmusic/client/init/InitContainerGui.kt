package com.github.tartaricacid.netmusic.client.init

import com.github.tartaricacid.netmusic.client.gui.CDBurnerMenuScreen
import com.github.tartaricacid.netmusic.client.gui.ComputerMenuScreen
import com.github.tartaricacid.netmusic.inventory.CDBurnerMenu
import com.github.tartaricacid.netmusic.inventory.ComputerMenu
import net.minecraft.client.gui.screens.MenuScreens
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent

@EventBusSubscriber(value = [Dist.CLIENT], bus = EventBusSubscriber.Bus.MOD)
object InitContainerGui {

    @SubscribeEvent
    fun clientSetup(evt: FMLClientSetupEvent) {
        evt.enqueueWork { MenuScreens.register(CDBurnerMenu.TYPE, ::CDBurnerMenuScreen)}
        evt.enqueueWork { MenuScreens.register(ComputerMenu.TYPE, ::ComputerMenuScreen) }
    }
}