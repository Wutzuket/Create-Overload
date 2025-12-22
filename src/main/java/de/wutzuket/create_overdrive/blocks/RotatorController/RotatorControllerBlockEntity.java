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
import java.util.ArrayList;

// Neu: Energie-Transfer imports
import de.wutzuket.create_overdrive.config.Config;
import de.wutzuket.create_overdrive.energy.IEnergyProvider;
import net.neoforged.neoforge.energy.IEnergyStorage;
import de.wutzuket.create_overdrive.blocks.RotatorOutput.RotatorOutputBlockEntity;

public class RotatorControllerBlockEntity extends GeneratingKineticBlockEntity {

    public int stress;
    // Axis used for rendering layers (set by RotatorStructure)
    public Direction.Axis renderAxis = Direction.Axis.Y;

    protected ScrollValueBehaviour StressCapacity;

    protected int stressMax = 100;

    protected int multiblockSize = 10;

    // Neue Variable: Breite (Anzahl von Layer-2 Spalten)
    protected int multiblockWidth = 1;

    private boolean first = true;

    // Neues Flag wie bei anderen Core BE
    private boolean wasJustAssembled = false;

    // Struktur‑Support
    private RotatorStructure structure;
    public List<BlockPos> casing_render = new ArrayList<>();
    public List<BlockPos> glass_render = new ArrayList<>();
    public List<BlockPos> flywheel_render = new ArrayList<>();
    public List<BlockPos> input_render = new ArrayList<>();
    // Persistente Referenz auf den tatsächlich eingesetzten Input-Block (absolute Position)
    public BlockPos inputPosition = null;
    // Persistente Referenz auf den tatsächlich eingesetzten Output-Block (absolute Position)
    public java.util.List<BlockPos> outputPositions = new ArrayList<>();
    // Round-robin pointer für die nächste Ausgabe-Position
    private int lastOutputIndex = 0;
    // Falls der Spieler eine Richtung durch Platzieren eines Input-Blocks angibt, wird diese Richtung hier gespeichert
    private Direction forcedDirection = null;

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

    // Neuer Getter/Setter für Breite
    public int getMultiblockWidth() {
        int max = Config.getIntSafe(de.wutzuket.create_overdrive.config.Config.ROTATOR_MAX_WIDTH, 8);
        return Math.max(1, Math.min(multiblockWidth, max));
    }

    public void setMultiblockWidth(int width) {
        int max = Config.getIntSafe(de.wutzuket.create_overdrive.config.Config.ROTATOR_MAX_WIDTH, 8);
        int w = Math.max(1, Math.min(width, max));
        if (this.multiblockWidth == w) return;
        this.multiblockWidth = w;
        setChanged();
        sendData();
    }

    public int getStressMax() {
        return stressMax;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);

        // Erzeuge immer den Stress-Scroll-Slot, aber mit der Seite = gegenüberliegende Seite des Block-Facing.
        final Direction slotSide = this.getBlockState().getValue(RotatorControllerBlock.FACING);
        CenteredSideValueBoxTransform StressSlot =
                new CenteredSideValueBoxTransform((reactor, side) -> side == slotSide);

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

