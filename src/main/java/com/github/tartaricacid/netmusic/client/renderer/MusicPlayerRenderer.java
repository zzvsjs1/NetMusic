package com.github.tartaricacid.netmusic.client.renderer;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.client.model.ModelMusicPlayer;
import com.github.tartaricacid.netmusic.tileentity.TileEntityMusicPlayer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import org.jetbrains.annotations.NotNull;

public class MusicPlayerRenderer implements BlockEntityRenderer<TileEntityMusicPlayer> {
    public static ModelMusicPlayer<?> MODEL;
    public static final ResourceLocation TEXTURE = new ResourceLocation(NetMusic.MOD_ID, "textures/block/music_player.png");
    public static MusicPlayerRenderer instance;

    public MusicPlayerRenderer(BlockEntityRendererProvider.@NotNull Context dispatcher) {
        MODEL = new ModelMusicPlayer<>(dispatcher.bakeLayer(ModelMusicPlayer.LAYER));
        instance = this;
    }

    @Override
    public void render(TileEntityMusicPlayer te, float pPartialTicks, @NotNull PoseStack matrixStack, @NotNull MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        Direction facing = te.getBlockState().getValue(HorizontalDirectionalBlock.FACING);
        ItemStack cd = te.getPlayerInv().getStackInSlot(0);
        ModelPart disc = MODEL.getDiscBone();
        disc.visible = !cd.isEmpty();
        if (!cd.isEmpty() && te.isPlay()) {
            disc.yRot = (float) ((2 * Math.PI / 40) * (((double) System.currentTimeMillis() / 50) % 40));
        }
        renderMusicPlayer(matrixStack, buffer, combinedLight, facing);
    }

    public void renderMusicPlayer(PoseStack matrixStack, MultiBufferSource buffer, int combinedLight, Direction facing) {
        matrixStack.pushPose();
        matrixStack.scale(0.75f, 0.75f, 0.75f);
        matrixStack.translate(0.5 / 0.75, 1.5, 0.5 / 0.75);
        switch (facing) {
            case SOUTH:
                matrixStack.mulPose(Axis.YP.rotationDegrees(180));
                break;
            case EAST:
                matrixStack.mulPose(Axis.YP.rotationDegrees(270));
                break;
            case WEST:
                matrixStack.mulPose(Axis.YP.rotationDegrees(90));
                break;
            case NORTH:
            default:
                break;
        }

        matrixStack.mulPose(Axis.ZP.rotationDegrees(180));
        VertexConsumer vertexBuilder = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));
        MODEL.renderToBuffer(matrixStack, vertexBuilder, combinedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
        matrixStack.popPose();
    }
}
