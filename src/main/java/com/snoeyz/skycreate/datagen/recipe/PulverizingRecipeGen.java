package com.snoeyz.skycreate.datagen.recipe;

import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.utility.RegisteredObjects;
import com.snoeyz.skycreate.SkyCreateMod;
import com.snoeyz.skycreate.registry.SCRecipeTypes;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class PulverizingRecipeGen extends SkyCreateRecipeProvider {

    GeneratedRecipe

    COBBLESTONE = pulverize(I::cobblestone, b -> b.duration(200)
            .output(.1f, ForgeRegistries.ITEMS.getValue(new ResourceLocation("notreepunching", "andesite_loose_rock")))
            .output(.1f, ForgeRegistries.ITEMS.getValue(new ResourceLocation("notreepunching", "diorite_loose_rock")))
            .output(.1f, ForgeRegistries.ITEMS.getValue(new ResourceLocation("notreepunching", "granite_loose_rock")))
            .output(.70f, ForgeRegistries.ITEMS.getValue(new ResourceLocation("notreepunching", "stone_loose_rock")), 3)),

    GRAVEL = pulverize(I::gravel, b -> b.duration(200)
            .blockOutput(Items.SAND)
            .output(.1f, Items.CLAY_BALL)
            .output(.2f, Items.FLINT)),

    ICE = pulverize(I::ice, b -> b.duration(200)
            .blockOutput(Fluids.FLOWING_WATER, 1000)),

    LOGS = pulverize("logs", b -> b.duration(200)
            .require(I.logs())
            .output(Items.STICK, 4)),

    STONE = pulverize(I::stone, b -> b.duration(200)
            .blockOutput(.5f, Items.COBBLESTONE)
            .blockOutput(.5f, Items.TUFF)
            .output(.05f, Items.IRON_NUGGET, 2)
            .output(.05f, AllItems.COPPER_NUGGET.get(), 2)),

    TUFF = pulverize(I::tuff, b -> b.duration(200)
            .output(.25f, Items.FLINT)
            .output(.1f, AllItems.ZINC_INGOT.get())
            .output(.1f, AllItems.COPPER_NUGGET.get())
            .output(.1f, Items.IRON_NUGGET)
            .output(.1f, Items.GOLD_NUGGET));

    public PulverizingRecipeGen(DataGenerator gen) {
        super(gen);
    }

    /**
     * Create a processing recipe with a single itemstack ingredient, using its id
     * as the name of the recipe
     */
    GeneratedRecipe pulverize(Supplier<ItemLike> singleIngredient, UnaryOperator<PulverizingRecipeBuilder> transform) {
        GeneratedRecipe generatedRecipe = c -> {
            ItemLike iItemProvider = singleIngredient.get();
            transform
                    .apply(new PulverizingRecipeBuilder(new ResourceLocation(SkyCreateMod.MOD_ID, RegisteredObjects.getKeyOrThrow(iItemProvider.asItem())
                                    .getPath())).withItemIngredients(Ingredient.of(iItemProvider)))
                    .build(c);
        };
        all.add(generatedRecipe);
        return generatedRecipe;
    }

    protected GeneratedRecipe pulverizeWithDeferredId(Supplier<ResourceLocation> name, UnaryOperator<PulverizingRecipeBuilder> transform) {
        GeneratedRecipe generatedRecipe =
                c -> transform.apply(new PulverizingRecipeBuilder(name.get()))
                        .build(c);
        all.add(generatedRecipe);
        return generatedRecipe;
    }

    /**
     * Create a new processing recipe, with recipe definitions provided by the
     * function
     */
    protected GeneratedRecipe pulverize(ResourceLocation name, UnaryOperator<PulverizingRecipeBuilder> transform) {
        return pulverizeWithDeferredId(() -> name, transform);
    }

    /**
     * Create a new processing recipe, with recipe definitions provided by the
     * function
     */
    GeneratedRecipe pulverize(String name, UnaryOperator<PulverizingRecipeBuilder> transform) {
        return pulverize(SkyCreateMod.asResource(name), transform);
    }

    protected SCRecipeTypes getRecipeType() {
        return SCRecipeTypes.PULVERIZING;
    }

    @Override
    public String getName() {
        return "SkyCreate's Pulverizing Recipes: " + getRecipeType().getId().getPath();
    }

    protected static class I {
        static ItemLike stone() {
            return Items.STONE;
        }

        static ItemLike cobblestone() {
            return Items.COBBLESTONE;
        }

        static ItemLike ice() {
            return Items.ICE;
        }

        static ItemLike tuff() {
            return Items.TUFF;
        }

        static ItemLike gravel() {
            return Items.GRAVEL;
        }

        static TagKey<Item> logs() {
            return ItemTags.LOGS;
        }
    }
}
