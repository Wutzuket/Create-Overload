package de.wutzuket.create_overdrive.blocks.FusionReactor;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.SingleAxisRotatingVisual;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import java.util.function.Consumer;
import net.minecraft.core.Direction;

public class FusionReactorCoreVisual extends SingleAxisRotatingVisual<FusionReactorCoreBlockEntity> implements SimpleDynamicVisual {

    public FusionReactorCoreVisual(VisualizationContext context, FusionReactorCoreBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick, Models.partial(AllPartialModels.SHAFT_HALF, Direction.UP));
    }

    @Override
    public void beginFrame(DynamicVisual.Context ctx) {
        this.animate(ctx.partialTick());
    }

    private void animate(float pt) {
        // Animation logic for fusion reactor core
    }

    @Override
    public void updateLight(float partialTick) {
        super.updateLight(partialTick);
    }

    @Override
    protected void _delete() {
        super._delete();
    }

    @Override
    public void collectCrumblingInstances(Consumer<Instance> consumer) {
        super.collectCrumblingInstances(consumer);
    }
}

