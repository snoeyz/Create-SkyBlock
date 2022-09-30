package com.snoeyz.skycreate.compat.jei.category.animations;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.snoeyz.skycreate.components.pulverizer.PulverizerBlock;
import com.snoeyz.skycreate.registry.SCBlockPartials;
import com.snoeyz.skycreate.registry.SCBlocks;
import net.minecraft.core.Direction;

public class AnimatedPulverizer extends AnimatedKinetics {

    @Override
    public void draw(PoseStack matrixStack, int xOffset, int yOffset) {
        matrixStack.pushPose();
        matrixStack.translate(xOffset, yOffset, 100);
        matrixStack.mulPose(Vector3f.XP.rotationDegrees(-15.5f));
        matrixStack.mulPose(Vector3f.YP.rotationDegrees(22.5f));
        int scale = 20;

        blockElement(shaft(Direction.Axis.Z))
                .rotateBlock(0, 0, getCurrentAngle())
                .scale(scale)
                .render(matrixStack);

        blockElement(SCBlocks.PULVERIZER.getDefaultState()
                .setValue(PulverizerBlock.FACING, Direction.DOWN)
                .setValue(PulverizerBlock.AXIS_ALONG_FIRST_COORDINATE, false))
                .scale(scale)
                .render(matrixStack);

        float cycle = (AnimationTickHolder.getRenderTime() - offset * 8) % 30;
        float offset = cycle < 10 ? cycle / 10f : cycle < 20 ? (20 - cycle) / 10f : 0;

        matrixStack.pushPose();

        matrixStack.translate(0, offset * 17, 0);
        blockElement(AllBlockPartials.DEPLOYER_POLE)
                .rotateBlock(90, 0, 0)
                .scale(scale)
                .render(matrixStack);
        blockElement(SCBlockPartials.PULVERIZER)
                .rotateBlock(90, 0, 0)
                .scale(scale)
                .render(matrixStack);

        matrixStack.popPose();

//        blockElement(AllBlocks.DEPOT.getDefaultState())
//                .atLocal(0, 2, 0)
//                .scale(scale)
//                .render(matrixStack);

        matrixStack.popPose();
    }

}
