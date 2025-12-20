package de.wutzuket.create_overdrive.blocks.RotatorController;

import com.simibubi.create.content.decoration.palettes.AllPaletteBlocks;
import de.wutzuket.create_overdrive.config.Config;
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
    // Render-lists: only show ghost blocks layer-by-layer (stops after first incomplete column)
    public List<BlockPos> RenderCasingPositions = new ArrayList<>();
    public List<BlockPos> RenderGlassPositions = new ArrayList<>();
    public List<BlockPos> RenderFlywheelPositions = new ArrayList<>();
    public List<BlockPos> RenderInputPositions = new ArrayList<>();
    public int renderWidthUsed = 1;

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
        RenderCasingPositions.clear();
        RenderGlassPositions.clear();
        RenderFlywheelPositions.clear();
        RenderInputPositions.clear();
        renderWidthUsed = 1;

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
        int cfgMaxWidth = Config.ROTATOR_MAX_WIDTH.get();
        int maxOuter = cfgMaxWidth + 1; // outer = width + 1, cap to reasonable upper bound
        int foundInputAtOuter = -1;
        int farthestFlywheelAt = -1;
        for (int x = 2; x <= maxOuter; x++) {
            BlockPos testPos = corePos.offset(x * cos - 0 * sin, 0, x * sin + 0 * cos);
            try {
                BlockState ts = level.getBlockState(testPos);
                if (ts.is(CPABlocks.ROTATOR_INPUT.get())) {
                    foundInputAtOuter = x;
                    try { coreBlockEntity.setInputPosition(testPos); } catch (Exception ignored) {}
                    break;
                } else if (ts.is(CPABlocks.ROTATORWHEEL.get())) {
                    // remember outermost rotator wheel we find so we can visualize/build up to it
                    farthestFlywheelAt = x;
                }
            } catch (Exception ignored) {}
        }

        // Layer 1
        for (int i = 0; i <= 1; i++) {
            casingCount += checkCasingWithRender(corePos.offset(0 * cos - 1 * sin, i, -1 * cos + 0 * sin));
            casingCount += checkCasingWithRender(corePos.offset(0 * cos + 1 * sin, -i, 1 * cos + 0 * sin));
            casingCount += checkCasingWithRender(corePos.offset(0 * cos - i * sin, -1, -i * cos + 0 * sin));
            casingCount += checkCasingWithRender(corePos.offset(0 * cos + i * sin, 1, i * cos + 0 * sin));
        }

        boolean layer1 = casingCount >= 8;

        // Layer 2 (variable width)
        if(layer1) {
            int cfgMaxW = Config.getIntSafe(Config.ROTATOR_MAX_WIDTH, 10);
            int widthRequested = 1;
            try { widthRequested = Math.max(1, Math.min(coreBlockEntity.getMultiblockWidth(), cfgMaxW)); } catch (Throwable ignored) {}
            int widthUsed = widthRequested;
            if (foundInputAtOuter != -1) {
                // input at outer -> width = outer-1
                widthUsed = Math.max(1, Math.min(foundInputAtOuter - 1, cfgMaxW));
            } else if (farthestFlywheelAt != -1) {
                // No input found, but there are rotator wheels further out -> extend width to include them
                widthUsed = Math.max(widthUsed, Math.max(1, Math.min(farthestFlywheelAt, cfgMaxW)));
            }

            boolean renderStopped = false;
            for (int x = 1; x <= widthUsed; x++) {
                boolean columnMissing = false;
                // For each x, rotate the pattern and check casings/glass/flywheel
                // casing corners for this x
                BlockPos p1 = corePos.offset(x * cos - 1 * sin, 1, x * sin + 1 * cos);
                BlockPos p2 = corePos.offset(x * cos - (-1) * sin, -1, x * sin + (-1) * cos);
                BlockPos p3 = corePos.offset(x * cos - 1 * sin, -1, x * sin + 1 * cos);
                BlockPos p4 = corePos.offset(x * cos - (-1) * sin, 1, x * sin + (-1) * cos);
                int r1 = checkCasingWithRender(p1); casingCount += r1;
                int r2 = checkCasingWithRender(p2); casingCount += r2;
                int r3 = checkCasingWithRender(p3); casingCount += r3;
                int r4 = checkCasingWithRender(p4); casingCount += r4;
                if (!renderStopped) {
                    if (r1 == 0) { RenderCasingPositions.add(p1.subtract(corePos)); columnMissing = true; }
                    if (r2 == 0) { RenderCasingPositions.add(p2.subtract(corePos)); columnMissing = true; }
                    if (r3 == 0) { RenderCasingPositions.add(p3.subtract(corePos)); columnMissing = true; }
                    if (r4 == 0) { RenderCasingPositions.add(p4.subtract(corePos)); columnMissing = true; }
                }

                // glass positions per x: (x,0,-1), (x,0,1), (x,1,0), (x,-1,0)
                BlockPos g1 = corePos.offset(x * cos - (-1) * sin, 0, x * sin + (-1) * cos);
                BlockPos g2 = corePos.offset(x * cos - 1 * sin, 0, x * sin + 1 * cos);
                BlockPos g3 = corePos.offset(x * cos - 0 * sin, 1, x * sin + 0 * cos);
                BlockPos g4 = corePos.offset(x * cos - 0 * sin, -1, x * sin + 0 * cos);
                int gr1 = checkGlassWithRender(g1); glassCount += gr1;
                int gr2 = checkGlassWithRender(g2); glassCount += gr2;
                int gr3 = checkGlassWithRender(g3); glassCount += gr3;
                int gr4 = checkGlassWithRender(g4); glassCount += gr4;
                if (!renderStopped) {
                    if (gr1 == 0) { RenderGlassPositions.add(g1.subtract(corePos)); columnMissing = true; }
                    if (gr2 == 0) { RenderGlassPositions.add(g2.subtract(corePos)); columnMissing = true; }
                    if (gr3 == 0) { RenderGlassPositions.add(g3.subtract(corePos)); columnMissing = true; }
                    if (gr4 == 0) { RenderGlassPositions.add(g4.subtract(corePos)); columnMissing = true; }
                }

                // rotator wheel at (x,0,0) (count any valid rotator wheel; require one per column)
                BlockPos fpos = corePos.offset(x * cos - 0 * sin, 0, x * sin + 0 * cos);
                int fr = checkFlywheelWithRender(fpos); flywheelCount += fr;
                if (!renderStopped) {
                    if (fr == 0) { RenderFlywheelPositions.add(fpos.subtract(corePos)); columnMissing = true; }
                }

                if (!renderStopped && !columnMissing) {
                    // this column is complete; update renderWidthUsed to include it
                    renderWidthUsed = x;
                }
                if (!renderStopped && columnMissing) {
                    // stop rendering further columns beyond the first incomplete one
                    renderStopped = true;
                }
            }
        }

        // Determine widthUsed for validation (either found via input or requested)
        int cfgMaxW2 = de.wutzuket.create_overdrive.config.Config.getIntSafe(de.wutzuket.create_overdrive.config.Config.ROTATOR_MAX_WIDTH, 8);
        int widthUsed = 1;
        try { widthUsed = Math.max(1, Math.min(coreBlockEntity.getMultiblockWidth(), cfgMaxW2)); } catch (Throwable ignored) {}
        if (foundInputAtOuter != -1) widthUsed = Math.max(1, Math.min(foundInputAtOuter - 1, cfgMaxW2));
        else if (farthestFlywheelAt != -1) widthUsed = Math.max(widthUsed, Math.max(1, Math.min(farthestFlywheelAt, cfgMaxW2)));
        // Require a rotator wheel per Layer-2 column now
        boolean layer2 = casingCount >= (8 + 4 * widthUsed) && glassCount >= (4 * widthUsed) && flywheelCount >= widthUsed;

        // Layer 3 (outer ring depends on width)
        if(layer2) {
            int outer = widthUsed + 1; // replaces constant 2
            for (int i = 0; i <= 1; i++) {
                // (outer, i, -1)
                BlockPos c1 = corePos.offset(outer * cos - (-1) * sin, i, outer * sin + (-1) * cos);
                int cr1 = checkCasingWithRender(c1); casingCount += cr1;
                if (cr1 == 0) RenderCasingPositions.add(c1.subtract(corePos));
                // (outer, -i, 1)
                BlockPos c2 = corePos.offset(outer * cos - 1 * sin, -i, outer * sin + 1 * cos);
                int cr2 = checkCasingWithRender(c2); casingCount += cr2;
                if (cr2 == 0) RenderCasingPositions.add(c2.subtract(corePos));
                // (outer, -1, -i)
                BlockPos c3 = corePos.offset(outer * cos - (-i) * sin, -1, outer * sin + (-i) * cos);
                int cr3 = checkCasingWithRender(c3); casingCount += cr3;
                if (cr3 == 0) RenderCasingPositions.add(c3.subtract(corePos));
                // (outer, 1, i)
                BlockPos c4 = corePos.offset(outer * cos - i * sin, 1, outer * sin + i * cos);
                int cr4 = checkCasingWithRender(c4); casingCount += cr4;
                if (cr4 == 0) RenderCasingPositions.add(c4.subtract(corePos));
            }

            // If we didn't already find an input earlier, check the expected input spot at outer
            if (foundInputAtOuter == -1) {
                int ic = checkInputWithRender(corePos.offset(outer * cos - 0 * sin, 0, outer * sin + 0 * cos));
                inputCount += ic;
                if (ic == 0) {
                    // show ghost input at this position relative to core
                    RenderInputPositions.add(corePos.offset(outer * cos - 0 * sin, 0, outer * sin + 0 * cos).subtract(corePos));
                }
            } else {
                // we already discovered an input while scanning
                inputCount += 1;
            }
        }

        boolean layer3 = inputCount >= 1 && casingCount >= (16 + 4 * widthUsed) && glassCount >= (4 * widthUsed) && flywheelCount >= widthUsed;

        boolean structureValid = layer1 && layer2 && layer3;
        if (structureValid) {
            // Set controller size to number of rotator wheels found (1 wheel == size 1 -> stress 1*10k)
            try { coreBlockEntity.setMultiblockSize(flywheelCount); } catch (Exception ignored) {}
            if (!coreBlockEntity.wasJustAssembled()) {
                showRedstoneParticles(corePos);
                coreBlockEntity.setWasJustAssembled(true);
            }
        } else {
            // Reset controller size when structure invalid
            try { coreBlockEntity.setMultiblockSize(0); } catch (Exception ignored) {}
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
        // Prüfe zuerst, ob der aktuelle Block ein RotatorWheel ist
        if (state.is(CPABlocks.ROTATORWHEEL.get())) {
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

        // Layer2 pattern (variable width)
         int cfgMaxWVis = de.wutzuket.create_overdrive.config.Config.getIntSafe(de.wutzuket.create_overdrive.config.Config.ROTATOR_MAX_WIDTH, 8);
         int width = 1;
         try { width = Math.max(1, Math.min(coreBlockEntity.getMultiblockWidth(), cfgMaxWVis)); } catch (Throwable ignored) {}
        // scan outward to find either an input (end) or farthest rotator wheel to visualize further columns
        int maxOuterVis = 9;
        int foundOuterVis = -1;
        int farthestFlywheelVis = -1;
        for (int x = 2; x <= maxOuterVis; x++) {
            BlockPos testPos = corePos.offset(x * (dir == Direction.EAST || dir == Direction.WEST ? 1 : 0), 0, x * (dir == Direction.NORTH || dir == Direction.SOUTH ? 1 : 0));
            try {
                BlockState ts = level.getBlockState(testPos);
                if (ts.is(CPABlocks.ROTATOR_INPUT.get())) { foundOuterVis = x; break; }
                if (ts.is(CPABlocks.ROTATORWHEEL.get())) { farthestFlywheelVis = x; }
            } catch (Exception ignored) {}
        }
        int widthVis = width;
        if (foundOuterVis != -1) widthVis = Math.max(1, Math.min(foundOuterVis - 1, cfgMaxWVis));
        else if (farthestFlywheelVis != -1) widthVis = Math.max(widthVis, Math.max(1, Math.min(farthestFlywheelVis, cfgMaxWVis)));
        for (int x = 1; x <= widthVis; x++) {
             int[] rc1 = rot.apply(new int[]{x, -1}, dir);
             int[] rc2 = rot.apply(new int[]{x, 1}, dir);
             int[] rc3 = rot.apply(new int[]{x, 1}, dir);
             int[] rc4 = rot.apply(new int[]{x, -1}, dir);
             showRedstoneParticle(corePos.offset(rc1[0], 1, rc1[1]));
             showRedstoneParticle(corePos.offset(rc2[0], -1, rc2[1]));
             showRedstoneParticle(corePos.offset(rc3[0], -1, rc3[1]));
             showRedstoneParticle(corePos.offset(rc4[0], 1, rc4[1]));

             int[] gg1 = rot.apply(new int[]{x, -1}, dir);
             int[] gg2 = rot.apply(new int[]{x, 1}, dir);
             int[] gg3 = rot.apply(new int[]{x, 0}, dir);
             showRedstoneParticle(corePos.offset(gg1[0], 0, gg1[1]));
             showRedstoneParticle(corePos.offset(gg2[0], 0, gg2[1]));
             showRedstoneParticle(corePos.offset(gg3[0], -1, gg3[1]));
             showRedstoneParticle(corePos.offset(gg3[0], 1, gg3[1]));
             showRedstoneParticle(corePos.offset(gg3[0], 0, gg3[1]));
         }

        // Layer3 pattern (outer depends on width or discovered input)
        int cfgMaxOuter = Math.min(9, cfgMaxWVis + 1);
        int outer = 2;
        try { outer = Math.max(2, Math.min(coreBlockEntity.getMultiblockWidth() + 1, cfgMaxOuter)); } catch (Throwable ignored) {}
        if (foundOuterVis != -1) outer = Math.max(2, Math.min(foundOuterVis, cfgMaxOuter));
         for (int i = -1; i <= 0; i++) {
             int[] o1 = rot.apply(new int[]{outer, -1}, dir);
             int[] o2 = rot.apply(new int[]{outer, 1}, dir);
             int[] o3 = rot.apply(new int[]{outer, -1}, dir);
             int[] o4 = rot.apply(new int[]{outer, 1}, dir);
             showRedstoneParticle(corePos.offset(o1[0], i, o1[1]));
             showRedstoneParticle(corePos.offset(o2[0], -i, o2[1]));
             showRedstoneParticle(corePos.offset(o3[0], -1, o3[1]));
             showRedstoneParticle(corePos.offset(o4[0], 1, o4[1]));
         }

         int[] inp = rot.apply(new int[]{outer, 0}, dir);
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
