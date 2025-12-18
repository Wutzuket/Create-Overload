package de.wutzuket.create_overdrive.blocks.RotatorOutput;

import de.wutzuket.create_overdrive.Main;
import de.wutzuket.create_overdrive.index.CPABlockEntities;
import de.wutzuket.create_overdrive.config.Config;
import de.wutzuket.create_overdrive.energy.IEnergyProvider;
import de.wutzuket.create_overdrive.energy.InternalEnergyStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;

import java.util.EnumMap;
import java.util.EnumSet;

public class RotatorOutputBlockEntity extends BlockEntity implements IEnergyProvider {

    public static final BlockCapability<IEnergyStorage, Direction> ENERGY_HANDLER_BLOCK =
            BlockCapability.create(ResourceLocation.fromNamespaceAndPath(Main.MODID, "rotator_output_block"), IEnergyStorage.class, Direction.class);

    protected final InternalEnergyStorage energy;
    // Exponierte Capability: Wrapper schützt vor sofortiger Extraktion nach Empfang
    private final IEnergyStorage exposedStorage;

    private final EnumSet<Direction> invalidSides = EnumSet.allOf(Direction.class);
    private final EnumMap<Direction, net.neoforged.neoforge.capabilities.BlockCapabilityCache<IEnergyStorage, Direction>> cache = new EnumMap<>(Direction.class);

    public boolean partOfStructure = false; // flag, kann später durch Struktur-Registrierung gesetzt werden

    private long lastReceiveTick = -1;

    public RotatorOutputBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
        int cap = Config.getIntSafe(Config.ROTATOR_CAPACITY, 100000);
        int maxOut = Config.getIntSafe(Config.ROTATOR_MAX_OUTPUT, 1000);
        // Allow output to both receive and provide energy so external blocks can extract from it.
        energy = new InternalEnergyStorage(cap, maxOut, maxOut);

        exposedStorage = energy;
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                CPABlockEntities.ROTATOR_OUTPUT.get(),
                (be, context) -> {
                    if (be instanceof RotatorOutputBlockEntity outputBe) {
                        return outputBe.exposedStorage;
                    }
                    return null;
                }
        );
    }

    @Override
    public IEnergyStorage getEnergyStorage(@Nullable Direction direction) {
        return exposedStorage;
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        energy.write(tag);
        tag.putBoolean("PartOfStructure", partOfStructure);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        energy.read(tag);
        if (tag.contains("PartOfStructure")) partOfStructure = tag.getBoolean("PartOfStructure");
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return super.getUpdateTag(registries);
    }

    // Versucht, verfügbare Energie an benachbarte Energy-Handler zu pushen.
    public void pushToNeighbors() {
        if (level == null || level.isClientSide) return;
        try {
            int maxPerSide = Config.getIntSafe(Config.ROTATOR_MAX_OUTPUT, 1000);
            // Keep trying while we still have energy and we are making progress.
            // Limit iterations to avoid pathological long loops.
            final int MAX_LOOPS = 64;
            int loop = 0;
            boolean progress = true;
            while (progress && loop++ < MAX_LOOPS && energy.getEnergyStored() > 0) {
                progress = false;
                for (Direction dir : Direction.values()) {
                    BlockPos npos = this.getBlockPos().relative(dir);
                    // Query the general energy capability at neighbor (supports other mods/cables)
                    IEnergyStorage target = level.getCapability(net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage.BLOCK, npos, dir.getOpposite());
                    if (target == null) continue;
                    int available = Math.min(energy.getEnergyStored(), maxPerSide);
                    if (available <= 0) continue;
                    int acceptedSim = target.receiveEnergy(available, true);
                    if (acceptedSim <= 0) continue;
                    int extracted = energy.extractEnergy(acceptedSim, false);
                    if (extracted <= 0) continue;
                    int received = target.receiveEnergy(extracted, false);
                    if (received > 0) {
                        progress = true;
                        try { setChanged(); } catch (Throwable ignored) {}
                    }
                }
            }
        } catch (Exception e) {
            // suppressed
        }
    }
}
