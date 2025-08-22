package de.wutzuket.create_overdrive.blocks.Ionator;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.SingleAxisRotatingVisual;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;

import java.util.function.Consumer;

public class IonatorVisual extends SingleAxisRotatingVisual<IonatorBlockEntity> implements SimpleDynamicVisual {

    private final IonatorBlockEntity Ionator;

    public IonatorVisual(VisualizationContext context, IonatorBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick, Models.partial(AllPartialModels.SHAFTLESS_COGWHEEL));
        this.Ionator = blockEntity;

        animate(partialTick);
    }

    @Override
    public void beginFrame(DynamicVisual.Context ctx) {
        animate(ctx.partialTick());
    }

    private void animate(float pt) {
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
