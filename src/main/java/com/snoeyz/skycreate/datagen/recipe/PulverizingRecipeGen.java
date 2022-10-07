package com.snoeyz.skycreate.datagen.recipe;

// import com.simibubi.create.AllItems;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class PulverizingRecipeGen extends SkyCreateRecipeProvider {

    GeneratedRecipe

    STONE = pulverize(I::stone, b -> b.duration(200)
        .blockOutput(Blocks.COBBLESTONE)
        .output(I.stoneLooseRock(), 2)
        .output(.25f, I.stoneLooseRock())),

    COBBLESTONE = pulverize(I::cobblestone, b -> b.duration(200)
        .output(.1f, I.andesiteLooseRock())
        .output(.1f, I.dioriteLooseRock())
        .output(.1f, I.graniteLooseRock())
        .output(.70f, I.stoneLooseRock(), 3)),

    ANDESITE = pulverize(I::andesite, b -> b.duration(200)
        .blockOutput(new ResourceLocation("notreepunching", "andesite_cobblestone"))
        .output(I.andesiteLooseRock(), 2)
        .output(.25f, I.andesiteLooseRock())),

    ANDESITE_COBBLESTONE = pulverize(I::andesiteCobblestone, b -> b.duration(200)
        .output(.1f, I.stoneLooseRock())
        .output(.1f, I.dioriteLooseRock())
        .output(.1f, I.graniteLooseRock())
        .output(.70f, I.andesiteLooseRock(), 3)),

    DIORITE = pulverize(I::diorite, b -> b.duration(200)
        .blockOutput(new ResourceLocation("notreepunching", "diorite_cobblestone"))
        .output(I.dioriteLooseRock(), 2)
        .output(.25f, I.dioriteLooseRock())),

    DIORITE_COBBLESTONE = pulverize(I::dioriteCobblestone, b -> b.duration(200)
        .output(.1f, I.stoneLooseRock())
        .output(.1f, I.andesiteLooseRock())
        .output(.1f, I.graniteLooseRock())
        .output(.70f, I.dioriteLooseRock(), 3)),

    GRANITE = pulverize(I::granite, b -> b.duration(200)
        .blockOutput(new ResourceLocation("notreepunching", "granite_cobblestone"))
        .output(I.graniteLooseRock(), 2)
        .output(.25f, I.graniteLooseRock())),

    GRANITE_COBBLESTONE = pulverize(I::graniteCobblestone, b -> b.duration(200)
        .output(.1f, I.stoneLooseRock())
        .output(.1f, I.dioriteLooseRock())
        .output(.1f, I.andesiteLooseRock())
        .output(.70f, I.graniteLooseRock(), 3)),

    GRAVEL = pulverize(I::gravel, b -> b.duration(200)
        .blockOutput(Blocks.SAND)
        .output(.1f, Items.CLAY_BALL)
        .output(.25f, Items.FLINT)),

    ICE = pulverize(I::ice, b -> b.duration(200)
        .blockOutput(Blocks.WATER, Fluids.WATER)),

    LOGS = pulverize("logs", b -> b.duration(200)
        .require(I.logs())
        .output(Items.STICK, 2)
        .output(.3f, Items.STICK,2));

    // TUFF = pulverize(I::tuff, b -> b.duration(200)
    //     .output(.25f, Items.FLINT)
    //     .output(.3f, AllItems.ZINC_INGOT.get())
    //     .output(.3f, AllItems.COPPER_NUGGET.get())
    //     .output(.3f, Items.IRON_NUGGET)
    //     .output(.1f, Items.GOLD_NUGGET));

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
                                    .getPath())).withItemIngredient(Ingredient.of(iItemProvider)))
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

        static ItemLike stoneLooseRock() {
            return ForgeRegistries.ITEMS.getValue(new ResourceLocation("notreepunching", "stone_loose_rock"));
        }
        
        static ItemLike andesite() {
            return Items.ANDESITE;
        }

        static ItemLike andesiteCobblestone() {
            return ForgeRegistries.ITEMS.getValue(new ResourceLocation("notreepunching", "andesite_cobblestone"));
        }

        static ItemLike andesiteLooseRock() {
            return ForgeRegistries.ITEMS.getValue(new ResourceLocation("notreepunching", "andesite_loose_rock"));
        }
        
        static ItemLike diorite() {
            return Items.DIORITE;
        }

        static ItemLike dioriteCobblestone() {
            return ForgeRegistries.ITEMS.getValue(new ResourceLocation("notreepunching", "diorite_cobblestone"));
        }

        static ItemLike dioriteLooseRock() {
            return ForgeRegistries.ITEMS.getValue(new ResourceLocation("notreepunching", "diorite_loose_rock"));
        }
        
        static ItemLike granite() {
            return Items.GRANITE;
        }

        static ItemLike graniteCobblestone() {
            return ForgeRegistries.ITEMS.getValue(new ResourceLocation("notreepunching", "granite_cobblestone"));
        }

        static ItemLike graniteLooseRock() {
            return ForgeRegistries.ITEMS.getValue(new ResourceLocation("notreepunching", "granite_loose_rock"));
        }

        static ItemLike ice() {
            return Items.ICE;
        }

        // static ItemLike tuff() {
        //     return Items.TUFF;
        // }

        static ItemLike gravel() {
            return Items.GRAVEL;
        }

        static TagKey<Item> logs() {
            return ItemTags.LOGS;
        }
    }
}
