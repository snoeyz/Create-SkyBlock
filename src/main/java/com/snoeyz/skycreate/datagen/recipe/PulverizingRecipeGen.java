package com.snoeyz.skycreate.datagen.recipe;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipeBuilder;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipeSerializer;
import com.simibubi.create.foundation.utility.RegisteredObjects;
import com.snoeyz.skycreate.SkyCreateMod;
import com.snoeyz.skycreate.recipe.PulverizingRecipe;
import com.snoeyz.skycreate.registry.SCRecipeTypes;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
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
            .output(.1f, Items.CLAY_BALL)
            .output(.2f, Items.FLINT)),

    LOGS = pulverize("logs", b -> b.duration(200)
            .require(I.logs())
            .output(Items.STICK, 4)),

    STONE = pulverize(I::stone, b -> b.duration(200)
            .output(.5f, Items.COBBLESTONE)
            .output(.5f, Items.TUFF)
            .output(.05f, Items.IRON_NUGGET, 2)
            .output(.05f, AllItems.COPPER_NUGGET.get(), 2)),

    TUFF = pulverize(I::tuff, b -> b.duration(200)
            .output(.25f, Items.FLINT)
            .output(.025f, AllItems.ZINC_INGOT.get())
            .output(.025f, AllItems.COPPER_NUGGET.get())
            .output(.025f, Items.IRON_NUGGET)
            .output(.025f, Items.GOLD_NUGGET));

    public PulverizingRecipeGen(DataGenerator gen) {
        super(gen);
    }

    /**
     * Create a processing recipe with a single itemstack ingredient, using its id
     * as the name of the recipe
     */
    GeneratedRecipe pulverize(Supplier<ItemLike> singleIngredient, UnaryOperator<ProcessingRecipeBuilder<PulverizingRecipe>> transform) {
        ProcessingRecipeSerializer<PulverizingRecipe> serializer = SCRecipeTypes.PULVERIZING.getSerializer();
        GeneratedRecipe generatedRecipe = c -> {
            ItemLike iItemProvider = singleIngredient.get();
            transform
                    .apply(new ProcessingRecipeBuilder<PulverizingRecipe>(serializer.getFactory(),
                            new ResourceLocation(SkyCreateMod.MOD_ID, RegisteredObjects.getKeyOrThrow(iItemProvider.asItem())
                                    .getPath())).withItemIngredients(Ingredient.of(iItemProvider)))
                    .build(c);
        };
        all.add(generatedRecipe);
        return generatedRecipe;
    }

    protected GeneratedRecipe pulverizeWithDeferredId(Supplier<ResourceLocation> name, UnaryOperator<ProcessingRecipeBuilder<PulverizingRecipe>> transform) {
        ProcessingRecipeSerializer<PulverizingRecipe> serializer = SCRecipeTypes.PULVERIZING.getSerializer();
        GeneratedRecipe generatedRecipe =
                c -> transform.apply(new ProcessingRecipeBuilder<>(serializer.getFactory(), name.get()))
                        .build(c);
        all.add(generatedRecipe);
        return generatedRecipe;
    }

    /**
     * Create a new processing recipe, with recipe definitions provided by the
     * function
     */
    protected GeneratedRecipe pulverize(ResourceLocation name, UnaryOperator<ProcessingRecipeBuilder<PulverizingRecipe>> transform) {
        return pulverizeWithDeferredId(() -> name, transform);
    }

    /**
     * Create a new processing recipe, with recipe definitions provided by the
     * function
     */
    GeneratedRecipe pulverize(String name, UnaryOperator<ProcessingRecipeBuilder<PulverizingRecipe>> transform) {
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
