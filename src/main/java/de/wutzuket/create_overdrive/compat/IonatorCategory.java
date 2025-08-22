package de.wutzuket.create_overdrive.compat;

import com.simibubi.create.compat.jei.category.BasinCategory;
import com.simibubi.create.compat.jei.category.animations.AnimatedBlazeBurner;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import de.wutzuket.create_overdrive.compat.animated.AnimatedIonator;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import net.minecraft.client.gui.GuiGraphics;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class IonatorCategory extends BasinCategory {

    private final AnimatedIonator ionator = new AnimatedIonator();
    private final AnimatedBlazeBurner heater = new AnimatedBlazeBurner();
    IonatorType type;

    enum IonatorType {
        IONATING, AUTO_IONATING
    }

    public static IonatorCategory standard(Info<BasinRecipe> info) {
        return new IonatorCategory(info, IonatorType.IONATING);
    }

    public static IonatorCategory autoIonating(Info<BasinRecipe> info) {
        return new IonatorCategory(info, IonatorType.AUTO_IONATING);
    }

    protected IonatorCategory(Info<BasinRecipe> info, IonatorType type) {
        super(info, type != IonatorType.AUTO_IONATING);
        this.type = type;
    }

    @Override
    public void draw(BasinRecipe recipe, IRecipeSlotsView iRecipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        super.draw(recipe, iRecipeSlotsView, graphics, mouseX, mouseY);

        HeatCondition requiredHeat = recipe.getRequiredHeat();
        if (requiredHeat != HeatCondition.NONE)
            heater.withHeat(requiredHeat.visualizeAsBlazeBurner())
                .draw(graphics, getBackground().getWidth() / 2 + 3, 55);
        ionator.draw(graphics, getBackground().getWidth() / 2 + 3, 34);
    }
}
