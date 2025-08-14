package de.wutzuket.create_overdrive.ponder;


import de.wutzuket.create_overdrive.Main;
import de.wutzuket.create_overdrive.index.CPAPonders;
import com.simibubi.create.foundation.ponder.PonderWorldBlockEntityFix;
import net.createmod.ponder.api.level.PonderLevel;
import net.createmod.ponder.api.registration.IndexExclusionHelper;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.createmod.ponder.api.registration.SharedTextRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

public class CPAPonderPlugin implements PonderPlugin {
    public CPAPonderPlugin() {
    }

    public String getModId() {
        return Main.MODID;
    }

    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        CPAPonders.registerScenes(helper);
    }

    public void registerTags(PonderTagRegistrationHelper<ResourceLocation> helper) {
        CPAPonders.registerTags(helper);
    }

    public void registerSharedText(SharedTextRegistrationHelper helper) {
    }

    public void onPonderLevelRestore(PonderLevel ponderLevel) {
        PonderWorldBlockEntityFix.fixControllerBlockEntities(ponderLevel);
    }

    public void indexExclusions(IndexExclusionHelper helper) {
    }
}
