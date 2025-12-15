package de.wutzuket.create_overdrive.blocks.RotatorController;

import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.CenteredSideValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import de.wutzuket.create_overdrive.util.StressScrollValueBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class RotatorControllerBlockEntity extends GeneratingKineticBlockEntity {

    public int stress;

    protected ScrollValueBehaviour StressCapacity;

    protected int stressMax = 100;

    protected int multiblockSize = 10;

    private boolean first = true;

    public RotatorControllerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public int getMultiblockSize() {
        return multiblockSize;
    }

    public void setMultiblockSize(int size) {
        int s = Math.max(0, size);
        if (this.multiblockSize == s) return;
        this.multiblockSize = s;
        updateStressMaxFromStructureSize(this.multiblockSize);
    }

    public int getStressMax() {
        return stressMax;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        CenteredSideValueBoxTransform StressSlot =
                new CenteredSideValueBoxTransform((reactor, side) -> side != Direction.DOWN && side != Direction.UP);

        StressCapacity = new StressScrollValueBehaviour(Component.translatable("create_overdrive.gui.power.stress"), this, StressSlot);
        StressCapacity.between(0, stressMax);
        StressCapacity.value = 50;
        StressCapacity.withCallback(this::updateStress);
        behaviours.add(StressCapacity);
    }

    @Override
    public void tick() {
        super.tick();
        if (first && StressCapacity != null) {
            this.stress = StressCapacity.getValue();
            updateGeneratedRotation();
            first = false;
        }
        updateStressMaxFromStructureSize(this.multiblockSize);
    }

    protected void updateStress(int stresss) {
        stress = stresss;
    }

    public void updateStressMaxFromStructureSize(int multiblockSize) {
        // Lineare Skalierung: pro size +10_000, start bei 0
        long size = Math.max(0, multiblockSize);
        long candidate = size * 10_000L;
        int newMax;
        if (candidate > Integer.MAX_VALUE) {
            newMax = Integer.MAX_VALUE;
        } else {
            newMax = (int) candidate;
        }
        setStressMax(newMax);
    }

    public void setStressMax(int newMax) {
        // Erlaube jetzt auch 0 als Mindestwert (size 0 => 0)
        stressMax = Math.max(0, newMax);
        if (StressCapacity != null) {
            StressCapacity.between(0, stressMax);
            if (stress > stressMax) stress = stressMax;
            StressCapacity.setValue(stress);
        }
        setChanged();
        sendData();
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        if (compound.contains("MultiblockSize")) {
            this.multiblockSize = compound.getInt("MultiblockSize");
            // Immediately adjust stressMax to reflect loaded size
            updateStressMaxFromStructureSize(this.multiblockSize);
        }
        if (compound.contains("Stress")) {
            this.stress = compound.getInt("Stress");
        }
        // If behaviour already exists (client side), update displayed value
        if (StressCapacity != null)
            StressCapacity.setValue(this.stress);
    }

    @Override
    public void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        compound.putInt("MultiblockSize", this.multiblockSize);
        compound.putInt("Stress", this.stress);
        super.write(compound, registries, clientPacket);
    }



}
