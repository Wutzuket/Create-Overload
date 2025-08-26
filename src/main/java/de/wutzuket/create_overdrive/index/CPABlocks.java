package de.wutzuket.create_overdrive.index;

import com.simibubi.create.AllTags.AllBlockTags;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntry;
import de.wutzuket.create_overdrive.blocks.AcceleratorInput.AcceleratorinputBlock;
import de.wutzuket.create_overdrive.blocks.AcceleratorOutput.AcceleratorOutputBlock;
import de.wutzuket.create_overdrive.blocks.FusionReactor.Casing;
import de.wutzuket.create_overdrive.blocks.FusionReactor.FusionReactorCoreBlock;
import de.wutzuket.create_overdrive.blocks.FusionReactorInput.FusionReactorInputBlock;
import de.wutzuket.create_overdrive.blocks.Ionator.IonatorBlock;
import de.wutzuket.create_overdrive.blocks.ParticleAccelerator.ParticleAcceleratorCoreBlock;

import static com.simibubi.create.foundation.data.ModelGen.customItemModel;
import static com.simibubi.create.foundation.data.TagGen.axeOrPickaxe;
import static de.wutzuket.create_overdrive.Main.REGISTRATE;

public class CPABlocks {
    public static BlockEntry<ParticleAcceleratorCoreBlock> PARTICLE_ACCELERATOR_CORE;
    public static BlockEntry<AcceleratorinputBlock> ACCELERATOR_INPUT;
    public static BlockEntry<AcceleratorOutputBlock> ACCELERATOR_OUTPUT;
    public static BlockEntry<Casing> FUSION_REACTOR_CASING;
    public static BlockEntry<FusionReactorCoreBlock> FUSION_REACTOR_CORE;
    public static BlockEntry<FusionReactorInputBlock> FUSION_REACTOR_INPUT;
    public static BlockEntry<IonatorBlock> IONATOR;

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
                .block("fusion_reactor_casing", Casing::new)
                .initialProperties(SharedProperties::stone)
                .properties(p -> p.strength(1.5f, 6.0f))
                .tag(AllBlockTags.SAFE_NBT.tag)
                .transform(axeOrPickaxe())
                .item()
                .transform(customItemModel())
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
    }
}
