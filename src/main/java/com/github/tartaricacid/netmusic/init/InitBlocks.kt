package com.github.tartaricacid.netmusic.init

import com.github.tartaricacid.netmusic.NetMusic
import com.github.tartaricacid.netmusic.block.BlockCDBurner
import com.github.tartaricacid.netmusic.block.BlockComputer
import com.github.tartaricacid.netmusic.block.BlockMusicPlayer
import com.github.tartaricacid.netmusic.tileentity.TileEntityMusicPlayer
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.ForgeRegistries
import net.minecraftforge.registries.RegistryObject
import java.util.function.Supplier

object InitBlocks {

    @JvmField
    val BLOCKS: DeferredRegister<Block> = DeferredRegister.create(ForgeRegistries.BLOCKS, NetMusic.MOD_ID)

    @JvmField
    val TILE_ENTITIES: DeferredRegister<BlockEntityType<*>> =
        DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, NetMusic.MOD_ID)

    @JvmField
    var MUSIC_PLAYER: RegistryObject<Block> = BLOCKS.register<Block>("music_player", ::BlockMusicPlayer)

    @JvmField
    var CD_BURNER: RegistryObject<Block> = BLOCKS.register<Block>("cd_burner", ::BlockCDBurner)

    @JvmField
    var COMPUTER: RegistryObject<Block> = BLOCKS.register<Block>("computer", ::BlockComputer)

    @JvmField
    var MUSIC_PLAYER_TE: RegistryObject<BlockEntityType<TileEntityMusicPlayer>> = TILE_ENTITIES.register(
        "music_player"
    ) { TileEntityMusicPlayer.TYPE }
}