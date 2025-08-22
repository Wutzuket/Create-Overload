package de.wutzuket.create_overdrive.recipe;

import de.wutzuket.create_overdrive.Main;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;
import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public class IonatorRecipe extends BasinRecipe {

    private static final ResourceLocation IONATOR_ID = ResourceLocation.fromNamespaceAndPath(Main.MODID, "ionator");

    public static final IRecipeTypeInfo TYPE_INFO = new IRecipeTypeInfo() {
        @Override public ResourceLocation getId() { return IONATOR_ID; }
        @Override public RecipeSerializer<?> getSerializer() { return ModRecipes.IONATOR_RECIPE_SERIALIZER.get(); }
        @SuppressWarnings("unchecked")
        @Override public <I extends RecipeInput, R extends Recipe<I>> RecipeType<R> getType() {
            return (RecipeType<R>) ModRecipes.IONATOR_RECIPE_TYPE.get();
        }
    };

    public IonatorRecipe(ProcessingRecipeParams params) {
        super(TYPE_INFO, params);
    }

    public static class Serializer extends StandardProcessingRecipe.Serializer<IonatorRecipe> {
        public Serializer() { super(IonatorRecipe::new); }
    }
}
