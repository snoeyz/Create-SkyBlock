package com.snoeyz.skycreate.datagen.recipe;

import com.mojang.logging.LogUtils;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class SkyCreateRecipeProvider extends RecipeProvider {

    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    protected final List<GeneratedRecipe> all = new ArrayList<>();

    public SkyCreateRecipeProvider(DataGenerator gen) {
        super(gen);
    }

    @Override
    protected void buildCraftingRecipes(Consumer<FinishedRecipe> p_200404_1_) {
        all.forEach(c -> c.register(p_200404_1_));
        LOGGER.info(getName() + " registered " + all.size() + " recipe" + (all.size() == 1 ? "" : "s"));
    }

    protected GeneratedRecipe register(GeneratedRecipe recipe) {
        all.add(recipe);
        return recipe;
    }

    @FunctionalInterface
    public interface GeneratedRecipe {
        void register(Consumer<FinishedRecipe> recipe);
    }
}
