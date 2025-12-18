package de.wutzuket.create_overdrive.index;

import com.simibubi.create.content.kinetics.base.SingleAxisRotatingVisual;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import de.wutzuket.create_overdrive.blocks.AcceleratorInput.AcceleratorinputBlockEntity;
import de.wutzuket.create_overdrive.blocks.AcceleratorOutput.AcceleratorOutputBlockEntity;
import de.wutzuket.create_overdrive.blocks.FusionReactor.FusionReactorCoreBlockEntity;
import de.wutzuket.create_overdrive.blocks.FusionReactor.FusionReactorCoreBlockRenderer;
import de.wutzuket.create_overdrive.blocks.FusionReactor.FusionReactorCoreVisual;
import de.wutzuket.create_overdrive.blocks.FusionReactorInput.FusionReactorInputBlockEntity;
import de.wutzuket.create_overdrive.blocks.Ionator.IonatorBlockEntity;
import de.wutzuket.create_overdrive.blocks.Ionator.IonatorRenderer;
import de.wutzuket.create_overdrive.blocks.Ionator.IonatorVisual;
import de.wutzuket.create_overdrive.blocks.ParticleAccelerator.ParticleAcceleratorCoreBlockEntity;
import de.wutzuket.create_overdrive.blocks.ParticleAccelerator.ParticleAcceleratorCoreBlockRenderer;
import de.wutzuket.create_overdrive.blocks.ParticleAccelerator.ParticleAcceleratorCoreVisual;
import de.wutzuket.create_overdrive.blocks.RotatorController.RotatorControllerBlockEntity;
import de.wutzuket.create_overdrive.blocks.RotatorController.RotatorControllerBlockRenderer;
import de.wutzuket.create_overdrive.blocks.RotatorInput.RotatorInputBlockEntity;
import de.wutzuket.create_overdrive.blocks.RotatorInput.RotatorInputBlockRenderer;
import de.wutzuket.create_overdrive.blocks.RotatorOutput.RotatorOutputBlockEntity;

import static de.wutzuket.create_overdrive.Main.REGISTRATE;

public class CPABlockEntities {
    public static final BlockEntityEntry<ParticleAcceleratorCoreBlockEntity> PARTICLE_ACCELERATOR_CORE;
    public static final BlockEntityEntry<AcceleratorinputBlockEntity> ACCELERATOR_INPUT;
    public static final BlockEntityEntry<AcceleratorOutputBlockEntity> ACCELERATOR_OUTPUT;
    public static final BlockEntityEntry<FusionReactorCoreBlockEntity> FUSION_REACTOR_CORE;
    public static final BlockEntityEntry<FusionReactorInputBlockEntity> FUSION_REACTOR_INPUT;
    public static final BlockEntityEntry<IonatorBlockEntity> IONATOR;
    public static final BlockEntityEntry<RotatorControllerBlockEntity> ROTATOR_CONTROLLER;
    public static final BlockEntityEntry<RotatorInputBlockEntity> ROTATOR_INPUT;
    public static final BlockEntityEntry<RotatorOutputBlockEntity> ROTATOR_OUTPUT;

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

        FUSION_REACTOR_INPUT = REGISTRATE.blockEntity("fusion_reactor_input", FusionReactorInputBlockEntity::new)
                .validBlocks(() -> CPABlocks.FUSION_REACTOR_INPUT.get())
                .register();

        IONATOR = REGISTRATE.blockEntity("ionator", IonatorBlockEntity::new)
                .visual(() -> IonatorVisual::new)
                .renderer(() -> IonatorRenderer::new)
                .validBlocks(() -> CPABlocks.IONATOR.get())
                .register();

        ROTATOR_CONTROLLER = REGISTRATE.blockEntity("rotator_controller", RotatorControllerBlockEntity::new)
                .validBlocks(() -> CPABlocks.ROTATOR_CONTROLLER.get())
                .renderer(() -> RotatorControllerBlockRenderer::new)
                .register();

        ROTATOR_INPUT = REGISTRATE.blockEntity("rotator_input", RotatorInputBlockEntity::new)
                .visual(() -> SingleAxisRotatingVisual::shaft, false)
                .validBlocks(() -> CPABlocks.ROTATOR_INPUT.get())
                .renderer(() -> RotatorInputBlockRenderer::new)
                .register();

        ROTATOR_OUTPUT = REGISTRATE.blockEntity("rotator_output", RotatorOutputBlockEntity::new)
                .validBlocks(() -> CPABlocks.ROTATOR_OUTPUT.get())
                .register();

    }

    public static void register() {
    }
}
