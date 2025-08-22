package de.wutzuket.create_overdrive.compat.animated;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics;
import de.wutzuket.create_overdrive.index.CPABlocks;
import net.minecraft.client.gui.GuiGraphics;

public class AnimatedIonator extends AnimatedKinetics {

    @Override
    public void draw(GuiGraphics graphics, int xOffset, int yOffset) {
        PoseStack matrixStack = graphics.pose();
        matrixStack.pushPose();
        matrixStack.translate(xOffset, yOffset, 200);
        matrixStack.mulPose(Axis.XP.rotationDegrees(-15.5f));
        matrixStack.mulPose(Axis.YP.rotationDegrees(22.5f));
        int scale = 23;

        blockElement(cogwheel())
                .rotateBlock(0, getCurrentAngle() * 2, 0)
                .atLocal(0, 0, 0)
                .scale(scale)
                .render(graphics);

        blockElement(CPABlocks.IONATOR.getDefaultState())
                .atLocal(0, -0.25, 0)
                .scale(scale)
                .render(graphics);

        blockElement(AllBlocks.BASIN.getDefaultState())
                .atLocal(0, 1.65, 0)
                .scale(scale)
                .render(graphics);

        matrixStack.popPose();
    }

}
