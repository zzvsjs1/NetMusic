package com.github.tartaricacid.netmusic.compat.tlm.init;

import com.github.tartaricacid.netmusic.compat.tlm.client.gui.MusicPlayerBackpackContainerScreen;
import com.github.tartaricacid.netmusic.compat.tlm.inventory.MusicPlayerBackpackContainer;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ContainerScreenInit {
    public static void init(FMLClientSetupEvent evt) {
        evt.enqueueWork(() -> MenuScreens.register(MusicPlayerBackpackContainer.TYPE, MusicPlayerBackpackContainerScreen::new));
    }
}
