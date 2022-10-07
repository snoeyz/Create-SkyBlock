package com.snoeyz.skycreate.recipe;

import com.simibubi.create.content.contraptions.processing.ProcessingOutput;
import com.snoeyz.skycreate.datagen.recipe.PulverizingBlockOutput;
import com.snoeyz.skycreate.datagen.recipe.PulverizingRecipeBuilder.PulverizingRecipeParams;
import com.snoeyz.skycreate.registry.SCRecipeTypes;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.wrapper.RecipeWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class PulverizingRecipe implements Recipe<RecipeWrapper> {
    
    private static final Random r = new Random();

    protected ResourceLocation id;
    protected Ingredient ingredient;
    protected NonNullList<ProcessingOutput> outputItems;
    protected NonNullList<PulverizingBlockOutput> outputBlocks;
    protected int processingDuration;

    public PulverizingRecipe(PulverizingRecipeParams params) {
        id = params.id;
        ingredient = params.ingredient;
        outputItems = params.itemResults;
        outputBlocks = params.blockResults;
        processingDuration = params.processingDuration;
    }

    @Override
    public boolean matches(RecipeWrapper inv, Level worldIn) {
        if (inv.isEmpty())
            return false;
        return ingredient.test(inv.getItem(0));
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public NonNullList<ProcessingOutput> getOutputItems() {
        return outputItems;
    }

    public NonNullList<PulverizingBlockOutput> getOutputBlocks() {
        return outputBlocks;
    }

    public int getProcessingDuration() {
        return processingDuration;
    }

    public BlockState rollResultBlockState() {
        int ndx = r.nextInt(outputBlocks.size());
        return outputBlocks.get(ndx).getBlockState();
    }
    
    public List<ItemStack> rollResultItems() {
        List<ItemStack> results = new ArrayList<>();
        for (int i = 0; i < outputItems.size(); i++) {
            ProcessingOutput output = outputItems.get(i);
            ItemStack stack = output.rollOutput();
            if (!stack.isEmpty())
                results.add(stack);
        }
        return results;
    }
    
    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(ingredient);
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

    private boolean appliesTo(BlockState target) {
        return ingredient.test(new ItemStack(target.getBlock(), 1));
    }

    public static Optional<PulverizingRecipe> getRecipe(Level world, BlockState target) {
        List<PulverizingRecipe> all = world.getRecipeManager().getAllRecipesFor(SCRecipeTypes.PULVERIZING.getType());
        for (PulverizingRecipe pulverizingRecipe : all) {
            if (!pulverizingRecipe.appliesTo(target))
                continue;
            return Optional.of(pulverizingRecipe);
        }
        return Optional.empty();
    }

    public static PulverizingRecipe getRecipe(ResourceLocation recipeId) {
        return (PulverizingRecipe)
            Minecraft
                .getInstance()
                .getConnection()
                .getRecipeManager()
                .byKey(recipeId)
                .orElseThrow();
    }
}
