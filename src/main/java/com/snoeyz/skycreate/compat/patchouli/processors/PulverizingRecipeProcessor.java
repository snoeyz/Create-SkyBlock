package com.snoeyz.skycreate.compat.patchouli.processors;

import com.snoeyz.skycreate.datagen.recipe.PulverizingBlockOutput;
import com.snoeyz.skycreate.recipe.PulverizingRecipe;

import net.minecraft.resources.ResourceLocation;
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
                return IVariable.from(output);
            }
        } else if (key.startsWith("dOut")) {
            int ndx = Integer.parseInt(key.substring(4)) - 1;
            if (recipe.getOutputItems().size() > ndx) {
                return IVariable.from(recipe.getOutputItems().get(ndx));
            }
        } else if (key.equals("input")) {
            return IVariable.from(recipe.getIngredient());
        } else if (key.equals("pageTitle")) {
            String recipeName = recipe.getId().getPath();
            if (recipeName.indexOf("/") != -1) {
                recipeName = recipeName.substring(recipeName.lastIndexOf("/") + 1);
            }
            return IVariable.wrap(recipeName.replaceAll("_", " "));
        }

        return null;
    }
    
}
