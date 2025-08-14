package de.wutzuket.create_overdrive.compat;

import de.wutzuket.create_overdrive.Main;
import de.wutzuket.create_overdrive.recipe.AcceleratorRecipe;
import de.wutzuket.create_overdrive.recipe.AcceleratorRecipeInput;
import de.wutzuket.create_overdrive.recipe.ModRecipes;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.List;

import static de.wutzuket.create_overdrive.index.CPABlocks.PARTICLE_ACCELERATOR_CORE;

@JeiPlugin
public class JEIAcceleratorPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(Main.MODID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new ParticalAcceleratorCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();

        List<AcceleratorRecipe> acceleratorRecipes = recipeManager.getAllRecipesFor(ModRecipes.ACCELERATOR_RECIPE_TYPE.get()).stream().map(RecipeHolder::value).toList();
        registration.addRecipes(ParticalAcceleratorCategory.TYPE, acceleratorRecipes);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(PARTICLE_ACCELERATOR_CORE.get()), ParticalAcceleratorCategory.TYPE);
    }
}
