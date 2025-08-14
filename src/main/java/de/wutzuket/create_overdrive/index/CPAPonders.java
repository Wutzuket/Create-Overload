package de.wutzuket.create_overdrive.index;

import com.simibubi.create.infrastructure.ponder.AllCreatePonderTags;
import com.tterrag.registrate.util.entry.ItemProviderEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import de.wutzuket.create_overdrive.ponder.CPAPonderScenes;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

public class CPAPonders {
    public CPAPonders() {
    }

    public static void registerTags(PonderTagRegistrationHelper<ResourceLocation> helper) {
        PonderTagRegistrationHelper<RegistryEntry<?,?>> HELPER = helper.withKeyFunction(RegistryEntry::getId);
        HELPER.addToTag(AllCreatePonderTags.KINETIC_APPLIANCES).add(CPABlocks.PARTICLE_ACCELERATOR_CORE);
    }

    public static void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        PonderSceneRegistrationHelper<ItemProviderEntry<?,?>> HELPER = helper.withKeyFunction(RegistryEntry::getId);
        HELPER.addStoryBoard(CPABlocks.PARTICLE_ACCELERATOR_CORE, "particle_accelerator_core", 
                CPAPonderScenes::particleAcceleratorCoreScene, 
                new ResourceLocation[]{AllCreatePonderTags.KINETIC_APPLIANCES});
    }
}
