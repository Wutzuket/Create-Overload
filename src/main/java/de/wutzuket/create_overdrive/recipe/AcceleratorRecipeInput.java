package de.wutzuket.create_overdrive.recipe;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

/**
 * Eine Klasse, die als Eingabe für das AcceleratorRecipe dient.
 * Implementiert RecipeInput und kapselt einen SimpleContainer.
 */
public class AcceleratorRecipeInput implements RecipeInput {

    private final SimpleContainer container;

    public AcceleratorRecipeInput(net.minecraft.world.SimpleContainer container) {
        this.container = container;
    }

    @Override
    public ItemStack getItem(int i) {
        return this.container.getItem(i);
    }

    @Override
    public int size() {
        return this.container.getContainerSize();
    }
}
