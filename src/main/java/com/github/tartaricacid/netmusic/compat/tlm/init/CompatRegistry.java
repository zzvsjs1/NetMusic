package com.github.tartaricacid.netmusic.compat.tlm.init;

import com.github.tartaricacid.netmusic.init.InitItems;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
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

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        checkModLoad(TLM, () -> ModelInit.init(event));
    }

    @OnlyIn(Dist.CLIENT)
    public static void initContainerScreen(FMLClientSetupEvent event) {
        checkModLoad(TLM, () -> ContainerScreenInit.init(event));
    }

    public static void initCreativeModeTab(CreativeModeTab.Output output) {
        checkModLoad(TLM, () -> output.accept(new ItemStack(InitItems.MUSIC_PLAYER_BACKPACK.get())));
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
