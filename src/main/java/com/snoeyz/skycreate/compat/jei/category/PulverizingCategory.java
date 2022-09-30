package com.snoeyz.skycreate.compat.jei.category;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.contraptions.processing.ProcessingOutput;
import com.snoeyz.skycreate.compat.jei.category.animations.AnimatedPulverizer;
import com.snoeyz.skycreate.datagen.Lang;
import com.snoeyz.skycreate.recipe.PulverizingRecipe;
import com.snoeyz.skycreate.gui.SCGuiTextures;

import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotTooltipCallback;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.ChatFormatting;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class PulverizingCategory extends SkyCreateRecipeCategory<PulverizingRecipe> {

    private final AnimatedPulverizer pulverizer = new AnimatedPulverizer();

    public PulverizingCategory(Info<PulverizingRecipe> info) {
        super(info);
    }

    public static IRecipeSlotTooltipCallback addPulverizeTooltip(boolean isDrop, ProcessingOutput output) {
        return (view, tooltip) -> {
            tooltip.add(1, Lang.translateDirect(isDrop ? "recipe.pulverizing.drop" : "recipe.pulverizing.output")
                .withStyle(ChatFormatting.DARK_GRAY));
            if (output != null) {
                float chance = output.getChance();
                if (chance != 1)
                    tooltip.add(2, Lang.translateDirect("recipe.processing.chance", chance < 0.01 ? "<1" : (int) (chance * 100))
                            .withStyle(ChatFormatting.GOLD));
            }
        };
    }

    public static IRecipeSlotTooltipCallback addPulverizeTooltip(boolean isDrop) {
        return addPulverizeTooltip(isDrop, null);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, PulverizingRecipe recipe, IFocusGroup focuses) {
        builder
                .addSlot(RecipeIngredientRole.INPUT, 30, 48)
                .setBackground(getRenderedSlot(), -1, -1)
                .addIngredients(recipe.getIngredients().get(0));

        int i = 0;
        for (FluidStack fluidResult : recipe.getOutputFluids()) {
            int xOffset = (i % 3) * 19;
            int yOffset = (i / 3) * -19;
            builder
                    .addSlot(RecipeIngredientRole.OUTPUT, 99 + xOffset, 48 + yOffset)
                    .setBackground(getRenderedSlot(), -1, -1)
                    .addIngredient(ForgeTypes.FLUID_STACK, withImprovedVisibility(fluidResult))
                    .addTooltipCallback(addPulverizeTooltip(false));
            i++;
        }
        for (ProcessingOutput output : recipe.getOutputBlocks()) {
            int xOffset = (i % 3) * 19;
            int yOffset = (i / 3) * -19;
            builder
                    .addSlot(RecipeIngredientRole.OUTPUT, 99 + xOffset, 48 + yOffset)
                    .setBackground(getRenderedSlot(output), -1, -1)
                    .addItemStack(output.getStack())
                    .addTooltipCallback(addPulverizeTooltip(false, output));
            i++;
        }
        if (i > 0) {
            i += 5;
            i = i - (i % 3);
        }
        for (ProcessingOutput output : recipe.getOutputItems()) {
            int xOffset = (i % 3) * 19;
            int yOffset = (i / 3) * -19;
            builder
                    .addSlot(RecipeIngredientRole.OUTPUT, 99 + xOffset, 48 + yOffset)
                    .setBackground(getRenderedSlot(output), -1, -1)
                    .addItemStack(output.getStack())
                    .addTooltipCallback(addPulverizeTooltip(true, output));
            i++;
        }
    }

    @Override
    public void draw(PulverizingRecipe recipe, IRecipeSlotsView iRecipeSlotsView, PoseStack matrixStack, double mouseX, double mouseY) {
        //SCGuiTextures.JEI_SHADOW.render(matrixStack, 72 - 17, 42 + 13);
        SCGuiTextures.JEI_ARROW.render(matrixStack, 45, 52);
        // SCGuiTextures.JEI_UP_ARROW.render(matrixStack, 50, 45);

        pulverizer.draw(matrixStack, 27, 27);
    }

    public static FluidStack withImprovedVisibility(FluidStack stack) {
        FluidStack display = stack.copy();
        int displayedAmount = (int) (stack.getAmount() * .75f) + 250;
        display.setAmount(displayedAmount);
        return display;
    }

}
