package de.wutzuket.create_overdrive.index;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllSpriteShifts;
import com.simibubi.create.AllTags.AllBlockTags;
import com.simibubi.create.content.decoration.encasing.CasingBlock;
import com.simibubi.create.content.decoration.encasing.EncasingRegistry;
import com.simibubi.create.content.kinetics.simpleRelays.encased.EncasedShaftBlock;
import com.simibubi.create.foundation.data.BuilderTransformers;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntry;
import de.wutzuket.create_overdrive.blocks.AcceleratorInput.AcceleratorinputBlock;
import de.wutzuket.create_overdrive.blocks.AcceleratorOutput.AcceleratorOutputBlock;
import de.wutzuket.create_overdrive.blocks.FusionReactor.FusionReactorCoreBlock;
import de.wutzuket.create_overdrive.blocks.FusionReactorInput.FusionReactorInputBlock;
import de.wutzuket.create_overdrive.blocks.Ionator.IonatorBlock;
import de.wutzuket.create_overdrive.blocks.ParticleAccelerator.ParticleAcceleratorCoreBlock;
import de.wutzuket.create_overdrive.blocks.RotatorController.RotatorControllerBlock;
import de.wutzuket.create_overdrive.blocks.RotatorInput.RotatorInputBlock;
import de.wutzuket.create_overdrive.blocks.RotatorOutput.RotatorOutputBlock;
import net.minecraft.world.level.material.MapColor;

import static com.simibubi.create.foundation.data.ModelGen.customItemModel;
import static com.simibubi.create.foundation.data.TagGen.axeOrPickaxe;
import static de.wutzuket.create_overdrive.Main.REGISTRATE;

public class CPABlocks {
    public static BlockEntry<ParticleAcceleratorCoreBlock> PARTICLE_ACCELERATOR_CORE;
    public static BlockEntry<AcceleratorinputBlock> ACCELERATOR_INPUT;
    public static BlockEntry<AcceleratorOutputBlock> ACCELERATOR_OUTPUT;
    public static BlockEntry<CasingBlock> FUSION_REACTOR_CASING;
    public static BlockEntry<FusionReactorCoreBlock> FUSION_REACTOR_CORE;
    public static BlockEntry<FusionReactorInputBlock> FUSION_REACTOR_INPUT;
    public static BlockEntry<IonatorBlock> IONATOR;
    public static BlockEntry<CasingBlock> ROTATOR_CASING;
    public static BlockEntry<RotatorControllerBlock> ROTATOR_CONTROLLER;
    public static BlockEntry<RotatorInputBlock> ROTATOR_INPUT;
    public static BlockEntry<RotatorOutputBlock> ROTATOR_OUTPUT;

    public CPABlocks() {
    }

    public static void register() {
    }

    static {
        PARTICLE_ACCELERATOR_CORE = REGISTRATE
                .block("particle_accelerator_core", ParticleAcceleratorCoreBlock::new)
                .initialProperties(SharedProperties::stone)
                .properties(p -> p.strength(1.5f, 6.0f))
                .tag(AllBlockTags.SAFE_NBT.tag)
                .transform(axeOrPickaxe())
                .item()
                .transform(customItemModel())
                .register();
        ACCELERATOR_INPUT = REGISTRATE
                .block("particle_accelerator_input", AcceleratorinputBlock::new)
                .initialProperties(SharedProperties::stone)
                .properties(p -> p.strength(1.5f, 6.0f))
                .tag(AllBlockTags.SAFE_NBT.tag)
                .transform(axeOrPickaxe())
                .item()
                .transform(customItemModel())
                .register();
        ACCELERATOR_OUTPUT = REGISTRATE
                .block("particle_accelerator_output", AcceleratorOutputBlock::new)
                .initialProperties(SharedProperties::stone)
                .properties(p -> p.strength(1.5f, 6.0f))
                .tag(AllBlockTags.SAFE_NBT.tag)
                .transform(axeOrPickaxe())
                .item()
                .transform(customItemModel())
                .register();
        FUSION_REACTOR_CASING = REGISTRATE
                .block("fusion_reactor_casing", CasingBlock::new)
                .properties(p -> p.mapColor(MapColor.TERRACOTTA_BROWN))
                .transform(BuilderTransformers.casing(() -> CPASpriteShifts.FUSION_REACTOR_CASING))
                .register();
        FUSION_REACTOR_CORE = REGISTRATE
                .block("fusion_reactor_core", FusionReactorCoreBlock::new)
                .initialProperties(SharedProperties::stone)
                .properties(p -> p.strength(1.5f, 6.0f))
                .tag(AllBlockTags.SAFE_NBT.tag)
                .transform(axeOrPickaxe())
                .item()
                .transform(customItemModel())
                .register();
        FUSION_REACTOR_INPUT = REGISTRATE
                .block("fusion_reactor_input", FusionReactorInputBlock::new)
                .initialProperties(SharedProperties::stone)
                .properties(p -> p.strength(1.5f, 6.0f))
                .tag(AllBlockTags.SAFE_NBT.tag)
                .transform(axeOrPickaxe())
                .item()
                .transform(customItemModel())
                .register();
        IONATOR = REGISTRATE
                .block("ionator", IonatorBlock::new)
                .initialProperties(SharedProperties::stone)
                .properties(p -> p.strength(1.5f, 6.0f))
                .tag(AllBlockTags.SAFE_NBT.tag)
                .transform(axeOrPickaxe())
                .item()
                .transform(customItemModel())
                .register();
        ROTATOR_CASING = REGISTRATE
                .block("rotator_casing", CasingBlock::new)
                .properties(p -> p.mapColor(MapColor.TERRACOTTA_BROWN))
                .transform(BuilderTransformers.casing(() -> CPASpriteShifts.ROTATOR_CASING))
                .register();

        ROTATOR_CONTROLLER = REGISTRATE
                .block("rotator_controller", RotatorControllerBlock::new)
                .initialProperties(SharedProperties::stone)
                .properties(p -> p.strength(1.5f, 6.0f))
                .tag(AllBlockTags.SAFE_NBT.tag)
                .transform(axeOrPickaxe())
                .item()
                .transform(customItemModel())
                .register();

        ROTATOR_INPUT = REGISTRATE
                .block("rotator_input", RotatorInputBlock::new)
                .initialProperties(SharedProperties::stone)
                .properties(p -> p.strength(1.5f, 6.0f))
                .tag(AllBlockTags.SAFE_NBT.tag)
                .transform(axeOrPickaxe())
                .item()
                .transform(customItemModel())
                .register();

        ROTATOR_OUTPUT = REGISTRATE
                .block("rotator_output", RotatorOutputBlock::new)
                .initialProperties(SharedProperties::stone)
                .properties(p -> p.strength(1.5f, 6.0f))
                .tag(AllBlockTags.SAFE_NBT.tag)
                .transform(axeOrPickaxe())
                .item()
                .transform(customItemModel())
                .register();

    }
}
