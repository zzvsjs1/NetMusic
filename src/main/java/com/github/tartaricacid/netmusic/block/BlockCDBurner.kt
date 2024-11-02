package com.github.tartaricacid.netmusic.block

import com.github.tartaricacid.netmusic.inventory.CDBurnerMenu
import net.minecraft.ChatFormatting
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.MenuProvider
import net.minecraft.world.SimpleMenuProvider
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.HorizontalDirectionalBlock
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.VoxelShape

class BlockCDBurner
    : HorizontalDirectionalBlock(
    Properties.of().sound(SoundType.WOOD).strength(0.5f).noOcclusion()
) {

    companion object {
        private val BLOCK_AABB: VoxelShape = box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0)
    }

    init {
        this.registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH))
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(FACING)
    }

    override fun getStateForPlacement(context: BlockPlaceContext): BlockState? {
        val direction = context.horizontalDirection.opposite
        return defaultBlockState().setValue(FACING, direction)
    }

    @Deprecated(
        "Deprecated in Java", ReplaceWith(
            "BLOCK_AABB",
            "com.github.tartaricacid.netmusic.block.BlockCDBurner.Companion.BLOCK_AABB"
        )
    )
    override fun getShape(
        state: BlockState,
        worldIn: BlockGetter,
        pos: BlockPos,
        context: CollisionContext
    ): VoxelShape {
        return BLOCK_AABB
    }

    @Deprecated("Deprecated in Java")
    override fun use(
        blockState: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        hand: InteractionHand,
        hitResult: BlockHitResult
    ): InteractionResult {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS
        }

        player.openMenu(blockState.getMenuProvider(level, pos))
        return InteractionResult.CONSUME
    }

    @Deprecated("Deprecated in Java")
    override fun getMenuProvider(blockState: BlockState, level: Level, blockPos: BlockPos): MenuProvider {
        return SimpleMenuProvider({ id: Int, inventory: Inventory?, _: Player? ->
            CDBurnerMenu(
                id,
                inventory!!
            )
        }, Component.literal("cd_burner"))
    }

    override fun appendHoverText(
        stack: ItemStack,
        level: BlockGetter?,
        tooltip: MutableList<Component>,
        flag: TooltipFlag
    ) {
        tooltip.add(Component.translatable("block.netmusic.cd_burner.desc").withStyle(ChatFormatting.GRAY))
    }

}