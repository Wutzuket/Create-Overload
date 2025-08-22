package de.wutzuket.create_overdrive.compat;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import de.wutzuket.create_overdrive.Main;
import de.wutzuket.create_overdrive.index.CPABlocks;
import de.wutzuket.create_overdrive.recipe.AcceleratorRecipe;
import de.wutzuket.create_overdrive.recipe.IonatorRecipe;
import de.wutzuket.create_overdrive.recipe.ModRecipes;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.ArrayList;
import java.util.List;

import static de.wutzuket.create_overdrive.index.CPABlocks.PARTICLE_ACCELERATOR_CORE;


@JeiPlugin
public class JEIAcceleratorPlugin implements IModPlugin {

    private final List<CreateRecipeCategory<?>> allCategories = new ArrayList<>();

    public static final ResourceLocation IONATOR_UID = ResourceLocation.fromNamespaceAndPath(Main.MODID, "ionator");
    public static final RecipeType<IonatorRecipe> IONATOR_TYPE = new RecipeType<>(IONATOR_UID, IonatorRecipe.class);

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(Main.MODID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        loadCategories();
        registration.addRecipeCategories(
                new ParticalAcceleratorCategory(registration.getJeiHelpers().getGuiHelper())
        );
        registration.addRecipeCategories(allCategories.toArray(CreateRecipeCategory[]::new));
    }

    private void loadCategories() {
        allCategories.clear();

        CreateRecipeCategory<?> ionating = builder(BasinRecipe.class)
                .addTypedRecipes(() -> ModRecipes.IONATOR_RECIPE_TYPE.get())
                .catalyst(() -> new ItemStack(CPABlocks.IONATOR.get()).getItem())
                .catalyst(() -> new ItemStack(AllBlocks.BASIN.get()).getItem())
                .itemIcon(() -> new ItemStack(CPABlocks.IONATOR.get()).getItem())
                .emptyBackground(177, 103)
                .build("ionator", IonatorCategory::standard);
    }

    private <T extends Recipe<? extends RecipeInput>> CategoryBuilder<T> builder(Class<T> recipeClass) {
        return new CategoryBuilder<>(recipeClass);
    }

    private class CategoryBuilder<T extends Recipe<?>> extends CreateRecipeCategory.Builder<T> {
        public CategoryBuilder(Class<? extends T> recipeClass) {
            super(recipeClass);
        }

        @Override
        public CreateRecipeCategory<T> build(ResourceLocation id, CreateRecipeCategory.Factory<T> factory) {
            CreateRecipeCategory<T> category = super.build(id, factory);
            allCategories.add(category);
            return category;
        }
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();

        List<AcceleratorRecipe> acceleratorRecipes = recipeManager
                .getAllRecipesFor(ModRecipes.ACCELERATOR_RECIPE_TYPE.get())
                .stream().map(RecipeHolder::value).toList();
        registration.addRecipes(ParticalAcceleratorCategory.TYPE, acceleratorRecipes);

        allCategories.forEach(c -> c.registerRecipes(registration));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(PARTICLE_ACCELERATOR_CORE.get()), ParticalAcceleratorCategory.TYPE);
        allCategories.forEach(c -> c.registerCatalysts(registration));
    }
}
