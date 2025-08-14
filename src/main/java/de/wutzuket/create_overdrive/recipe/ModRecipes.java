package de.wutzuket.create_overdrive.recipe;

import de.wutzuket.create_overdrive.Main;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, Main.MODID);

    public static final DeferredRegister<RecipeType<?>> TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, Main.MODID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<AcceleratorRecipe>> ACCELERATOR_RECIPE_SERIALIZER =
            SERIALIZERS.register("particle_accelerator", AcceleratorRecipe.Serializer::new);
    public static final DeferredHolder<RecipeType<?>, RecipeType<AcceleratorRecipe>> ACCELERATOR_RECIPE_TYPE =
            TYPES.register("particle_accelerator", () -> new RecipeType<AcceleratorRecipe>() {
                @Override
                public String toString() {
                    return "particle_accelerator";
                }
            });


    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
        TYPES.register(eventBus);
    }
}
