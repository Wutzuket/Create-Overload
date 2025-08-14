package de.wutzuket.create_overdrive.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.wutzuket.create_overdrive.Main;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public record AcceleratorRecipe(NonNullList<Ingredient> inputs, ItemStack output) implements Recipe<AcceleratorRecipeInput> {

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.add(inputs.get(0));
        list.add(inputs.get(1));
        return list;
    }

    @Override
    public boolean matches(AcceleratorRecipeInput acceleratorRecipeInput, Level level) {
        if (level.isClientSide) return false;

        for (Ingredient ingredient : inputs) {
            boolean matched = false;
            for (int i = 0; i < acceleratorRecipeInput.size(); i++) {
                if (ingredient.test(acceleratorRecipeInput.getItem(i))) {
                    matched = true;
                    break;
                }
            }
            if (!matched) return false;
        }
        return true;
    }

    @Override
    public ItemStack assemble(AcceleratorRecipeInput acceleratorRecipeInput, HolderLookup.Provider provider) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int i, int i1) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider provider) {
        return output;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.ACCELERATOR_RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.ACCELERATOR_RECIPE_TYPE.get();
    }

    public static class Serializer implements RecipeSerializer<AcceleratorRecipe> {
        public static final MapCodec<AcceleratorRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Ingredient.CODEC.listOf().fieldOf("ingredients").forGetter(AcceleratorRecipe::inputs),
                ItemStack.CODEC.fieldOf("result").forGetter(AcceleratorRecipe::output)
        ).apply(inst, (ingredients, result) -> {
            NonNullList<Ingredient> nonNullIngredients = NonNullList.create();
            nonNullIngredients.addAll(ingredients);
            return new AcceleratorRecipe(nonNullIngredients, result);
        }));

        // Korrigierter StreamCodec
        public static final StreamCodec<RegistryFriendlyByteBuf, AcceleratorRecipe> STREAM_CODEC = new StreamCodec<>() {
            @Override
            public void encode(RegistryFriendlyByteBuf buf, AcceleratorRecipe value) {
                // Zuerst die Größe der Ingredients-Liste schreiben
                buf.writeVarInt(value.inputs.size());
                // Dann jedes Ingredient einzeln codieren
                for (Ingredient ingredient : value.inputs) {
                    Ingredient.CONTENTS_STREAM_CODEC.encode(buf, ingredient);
                }
                // Zum Schluss das Ergebnis-ItemStack
                ItemStack.STREAM_CODEC.encode(buf, value.output);
            }

            @Override
            public AcceleratorRecipe decode(RegistryFriendlyByteBuf buf) {
                // Zuerst die Größe der Liste lesen
                int size = buf.readVarInt();
                // Die Ingredients-Liste erstellen
                NonNullList<Ingredient> ingredients = NonNullList.create();
                for (int i = 0; i < size; i++) {
                    ingredients.add(Ingredient.CONTENTS_STREAM_CODEC.decode(buf));
                }
                // Das Ergebnis-ItemStack lesen
                ItemStack output = ItemStack.STREAM_CODEC.decode(buf);
                // Das AcceleratorRecipe-Objekt erstellen
                return new AcceleratorRecipe(ingredients, output);
            }
        };

        @Override
        public MapCodec<AcceleratorRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, AcceleratorRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
