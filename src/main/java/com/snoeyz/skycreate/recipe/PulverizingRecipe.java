package com.snoeyz.skycreate.recipe;

import com.simibubi.create.compat.jei.category.sequencedAssembly.SequencedAssemblySubCategory;
import com.simibubi.create.content.contraptions.itemAssembly.IAssemblyRecipe;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipe;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipeBuilder;
import com.snoeyz.skycreate.datagen.Lang;
import com.snoeyz.skycreate.registry.SCBlocks;
import com.snoeyz.skycreate.registry.SCRecipeTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.wrapper.RecipeWrapper;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class PulverizingRecipe extends ProcessingRecipe<RecipeWrapper> implements IAssemblyRecipe {

    public PulverizingRecipe(ProcessingRecipeBuilder.ProcessingRecipeParams params) {
        super(SCRecipeTypes.PULVERIZING, params);
    }

    @Override
    public boolean matches(RecipeWrapper inv, Level worldIn) {
        if (inv.isEmpty())
            return false;
        return ingredients.get(0)
                .test(inv.getItem(0));
    }

    @Override
    protected int getMaxInputCount() {
        return 1;
    }

    @Override
    protected int getMaxOutputCount() {
        return 4;
    }

    @Override
    public void addAssemblyIngredients(List<Ingredient> list) {}

    @Override
    @OnlyIn(Dist.CLIENT)
    public Component getDescriptionForAssembly() {
        return Lang.translateDirect("recipe.assembly.cutting");
    }

    @Override
    public void addRequiredMachines(Set<ItemLike> list) {
        list.add(SCBlocks.PULVERIZER.get());
    }

    @Override
    public Supplier<Supplier<SequencedAssemblySubCategory>> getJEISubCategory() {
        return () -> SequencedAssemblySubCategory.AssemblyCutting::new;
    }

}
