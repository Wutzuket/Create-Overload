package de.wutzuket.create_overdrive.blocks.FusionReactor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import de.wutzuket.create_overdrive.index.CPABlocks;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.RandomSource;

import java.util.List;

public class FusionReactorCoreBlockRenderer extends KineticBlockEntityRenderer<FusionReactorCoreBlockEntity> {
    public FusionReactorCoreBlockRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(FusionReactorCoreBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {

        if (!VisualizationManager.supportsVisualization(be.getLevel())) {
            BlockState blockState = be.getBlockState();
            VertexConsumer vb = buffer.getBuffer(RenderType.solid());
            SuperByteBuffer superBuffer = CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, blockState, Direction.UP);
            standardKineticRotationTransform(superBuffer, be, light).renderInto(ms, vb);
        }

        // Render die gesamte Struktur mit transparenten Blöcken
        renderStructureBlocks(be, ms, buffer, light, overlay);
    }

    private void renderStructureBlocks(FusionReactorCoreBlockEntity be, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        // Render Casing Ghost-Blöcke
        if (!be.casing_render.isEmpty()) {

            BlockRenderDispatcher blockRender = Minecraft.getInstance().getBlockRenderer();
            BlockState casingState = CPABlocks.FUSION_REACTOR_CASING.getDefaultState();
            VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.translucent());

            for (BlockPos offset : be.casing_render) {
                renderBlockAtOffset(blockRender, casingState, ms, vertexConsumer, light, overlay, offset.getX(), offset.getY(), offset.getZ());
            }
        }

        // Render Input Ghost-Blöcke
        if (!be.input_render.isEmpty()) {

            BlockRenderDispatcher blockRender = Minecraft.getInstance().getBlockRenderer();
            BlockState inputState = CPABlocks.FUSION_REACTOR_INPUT.getDefaultState();
            VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.translucent());

            for (BlockPos offset : be.input_render) {
                renderBlockAtOffset(blockRender, inputState, ms, vertexConsumer, light, overlay, offset.getX(), offset.getY(), offset.getZ());
            }
        }
    }

    private void renderBlockAtOffset(BlockRenderDispatcher blockRender, BlockState state, PoseStack ms, VertexConsumer vertexConsumer, int light, int overlay, int x, int y, int z) {
        ms.pushPose();
        ms.translate(x, y, z);
        renderBlockWithAlpha(blockRender, state, ms, vertexConsumer, light, overlay); // Erhöhe Alpha für bessere Sichtbarkeit
        ms.popPose();
    }

    private void renderBlockWithAlpha(BlockRenderDispatcher blockRender, BlockState state, PoseStack ms, VertexConsumer vertexConsumer, int light, int overlay) {
        var model = blockRender.getBlockModel(state);
        var random = RandomSource.create(42L);

        for (Direction direction : Direction.values()) {
            random.setSeed(42L);
            List<BakedQuad> quads = model.getQuads(state, direction, random);
            for (BakedQuad quad : quads) {
                vertexConsumer.putBulkData(ms.last(), quad, 1.0f, 1.0f, 1.0f, (float) 0.8, light, overlay);
            }
        }

        // Render quads without specific direction
        random.setSeed(42L);
        List<BakedQuad> quads = model.getQuads(state, null, random);
        for (BakedQuad quad : quads) {
            vertexConsumer.putBulkData(ms.last(), quad, 1.0f, 1.0f, 1.0f, (float) 0.8, light, overlay);
        }
    }

    @Override
    protected BlockState getRenderedBlockState(FusionReactorCoreBlockEntity be) {
        return shaft(Direction.Axis.Y);
    }

    @Override
    protected SuperByteBuffer getRotatedModel(FusionReactorCoreBlockEntity be, BlockState state) {
        return CachedBuffers.partialFacingVertical(AllPartialModels.SHAFT_HALF, state, Direction.UP);
    }
}
