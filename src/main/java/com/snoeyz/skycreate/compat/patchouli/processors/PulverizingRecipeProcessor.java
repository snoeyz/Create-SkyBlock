package com.snoeyz.skycreate.compat.patchouli.processors;

import com.simibubi.create.content.contraptions.processing.ProcessingOutput;
import com.snoeyz.skycreate.datagen.recipe.PulverizingBlockOutput;
import com.snoeyz.skycreate.recipe.PulverizingRecipe;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import vazkii.patchouli.api.IComponentProcessor;
import vazkii.patchouli.api.IVariable;
import vazkii.patchouli.api.IVariableProvider;

public class PulverizingRecipeProcessor implements IComponentProcessor {

    private PulverizingRecipe recipe;

    @Override
    public void setup(IVariableProvider variables) {
        String recipeId = variables.get("recipe").asString();
        recipe = PulverizingRecipe.getRecipe(new ResourceLocation(recipeId));
    }

    @Override
    public IVariable process(String key) {
        if (key.startsWith("bOut")) {
            int ndx = Integer.parseInt(key.substring(4)) - 1;
            if (recipe.getOutputBlocks().size() > ndx) {
                PulverizingBlockOutput output = recipe.getOutputBlocks().get(ndx);
                if (output.getFluidStack().isPresent()) {
                    return IVariable.wrap(output.getFluidStack().get().getFluid().getRegistryName().getPath());
                }
                return IVariable.from(output);
            }
            return IVariable.from(ItemStack.EMPTY);
        } else if (key.startsWith("dOutChance")) {
            int ndx = Integer.parseInt(key.substring(10)) - 1;
            if (recipe.getOutputItems().size() > ndx) {
                ProcessingOutput output = recipe.getOutputItems().get(ndx);
                return IVariable.wrap(output.getChance() * 100);
            }
        } else if (key.startsWith("dOut")) {
            int ndx = Integer.parseInt(key.substring(4)) - 1;
            if (recipe.getOutputItems().size() > ndx) {
                return IVariable.from(recipe.getOutputItems().get(ndx));
            }
            return IVariable.from(ItemStack.EMPTY);
        } else if (key.equals("input")) {
            return IVariable.from(recipe.getIngredient().getItems()[0]);
        }

        return null;
    }
    
}
