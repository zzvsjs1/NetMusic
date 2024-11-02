package com.github.tartaricacid.netmusic.block

import com.github.tartaricacid.netmusic.inventory.ComputerMenu
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
import net.minecraft.world.phys.shapes.BooleanOp
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape

class BlockComputer
    : HorizontalDirectionalBlock(
    Properties.of().sound(SoundType.WOOD).strength(0.5f).noOcclusion()
) {

    companion object {
        private val NORTH_AABB: VoxelShape = makeShape()
        private val SOUTH_AABB: VoxelShape = rotateShape(Direction.SOUTH, Direction.NORTH, NORTH_AABB)
        private val EAST_AABB: VoxelShape = rotateShape(Direction.SOUTH, Direction.EAST, NORTH_AABB)
        private val WEST_AABB: VoxelShape = rotateShape(Direction.NORTH, Direction.EAST, NORTH_AABB)

        private fun makeShape(): VoxelShape {
            var shape = Shapes.empty()
            shape = Shapes.join(shape, Shapes.box(0.0, 0.0, 0.40625, 1.0, 0.3125, 1.0), BooleanOp.OR)
            shape = Shapes.join(shape, Shapes.box(0.1875, 0.3125, 0.40625, 0.8125, 0.375, 0.875), BooleanOp.OR)
            shape = Shapes.join(shape, Shapes.box(0.125, 0.375, 0.53125, 0.875, 0.84375, 0.9375), BooleanOp.OR)
            shape = Shapes.join(
                shape,
                Shapes.box(0.1250625, 0.5608175, 0.47502125, 0.8749375, 0.9356925, 0.88114625),
                BooleanOp.OR
            )
            shape = Shapes.join(shape, Shapes.box(0.1875, 0.4375, 0.40625, 0.8125, 0.9375, 0.59375), BooleanOp.OR)
            shape = Shapes.join(shape, Shapes.box(0.0625, 0.3125, 0.34375, 0.9375, 0.4375, 0.59375), BooleanOp.OR)
            shape = Shapes.join(shape, Shapes.box(0.0625, 0.9375, 0.34375, 0.9375, 1.0625, 0.59375), BooleanOp.OR)
            shape = Shapes.join(shape, Shapes.box(0.8125, 0.4375, 0.34375, 0.9375, 0.9375, 0.59375), BooleanOp.OR)
            shape = Shapes.join(shape, Shapes.box(0.0625, 0.4375, 0.34375, 0.1875, 0.9375, 0.59375), BooleanOp.OR)
            shape = Shapes.join(shape, Shapes.box(0.0, 0.0625, 0.03125, 1.0, 0.1875, 0.375), BooleanOp.OR)
            return shape
        }

        private fun rotateShape(from: Direction, to: Direction, shape: VoxelShape): VoxelShape {
            val buffer = arrayOf(shape, Shapes.empty())
            val times = (to.ordinal - from.get2DDataValue() + 4) % 4
            for (i in 0 until times) {
                buffer[0].forAllBoxes { minX: Double, minY: Double, minZ: Double, maxX: Double, maxY: Double, maxZ: Double ->
                    buffer[1] = Shapes.or(
                        buffer[1], Shapes.create(1 - maxZ, minY, minX, 1 - minZ, maxY, maxX)
                    )
                }
                buffer[0] = buffer[1]
                buffer[1] = Shapes.empty()
            }

            return buffer[0]
        }
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

    @Deprecated("Deprecated in Java")
    override fun getShape(
        state: BlockState,
        worldIn: BlockGetter,
        pos: BlockPos,
        context: CollisionContext
    ): VoxelShape {
        val direction = state.getValue(FACING)
        return when (direction) {
            Direction.SOUTH -> {
                SOUTH_AABB
            }

            Direction.EAST -> {
                EAST_AABB
            }

            Direction.WEST -> {
                WEST_AABB
            }

            else -> {
                NORTH_AABB
            }
        }
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
        return SimpleMenuProvider(
            { id: Int, inventory: Inventory?, _: Player? -> ComputerMenu(id, inventory) },
            Component.literal("computer")
        )
    }

    override fun appendHoverText(
        stack: ItemStack,
        level: BlockGetter?,
        tooltip: MutableList<Component>,
        flag: TooltipFlag
    ) {
        tooltip.add(Component.translatable("block.netmusic.computer.web_link.desc").withStyle(ChatFormatting.GRAY))
        tooltip.add(Component.translatable("block.netmusic.computer.local_file.desc").withStyle(ChatFormatting.GRAY))
    }

}