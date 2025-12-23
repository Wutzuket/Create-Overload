package de.wutzuket.create_overdrive.blocks.FusionReactor;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.CenteredSideValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;

import de.wutzuket.create_overdrive.index.CPAFluids;
import de.wutzuket.create_overdrive.util.BurnrateScrollValueBehaviour;
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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;


public class FusionReactorCoreBlockEntity extends GeneratingKineticBlockEntity {

    protected float burnrate;
    protected ScrollValueBehaviour burnrateCapacity;
    protected Fluid requiredFluid;

    private boolean active = false;
    private boolean wasJustAssembled = false;
    private boolean first = true;

    private FusionReactorStructure structure;

    public List<BlockPos> casing_render = new ArrayList<>();
    public List<BlockPos> input_render = new ArrayList<>(); // Neue Liste für Input-Ghost-Blöcke

    public FusionReactorCoreBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        setLazyTickRate(20);
        // Direkt auf unser Fuel setzen
        this.requiredFluid = CPAFluids.FUSIONREACTORFUEL.get(); // benutzt FluidType für Vergleich (Flowing Variante ok)
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);

        CenteredSideValueBoxTransform burnrateSlot =
            new CenteredSideValueBoxTransform((reactor, side) -> side == Direction.DOWN);

        burnrateCapacity = new BurnrateScrollValueBehaviour(Component.translatable("create_overdrive.gui.burnrate.title"), this, burnrateSlot);
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

    @Override
    public void write(CompoundTag tag, HolderLookup.Provider holderLookup, boolean clientPacket) {
        super.write(tag, holderLookup, clientPacket);
        tag.putBoolean("Active", active);
        tag.putBoolean("WasJustAssembled", wasJustAssembled);
        tag.putFloat("Burnrate", burnrate);
        tag.putBoolean("First", first);
        tag.putString("RequiredFluid",
            BuiltInRegistries.FLUID.getKey(requiredFluid).toString());

        // Synchronisiere casing_render Liste für Client
        if (clientPacket) {
            CompoundTag casingTag = new CompoundTag();
            for (int i = 0; i < casing_render.size(); i++) {
                BlockPos pos = casing_render.get(i);
                casingTag.putLong("pos_" + i, pos.asLong());
            }
            casingTag.putInt("size", casing_render.size());
            tag.put("CasingRender", casingTag);

            // Synchronisiere input_render Liste für Client
            CompoundTag inputTag = new CompoundTag();
            for (int i = 0; i < input_render.size(); i++) {
                BlockPos pos = input_render.get(i);
                inputTag.putLong("pos_" + i, pos.asLong());
            }
            inputTag.putInt("size", input_render.size());
            tag.put("InputRender", inputTag);
        }
    }

    @Override
    public void read(CompoundTag tag, HolderLookup.Provider holderLookup, boolean clientPacket) {
        super.read(tag, holderLookup, clientPacket);
        active = tag.getBoolean("Active");
        wasJustAssembled = tag.getBoolean("WasJustAssembled");
        burnrate = tag.getFloat("Burnrate");
        first = tag.getBoolean("First");

        // Fluid aus NBT laden
        if (tag.contains("RequiredFluid")) {
            try {
                ResourceLocation rl = ResourceLocation.parse(tag.getString("RequiredFluid"));
                Fluid loaded = BuiltInRegistries.FLUID.get(rl);
                if (loaded != Fluids.EMPTY)
                    requiredFluid = loaded;
            } catch (Exception ignored) {
            }
        }

        // Lade casing_render Liste für Client
        if (clientPacket && tag.contains("CasingRender")) {
            CompoundTag casingTag = tag.getCompound("CasingRender");
            int size = casingTag.getInt("size");
            casing_render.clear();
            for (int i = 0; i < size; i++) {
                if (casingTag.contains("pos_" + i)) {
                    BlockPos pos = BlockPos.of(casingTag.getLong("pos_" + i));
                    casing_render.add(pos);
                }
            }
        }

        // Lade input_render Liste für Client
        if (clientPacket && tag.contains("InputRender")) {
            CompoundTag inputTag = tag.getCompound("InputRender");
            int size = inputTag.getInt("size");
            input_render.clear();
            for (int i = 0; i < size; i++) {
                if (inputTag.contains("pos_" + i)) {
                    BlockPos pos = BlockPos.of(inputTag.getLong("pos_" + i));
                    input_render.add(pos);
                }
            }
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

        assert this.level != null;
        if (!this.level.isClientSide) {
            if (structure == null) {
                structure = new FusionReactorStructure(this.level, this);
            }

            boolean structureValid = structure.checkStructure();
            List<BlockPos> oldCasingRender = new ArrayList<>(casing_render);
            List<BlockPos> oldInputRender = new ArrayList<>(input_render);

            // Kopiere die Ghost-Block-Positionen nur wenn die Struktur unvollständig ist
            if (!structureValid) {
                casing_render = new ArrayList<>(structure.CasingPositions);
                input_render = new ArrayList<>(structure.InputGhostPositions);
                // server: set casing and input render positions
            } else {
                casing_render.clear(); // Lösche Ghost-Blöcke wenn Struktur vollständig ist
                input_render.clear();
                // server: clearing casing and input render positions
            }

            // Synchronisiere mit Client wenn sich etwas geändert hat
            if (!oldCasingRender.equals(casing_render) || !oldInputRender.equals(input_render)) {
                // server: notifying client of render changes
                notifyUpdate();
            }

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

}
