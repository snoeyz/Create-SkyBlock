package com.snoeyz.skycreate.datagen.recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.snoeyz.skycreate.SkyCreateMod;
import com.snoeyz.skycreate.datagen.Lang;
import com.snoeyz.skycreate.datagen.recipe.SkyCreateRecipeProvider.GeneratedRecipe;
import com.snoeyz.skycreate.recipe.SCSections;
import com.snoeyz.skycreate.registry.SCBlocks;
import com.snoeyz.skycreate.util.RegisteredObjects;
import com.tterrag.registrate.util.entry.ItemProviderEntry;

import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.ModLoadedCondition;
import net.minecraftforge.common.crafting.conditions.NotCondition;
import net.minecraftforge.registries.ForgeRegistries;

@SuppressWarnings("unused")
public class StandardRecipeGen extends RecipeProvider {

    protected final List<GeneratedRecipe> all = new ArrayList<>();
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    protected static class Marker {}

    public StandardRecipeGen(DataGenerator generator) {
        super(generator);
    }

    private Marker KINETICS = enterSection(SCSections.KINETICS);

    GeneratedRecipe
        PULVERIZER = skycreate(SCBlocks.PULVERIZER).unlockedBy(() -> Items.CRAFTING_TABLE)
        .viaShaped(b -> b.define('C', I.andesiteCasing())
            .define('S', I.shaft())
            .define('P', Items.PISTON)
            .pattern("S")
            .pattern("C")
            .pattern("P"));


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
        void register(Consumer<FinishedRecipe> consumer);
    }

    String currentFolder = "";

    Marker enterSection(SCSections section) {
        currentFolder = Lang.asId(section.name());
        return new Marker();
    }

    Marker enterFolder(String folder) {
        currentFolder = folder;
        return new Marker();
    }

    GeneratedRecipeBuilder skycreate(Supplier<ItemLike> result) {
        return new GeneratedRecipeBuilder(currentFolder, result);
    }

    GeneratedRecipeBuilder skycreate(ResourceLocation result) {
        return new GeneratedRecipeBuilder(currentFolder, result);
    }

    GeneratedRecipeBuilder skycreate(ItemProviderEntry<? extends ItemLike> result) {
        return skycreate(result::get);
    }

    class GeneratedRecipeBuilder {

        private String path;
        private String suffix;
        private Supplier<? extends ItemLike> result;
        private ResourceLocation compatDatagenOutput;
        List<ICondition> recipeConditions;

        private Supplier<ItemPredicate> unlockedBy;
        private int amount;

        private GeneratedRecipeBuilder(String path) {
            this.path = path;
            this.recipeConditions = new ArrayList<>();
            this.suffix = "";
            this.amount = 1;
        }

        public GeneratedRecipeBuilder(String path, Supplier<? extends ItemLike> result) {
            this(path);
            this.result = result;
        }

        public GeneratedRecipeBuilder(String path, ResourceLocation result) {
            this(path);
            this.compatDatagenOutput = result;
        }

        GeneratedRecipeBuilder returns(int amount) {
            this.amount = amount;
            return this;
        }

        GeneratedRecipeBuilder unlockedBy(Supplier<? extends ItemLike> item) {
            this.unlockedBy = () -> ItemPredicate.Builder.item()
                .of(item.get())
                .build();
            return this;
        }

        GeneratedRecipeBuilder unlockedByTag(Supplier<TagKey<Item>> tag) {
            this.unlockedBy = () -> ItemPredicate.Builder.item()
                .of(tag.get())
                .build();
            return this;
        }

        GeneratedRecipeBuilder whenModLoaded(String modid) {
            return withCondition(new ModLoadedCondition(modid));
        }

        GeneratedRecipeBuilder whenModMissing(String modid) {
            return withCondition(new NotCondition(new ModLoadedCondition(modid)));
        }

        GeneratedRecipeBuilder withCondition(ICondition condition) {
            recipeConditions.add(condition);
            return this;
        }

        GeneratedRecipeBuilder withSuffix(String suffix) {
            this.suffix = suffix;
            return this;
        }

        GeneratedRecipe viaShaped(UnaryOperator<ShapedRecipeBuilder> builder) {
            return register(consumer -> {
                ShapedRecipeBuilder b = builder.apply(ShapedRecipeBuilder.shaped(result.get(), amount));
                if (unlockedBy != null)
                    b.unlockedBy("has_item", inventoryTrigger(unlockedBy.get()));
                b.save(consumer, skycreateLocation("crafting"));
            });
        }

        GeneratedRecipe viaShapeless(UnaryOperator<ShapelessRecipeBuilder> builder) {
            return register(consumer -> {
                ShapelessRecipeBuilder b = builder.apply(ShapelessRecipeBuilder.shapeless(result.get(), amount));
                if (unlockedBy != null)
                    b.unlockedBy("has_item", inventoryTrigger(unlockedBy.get()));
                b.save(consumer, skycreateLocation("crafting"));
            });
        }

        private ResourceLocation skycreateLocation(String recipeType) {
            return SkyCreateMod.asResource(recipeType + "/" + path + "/" + getRegistryName().getPath() + suffix);
        }

        private ResourceLocation getRegistryName() {
            return compatDatagenOutput == null ? RegisteredObjects.getKeyOrThrow(result.get()
                .asItem()) : compatDatagenOutput;
        }
    }

    @Override
    public String getName() {
        return "SkyCreate's Standard Recipes";
    }

    private static class I {

        static ItemLike andesiteCasing() {
            return ForgeRegistries.ITEMS.getValue(new ResourceLocation("create", "andesite_casing"));
        }

        static ItemLike shaft() {
            return ForgeRegistries.ITEMS.getValue(new ResourceLocation("create", "shaft"));
        }
    }
}
