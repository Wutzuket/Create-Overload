package de.wutzuket.create_overdrive.index;

import com.simibubi.create.foundation.block.connected.AllCTTypes;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import de.wutzuket.create_overdrive.Main;
import net.minecraft.resources.ResourceLocation;

import static com.simibubi.create.foundation.block.connected.CTSpriteShifter.getCT;

public class CPASpriteShifts {
    public static final CTSpriteShiftEntry ROTATOR_CASING = omni("rotator_casing"),
    FUSION_REACTOR_CASING = omni("fusion_reactor_casing");

    private static CTSpriteShiftEntry omni(String name) {
        return getCT(AllCTTypes.OMNIDIRECTIONAL,
            ResourceLocation.fromNamespaceAndPath(Main.MODID, "block/" + name),
            ResourceLocation.fromNamespaceAndPath(Main.MODID, "block/" + name + "_connected"));
    }
}
