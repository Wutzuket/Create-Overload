package de.wutzuket.create_overdrive.blocks.ParticleAccelerator;

import com.simibubi.create.AllBlocks;
import de.wutzuket.create_overdrive.index.CPABlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

import static de.wutzuket.create_overdrive.Main.BlockCountAccelerator;
import static de.wutzuket.create_overdrive.Main.RADIUS;

public class AcceleratorStructure {
    private final Level level;
    private final ParticleAcceleratorCoreBlockEntity coreBlockEntity;

    public AcceleratorStructure(Level level, ParticleAcceleratorCoreBlockEntity coreBlockEntity) {
        this.level = level;
        this.coreBlockEntity = coreBlockEntity;
    }

    public boolean checkStructure() {
        int brassCasingCount = 0;
        boolean hasInput = false;
        boolean hasOutput = false;

        BlockPos corePos = coreBlockEntity.getBlockPos();
        int radius = RADIUS;

        // Check for brass casings in the ring
        for (int x = 0; x <= radius; x++) {
            for (int z = 0; z <= radius; z++) {
                if (x == 0 && z == 0) {
                    continue; // Skip the core position
                }
                int distanceSquared = x * x + z * z;
                if (distanceSquared >= radius * radius - radius && distanceSquared <= radius * radius + radius) {
                    brassCasingCount += checkAndShowHologram(corePos.offset(x, 0, z));
                    if (x != 0) brassCasingCount += checkAndShowHologram(corePos.offset(-x, 0, z));
                    if (z != 0) brassCasingCount += checkAndShowHologram(corePos.offset(x, 0, -z));
                    if (x != 0 && z != 0) brassCasingCount += checkAndShowHologram(corePos.offset(-x, 0, -z));
                }
            }
        }

        // Check for brass casings connecting the core to the ring
        for (int i = 1; i < radius; i++) {
            brassCasingCount += checkAndShowHologram(corePos.offset(i, 0, 0)); // Positive X-axis
            brassCasingCount += checkAndShowHologram(corePos.offset(-i, 0, 0)); // Negative X-axis
            brassCasingCount += checkAndShowHologram(corePos.offset(0, 0, i)); // Positive Z-axis
            brassCasingCount += checkAndShowHologram(corePos.offset(0, 0, -i)); // Negative Z-axis
        }

        // Define possible positions for input and output blocks
        BlockPos[] possiblePositions = {
                corePos.offset(radius, 0, 0),
                corePos.offset(0, 0, radius),
                corePos.offset(-radius, 0, 0),
                corePos.offset(0, 0, -radius)
        };

        // Check for input and output blocks
        for (BlockPos pos : possiblePositions) {
            if (level.getBlockState(pos).is(CPABlocks.ACCELERATOR_INPUT.get())) {
                hasInput = true;
            } else if (level.getBlockState(pos).is(CPABlocks.ACCELERATOR_OUTPUT.get())) {
                hasOutput = true;
            }
        }

        // Validate the structure
        boolean structureValid = brassCasingCount == BlockCountAccelerator - 2 && hasInput && hasOutput;

        if (structureValid && !coreBlockEntity.wasJustAssembled()) {
            showRedstoneParticles(corePos, radius);
            coreBlockEntity.setWasJustAssembled(true);
        } else if (!structureValid) {
            coreBlockEntity.setWasJustAssembled(false);
        }

        return structureValid;
    }

    private int checkAndShowHologram(BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.is(AllBlocks.BRASS_CASING.get())) {
            return 1;
        } else if (!state.is(CPABlocks.ACCELERATOR_INPUT.get()) && !state.is(CPABlocks.ACCELERATOR_OUTPUT.get())) {
            showHologram(pos);

            return 0;
        }
        return 0;
    }

    private void showHologram(BlockPos pos) {
        if (level instanceof ServerLevel serverLevel) {
            // Mehr Partikel mit Streuung und Bewegung erzeugen
            serverLevel.sendParticles(
                ParticleTypes.END_ROD,
                pos.getX() + 0.5,
                pos.getY() + 0.5,
                pos.getZ() + 0.5,
                1, // Mehr Partikel (statt nur 1)
                0, // Streuung in X-Richtung
                0, // Streuung in Y-Richtung
                0, // Streuung in Z-Richtung
                0 // Geschwindigkeit
            );
        }
    }

    private void showRedstoneParticles(BlockPos corePos, int radius) {
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                int distanceSquared = x * x + z * z;
                if (distanceSquared >= radius * radius - radius && distanceSquared <= radius * radius + radius) {
                    showRedstoneParticle(corePos.offset(x, 0, z));
                }
            }
        }
    }

    private void showRedstoneParticle(BlockPos pos) {
        if (level instanceof ServerLevel _level) {
            _level.sendParticles(new DustParticleOptions(new Vector3f(1.0F, 0.0F, 0.0F), 1.0F), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 10, 0.5, 0.5, 0.5, 0.1);
        }
    }

    public static int calc_Blocks() {
        int blockCount = 0;
        int radius = RADIUS > 0 ? RADIUS : 10;

        for (int x = 0; x <= radius; x++) {
            for (int z = 0; z <= radius; z++) {
                if (x == 0 && z == 0) {
                    continue; // Skip the core position
                }
                int distanceSquared = x * x + z * z;
                if (distanceSquared >= radius * radius - radius && distanceSquared <= radius * radius + radius) {
                    blockCount += 1;
                    if (x != 0) blockCount += 1;
                    if (z != 0) blockCount += 1;
                    if (x != 0 && z != 0) blockCount += 1;
                }
            }
        }

        blockCount += (radius - 1) * 4;
        return blockCount;
    }

    private void logToAllPlayers(ServerLevel serverLevel, String message) {
        for (ServerPlayer player : serverLevel.players()) {
            player.sendSystemMessage(Component.literal(message));
        }
    }
}
