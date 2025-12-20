package de.wutzuket.create_overdrive.index;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import de.wutzuket.create_overdrive.Main;
import net.minecraft.resources.ResourceLocation;

public class CPAPartialModels {
    public static final PartialModel
            ROTATORWHEEL = block("rotatorwheel/block");


    private static PartialModel block(String path) {
        return PartialModel.of(ResourceLocation.fromNamespaceAndPath(Main.MODID, "block/" + path));
    }


    public static void init() {
        // init static fields
    }

}
