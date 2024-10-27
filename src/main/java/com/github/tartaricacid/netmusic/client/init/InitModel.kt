package com.github.tartaricacid.netmusic.client.init

import com.github.tartaricacid.netmusic.client.model.ModelMusicPlayer
import com.github.tartaricacid.netmusic.client.renderer.MusicPlayerRenderer
import com.github.tartaricacid.netmusic.tileentity.TileEntityMusicPlayer
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.client.event.EntityRenderersEvent.RegisterLayerDefinitions
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent

@EventBusSubscriber(value = [Dist.CLIENT], bus = EventBusSubscriber.Bus.MOD)
object InitModel {

    @SubscribeEvent
    fun clientSetup(evt: FMLClientSetupEvent?) {
        BlockEntityRenderers.register(TileEntityMusicPlayer.TYPE, ::MusicPlayerRenderer)
    }

    @SubscribeEvent
    fun onRegisterLayers(event: RegisterLayerDefinitions) {
        event.registerLayerDefinition(ModelMusicPlayer.LAYER) { ModelMusicPlayer.createBodyLayer() }
    }
}