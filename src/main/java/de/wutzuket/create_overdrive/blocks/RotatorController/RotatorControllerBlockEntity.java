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

public class RotatorControllerBlockEntity extends GeneratingKineticBlockEntity {

    public int stress;
    // Axis used for rendering layers (set by RotatorStructure)
    public Direction.Axis renderAxis = Direction.Axis.Y;

    protected ScrollValueBehaviour StressCapacity;

    protected int stressMax = 100;

    protected int multiblockSize = 10;

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
                casing_render = new ArrayList<>(structure.CasingPositions);
                // RotatorStructure liefert FlywheelPositions als ergänzende Positionen
                flywheel_render = new ArrayList<>(structure.FlywheelPositions);
                // Input-Render aus der eigenen Struktur-Liste (InputPositions)
                input_render = new ArrayList<>(structure.InputPositions);
                // Falls RotatorStructure GlassPositions zur Verfügung stellt, nutze sie analog
                glass_render = new ArrayList<>(structure.getGlassBlockPositions());
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
        }
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
        // Lade persistente inputPosition wenn vorhanden
        if (compound.contains("InputPos")) {
            long lp = compound.getLong("InputPos");
            this.inputPosition = BlockPos.of(lp);
        } else {
            this.inputPosition = null;
        }
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
            // Debug: report which lists were read on client
            if (compound.contains("CasingRender")) System.out.println("[BE-read-client] CasingRender size=" + compound.getCompound("CasingRender").getInt("size"));
            if (compound.contains("GlassRender")) System.out.println("[BE-read-client] GlassRender size=" + compound.getCompound("GlassRender").getInt("size"));
            if (compound.contains("FlywheelRender")) System.out.println("[BE-read-client] FlywheelRender size=" + compound.getCompound("FlywheelRender").getInt("size"));
            if (compound.contains("InputRender")) System.out.println("[BE-read-client] InputRender size=" + compound.getCompound("InputRender").getInt("size"));
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
        compound.putInt("MultiblockSize", this.multiblockSize);
        compound.putInt("Stress", this.stress);
        compound.putBoolean("WasJustAssembled", this.wasJustAssembled);
        // Forced direction persist
        compound.putString("ForcedDir", this.forcedDirection == null ? "" : this.forcedDirection.name());

        // Synchronisiere casing_render Liste für Client
        if (clientPacket) {
            // Debug: report which lists are being sent from server
            System.out.println("[BE-write-server] sending Casing=" + casing_render.size() + " Glass=" + glass_render.size() + " Flywheel=" + flywheel_render.size() + " Input=" + input_render.size());
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
