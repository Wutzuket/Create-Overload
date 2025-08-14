package de.wutzuket.create_overdrive.index;

import com.simibubi.create.AllTags.AllBlockTags;
import com.simibubi.create.foundation.data.ModelGen;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.util.entry.BlockEntry;
import de.wutzuket.create_overdrive.Main;
import de.wutzuket.create_overdrive.blocks.AcceleratorInput.AcceleratorinputBlock;
import de.wutzuket.create_overdrive.blocks.AcceleratorOutput.AcceleratorOutputBlock;
import de.wutzuket.create_overdrive.blocks.FusionReactor.Casing;
import de.wutzuket.create_overdrive.blocks.FusionReactor.FusionReactorCoreBlock;
import de.wutzuket.create_overdrive.blocks.ParticleAccelerator.ParticleAcceleratorCoreBlock;
import net.minecraft.tags.TagKey;

public class CPABlocks {
    public static final BlockEntry<ParticleAcceleratorCoreBlock> PARTICLE_ACCELERATOR_CORE;
    public static BlockEntry<AcceleratorinputBlock> ACCELERATOR_INPUT;
    public static BlockEntry<AcceleratorOutputBlock> ACCELERATOR_OUTPUT;
    public static BlockEntry<Casing> FUSION_REACTOR_CASING;
    public static BlockEntry<FusionReactorCoreBlock> FUSION_REACTOR_CORE;

    public CPABlocks() {
    }

    public static void register() {
    }

    static {
        PARTICLE_ACCELERATOR_CORE = ((BlockBuilder)Main.REGISTRATE.block("particle_accelerator_core", ParticleAcceleratorCoreBlock::new).initialProperties(SharedProperties::softMetal).properties((p) -> p.strength(2.0F, 2.0F)).tag(new TagKey[]{AllBlockTags.SAFE_NBT.tag, AllBlockTags.WRENCH_PICKUP.tag}).item().transform(ModelGen.customItemModel())).register();
        ACCELERATOR_INPUT = ((BlockBuilder)Main.REGISTRATE.block("particle_accelerator_input", AcceleratorinputBlock::new).initialProperties(SharedProperties::softMetal).properties((p) -> p.strength(2.0F, 2.0F)).tag(new TagKey[]{AllBlockTags.SAFE_NBT.tag, AllBlockTags.WRENCH_PICKUP.tag}).item().transform(ModelGen.customItemModel())).register();
        ACCELERATOR_OUTPUT = ((BlockBuilder)Main.REGISTRATE.block("particle_accelerator_output", AcceleratorOutputBlock::new).initialProperties(SharedProperties::softMetal).properties((p) -> p.strength(2.0F, 2.0F)).tag(new TagKey[]{AllBlockTags.SAFE_NBT.tag, AllBlockTags.WRENCH_PICKUP.tag}).item().transform(ModelGen.customItemModel())).register();
        FUSION_REACTOR_CASING = ((BlockBuilder)Main.REGISTRATE.block("fusion_reactor_casing", AcceleratorOutputBlock::new).initialProperties(SharedProperties::softMetal).properties((p) -> p.strength(2.0F, 2.0F)).tag(new TagKey[]{AllBlockTags.SAFE_NBT.tag, AllBlockTags.WRENCH_PICKUP.tag}).item().transform(ModelGen.customItemModel())).register();
        FUSION_REACTOR_CORE = ((BlockBuilder)Main.REGISTRATE.block("fusion_reactor_core", ParticleAcceleratorCoreBlock::new).initialProperties(SharedProperties::softMetal).properties((p) -> p.strength(2.0F, 2.0F)).tag(new TagKey[]{AllBlockTags.SAFE_NBT.tag, AllBlockTags.WRENCH_PICKUP.tag}).item().transform(ModelGen.customItemModel())).register();

    }
}
