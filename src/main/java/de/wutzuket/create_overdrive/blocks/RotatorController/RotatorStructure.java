package de.wutzuket.create_overdrive.blocks.RotatorController;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.decoration.palettes.AllPaletteBlocks;
import de.wutzuket.create_overdrive.index.CPABlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.Tags;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class RotatorStructure {

    private final Level level;
    private final RotatorControllerBlockEntity coreBlockEntity;
    private List<BlockPos> GlassBlockPositions = new ArrayList<>();
    public List<BlockPos> CasingPositions = new ArrayList<>();
    public List<BlockPos> FlywheelPositions = new ArrayList<>();
    public List<BlockPos> InputPositions = new ArrayList<>();

    public RotatorStructure(Level level, RotatorControllerBlockEntity coreBlockEntity) {
        this.level = level;
        this.coreBlockEntity = coreBlockEntity;
    }

    public boolean checkStructure() {
        BlockPos corePos = coreBlockEntity.getBlockPos();

        // Reset any previously stored input position; we'll set it again if we find one.
        try { coreBlockEntity.setInputPosition(null); } catch (Exception ignored) {}
        // Reset any previously stored output positions too (we'll set them again if we find any)
        try { coreBlockEntity.clearOutputPositions(); } catch (Exception ignored) {}

        GlassBlockPositions.clear();
        CasingPositions.clear();
        FlywheelPositions.clear();
        InputPositions.clear();

        int inputCount = 0;
        int casingCount = 0;
        int glassCount = 0;
        int flywheelCount = 0;

        // Determine facing from block state so rotation matches world orientation
        Direction facing = Direction.NORTH;
        try {
            BlockState s = level.getBlockState(corePos);
            if (s.hasProperty(de.wutzuket.create_overdrive.blocks.RotatorController.RotatorControllerBlock.FACING))
                facing = s.getValue(de.wutzuket.create_overdrive.blocks.RotatorController.RotatorControllerBlock.FACING);
        } catch (Exception ignored) {}

        int sin = 0;
        int cos = 1;

        switch (facing) {
            case SOUTH -> {
                sin = -1;
                cos = 0;
            }
            case WEST -> {
                sin = 0;
                cos = 1;
            }
            case NORTH -> {
                sin = 1;
                cos = 0;
            }
            default -> {
                sin = 0;
                cos = -1;
            }
        }

        // expected world axis for a model-space X-axis (1,0) after rotation
        Direction.Axis expectedFlywheelAxis = (cos != 0) ? Direction.Axis.X : Direction.Axis.Z;



        // Layer 1
        for (int i = 0; i <= 1; i++) {
            casingCount += checkCasingWithRender(corePos.offset(0 * cos - 1 * sin, i, -1 * cos + 0 * sin));
            casingCount += checkCasingWithRender(corePos.offset(0 * cos + 1 * sin, -i, 1 * cos + 0 * sin));
            casingCount += checkCasingWithRender(corePos.offset(0 * cos - i * sin, -1, -i * cos + 0 * sin));
            casingCount += checkCasingWithRender(corePos.offset(0 * cos + i * sin, 1, i * cos + 0 * sin));
        }

        boolean layer1 = casingCount >= 8;

        // Layer 2
        if(layer1) {

            // rotate (1,1), (1,-1), (1,1) with varying y
            casingCount += checkCasingWithRender(corePos.offset(1 * cos - 1 * sin, 1, 1 * sin + 1 * cos));
            casingCount += checkCasingWithRender(corePos.offset(1 * cos - (-1) * sin, -1, 1 * sin + (-1) * cos));
            casingCount += checkCasingWithRender(corePos.offset(1 * cos - 1 * sin, -1, 1 * sin + 1 * cos));
            casingCount += checkCasingWithRender(corePos.offset(1 * cos - (-1) * sin, 1, 1 * sin + (-1) * cos));

            // glass positions: (1,0,-1), (1,0,1), (1,1,0), (1,-1,0)
            glassCount += checkGlassWithRender(corePos.offset(1 * cos - (-1) * sin, 0, 1 * sin + (-1) * cos));
            glassCount += checkGlassWithRender(corePos.offset(1 * cos - 1 * sin, 0, 1 * sin + 1 * cos));
            glassCount += checkGlassWithRender(corePos.offset(1 * cos - 0 * sin, 1, 1 * sin + 0 * cos));
            glassCount += checkGlassWithRender(corePos.offset(1 * cos - 0 * sin, -1, 1 * sin + 0 * cos));

            // flywheel at (1,0,0)
            flywheelCount += checkFlywheelWithRender(corePos.offset(1 * cos - 0 * sin, 0, 1 * sin + 0 * cos));
        }

        boolean layer2 = casingCount >= 12 && glassCount >= 4 && flywheelCount >= 1;

        // Layer 3
        if(layer2) {
            for (int i = 0; i <= 1; i++) {
                // (2, i, -1)
                casingCount += checkCasingWithRender(corePos.offset(2 * cos - (-1) * sin, i, 2 * sin + (-1) * cos));
                // (2, -i, 1)
                casingCount += checkCasingWithRender(corePos.offset(2 * cos - 1 * sin, -i, 2 * sin + 1 * cos));
                // (2, -1, -i)
                casingCount += checkCasingWithRender(corePos.offset(2 * cos - (-i) * sin, -1, 2 * sin + (-i) * cos));
                // (2, 1, i)
                casingCount += checkCasingWithRender(corePos.offset(2 * cos - i * sin, 1, 2 * sin + i * cos));
            }

            // input at (2,0,0)
            inputCount += checkInputWithRender(corePos.offset(2 * cos - 0 * sin, 0, 2 * sin + 0 * cos));
        }

        boolean layer3 = inputCount >= 1 && casingCount >= 20 && glassCount >= 4 && flywheelCount >= 1;

        boolean structureValid = layer1 && layer2 && layer3;
        if (structureValid) {
            if (!coreBlockEntity.wasJustAssembled()) {
                showRedstoneParticles(corePos);
                coreBlockEntity.setWasJustAssembled(true);
            }
        } else {
            coreBlockEntity.setWasJustAssembled(false);
        }

        return structureValid;
    }

    private int checkCasingWithRender(BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        // Wenn normales Casing vorhanden -> OK
        if (state.is(CPABlocks.ROTATOR_CASING.get())) {
            return 1;
        }

        // Wenn ein RotatorOutput vorhanden ist, akzeptiere es als Casing und speichere seine Position im Controller
        if (state.is(CPABlocks.ROTATOR_OUTPUT.get())) {
            try { coreBlockEntity.addOutputPosition(pos); } catch (Exception ignored) {}
            return 1;
        }

        // Füge Position hinzu wenn der Block NICHT vorhanden ist
        BlockPos offset = pos.subtract(coreBlockEntity.getBlockPos());
        CasingPositions.add(offset);
        return 0;
    }

    private int checkFlywheelWithRender(BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        // Prüfe zuerst, ob der aktuelle Block ein Flywheel ist
        if (state.is(AllBlocks.FLYWHEEL.get())) {
            // Wenn der Block ein KineticBlock ist, können wir zusätzlich die Rotationsachse abfragen
            if (state.getBlock() instanceof com.simibubi.create.content.kinetics.base.KineticBlock) {
                com.simibubi.create.content.kinetics.base.KineticBlock kb = (com.simibubi.create.content.kinetics.base.KineticBlock) state.getBlock();
                Direction.Axis axis = kb.getRotationAxis(state);
                // Bestimme erwartete Weltachse anhand der Position relativ zum Core: wenn dx größer als dz -> X, sonst Z
                BlockPos corePos = coreBlockEntity.getBlockPos();
                int dx = pos.getX() - corePos.getX();
                int dz = pos.getZ() - corePos.getZ();
                Direction.Axis expectedAxis = (Math.abs(dx) >= Math.abs(dz)) ? Direction.Axis.X : Direction.Axis.Z;
                if (axis == expectedAxis) {
                    return 1;
                } else {
                    return 0;
                }
            }
            // Falls es kein KineticBlock ist (selten), behandeln wir es dennoch als vorhanden
            return 1;
        } else {
            // Füge Position hinzu wenn der Block NICHT vorhanden ist
            BlockPos offset = pos.subtract(coreBlockEntity.getBlockPos());
            FlywheelPositions.add(offset);
            return 0;
        }
    }

    private int checkGlassWithRender(BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        // Akzeptiere sowohl farblose Glas-Tags als auch das framed glass aus der Create-Palette
        if (state.is(Tags.Blocks.GLASS_BLOCKS_COLORLESS) || state.is(AllPaletteBlocks.FRAMED_GLASS.get())) {
            return 1;
        } else {
            // Füge Position hinzu wenn der Block NICHT vorhanden ist
            BlockPos offset = pos.subtract(coreBlockEntity.getBlockPos());
            GlassBlockPositions.add(offset);
            return 0;
        }
    }

    private int checkInputWithRender(BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.is(CPABlocks.ROTATOR_INPUT.get())) {
            // Wenn ein Input-Block vorhanden ist, akzeptiere ihn unabhängig von interner Achse.
            // Bestimme die Richtung basierend auf der Position relativ zum Core (nur für Debug), aber setze keine forcedDirection.
            BlockPos corePos = coreBlockEntity.getBlockPos();
            int dx = pos.getX() - corePos.getX();
            int dz = pos.getZ() - corePos.getZ();
            Direction dir;
            if (Math.abs(dx) >= Math.abs(dz)) {
                dir = dx > 0 ? Direction.EAST : Direction.WEST;
            } else {
                dir = dz > 0 ? Direction.SOUTH : Direction.NORTH;
            }
            // Speichere die absolute Position des gefundenen Input-Blocks im Core (persistente NBT wird später gesetzt)
            try { coreBlockEntity.setInputPosition(pos); } catch (Exception ignored) {}
            return 1;
        } else {
            // Füge Position hinzu wenn der Block NICHT vorhanden ist
            BlockPos offset = pos.subtract(coreBlockEntity.getBlockPos());
            InputPositions.add(offset);
            return 0;
        }
    }

    private void showRedstoneParticles(BlockPos corePos) {
        // Determine facing and rotate the standard pattern into the actual world direction
        Direction blockFacing = Direction.EAST;
        try {
            BlockState s = level.getBlockState(corePos);
            if (s.hasProperty(de.wutzuket.create_overdrive.blocks.RotatorController.RotatorControllerBlock.FACING))
                blockFacing = s.getValue(de.wutzuket.create_overdrive.blocks.RotatorController.RotatorControllerBlock.FACING);
        } catch (Exception ignored) {}

        Direction dir = blockFacing.getOpposite();

        java.util.function.BiFunction<int[], Direction, int[]> rot = (arr, d) -> {
            int x = arr[0], z = arr[1];
            if (d == Direction.EAST) return new int[]{x, z};
            if (d == Direction.SOUTH) return new int[]{-z, x};
            if (d == Direction.WEST) return new int[]{-x, -z};
            return new int[]{z, -x};
        };

        // Layer1 pattern
        for (int i = -1; i <= 0; i++) {
            int[] p1 = rot.apply(new int[]{0, -1}, dir);
            showRedstoneParticle(corePos.offset(p1[0], i, p1[1]));
            int[] p2 = rot.apply(new int[]{0, 1}, dir);
            showRedstoneParticle(corePos.offset(p2[0], -i, p2[1]));
            int[] p3 = rot.apply(new int[]{-1, 0}, dir);
            showRedstoneParticle(corePos.offset(p3[0], -1, p3[1]));
            int[] p4 = rot.apply(new int[]{1, 0}, dir);
            showRedstoneParticle(corePos.offset(p4[0], 1, p4[1]));
        }

        // Layer2 pattern
        int[] rc1 = rot.apply(new int[]{1, -1}, dir);
        int[] rc2 = rot.apply(new int[]{1, 1}, dir);
        int[] rc3 = rot.apply(new int[]{1, 1}, dir);
        int[] rc4 = rot.apply(new int[]{1, -1}, dir);
        showRedstoneParticle(corePos.offset(rc1[0], 1, rc1[1]));
        showRedstoneParticle(corePos.offset(rc2[0], -1, rc2[1]));
        showRedstoneParticle(corePos.offset(rc3[0], -1, rc3[1]));
        showRedstoneParticle(corePos.offset(rc4[0], 1, rc4[1]));

        int[] gg1 = rot.apply(new int[]{1, -1}, dir);
        int[] gg2 = rot.apply(new int[]{1, 1}, dir);
        int[] gg3 = rot.apply(new int[]{1, 0}, dir);
        showRedstoneParticle(corePos.offset(gg1[0], 0, gg1[1]));
        showRedstoneParticle(corePos.offset(gg2[0], 0, gg2[1]));
        showRedstoneParticle(corePos.offset(gg3[0], -1, gg3[1]));
        showRedstoneParticle(corePos.offset(gg3[0], 1, gg3[1]));
        showRedstoneParticle(corePos.offset(gg3[0], 0, gg3[1]));

        // Layer3 pattern
        for (int i = -1; i <= 0; i++) {
            int[] o1 = rot.apply(new int[]{2, -1}, dir);
            int[] o2 = rot.apply(new int[]{2, 1}, dir);
            int[] o3 = rot.apply(new int[]{2, -1}, dir);
            int[] o4 = rot.apply(new int[]{2, 1}, dir);
            showRedstoneParticle(corePos.offset(o1[0], i, o1[1]));
            showRedstoneParticle(corePos.offset(o2[0], -i, o2[1]));
            showRedstoneParticle(corePos.offset(o3[0], -1, o3[1]));
            showRedstoneParticle(corePos.offset(o4[0], 1, o4[1]));
        }

        int[] inp = rot.apply(new int[]{2, 0}, dir);
        showRedstoneParticle(corePos.offset(inp[0], 0, inp[1]));
    }

    private void showRedstoneParticle(BlockPos pos) {
        if (level instanceof ServerLevel _level) {
            _level.sendParticles(new DustParticleOptions(new Vector3f(1.0F, 0.0F, 0.0F), 1.0F), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 10, 0.5, 0.5, 0.5, 0.1);
        }
    }

    // Öffentlicher Getter für die Glas-Positionen (lesend)
    public List<BlockPos> getGlassBlockPositions() {
        return GlassBlockPositions;
    }

}
