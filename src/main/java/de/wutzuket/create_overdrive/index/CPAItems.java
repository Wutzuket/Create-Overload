package de.wutzuket.create_overdrive.index;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;

import static de.wutzuket.create_overdrive.Main.ITEMS;

public class CPAItems {
    public static final DeferredItem<Item> STELLAR_STEEL_ALLOY;
    public static final DeferredItem<Item> VERDANT_STEEL;

    static {
        STELLAR_STEEL_ALLOY = ITEMS.registerSimpleItem("stellar_steel_alloy");
        VERDANT_STEEL = ITEMS.registerSimpleItem("verdant_steel");
    }

    public static void register() {
    }
}
