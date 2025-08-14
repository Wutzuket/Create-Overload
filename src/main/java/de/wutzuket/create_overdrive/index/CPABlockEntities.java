package de.wutzuket.create_overdrive.index;

import com.mojang.logging.LogUtils;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import de.wutzuket.create_overdrive.Main;
import de.wutzuket.create_overdrive.blocks.AcceleratorInput.AcceleratorinputBlockEntity;
import de.wutzuket.create_overdrive.blocks.AcceleratorOutput.AcceleratorOutputBlockEntity;
import de.wutzuket.create_overdrive.blocks.FusionReactor.FusionReactorCoreBlock;
import de.wutzuket.create_overdrive.blocks.FusionReactor.FusionReactorCoreBlockEntity;
import de.wutzuket.create_overdrive.blocks.FusionReactor.FusionReactorCoreBlockRenderer;
import de.wutzuket.create_overdrive.blocks.FusionReactor.FusionReactorCoreVisual;
import de.wutzuket.create_overdrive.blocks.ParticleAccelerator.ParticleAcceleratorCoreBlockEntity;
import de.wutzuket.create_overdrive.blocks.ParticleAccelerator.ParticleAcceleratorCoreBlockRenderer;
import de.wutzuket.create_overdrive.blocks.ParticleAccelerator.ParticleAcceleratorCoreVisual;

import static de.wutzuket.create_overdrive.Main.REGISTRATE;

public class CPABlockEntities {
    public static final BlockEntityEntry<ParticleAcceleratorCoreBlockEntity> PARTICLE_ACCELERATOR_CORE;
    public static final BlockEntityEntry<AcceleratorinputBlockEntity> ACCELERATOR_INPUT;
    public static final BlockEntityEntry<AcceleratorOutputBlockEntity> ACCELERATOR_OUTPUT;
    public static final BlockEntityEntry<FusionReactorCoreBlockEntity> FUSION_REACTOR_CORE;

    static {

        PARTICLE_ACCELERATOR_CORE = REGISTRATE.blockEntity("particle_accelerator_core", ParticleAcceleratorCoreBlockEntity::new)
                .visual(() -> ParticleAcceleratorCoreVisual::new)
                .validBlocks(() -> CPABlocks.PARTICLE_ACCELERATOR_CORE.get())
                .renderer(() -> ParticleAcceleratorCoreBlockRenderer::new)
                .register();

        ACCELERATOR_INPUT = REGISTRATE.blockEntity("particle_accelerator_input", AcceleratorinputBlockEntity::new)
                .validBlocks(() -> CPABlocks.ACCELERATOR_INPUT.get())
                .register();

        ACCELERATOR_OUTPUT = REGISTRATE.blockEntity("particle_accelerator_output", AcceleratorOutputBlockEntity::new)
                .validBlocks(() -> CPABlocks.ACCELERATOR_OUTPUT.get())
                .register();

        FUSION_REACTOR_CORE = REGISTRATE.blockEntity("fusion_reactor_core", FusionReactorCoreBlockEntity::new)
                .visual(() -> FusionReactorCoreVisual::new)
                .validBlocks(() -> CPABlocks.FUSION_REACTOR_CORE.get())
                .renderer(() -> FusionReactorCoreBlockRenderer::new)
                .register();

    }

    public static void register() {
    }
}
