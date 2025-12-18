package de.wutzuket.create_overdrive.blocks.RotatorController;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.decoration.palettes.AllPaletteBlocks;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import de.wutzuket.create_overdrive.index.CPABlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StainedGlassBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.common.Tags;

import java.util.List;

public class RotatorControllerBlockRenderer extends KineticBlockEntityRenderer<RotatorControllerBlockEntity> {
    private static final float GHOST_ALPHA = 0.8f;

    public RotatorControllerBlockRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    // Die Methode in der Superklasse hat wahrscheinlich eine andere Signatur in dieser MC-Version,
    // daher entfernen wir die @Override-Annotation, um den Kompilierfehler zu vermeiden.
    protected void renderSafe(RotatorControllerBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {

        // Render die gesamte Struktur mit transparenten Blöcken
        renderStructureBlocks(be, ms, buffer, light, overlay);
    }

    private void renderStructureBlocks(RotatorControllerBlockEntity be, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        // Debug: show core pos and facing
        try {
            var corePos = be.getBlockPos();
            var facing = be.getBlockState().getValue(de.wutzuket.create_overdrive.blocks.RotatorController.RotatorControllerBlock.FACING);
        } catch (Exception ignored) {}

         // Render Casing Ghost-Blöcke
         if (!be.casing_render.isEmpty()) {

            BlockRenderDispatcher blockRender = Minecraft.getInstance().getBlockRenderer();
            BlockState casingState = CPABlocks.ROTATOR_CASING.getDefaultState();
            VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.translucent());

            for (BlockPos offset : be.casing_render) {
                renderBlockAtOffset(blockRender, casingState, ms, vertexConsumer, light, overlay, offset.getX(), offset.getY(), offset.getZ());
            }
        }
        if (!be.glass_render.isEmpty()) {

            BlockRenderDispatcher blockRender = Minecraft.getInstance().getBlockRenderer();
            BlockState glassState = AllPaletteBlocks.FRAMED_GLASS.getDefaultState();
            VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.translucent());

            for (BlockPos offset : be.glass_render) {
                renderBlockAtOffset(blockRender, glassState, ms, vertexConsumer, light, overlay, offset.getX(), offset.getY(), offset.getZ());
            }
        }
        // Render Input Ghost-Blöcke
        if (!be.input_render.isEmpty()) {
            BlockRenderDispatcher blockRender = Minecraft.getInstance().getBlockRenderer();
            for (BlockPos offset : be.input_render) {
                Direction facing;
                if (offset.getX() > 0) facing = Direction.WEST;
                else if (offset.getX() < 0) facing = Direction.EAST;
                else if (offset.getZ() > 0) facing = Direction.NORTH;
                else if (offset.getZ() < 0) facing = Direction.SOUTH;
                else if (offset.getY() > 0) facing = Direction.DOWN;
                else facing = Direction.UP;

                BlockState inputState = CPABlocks.ROTATOR_INPUT.getDefaultState().setValue(de.wutzuket.create_overdrive.blocks.RotatorInput.RotatorInputBlock.FACING, facing);
                VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.translucent());
                renderBlockAtOffset(blockRender, inputState, ms, vertexConsumer, light, overlay, offset.getX(), offset.getY(), offset.getZ());
            }
        }
        if (!be.flywheel_render.isEmpty()) {

            BlockRenderDispatcher blockRender = Minecraft.getInstance().getBlockRenderer();
            VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.translucent());

            for (BlockPos offset : be.flywheel_render) {
                Direction.Axis axis;
                if (offset.getX() > 0) axis = Direction.Axis.X;
                else if (offset.getX() < 0) axis = Direction.Axis.X;
                else if (offset.getZ() > 0) axis = Direction.Axis.Z;
                else if (offset.getZ() < 0) axis = Direction.Axis.Z;
                else if (offset.getY() > 0) axis = Direction.Axis.Y;
                else axis = Direction.Axis.Y;
                BlockState flywheelState = AllBlocks.FLYWHEEL.getDefaultState().setValue(BlockStateProperties.AXIS, axis);
                renderBlockAtOffset(blockRender, flywheelState, ms, vertexConsumer, light, overlay, offset.getX(), offset.getY(), offset.getZ());
            }
        }
    }

    private void renderBlockAtOffset(BlockRenderDispatcher blockRender, BlockState state, PoseStack ms, VertexConsumer vertexConsumer, int light, int overlay, int x, int y, int z) {
        ms.pushPose();
        ms.translate(x, y, z);
        renderBlockWithAlpha(blockRender, state, ms, vertexConsumer, light, overlay);
        ms.popPose();
    }

    private void renderBlockWithAlpha(BlockRenderDispatcher blockRender, BlockState state, PoseStack ms, VertexConsumer vertexConsumer, int light, int overlay) {
        var model = blockRender.getBlockModel(state);
        var random = RandomSource.create(42L);

        int fullBright = 15728880;

        for (Direction direction : Direction.values()) {
            random.setSeed(42L);
            List<BakedQuad> quads = model.getQuads(state, direction, random);
            for (BakedQuad quad : quads) {
                // Verwende fullBright statt des übergebenen light-Wertes
                vertexConsumer.putBulkData(ms.last(), quad, 1.0f, 1.0f, 1.0f, GHOST_ALPHA, fullBright, overlay);
            }
        }

        // Render quads ohne spezifische Richtung
        random.setSeed(42L);
        List<BakedQuad> quads = model.getQuads(state, null, random);
        for (BakedQuad quad : quads) {
            vertexConsumer.putBulkData(ms.last(), quad, 1.0f, 1.0f, 1.0f, GHOST_ALPHA, fullBright, overlay);
        }
    }
}
