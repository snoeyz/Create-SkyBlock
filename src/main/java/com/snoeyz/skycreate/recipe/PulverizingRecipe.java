package com.snoeyz.skycreate.recipe;

import com.simibubi.create.content.contraptions.processing.ProcessingOutput;
import com.snoeyz.skycreate.datagen.recipe.PulverizingRecipeBuilder.PulverizingRecipeParams;
import com.snoeyz.skycreate.registry.SCRecipeTypes;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.wrapper.RecipeWrapper;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class PulverizingRecipe implements Recipe<RecipeWrapper> {

    protected ResourceLocation id;
    protected NonNullList<Ingredient> ingredients;
    protected NonNullList<ProcessingOutput> outputItems;
    protected NonNullList<ProcessingOutput> outputBlocks;
    protected NonNullList<FluidStack> outputFluids;
    protected int processingDuration;

    public PulverizingRecipe(PulverizingRecipeParams params) {
        id = params.id;
        ingredients = params.ingredients;
        outputItems = params.itemResults;
        outputBlocks = params.blockResults;
        outputFluids = params.fluidResults;
        processingDuration = params.processingDuration;
    }

    @Override
    public boolean matches(RecipeWrapper inv, Level worldIn) {
        if (inv.isEmpty())
            return false;
        return ingredients.get(0).test(inv.getItem(0));
    }

    public NonNullList<Ingredient> getIngredients() {
        return ingredients;
    }

    public NonNullList<ProcessingOutput> getOutputItems() {
        return outputItems;
    }

    public NonNullList<ProcessingOutput> getOutputBlocks() {
        return outputBlocks;
    }

    public NonNullList<FluidStack> getOutputFluids() {
        return outputFluids;
    }

    public int getProcessingDuration() {
        return processingDuration;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack assemble(RecipeWrapper inv) {
        return getResultItem();
    }

    @Override
    public ItemStack getResultItem() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SCRecipeTypes.PULVERIZING.getSerializer();
    }

    @Override
    public RecipeType<?> getType() {
        return SCRecipeTypes.PULVERIZING.getType();
    }
}
