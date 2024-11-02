package com.github.tartaricacid.netmusic.block

import com.github.tartaricacid.netmusic.item.ItemMusicCD.Companion.getSongInfo
import com.github.tartaricacid.netmusic.tileentity.TileEntityMusicPlayer
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.particle.ParticleEngine
import net.minecraft.client.particle.TerrainParticle
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.*
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.VoxelShape
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import net.minecraftforge.client.extensions.common.IClientBlockExtensions
import net.minecraftforge.items.IItemHandler
import java.util.function.Consumer

class BlockMusicPlayer :
    HorizontalDirectionalBlock(Properties.of().sound(SoundType.WOOD).strength(0.5f).noOcclusion()),
    EntityBlock {

    init {
        this.registerDefaultState(stateDefinition.any().setValue(FACING, Direction.SOUTH))
    }

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity {
        return TileEntityMusicPlayer(pos, state)
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(FACING)
    }

    override fun getStateForPlacement(context: BlockPlaceContext): BlockState? {
        val direction = context.horizontalDirection.opposite
        return defaultBlockState().setValue(FACING, direction)
    }

    @Deprecated("Deprecated in Java", ReplaceWith("true"))
    override fun hasAnalogOutputSignal(blockState: BlockState): Boolean {
        return true
    }

    @Deprecated("Deprecated in Java")
    override fun getAnalogOutputSignal(state: BlockState, level: Level, blockPos: BlockPos): Int {
        val blockEntity = level.getBlockEntity(blockPos)
        if (blockEntity is TileEntityMusicPlayer) {
            val stackInSlot = blockEntity.playerInv.getStackInSlot(0)
            if (!stackInSlot.isEmpty) {
                if (blockEntity.isPlay) {
                    return 15
                }

                return 7
            }
        }
        return 0
    }

    @Deprecated("Deprecated in Java")
    override fun neighborChanged(
        state: BlockState,
        level: Level,
        blockPos: BlockPos,
        block: Block,
        fromPos: BlockPos,
        isMoving: Boolean
    ) {
        playerMusic(level, blockPos, level.hasNeighborSignal(blockPos))
    }

    @Deprecated("Deprecated in Java")
    override fun use(
        state: BlockState,
        worldIn: Level,
        pos: BlockPos,
        playerIn: Player,
        hand: InteractionHand,
        hit: BlockHitResult
    ): InteractionResult {
        if (hand == InteractionHand.OFF_HAND) {
            return InteractionResult.PASS
        }

        val te = worldIn.getBlockEntity(pos) as? TileEntityMusicPlayer ?: return InteractionResult.PASS

        val musicPlayer = te
        val handler: IItemHandler = musicPlayer.playerInv
        if (!handler.getStackInSlot(0).isEmpty) {
            val extract = handler.extractItem(0, 1, false)
            popResource(worldIn, pos, extract)
            return InteractionResult.SUCCESS
        }

        val stack = playerIn.mainHandItem
        val info = getSongInfo(stack) ?: return InteractionResult.PASS
        if (info.vip) {
            if (worldIn.isClientSide) {
                playerIn.sendSystemMessage(
                    Component.translatable("message.netmusic.music_player.need_vip").withStyle(ChatFormatting.RED)
                )
            }
            return InteractionResult.FAIL
        }

        handler.insertItem(0, stack.copy(), false)
        if (!playerIn.isCreative) {
            stack.shrink(1)
        }
        musicPlayer.setPlayToClient(info)
        musicPlayer.markDirty()
        return InteractionResult.SUCCESS
    }

    @Deprecated("Deprecated in Java")
    override fun onRemove(state: BlockState, worldIn: Level, pos: BlockPos, newState: BlockState, isMoving: Boolean) {
        val te = worldIn.getBlockEntity(pos)
        if (te is TileEntityMusicPlayer) {
            val stack = te.playerInv.getStackInSlot(0)
            if (!stack.isEmpty) {
                popResource(worldIn, pos, stack)
            }
        }
        super.onRemove(state, worldIn, pos, newState, isMoving)
    }

    override fun <T : BlockEntity?> getTicker(
        level: Level,
        blockState: BlockState,
        entityType: BlockEntityType<T>
    ): BlockEntityTicker<T>? {
        return if (!level.isClientSide) createTickerHelper(
            entityType, TileEntityMusicPlayer.TYPE
        ) { level: Level?, blockPos: BlockPos?, blockState: BlockState?, te: TileEntityMusicPlayer? ->
            TileEntityMusicPlayer.tick(
                level,
                blockPos,
                blockState,
                te
            )
        } else null
    }

    @Deprecated("Deprecated in Java")
    override fun getShape(
        state: BlockState,
        worldIn: BlockGetter,
        pos: BlockPos,
        context: CollisionContext
    ): VoxelShape {
        return BLOCK_AABB
    }

    @Deprecated("Deprecated in Java")
    override fun getRenderShape(state: BlockState): RenderShape {
        return RenderShape.ENTITYBLOCK_ANIMATED
    }

    override fun initializeClient(consumer: Consumer<IClientBlockExtensions>) {
        consumer.accept(object : IClientBlockExtensions {
            override fun addHitEffects(
                state: BlockState,
                world: Level,
                target: HitResult,
                manager: ParticleEngine
            ): Boolean {
                if (target is BlockHitResult && world is ClientLevel) {
                    val pos = target.blockPos
                    this.crack(world, pos, Blocks.ACACIA_WOOD.defaultBlockState(), target.direction)
                }
                return true
            }

            override fun addDestroyEffects(
                state: BlockState,
                world: Level,
                pos: BlockPos,
                manager: ParticleEngine
            ): Boolean {
                Minecraft.getInstance().particleEngine.destroy(pos, Blocks.ACACIA_WOOD.defaultBlockState())
                return true
            }

            @OnlyIn(Dist.CLIENT)
            fun crack(world: ClientLevel, pos: BlockPos, state: BlockState, side: Direction) {
                if (state.renderShape != RenderShape.INVISIBLE) {
                    val posX = pos.x
                    val posY = pos.y
                    val posZ = pos.z
                    val aabb = state.getShape(world, pos).bounds()
                    var x = posX + world.random.nextDouble() * (aabb.maxX - aabb.minX - 0.2) + 0.1 + aabb.minX
                    var y = posY + world.random.nextDouble() * (aabb.maxY - aabb.minY - 0.2) + 0.1 + aabb.minY
                    var z = posZ + world.random.nextDouble() * (aabb.maxZ - aabb.minZ - 0.2) + 0.1 + aabb.minZ
                    if (side == Direction.DOWN) {
                        y = posY + aabb.minY - 0.1
                    }
                    if (side == Direction.UP) {
                        y = posY + aabb.maxY + 0.1
                    }
                    if (side == Direction.NORTH) {
                        z = posZ + aabb.minZ - 0.1
                    }
                    if (side == Direction.SOUTH) {
                        z = posZ + aabb.maxZ + 0.1
                    }
                    if (side == Direction.WEST) {
                        x = posX + aabb.minX - 0.1
                    }
                    if (side == Direction.EAST) {
                        x = posX + aabb.maxX + 0.1
                    }
                    val diggingParticle = TerrainParticle(world, x, y, z, 0.0, 0.0, 0.0, state)
                    Minecraft.getInstance().particleEngine.add(
                        diggingParticle.updateSprite(state, pos).setPower(0.2f).scale(0.6f)
                    )
                }
            }
        })
    }

    companion object {
        protected val BLOCK_AABB: VoxelShape = box(2.0, 0.0, 2.0, 14.0, 6.0, 14.0)

        private fun playerMusic(level: Level, blockPos: BlockPos, signal: Boolean) {
            val blockEntity = level.getBlockEntity(blockPos)
            if (blockEntity is TileEntityMusicPlayer) {
                if (signal != blockEntity.hasSignal()) {
                    if (signal) {
                        if (blockEntity.isPlay) {
                            blockEntity.isPlay = false
                            blockEntity.setSignal(signal)
                            blockEntity.markDirty()
                            return
                        }
                        val stackInSlot = blockEntity.playerInv.getStackInSlot(0)
                        if (stackInSlot.isEmpty) {
                            blockEntity.setSignal(signal)
                            blockEntity.markDirty()
                            return
                        }

                        val songInfo = getSongInfo(stackInSlot)
                        if (songInfo != null) {
                            blockEntity.setPlayToClient(songInfo)
                        }
                    }
                    blockEntity.setSignal(signal)
                    blockEntity.markDirty()
                }
            }
        }

        protected fun <E : BlockEntity?, A : BlockEntity?> createTickerHelper(
            entityType: BlockEntityType<A>,
            type: BlockEntityType<E>,
            ticker: BlockEntityTicker<in E>?
        ): BlockEntityTicker<A>? {
            return if (type === entityType) ticker as BlockEntityTicker<A>? else null
        }
    }
}