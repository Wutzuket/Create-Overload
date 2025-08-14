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


public class FusionReactorCoreBlockEntity extends GeneratingKineticBlockEntity {

    protected float burnrate;
    protected ScrollValueBehaviour burnrateCapacity;

    private boolean active = false;
    private boolean wasJustAssembled = false;
    private boolean first = true;

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
    }

    @Override
    public void read(CompoundTag tag, HolderLookup.Provider holderLookup, boolean clientPacket) {
        super.read(tag, holderLookup, clientPacket);
        active = tag.getBoolean("Active");
        wasJustAssembled = tag.getBoolean("WasJustAssembled");
        burnrate = tag.getFloat("Burnrate");
        first = tag.getBoolean("First");
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
            FusionReactorStructure structure = new FusionReactorStructure(this.level, this);
            boolean isValid = structure.checkStructure() && burnrate > 0; // Nur aktiv wenn Struktur gültig UND Burnrate > 0

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

}
