package com.github.tartaricacid.netmusic.compat.tlm.init;

import com.github.tartaricacid.netmusic.compat.tlm.inventory.MusicPlayerBackpackContainer;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

public class ContainerInit {
    public static void init(RegisterEvent event) {
        event.register(ForgeRegistries.Keys.MENU_TYPES, helper -> helper.register("maid_music_player_backpack", MusicPlayerBackpackContainer.TYPE));
    }
}
