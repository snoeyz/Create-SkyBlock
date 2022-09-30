package com.snoeyz.skycreate.compat.jei.category;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.contraptions.processing.ProcessingOutput;
import com.snoeyz.skycreate.compat.jei.category.animations.AnimatedPulverizer;
import com.snoeyz.skycreate.recipe.PulverizingRecipe;
import com.snoeyz.skycreate.gui.SCGuiTextures;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public class PulverizingCategory extends SkyCreateRecipeCategory<PulverizingRecipe> {

    private final AnimatedPulverizer pulverizer = new AnimatedPulverizer();

    public PulverizingCategory(Info<PulverizingRecipe> info) {
        super(info);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, PulverizingRecipe recipe, IFocusGroup focuses) {
        builder
                .addSlot(RecipeIngredientRole.INPUT, 30, 48)
                .setBackground(getRenderedSlot(), -1, -1)
                .addIngredients(recipe.getIngredients().get(0));

        List<ProcessingOutput> results = recipe.getRollableResults();
        int i = 0;
        for (ProcessingOutput output : results) {
            int xOffset = i % 2 == 0 ? 0 : 19;
            int yOffset = (i / 2) * -19;
            builder
                    .addSlot(RecipeIngredientRole.OUTPUT, 118 + xOffset, 48 + yOffset)
                    .setBackground(getRenderedSlot(output), -1, -1)
                    .addItemStack(output.getStack())
                    .addTooltipCallback(addStochasticTooltip(output));
            i++;
        }
    }

    @Override
    public void draw(PulverizingRecipe recipe, IRecipeSlotsView iRecipeSlotsView, PoseStack matrixStack, double mouseX, double mouseY) {
        SCGuiTextures.JEI_SHADOW.render(matrixStack, 72 - 17, 42 + 13);
        SCGuiTextures.JEI_ARROW.render(matrixStack, 35, 52);

        pulverizer.draw(matrixStack, 72, 42);
    }

}
