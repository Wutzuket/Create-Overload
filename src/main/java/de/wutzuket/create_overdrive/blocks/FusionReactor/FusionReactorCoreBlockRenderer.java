package de.wutzuket.create_overdrive.blocks.FusionReactor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

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