        // Strukturprüfung nur serverseitig und lazy initialisieren, um NPE zu vermeiden
        if (!this.level.isClientSide) {
            if (structure == null) {
                structure = new RotatorStructure(this.level, this);
            }

            boolean structureValid = structure.checkStructure();
            List<BlockPos> oldCasingRender = new ArrayList<>(casing_render);
            List<BlockPos> oldFlywheelRender = new ArrayList<>(flywheel_render);
            List<BlockPos> oldGlassRender = new ArrayList<>(glass_render);
            List<BlockPos> oldInputRender = new ArrayList<>(input_render);

            // Kopiere die Ghost-Block-Positionen nur wenn die Struktur unvollständig ist
            if (!structureValid) {
                // Use render-specific lists so we show ghost blocks layer-by-layer
                casing_render = new ArrayList<>(structure.RenderCasingPositions);
                if (casing_render.isEmpty() && !structure.CasingPositions.isEmpty()) {
                    // fallback: if RenderCasingPositions wasn't filled for some reason, use missing casing positions
                    casing_render = new ArrayList<>(structure.CasingPositions);
                }
                flywheel_render = new ArrayList<>(structure.RenderFlywheelPositions);
                input_render = new ArrayList<>(structure.RenderInputPositions);
                glass_render = new ArrayList<>(structure.RenderGlassPositions);
            } else {
                casing_render.clear(); // Lösche Ghost-Blöcke wenn Struktur vollständig ist
                flywheel_render.clear();
                glass_render.clear();
                input_render.clear();
            }

            // Synchronisiere mit Client wenn sich etwas geändert hat
            if (!oldCasingRender.equals(casing_render) || !oldFlywheelRender.equals(flywheel_render) || !oldGlassRender.equals(glass_render) || !oldInputRender.equals(input_render)) {
                notifyUpdate();
            }

            // Wenn die Struktur vollständig ist und sowohl Input- als auch Output-Position gesetzt sind,
            // versuche pro Tick Energie vom Input-Block zum Output-Block zu transferieren.
            if (structureValid) {
                try {
                    BlockPos inPos = this.inputPosition;
                    // structure valid; proceed with energy transfer
                    if (inPos != null && !this.outputPositions.isEmpty() && level.isLoaded(inPos)) {
                        var inBe = level.getBlockEntity(inPos);
                        if (inBe instanceof IEnergyProvider inProv) {
                            IEnergyStorage inStorage = inProv.getEnergyStorage(null);
                            if (inStorage != null) {
                                int maxTransfer = Config.getIntSafe(Config.ROTATOR_MAX_OUTPUT, 1000);
                                int totalExtractable = Math.min(inStorage.getEnergyStored(), maxTransfer);
                                int n = this.outputPositions.size();
                                if (n > 0 && totalExtractable > 0) {
                                    int base = totalExtractable / n;
                                    int rem = totalExtractable % n;
                                    // distribute base + remainder (first rem outputs get +1), start at lastOutputIndex for round-robin
                                    for (int i = 0; i < n; i++) {
                                        int idx = (this.lastOutputIndex + i) % n;
                                        BlockPos outP = this.outputPositions.get(idx);
                                        if (outP == null) continue;
                                        if (!level.isLoaded(outP)) continue;
                                        var outBe = level.getBlockEntity(outP);
                                        if (!(outBe instanceof IEnergyProvider outProv)) continue;
                                        IEnergyStorage outStorage = outProv.getEnergyStorage(null);
                                        if (outStorage == null) continue;
                                        int amount = base + (i < rem ? 1 : 0);
                                        if (amount <= 0) continue;
                                        int acceptedSim = outStorage.receiveEnergy(amount, true);
                                        if (acceptedSim == 0) {
                                            try { if (outBe instanceof RotatorOutputBlockEntity rotOut) rotOut.pushToNeighbors(); } catch (Throwable ignored) {}
                                            acceptedSim = outStorage.receiveEnergy(amount, true);
                                        }
                                        if (acceptedSim > 0) {
                                            int extracted = inStorage.extractEnergy(acceptedSim, false);
                                            if (extracted <= 0) continue;
                                            int received = outStorage.receiveEnergy(extracted, false);
                                            try { inBe.setChanged(); } catch (Throwable ignored) {}
                                            try { outBe.setChanged(); } catch (Throwable ignored) {}
                                            // if we extracted less than requested, adjust remaining amounts accordingly by reducing base/rem is unnecessary here
                                        }
                                    }
                                    // rotate start index for next tick to achieve round-robin
                                    this.lastOutputIndex = (this.lastOutputIndex + 1) % n;
                                }
                                // always attempt to push outputs to neighbors as extra fallback
                                for (BlockPos outP : new ArrayList<>(this.outputPositions)) {
                                    try { var outBe = level.getBlockEntity(outP); if (outBe instanceof RotatorOutputBlockEntity rotOut) rotOut.pushToNeighbors(); } catch (Throwable ignored) {}
                                }
                            }
                        }
                    } else {
                        // positions not set or not loaded
                    }
                 } catch (Exception e) {
                    // transfer exception ignored
                     // ignore
                 }
             }
         }
     }


    protected void updateStress(int stresss) {
        stress = stresss;
    }

    public void updateStressMaxFromStructureSize(int multiblockSize) {
        // Lineare Skalierung: pro size * configured stress per wheel, start bei 0
        long size = Math.max(0, multiblockSize);
        int perWheel = Config.getIntSafe(Config.ROTATOR_STRESS_PER_WHEEL, 10000);
        long candidate = size * (long) perWheel;
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
        // Lade persistente inputPosition wenn vorhanden
        if (compound.contains("InputPos")) {
            long lp = compound.getLong("InputPos");
            this.inputPosition = BlockPos.of(lp);
        } else {
            this.inputPosition = null;
        }
        // Lade persistente outputPosition wenn vorhanden
        this.outputPositions.clear();
        if (compound.contains("OutputPosCount")) {
            int cnt = compound.getInt("OutputPosCount");
            for (int i = 0; i < cnt; i++) {
                if (compound.contains("OutputPos_" + i)) {
                    this.outputPositions.add(BlockPos.of(compound.getLong("OutputPos_" + i)));
                }
            }
        } else if (compound.contains("OutputPos")) {
            // legacy single pos
            long lp = compound.getLong("OutputPos");
            this.outputPositions.add(BlockPos.of(lp));
        }
        // Lade und clamp lastOutputIndex
        if (compound.contains("LastOutputIndex")) {
            int idx = compound.getInt("LastOutputIndex");
            if (idx < 0) idx = 0;
            if (idx >= this.outputPositions.size() && !this.outputPositions.isEmpty()) idx = idx % this.outputPositions.size();
            this.lastOutputIndex = idx;
        } else {
            this.lastOutputIndex = 0;
        }
        if (compound.contains("MultiblockSize")) {
            this.multiblockSize = compound.getInt("MultiblockSize");
            // Immediately adjust stressMax to reflect loaded size
            updateStressMaxFromStructureSize(this.multiblockSize);
        }
        // New: load multiblockWidth
        if (compound.contains("MultiblockWidth")) {
            int max = Config.getIntSafe(de.wutzuket.create_overdrive.config.Config.ROTATOR_MAX_WIDTH, 8);
            this.multiblockWidth = Math.max(1, Math.min(compound.getInt("MultiblockWidth"), max));
        }
        if (compound.contains("Stress")) {
            this.stress = compound.getInt("Stress");
        }
        // If behaviour already exists (client side), update displayed value
        if (StressCapacity != null)
            StressCapacity.setValue(this.stress);
        if (compound.contains("WasJustAssembled")) {
            this.wasJustAssembled = compound.getBoolean("WasJustAssembled");
        }
        // Forced direction (optional)
        if (compound.contains("ForcedDir")) {
            String dir = compound.getString("ForcedDir");
            if (dir == null || dir.isEmpty()) this.forcedDirection = null;
            else try { this.forcedDirection = Direction.valueOf(dir); } catch (Exception e) { this.forcedDirection = null; }
        }

        if (clientPacket) {
            // clientPacket read: handled silently
        }

        // Lade casing_render Liste für Client
        if (clientPacket && compound.contains("CasingRender")) {
            CompoundTag casingTag = compound.getCompound("CasingRender");
            int size = casingTag.getInt("size");
            casing_render.clear();
            for (int i = 0; i < size; i++) {
                if (casingTag.contains("pos_" + i)) {
                    BlockPos pos = BlockPos.of(casingTag.getLong("pos_" + i));
                    casing_render.add(pos);
                }
            }
        }

        // Lade flywheel_render Liste für Client
        if (clientPacket && compound.contains("FlywheelRender")) {
            CompoundTag inputTag = compound.getCompound("FlywheelRender");
            int size = inputTag.getInt("size");
            flywheel_render.clear();
            for (int i = 0; i < size; i++) {
                if (inputTag.contains("pos_" + i)) {
                    BlockPos pos = BlockPos.of(inputTag.getLong("pos_" + i));
                    flywheel_render.add(pos);
                }
            }
        }

        // Lade input_render Liste für Client (analog zu FlywheelRender)
        if (clientPacket && compound.contains("InputRender")) {
            CompoundTag inputTag = compound.getCompound("InputRender");
            int size = inputTag.getInt("size");
            input_render.clear();
            for (int i = 0; i < size; i++) {
                if (inputTag.contains("pos_" + i)) {
                    BlockPos pos = BlockPos.of(inputTag.getLong("pos_" + i));
                    input_render.add(pos);
                }
            }
        }

        // Lade glass_render Liste für Client (analog zu casing_render)
        if (clientPacket && compound.contains("GlassRender")) {
            CompoundTag glassTag = compound.getCompound("GlassRender");
            int size = glassTag.getInt("size");
            glass_render.clear();
            for (int i = 0; i < size; i++) {
                if (glassTag.contains("pos_" + i)) {
                    BlockPos pos = BlockPos.of(glassTag.getLong("pos_" + i));
                    glass_render.add(pos);
                }
            }
        }
    }

    @Override
    public void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        // Persistente Speicherung der Input-Position (nur serverseitig zur Welt-Save-Persitenz)
        if (this.inputPosition != null) {
            compound.putLong("InputPos", this.inputPosition.asLong());
        }
        // Persistente Speicherung der Output-Position (nur serverseitig zur Welt-Save-Persitenz)
        if (this.outputPositions != null && !this.outputPositions.isEmpty()) {
            compound.putInt("OutputPosCount", this.outputPositions.size());
            for (int i = 0; i < this.outputPositions.size(); i++) {
                compound.putLong("OutputPos_" + i, this.outputPositions.get(i).asLong());
            }
            // also keep legacy first-pos for compatibility
            compound.putLong("OutputPos", this.outputPositions.get(0).asLong());
            compound.putInt("LastOutputIndex", this.lastOutputIndex);
        }
        compound.putInt("MultiblockSize", this.multiblockSize);
        // Persist multiblockWidth
        compound.putInt("MultiblockWidth", this.multiblockWidth);
        compound.putInt("Stress", this.stress);
        compound.putBoolean("WasJustAssembled", this.wasJustAssembled);
        // Forced direction persist
        compound.putString("ForcedDir", this.forcedDirection == null ? "" : this.forcedDirection.name());

        // Synchronisiere casing_render Liste für Client
        if (clientPacket) {
             CompoundTag casingTag = new CompoundTag();
             for (int i = 0; i < casing_render.size(); i++) {
                 BlockPos pos = casing_render.get(i);
                 casingTag.putLong("pos_" + i, pos.asLong());
             }
             casingTag.putInt("size", casing_render.size());
             compound.put("CasingRender", casingTag);

             // Synchronisiere flywheel_render Liste für Client
             CompoundTag inputTag = new CompoundTag();
             for (int i = 0; i < flywheel_render.size(); i++) {
                 BlockPos pos = flywheel_render.get(i);
                 inputTag.putLong("pos_" + i, pos.asLong());
             }
             inputTag.putInt("size", flywheel_render.size());
             compound.put("FlywheelRender", inputTag);

             // Synchronisiere input_render Liste für Client (analog zu flywheel_render)
             CompoundTag inputRenderTag = new CompoundTag();
             for (int i = 0; i < input_render.size(); i++) {
                 BlockPos pos = input_render.get(i);
                 inputRenderTag.putLong("pos_" + i, pos.asLong());
             }
             inputRenderTag.putInt("size", input_render.size());
             compound.put("InputRender", inputRenderTag);

              // Synchronisiere glass_render Liste für Client (analog zu casing_render)
              CompoundTag glassTag = new CompoundTag();
              for (int i = 0; i < glass_render.size(); i++) {
                  BlockPos pos = glass_render.get(i);
                  glassTag.putLong("pos_" + i, pos.asLong());
              }
              glassTag.putInt("size", glass_render.size());
              compound.put("GlassRender", glassTag);
        }


        super.write(compound, registries, clientPacket);
    }

    // Setze die Input-Position (wird von RotatorStructure aufgerufen wenn ein Input-Block gefunden wird)
    public void setInputPosition(BlockPos pos) {
        if (pos == null) {
            this.inputPosition = null;
        } else {
            this.inputPosition = pos;
        }
        setChanged();
        sendData();
    }

    // Setze die Output-Position (wird von RotatorStructure aufgerufen wenn ein Output-Block gefunden wird)
    public void setOutputPosition(BlockPos pos) {
        // Legacy setter: set single output (clears others)
        this.outputPositions.clear();
        if (pos != null) this.outputPositions.add(pos);
        setChanged();
        sendData();
    }

    // Fügt eine Output-Position zur Struktur hinzu (wird von RotatorStructure aufgerufen)
    public void addOutputPosition(BlockPos pos) {
        if (pos == null) return;
        if (!this.outputPositions.contains(pos)) this.outputPositions.add(pos);
        setChanged();
        sendData();
    }

    public void removeOutputPosition(BlockPos pos) {
        if (pos == null) return;
        if (this.outputPositions.remove(pos)) {
            setChanged();
            sendData();
        }
    }

    public void clearOutputPositions() {
        if (!this.outputPositions.isEmpty()) {
            this.outputPositions.clear();
            setChanged();
            sendData();
        }
    }

    public java.util.List<BlockPos> getOutputPositions() {
        return this.outputPositions;
    }

    // Kompatibler Zugriff: gebe erste Output-Position zurück (oder null)
    public BlockPos getOutputPosition() {
        return this.outputPositions.isEmpty() ? null : this.outputPositions.get(0);
    }

    public BlockPos getInputPosition() {
        return this.inputPosition;
    }


    public Direction getForcedDirection() {
        return forcedDirection;
    }

    public void setForcedDirection(Direction dir) {
        if (dir == forcedDirection) return;
        this.forcedDirection = dir;
        setChanged();
        notifyUpdate();
    }

    public boolean wasJustAssembled() {
        return wasJustAssembled;
    }

    public void setWasJustAssembled(boolean wasJustAssembled) {
        this.wasJustAssembled = wasJustAssembled;
        setChanged();
        notifyUpdate();
    }

}
