package com.snoeyz.skycreate.compat.patchouli.components;

import java.util.function.UnaryOperator;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.snoeyz.skycreate.datagen.Lang;
import com.snoeyz.skycreate.datagen.recipe.PulverizingBlockOutput;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import vazkii.patchouli.api.IComponentRenderContext;
import vazkii.patchouli.api.IVariable;

public class ComponentPulverizingBlockOutput extends TemplateComponentBase {

    private transient static final int HEIGHT = 16;
    private transient static final int WIDTH = 16;

    public IVariable output;

    private transient PulverizingBlockOutput blockOutput;
    // TODO: Try using code from JEI's RecipeSlot to render things like animated water and block tag ingredients (i.e. Logs)

    @Override
    public void onVariablesAvailable(UnaryOperator<IVariable> lookup) {
        blockOutput = lookup.apply(output).as(PulverizingBlockOutput.class);
    }

    @Override
    public void render(PoseStack ms, IComponentRenderContext context, float pticks, int mouseX, int mouseY) {
        if (blockOutput != null) {
            ms.pushPose();
            {
                ms.translate(x, y, 0);
                RenderSystem.enableBlend();
                Component tooltip = Lang.translateDirect("recipe.pulverizing.output").withStyle(ChatFormatting.GRAY);
                if (blockOutput.getFluidStack().isPresent()) {
                    drawFluid(ms, blockOutput.getFluidStack().get());
                    if (context.isAreaHovered(mouseX, mouseY, x, y, WIDTH, HEIGHT)) {
                        setTooltip(context, blockOutput.getFluidStack().get(), tooltip);
                    }
                } else {
                    drawItem(ms, blockOutput.getStack().get());
                    if (context.isAreaHovered(mouseX, mouseY, x, y, WIDTH, HEIGHT)) {
                        setTooltip(context, blockOutput.getStack().get(), tooltip);
                    }
                }
                RenderSystem.disableBlend();
            }
            ms.popPose();
        }
    }
}
