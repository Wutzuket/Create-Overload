package de.wutzuket.create_overdrive.blocks.FusionReactor;

import com.simibubi.create.AllBlocks;
import de.wutzuket.create_overdrive.index.CPABlocks;
import de.wutzuket.create_overdrive.particles.ModParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

public class FusionReactorStructure {

    private final Level level;
    private final FusionReactorCoreBlockEntity coreBlockEntity;

    public FusionReactorStructure(Level level, FusionReactorCoreBlockEntity coreBlockEntity) {
        this.level = level;
        this.coreBlockEntity = coreBlockEntity;
    }

    public boolean checkStructure() {
        int FusionCasingCount = 0;
        BlockPos corePos = coreBlockEntity.getBlockPos();
        boolean layer1 = false;
        boolean layer2 = false;
        boolean layer3 = false;

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
            for (int i = -3; i <= 4; i++) {
                FusionCasingCount += checkBlockWithEndRod(corePos.offset(i, 0, 4));
                FusionCasingCount += checkBlockWithEndRod(corePos.offset(-i, 0, -4));
                FusionCasingCount += checkBlockWithEndRod(corePos.offset(4, 0, -i));
                FusionCasingCount += checkBlockWithEndRod(corePos.offset(-4, 0, i));
            }
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

        if (FusionCasingCount == 76 && !layer2) {
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

        boolean structureValid = layer3;

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

    private int checkBlockWithEndRod(BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.is(CPABlocks.FUSION_REACTOR_CASING.get())) {
            return 1;
        } else if (!state.is(CPABlocks.ACCELERATOR_INPUT.get()) && !state.is(CPABlocks.ACCELERATOR_OUTPUT.get())) {
            showFusionReactorCasingParticles(pos);
            return 0;
        }
        return 0;
    }

    private void showEndRodParticles(BlockPos pos) {
        if (level instanceof ServerLevel _level) {
            _level.sendParticles(ParticleTypes.END_ROD,
                    pos.getX() + 0.5,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5,
                    1,
                    0,
                    0,
                    0,
                    0);
        }
    }

    private void showFusionReactorCasingParticles(BlockPos pos) {
        if (level instanceof ServerLevel _level) {
            _level.sendParticles(ModParticleTypes.FUSION_REACTOR_CASING_PARTICLE.get(),
                    pos.getX() + 0.5,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5,
                    3, // Anzahl der Partikel
                    0, // X-Ausbreitung
                    0, // Y-Ausbreitung
                    0, // Z-Ausbreitung
                    0); // Geschwindigkeit
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
}
