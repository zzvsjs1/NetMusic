package com.github.tartaricacid.netmusic.compat.tlm.init;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.RegisterEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class CompatRegistry {
    public static final String TLM = "touhou_little_maid";

    @SubscribeEvent
    public static void initContainer(RegisterEvent event) {
        checkModLoad(TLM, () -> ContainerInit.init(event));
    }

    @OnlyIn(Dist.CLIENT)
    public static void initContainerScreen(FMLClientSetupEvent event) {
        checkModLoad(TLM, () -> ContainerScreenInit.init(event));
    }

    public static void initNetwork(SimpleChannel channel) {
        checkModLoad(TLM, () -> NetworkInit.init(channel));
    }

    private static void checkModLoad(String modId, Runnable runnable) {
        if (ModList.get().isLoaded(modId)) {
            runnable.run();
        }
    }
}
