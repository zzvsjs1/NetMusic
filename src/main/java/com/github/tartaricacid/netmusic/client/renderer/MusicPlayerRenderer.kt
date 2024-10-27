package com.github.tartaricacid.netmusic.client.renderer

import com.github.tartaricacid.netmusic.NetMusic
import com.github.tartaricacid.netmusic.client.model.ModelMusicPlayer
import com.github.tartaricacid.netmusic.tileentity.TileEntityMusicPlayer
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.core.Direction
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.block.HorizontalDirectionalBlock

class MusicPlayerRenderer(
    dispatcher: BlockEntityRendererProvider.Context
) : BlockEntityRenderer<TileEntityMusicPlayer> {

    companion object {
        lateinit var MODEL: ModelMusicPlayer<*>
        val TEXTURE: ResourceLocation = ResourceLocation(NetMusic.MOD_ID, "textures/block/music_player.png")
        lateinit var instance: MusicPlayerRenderer
    }

    init {
        MODEL = ModelMusicPlayer<Entity>(dispatcher.bakeLayer(ModelMusicPlayer.LAYER))
        instance = this
    }

    override fun render(
        te: TileEntityMusicPlayer,
        pPartialTicks: Float,
        matrixStack: PoseStack,
        buffer: MultiBufferSource,
        combinedLight: Int,
        combinedOverlay: Int
    ) {
        val facing = te.blockState.getValue(HorizontalDirectionalBlock.FACING)
        val cd = te.playerInv.getStackInSlot(0)
        val disc = MODEL.discBone
        disc.visible = !cd.isEmpty
        if (!cd.isEmpty && te.isPlay) {
            disc.yRot = ((2 * Math.PI / 40) * ((System.currentTimeMillis().toDouble() / 50) % 40)).toFloat()
        }
        renderMusicPlayer(matrixStack, buffer, combinedLight, facing)
    }

    fun renderMusicPlayer(matrixStack: PoseStack, buffer: MultiBufferSource, combinedLight: Int, facing: Direction) {
        matrixStack.pushPose()
        matrixStack.scale(0.75f, 0.75f, 0.75f)
        matrixStack.translate(0.5 / 0.75, 1.5, 0.5 / 0.75)
        when (facing) {
            Direction.SOUTH -> matrixStack.mulPose(Axis.YP.rotationDegrees(180f))
            Direction.EAST -> matrixStack.mulPose(Axis.YP.rotationDegrees(270f))
            Direction.WEST -> matrixStack.mulPose(Axis.YP.rotationDegrees(90f))
            Direction.NORTH -> {}
            else -> {}
        }

        matrixStack.mulPose(Axis.ZP.rotationDegrees(180f))
        val vertexBuilder = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE))
        MODEL.renderToBuffer(matrixStack, vertexBuilder, combinedLight, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 1f)
        matrixStack.popPose()
    }

}