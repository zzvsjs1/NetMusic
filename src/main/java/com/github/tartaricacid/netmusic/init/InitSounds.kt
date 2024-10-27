package com.github.tartaricacid.netmusic.init

import com.github.tartaricacid.netmusic.NetMusic
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvent
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.ForgeRegistries
import net.minecraftforge.registries.RegistryObject

object InitSounds {

    @JvmField
    val SOUND_EVENTS: DeferredRegister<SoundEvent> =
        DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, NetMusic.MOD_ID)

    @JvmField
    var NET_MUSIC: RegistryObject<SoundEvent> = SOUND_EVENTS.register(
        "net_music"
    ) { SoundEvent.createVariableRangeEvent(ResourceLocation(NetMusic.MOD_ID, "net_music")) }

}
