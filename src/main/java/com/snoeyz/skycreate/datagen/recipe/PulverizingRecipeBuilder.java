package com.snoeyz.skycreate.datagen.recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.simibubi.create.content.contraptions.processing.ProcessingOutput;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.snoeyz.skycreate.recipe.PulverizingRecipe;
import com.snoeyz.skycreate.registry.SCRecipeTypes;
import com.tterrag.registrate.util.DataIngredient;

import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.fluids.FluidStack;

public class PulverizingRecipeBuilder {

    protected PulverizingRecipeParams params;
    protected List<ICondition> recipeConditions;

    public PulverizingRecipeBuilder(ResourceLocation recipeId) {
        params = new PulverizingRecipeParams(recipeId);
        recipeConditions = new ArrayList<>();
    }

    public PulverizingRecipeBuilder withItemIngredients(Ingredient... ingredients) {
        return withItemIngredients(NonNullList.of(Ingredient.EMPTY, ingredients));
    }

    public PulverizingRecipeBuilder withItemIngredients(NonNullList<Ingredient> ingredients) {
        params.ingredients = ingredients;
        return this;
    }

    public PulverizingRecipeBuilder withSingleItemOutput(ItemStack output) {
        return withItemOutputs(new ProcessingOutput(output, 1));
    }

    public PulverizingRecipeBuilder withItemOutputs(ProcessingOutput... outputs) {
        return withItemOutputs(NonNullList.of(ProcessingOutput.EMPTY, outputs));
    }

    public PulverizingRecipeBuilder withItemOutputs(NonNullList<ProcessingOutput> outputs) {
        params.itemResults = outputs;
        return this;
    }

    public PulverizingRecipeBuilder withBlockOutputs(ProcessingOutput... outputs) {
        return withBlockOutputs(NonNullList.of(ProcessingOutput.EMPTY, outputs));
    }

    public PulverizingRecipeBuilder withBlockOutputs(NonNullList<ProcessingOutput> outputs) {
        params.blockResults = outputs;
        return this;
    }

    public PulverizingRecipeBuilder duration(int ticks) {
        params.processingDuration = ticks;
        return this;
    }

    public PulverizingRecipeBuilder averageProcessingDuration() {
        return duration(100);
    }

    public PulverizingRecipe build() {
        return new PulverizingRecipe(params);
    }

    public void build(Consumer<FinishedRecipe> consumer) {
        consumer.accept(new DataGenResult(build(), recipeConditions));
    }

    // Datagen shortcuts

    public PulverizingRecipeBuilder require(TagKey<Item> tag) {
        return require(Ingredient.of(tag));
    }

    public PulverizingRecipeBuilder require(ItemLike item) {
        return require(Ingredient.of(item));
    }

    public PulverizingRecipeBuilder require(Ingredient ingredient) {
        params.ingredients.add(ingredient);
        return this;
    }
    
    public PulverizingRecipeBuilder require(ResourceLocation ingredient) {
        params.ingredients.add(DataIngredient.ingredient(null, ingredient));
        return this;
    }

    public PulverizingRecipeBuilder output(ItemLike item) {
        return output(item, 1);
    }

    public PulverizingRecipeBuilder output(float chance, ItemLike item) {
        return output(chance, item, 1);
    }

    public PulverizingRecipeBuilder output(ItemLike item, int amount) {
        return output(1, item, amount);
    }

    public PulverizingRecipeBuilder output(float chance, ItemLike item, int amount) {
        return output(chance, new ItemStack(item, amount));
    }

    public PulverizingRecipeBuilder output(ItemStack output) {
        return output(1, output);
    }

    public PulverizingRecipeBuilder output(float chance, ItemStack output) {
        return output(new ProcessingOutput(output, chance));
    }
    
    public PulverizingRecipeBuilder output(ProcessingOutput output) {
        params.itemResults.add(output);
        return this;
    }

    public PulverizingRecipeBuilder blockOutput(ItemLike item) {
        return blockOutput(item, 1);
    }

    public PulverizingRecipeBuilder blockOutput(float chance, ItemLike item) {
        return blockOutput(chance, item, 1);
    }

    public PulverizingRecipeBuilder blockOutput(ItemLike item, int amount) {
        return blockOutput(1, item, amount);
    }

    public PulverizingRecipeBuilder blockOutput(float chance, ItemLike item, int amount) {
        return blockOutput(chance, new ItemStack(item, amount));
    }

    public PulverizingRecipeBuilder blockOutput(ItemStack output) {
        return blockOutput(1, output);
    }

    public PulverizingRecipeBuilder blockOutput(float chance, ItemStack output) {
        return blockOutput(new ProcessingOutput(output, chance));
    }
    
    public PulverizingRecipeBuilder blockOutput(ProcessingOutput output) {
        params.blockResults.add(output);
        return this;
    }

    public PulverizingRecipeBuilder blockOutput(Fluid fluid, int amount) {
        fluid = FluidHelper.convertToStill(fluid);
        return blockOutput(new FluidStack(fluid, amount));
    }

    public PulverizingRecipeBuilder blockOutput(FluidStack fluidStack) {
        params.fluidResults.add(fluidStack);
        return this;
    }

    public PulverizingRecipeBuilder withFluidOutputs(FluidStack... outputs) {
        return withFluidOutputs(NonNullList.of(FluidStack.EMPTY, outputs));
    }

    public PulverizingRecipeBuilder withFluidOutputs(NonNullList<FluidStack> outputs) {
        params.fluidResults = outputs;
        return this;
    }

    public static class PulverizingRecipeParams {

        public ResourceLocation id;
        public NonNullList<Ingredient> ingredients;
        public NonNullList<ProcessingOutput> itemResults;
        public NonNullList<ProcessingOutput> blockResults;
        public NonNullList<FluidStack> fluidResults;
        public int processingDuration;

        protected PulverizingRecipeParams(ResourceLocation id) {
            this.id = id;
            ingredients = NonNullList.create();
            itemResults = NonNullList.create();
            blockResults = NonNullList.create();
            fluidResults = NonNullList.create();
            processingDuration = 0;
        }

    }

    public static class DataGenResult implements FinishedRecipe {

        private List<ICondition> recipeConditions;
        private PulverizingRecipeSerializer serializer;
        private ResourceLocation id;
        private PulverizingRecipe recipe;

        @SuppressWarnings("unchecked")
        public DataGenResult(PulverizingRecipe recipe, List<ICondition> recipeConditions) {
            this.recipe = recipe;
            this.recipeConditions = recipeConditions;
            ResourceLocation typeId = SCRecipeTypes.PULVERIZING.getId();

            this.id = new ResourceLocation(recipe.getId().getNamespace(),
                    typeId.getPath() + "/" + recipe.getId().getPath());
            this.serializer = (PulverizingRecipeSerializer) recipe.getSerializer();
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            serializer.write(json, recipe);
            if (recipeConditions.isEmpty())
                return;

            JsonArray conds = new JsonArray();
            recipeConditions.forEach(c -> conds.add(CraftingHelper.serialize(c)));
            json.add("conditions", conds);
        }

        @Override
        public ResourceLocation getId() {
            return id;
        }

        @Override
        public RecipeSerializer<?> getType() {
            return serializer;
        }

        @Override
        public JsonObject serializeAdvancement() {
            return null;
        }

        @Override
        public ResourceLocation getAdvancementId() {
            return null;
        }

    }

}
