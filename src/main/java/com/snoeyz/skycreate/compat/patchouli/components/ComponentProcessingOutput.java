package com.snoeyz.skycreate.compat.patchouli.components;

import java.util.function.UnaryOperator;


import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.contraptions.processing.ProcessingOutput;
import com.snoeyz.skycreate.datagen.Lang;

import net.minecraft.ChatFormatting;
import vazkii.patchouli.api.IComponentRenderContext;
import vazkii.patchouli.api.IVariable;

public class ComponentProcessingOutput extends TemplateComponentBase {

    private transient static final int HEIGHT = 16;
    private transient static final int WIDTH = 16;

    public IVariable output;

    private transient ProcessingOutput itemOutput;

    @Override
    public void onVariablesAvailable(UnaryOperator<IVariable> lookup) {
        itemOutput = lookup.apply(output).as(ProcessingOutput.class);
    }

    @Override
    public void render(PoseStack ms, IComponentRenderContext context, float pticks, int mouseX, int mouseY) {
        if (itemOutput != null) {
            ms.pushPose();
            {
                ms.translate(x, y, 0);
                RenderSystem.enableBlend();
                drawItem(ms, itemOutput.getStack());
                if (context.isAreaHovered(mouseX, mouseY, x, y, WIDTH, HEIGHT)) {
                    float chance = itemOutput.getChance();
                    setTooltip(
                        context,
                        itemOutput.getStack(),
                        Lang.translateDirect("recipe.pulverizing.drop").withStyle(ChatFormatting.GRAY),
                        Lang.translateDirect("recipe.processing.chance", chance < 0.01 ? "<1" : (int) (chance * 100))
                            .withStyle(ChatFormatting.GOLD));
                }
                RenderSystem.disableBlend();
            }
            ms.popPose();
        }
    }
}
