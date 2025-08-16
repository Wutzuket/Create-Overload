package de.wutzuket.create_overdrive.blocks.FusionReactor;

import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.CenteredSideValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;

import de.wutzuket.create_overdrive.util.StressScrollValueBehaviour;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;


public class FusionReactorCoreBlockEntity extends GeneratingKineticBlockEntity {

    protected float burnrate;
    protected ScrollValueBehaviour burnrateCapacity;
    protected Fluid requiredFluid = Fluids.WATER; // Variable für gewünschten Fluid-Typ

    private boolean active = false;
    private boolean wasJustAssembled = false;
    private boolean first = true;

    private FusionReactorStructure structure;

    public FusionReactorCoreBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        setLazyTickRate(20);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);

        CenteredSideValueBoxTransform burnrateSlot =
            new CenteredSideValueBoxTransform((reactor, side) -> side == Direction.DOWN);

        burnrateCapacity = new StressScrollValueBehaviour(Component.translatable("create_overdrive.gui.burnrate.title"), this, burnrateSlot);
        burnrateCapacity.between(0, 100);
        burnrateCapacity.value = 50;
        burnrateCapacity.withCallback(this::updateBurnrate);
        behaviours.add(burnrateCapacity);
    }

    @Override
    public float getGeneratedSpeed() {
        return (active && burnrate > 0) ? 32 : 0; // Nur drehen wenn aktiv und Burnrate > 0
    }

    public float calculateAddedStressCapacity() {
        // Stress berechnet sich aus burnrate * 0.01 * 2^20, durch 32 geteilt
        float maxStress = (float) Math.pow(2, 20);
        float capacity = (active && burnrate > 0) ? (burnrate * 0.01f * maxStress / 32f) : 0;
        this.lastCapacityProvided = capacity;
        return capacity;
    }

    public void updateBurnrate(int rate) {
        burnrate = rate;
        updateGeneratedRotation();
    }

    @Override
    public void initialize() {
        super.initialize();
        if (!hasSource() || getGeneratedSpeed() > getTheoreticalSpeed())
            updateGeneratedRotation();
    }

    @Override
    protected Block getStressConfigKey() {
        return AllBlocks.WATER_WHEEL.get();
    }

    public int getGeneratedStress() {
        return (int) calculateAddedStressCapacity();
    }

    @Override
    public void write(CompoundTag tag, HolderLookup.Provider holderLookup, boolean clientPacket) {
        super.write(tag, holderLookup, clientPacket);
        tag.putBoolean("Active", active);
        tag.putBoolean("WasJustAssembled", wasJustAssembled);
        tag.putFloat("Burnrate", burnrate);
        tag.putBoolean("First", first);
        tag.putString("RequiredFluid", requiredFluid.toString());
    }

    @Override
    public void read(CompoundTag tag, HolderLookup.Provider holderLookup, boolean clientPacket) {
        super.read(tag, holderLookup, clientPacket);
        active = tag.getBoolean("Active");
        wasJustAssembled = tag.getBoolean("WasJustAssembled");
        burnrate = tag.getFloat("Burnrate");
        first = tag.getBoolean("First");
        // Fluid aus NBT laden (optional, standardmäßig Wasser)
        if (tag.contains("RequiredFluid")) {
            // Hier könnte man den Fluid aus dem String zurück konvertieren
            // Für jetzt bleibt es bei Wasser als Standard
        }
    }

    @Override
    public void tick() {
        super.tick();

        if(first && burnrateCapacity != null) {
            burnrate = burnrateCapacity.getValue();
            updateGeneratedRotation();
            first = false;
        }

        if (!this.level.isClientSide) {
            if (structure == null) {
                structure = new FusionReactorStructure(this.level, this);
            }

            boolean structureValid = structure.checkStructure();
            boolean hasFluid = structureValid && structure.consumeFluidFromInputs(burnrate, requiredFluid);
            boolean isValid = structureValid && burnrate > 0 && hasFluid;

            if(isValid != active) {
                setActive(isValid);
            }
        }
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
        updateGeneratedRotation(); // Füge das zurück um Stress-Änderungen zu übernehmen
        notifyUpdate();
    }

    public boolean wasJustAssembled() {
        return wasJustAssembled;
    }

    public void setWasJustAssembled(boolean wasJustAssembled) {
        this.wasJustAssembled = wasJustAssembled;
        notifyUpdate();
    }

    public float getBurnrate() {
        return burnrate;
    }

    // Getter und Setter für den Required Fluid
    public Fluid getRequiredFluid() {
        return requiredFluid;
    }

    public void setRequiredFluid(Fluid fluid) {
        this.requiredFluid = fluid;
        notifyUpdate();
    }

}
