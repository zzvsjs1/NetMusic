package com.github.tartaricacid.netmusic.init

import com.github.tartaricacid.netmusic.NetMusic
import com.github.tartaricacid.netmusic.client.config.MusicListManage
import com.github.tartaricacid.netmusic.item.ItemMusicCD
import com.github.tartaricacid.netmusic.item.ItemMusicCD.Companion.setSongInfo
import com.github.tartaricacid.netmusic.item.ItemMusicPlayer
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.CreativeModeTab.ItemDisplayParameters
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.ForgeRegistries
import net.minecraftforge.registries.RegistryObject

object InitItems {

    @JvmField
    val ITEMS: DeferredRegister<Item> = DeferredRegister.create(ForgeRegistries.ITEMS, NetMusic.MOD_ID)

    @JvmField
    val TABS: DeferredRegister<CreativeModeTab> = DeferredRegister.create(
        Registries.CREATIVE_MODE_TAB,
        NetMusic.MOD_ID
    )

    @JvmField
    var MUSIC_CD: RegistryObject<Item> = ITEMS.register(
        "music_cd", ::ItemMusicCD
    )

    @JvmField
    var MUSIC_PLAYER: RegistryObject<Item> = ITEMS.register<Item>("music_player", ::ItemMusicPlayer)

    @JvmField
    var CD_BURNER: RegistryObject<Item> = ITEMS.register(
        "cd_burner"
    ) {
        BlockItem(InitBlocks.CD_BURNER.get(), Item.Properties().stacksTo(1))
    }
    @JvmField
    var COMPUTER: RegistryObject<Item> = ITEMS.register(
        "computer"
    ) { BlockItem(InitBlocks.COMPUTER.get(), Item.Properties().stacksTo(1)) }

    @JvmField
    var NET_MUSIC_TAB: RegistryObject<CreativeModeTab> = TABS.register(
        "netmusic"
    ) {
        CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.netmusic"))
            .icon { ItemStack(InitBlocks.MUSIC_PLAYER.get()) }
            .displayItems { parameters: ItemDisplayParameters?, output: CreativeModeTab.Output ->
                output.accept(ItemStack(MUSIC_PLAYER.get()))
                output.accept(ItemStack(CD_BURNER.get()))
                output.accept(ItemStack(COMPUTER.get()))
                output.accept(ItemStack(MUSIC_CD.get()))
                for (info in MusicListManage.SONGS) {
                    val stack =
                        ItemStack(MUSIC_CD.get())
                    setSongInfo(info!!, stack)
                    output.accept(stack)
                }
            }
            .build()
    }
}