package de.wutzuket.create_overdrive.compat;

import de.wutzuket.create_overdrive.Main;
import de.wutzuket.create_overdrive.index.CPABlocks;
import de.wutzuket.create_overdrive.recipe.AcceleratorRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("removal")
public class ParticalAcceleratorCategory implements IRecipeCategory<AcceleratorRecipe> {

    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(Main.MODID, "particle_accelerator");
    public static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Main.MODID, "textures/gui/accelerator_gui.png");

    public static final RecipeType<AcceleratorRecipe> TYPE =
            new RecipeType<>(UID, AcceleratorRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    public ParticalAcceleratorCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 0, 0, 168, 90);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(CPABlocks.PARTICLE_ACCELERATOR_CORE));
    }


    @Override
    public RecipeType<AcceleratorRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("JEI.create_overdrive.particle_accelerator_core");
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder iRecipeLayoutBuilder, AcceleratorRecipe recipe, IFocusGroup iFocusGroup) {
        iRecipeLayoutBuilder.addSlot(RecipeIngredientRole.INPUT, 61, 7).addIngredients(recipe.getIngredients().get(0));
        iRecipeLayoutBuilder.addSlot(RecipeIngredientRole.INPUT, 91, 7).addIngredients(recipe.getIngredients().get(1));

        iRecipeLayoutBuilder.addSlot(RecipeIngredientRole.OUTPUT, 76, 55).addItemStack(recipe.getResultItem(null));
    }

}
