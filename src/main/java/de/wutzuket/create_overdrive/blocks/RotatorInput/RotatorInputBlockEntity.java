package de.wutzuket.create_overdrive.blocks.RotatorInput;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import de.wutzuket.create_overdrive.config.Config;
import de.wutzuket.create_overdrive.energy.IEnergyProvider;
import de.wutzuket.create_overdrive.blocks.RotatorController.RotatorControllerBlockEntity;
import de.wutzuket.create_overdrive.energy.InternalEnergyStorage;
import de.wutzuket.create_overdrive.index.CPABlockEntities; // changed import
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.EnumMap;
import java.util.EnumSet;

public class RotatorInputBlockEntity extends KineticBlockEntity implements IEnergyProvider {

    protected final InternalEnergyStorage energy;
    private final IEnergyStorage capability;

    private final EnumSet<Direction> invalidSides = EnumSet.allOf(Direction.class);
    private final EnumMap<Direction, BlockCapabilityCache<IEnergyStorage, Direction>> cache = new EnumMap<>(Direction.class);

    public RotatorInputBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
        int cap = Config.getIntSafe(Config.ROTATOR_CAPACITY, 100000);
        int maxOut = Config.getIntSafe(Config.ROTATOR_MAX_OUTPUT, 1000);
        energy = new InternalEnergyStorage(cap, 0, maxOut);
        capability = energy;
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                CPABlockEntities.ROTATOR_INPUT.get(),
                (be, context) -> {
                    if (be instanceof RotatorInputBlockEntity inputBe) {
                        return inputBe.capability;
                    }
                    return null;
                }
        );
    }

    private int stress = 0;

    @Override
    public IEnergyStorage getEnergyStorage(@Nullable Direction direction) {
        return energy;
    }

    private boolean firstTickState = true;

    public void firstTick() {
        updateCache();
    }

    public boolean isEnergyOutput(Direction side) {
        return true;
    }

    // Converted to instance method so we can send a server-side debug chat from here
    public int getEnergyProductionRate(float stress) {
        double fe_stress = Config.getDoubleSafe(Config.FE_STRESS, 1);
        double eff = Config.getDoubleSafe(Config.ROTATOR_EFFICIENCY, 1);
        return (int)(fe_stress * stress * Math.abs(getSpeed()) * eff* 0.001);
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null) return;
        if (level.isClientSide()) return;
        if (firstTickState) firstTick();
        firstTickState = false;

        if (calculateStressApplied() > 0) energy.internalProduceEnergy(getEnergyProductionRate(calculateStressApplied()));

        for (Direction d : Direction.values()) {
            if(!isEnergyOutput(d)) continue;
            IEnergyStorage ies = cache.get(d).getCapability();
            if(ies == null) continue;
            int maxOut = Config.getIntSafe(Config.ROTATOR_MAX_OUTPUT, 1000);
            int ext = energy.extractEnergy(ies.receiveEnergy(maxOut, true), false);
            ies.receiveEnergy(ext, false);
        }
    }

    public void updateCache() {
        if (level == null) return;
        if (level.isClientSide()) return;
        for (Direction side : Direction.values()) {
            cache.put(side, BlockCapabilityCache.create(
                    Capabilities.EnergyStorage.BLOCK,
                    (ServerLevel) level,
                    getBlockPos().relative(side),
                    side.getOpposite(),
                    () -> !this.isRemoved(),
                    () -> invalidSides.add(side)
            ));
        }
    }

    @Nullable
    private RotatorControllerBlockEntity findController() {
        if (level == null) return null;
        BlockPos origin = this.getBlockPos();
        final int MAX_DISTANCE = 32; // sicherer Oberwert

        // Suche nach einem Controller in Reichweite; akzeptiere ihn nur, wenn seine gespeicherte
        // inputPosition mit unserer Position übereinstimmt.
        for (Direction dir : Direction.values()) {
            for (int d = 1; d <= MAX_DISTANCE; d++) {
                BlockPos p = origin.relative(dir, d);
                if (!level.isLoaded(p)) break;
                if (level.getBlockEntity(p) instanceof RotatorControllerBlockEntity controller) {
                    try {
                        BlockPos ip = controller.getInputPosition();
                        if (ip != null && ip.equals(origin)) {
                            // Controller hat diese Input-Position registriert -> gehört zu uns
                            return controller;
                        }
                    } catch (Exception ignored) {}
                }
            }
        }
        return null;
    }

    @Override
    public float calculateStressApplied() {
        int usedStress = this.stress;
        RotatorControllerBlockEntity controller = findController();
        if (controller != null) {
            usedStress = controller.stress;
        }
        float impact = (usedStress / 256f);
        this.lastStressApplied = impact;

        return impact;
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        energy.read(tag);
    }

    @Override
    public void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        energy.write(tag);
    }


}
