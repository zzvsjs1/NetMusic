package com.github.tartaricacid.netmusic.init

import com.github.tartaricacid.netmusic.NetMusic
import com.github.tartaricacid.netmusic.inventory.CDBurnerMenu
import com.github.tartaricacid.netmusic.inventory.ComputerMenu
import net.minecraft.world.inventory.MenuType
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.ForgeRegistries
import net.minecraftforge.registries.RegistryObject

object InitContainer {
    @JvmStatic
    val CONTAINER_TYPE: DeferredRegister<MenuType<*>> =
        DeferredRegister.create(ForgeRegistries.MENU_TYPES, NetMusic.MOD_ID)

    @JvmStatic
    val CD_BURNER_CONTAINER: RegistryObject<MenuType<CDBurnerMenu>> = CONTAINER_TYPE.register(
        "cd_burner"
    ) { CDBurnerMenu.TYPE }

    @JvmStatic
    val COMPUTER_CONTAINER: RegistryObject<MenuType<ComputerMenu>> = CONTAINER_TYPE.register(
        "computer"
    ) { ComputerMenu.TYPE }
}