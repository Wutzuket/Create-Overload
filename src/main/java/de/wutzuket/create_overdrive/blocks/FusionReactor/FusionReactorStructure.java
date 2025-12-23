package de.wutzuket.create_overdrive.blocks.FusionReactor;

import de.wutzuket.create_overdrive.blocks.FusionReactorInput.FusionReactorInputBlockEntity;
import de.wutzuket.create_overdrive.index.CPABlocks;
import de.wutzuket.create_overdrive.index.CPAFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class FusionReactorStructure {

    private final Level level;
    private final FusionReactorCoreBlockEntity coreBlockEntity;
    private final List<BlockPos> inputBlockPositions = new ArrayList<>();
    public List<BlockPos> CasingPositions = new ArrayList<>();
    public List<BlockPos> InputGhostPositions = new ArrayList<>(); // Neue Liste für Input-Ghost-Blöcke


    public FusionReactorStructure(Level level, FusionReactorCoreBlockEntity coreBlockEntity) {
        this.level = level;
        this.coreBlockEntity = coreBlockEntity;
    }

    public boolean checkStructure() {
        int FusionCasingCount = 0;
        int inputBlockCount = 0;
        BlockPos corePos = coreBlockEntity.getBlockPos();
        boolean layer1 = false;
        boolean layer2 = false;
        boolean layer3 = false;

        inputBlockPositions.clear(); // Clear previous positions
        CasingPositions.clear(); // Leere die Liste am Anfang jeder Strukturprüfung
        InputGhostPositions.clear(); // Leere auch die Input-Ghost-Liste

        if (!layer1){
            for (int i = -2; i <= 3; i++) {
                FusionCasingCount += checkBlockWithEndRod(corePos.offset(i, -1, 3));
                FusionCasingCount += checkBlockWithEndRod(corePos.offset(-i, -1, -3));
                FusionCasingCount += checkBlockWithEndRod(corePos.offset(3, -1, -i));
                FusionCasingCount += checkBlockWithEndRod(corePos.offset(-3, -1, i));
            }
        }
        if (FusionCasingCount == 24 && !layer1) {
            layer1 = true;
        }

        if (!layer2 && layer1) {
            // Äußerer Ring - hier müssen Input-Blöcke gezählt werden
            for (int i = -3; i <= 4; i++) {
                FusionCasingCount += checkBlockWithInputSupport(corePos.offset(i, 0, 4));
                FusionCasingCount += checkBlockWithInputSupport(corePos.offset(-i, 0, -4));
                FusionCasingCount += checkBlockWithInputSupport(corePos.offset(4, 0, -i));
                FusionCasingCount += checkBlockWithInputSupport(corePos.offset(-4, 0, i));
                inputBlockCount += countInputBlocks(corePos.offset(i, 0, 4));
                inputBlockCount += countInputBlocks(corePos.offset(-i, 0, -4));
                inputBlockCount += countInputBlocks(corePos.offset(4, 0, -i));
                inputBlockCount += countInputBlocks(corePos.offset(-4, 0, i));
            }
            // Innerer Ring
            for (int i = -1; i <= 2; i++) {
                FusionCasingCount += checkBlockWithEndRod(corePos.offset(i, 0, 2));
                FusionCasingCount += checkBlockWithEndRod(corePos.offset(-i, 0, -2));
                FusionCasingCount += checkBlockWithEndRod(corePos.offset(2, 0, -i));
                FusionCasingCount += checkBlockWithEndRod(corePos.offset(-2, 0, i));
            }
            FusionCasingCount += checkBlockWithEndRod(corePos.offset(1, 0, 0));
            FusionCasingCount += checkBlockWithEndRod(corePos.offset(-1, 0, 0));
            FusionCasingCount += checkBlockWithEndRod(corePos.offset(0, 0, -1));
            FusionCasingCount += checkBlockWithEndRod(corePos.offset(0, 0, 1));
        }

        if (FusionCasingCount == 76 && !layer2 && inputBlockCount >= 1) {
            layer2 = true;
        }
        if (!layer3 && layer2) {
            for (int i = -2; i <= 3; i++) {
                FusionCasingCount += checkBlockWithEndRod(corePos.offset(i, 1, 3));
                FusionCasingCount += checkBlockWithEndRod(corePos.offset(-i, 1, -3));
                FusionCasingCount += checkBlockWithEndRod(corePos.offset(3, 1, -i));
                FusionCasingCount += checkBlockWithEndRod(corePos.offset(-3, 1, i));
            }
        }
        if (FusionCasingCount == 100 && !layer3) {
            layer3 = true;
        }

        boolean structureValid = layer3 && inputBlockCount >= 1;


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

    public boolean consumeFluidFromInputs(float burnrate, Fluid requiredFluid) {
        if (inputBlockPositions.isEmpty() || burnrate <= 0 || requiredFluid == null) {
            return false;
        }

        int totalFluidNeeded = Math.max(1, (int)(burnrate * 1));

        List<BlockPos> availableInputs = new ArrayList<>();
        for (BlockPos pos : inputBlockPositions) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof FusionReactorInputBlockEntity inputBE) {
                IFluidHandler tank = inputBE.tank.getPrimaryHandler();
                FluidStack fluidInTank = tank.getFluidInTank(0);
                if (isRequiredFluid(fluidInTank, requiredFluid) && fluidInTank.getAmount() > 0) {
                    availableInputs.add(pos);
                }
            }
        }

        if (availableInputs.isEmpty()) {
            return false;
        }

        int fluidPerInput = Math.max(1, totalFluidNeeded / availableInputs.size());
        int remainingFluid = totalFluidNeeded % availableInputs.size();

        for (int i = 0; i < availableInputs.size(); i++) {
            BlockPos pos = availableInputs.get(i);
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof FusionReactorInputBlockEntity inputBE) {
                IFluidHandler tank = inputBE.tank.getPrimaryHandler();
                int fluidToConsume = fluidPerInput + (i < remainingFluid ? 1 : 0);
                FluidStack fluidInTank = tank.getFluidInTank(0);
                if (isRequiredFluid(fluidInTank, requiredFluid)) {
                    int actual = Math.min(fluidToConsume, fluidInTank.getAmount());
                    if (actual > 0) {
                        tank.drain(actual, IFluidHandler.FluidAction.EXECUTE);
                    }
                }
            }
        }

        return true;
    }

    private boolean isRequiredFluid(FluidStack stack, Fluid required) {
        // Vergleicht über FluidType -> akzeptiert Source & Flowing derselben Registrierung
        return !stack.isEmpty() && stack.getFluid().getFluidType() == required.getFluidType();
    }

    // Behalte die alte Methode für Rückwärtskompatibilität
    public boolean consumeWaterFromInputs(float burnrate) {
        return consumeFluidFromInputs(burnrate, CPAFluids.FUSIONREACTORFUEL.get());
    }

    private int checkBlockWithEndRod(BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.is(CPABlocks.FUSION_REACTOR_CASING.get())) {
            return 1;
        } else {
            // Füge Position hinzu wenn der Block NICHT vorhanden ist
            BlockPos offset = pos.subtract(coreBlockEntity.getBlockPos());
            CasingPositions.add(offset);
            return 0;
        }
    }

    private void showRedstoneParticles(BlockPos corePos) {
        // Layer 1 (y = -1)
        for (int i = -2; i <= 3; i++) {
            showRedstoneParticle(corePos.offset(i, -1, 3));
            showRedstoneParticle(corePos.offset(-i, -1, -3));
            showRedstoneParticle(corePos.offset(3, -1, -i));
            showRedstoneParticle(corePos.offset(-3, -1, i));
        }

        // Layer 2 (y = 0) - Outer ring
        for (int i = -3; i <= 4; i++) {
            showRedstoneParticle(corePos.offset(i, 0, 4));
            showRedstoneParticle(corePos.offset(-i, 0, -4));
            showRedstoneParticle(corePos.offset(4, 0, -i));
            showRedstoneParticle(corePos.offset(-4, 0, i));
        }
        // Layer 2 - Inner ring
        for (int i = -1; i <= 2; i++) {
            showRedstoneParticle(corePos.offset(i, 0, 2));
            showRedstoneParticle(corePos.offset(-i, 0, -2));
            showRedstoneParticle(corePos.offset(2, 0, -i));
            showRedstoneParticle(corePos.offset(-2, 0, i));
        }
        // Layer 2 - Adjacent to core
        showRedstoneParticle(corePos.offset(1, 0, 0));
        showRedstoneParticle(corePos.offset(-1, 0, 0));
        showRedstoneParticle(corePos.offset(0, 0, -1));
        showRedstoneParticle(corePos.offset(0, 0, 1));

        // Layer 3 (y = 1)
        for (int i = -2; i <= 3; i++) {
            showRedstoneParticle(corePos.offset(i, 1, 3));
            showRedstoneParticle(corePos.offset(-i, 1, -3));
            showRedstoneParticle(corePos.offset(3, 1, -i));
            showRedstoneParticle(corePos.offset(-3, 1, i));
        }
    }

    private void showRedstoneParticle(BlockPos pos) {
        if (level instanceof ServerLevel _level) {
            _level.sendParticles(new DustParticleOptions(new Vector3f(1.0F, 0.0F, 0.0F), 1.0F), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 10, 0.5, 0.5, 0.5, 0.1);
        }
    }

    private int checkBlockWithInputSupport(BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.is(CPABlocks.FUSION_REACTOR_CASING.get())) {
            return 1;
        } else if (state.is(CPABlocks.FUSION_REACTOR_INPUT.get())) {
            return 1; // Input-Block zählt als gültiger Block
        } else {
            // Weder Casing noch Input vorhanden: Zeige beide Ghosts!
            BlockPos offset = pos.subtract(coreBlockEntity.getBlockPos());
            CasingPositions.add(offset);
            if (inputBlockPositions.size() == 0) {
                InputGhostPositions.add(offset);
            }
            return 0;
        }
    }

    // Hilfsmethode um zu prüfen ob bereits Input-Blöcke existieren
    private boolean hasAnyInputBlocks() {
        return !inputBlockPositions.isEmpty();
    }

    private int countInputBlocks(BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.is(CPABlocks.FUSION_REACTOR_INPUT.get())) {
            inputBlockPositions.add(pos); // Sammle Input-Block Positionen
            return 1;
        }
        return 0;
    }
}
