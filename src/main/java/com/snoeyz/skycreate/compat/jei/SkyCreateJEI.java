package com.snoeyz.skycreate.compat.jei;

import com.simibubi.create.compat.jei.*;
import com.simibubi.create.foundation.config.CRecipes;
import com.simibubi.create.foundation.config.ConfigBase;
import com.simibubi.create.foundation.utility.recipe.IRecipeTypeInfo;
import com.snoeyz.skycreate.SkyCreateMod;
import com.snoeyz.skycreate.compat.jei.category.PulverizingCategory;
import com.snoeyz.skycreate.compat.jei.category.SkyCreateRecipeCategory;
import com.snoeyz.skycreate.recipe.PulverizingRecipe;
import com.snoeyz.skycreate.datagen.Lang;
import com.snoeyz.skycreate.registry.SCBlocks;
import com.snoeyz.skycreate.registry.SCRecipeTypes;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.*;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

@JeiPlugin
@SuppressWarnings("unused")
@ParametersAreNonnullByDefault
public class SkyCreateJEI implements IModPlugin {

    private static final ResourceLocation ID = SkyCreateMod.asResource("jei_plugin");

    private final List<SkyCreateRecipeCategory<?>> scCategories = new ArrayList<>();
    private IIngredientManager ingredientManager;

    private void loadCategories() {
        scCategories.clear();

        SkyCreateRecipeCategory<?>

        pulverizing = builder(PulverizingRecipe.class)
                .addTypedRecipes(SCRecipeTypes.PULVERIZING)
                .catalyst(SCBlocks.PULVERIZER::get)
                .itemIcon(SCBlocks.PULVERIZER.get())
                .emptyBackground(177, 70)
                .build("pulverizing", PulverizingCategory::new);
    }

    private <C extends Container, T extends Recipe<C>> CategoryBuilder<C, T> builder(Class<T> recipeClass) {
        return new CategoryBuilder<>(recipeClass);
    }

    @Override
    @Nonnull
    public ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        loadCategories();
        registration.addRecipeCategories(scCategories.toArray(IRecipeCategory[]::new));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        ingredientManager = registration.getIngredientManager();

        scCategories.forEach(c -> c.registerRecipes(registration));

        registration.addRecipes(RecipeTypes.CRAFTING, ToolboxColoringRecipeMaker.createRecipes().toList());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        scCategories.forEach(c -> c.registerCatalysts(registration));
    }

    private class CategoryBuilder<C extends Container, T extends Recipe<C>> {
        private final Class<T> recipeClass;
        private Predicate<CRecipes> predicate = cRecipes -> true;

        private IDrawable background;
        private IDrawable icon;

        private final List<Consumer<List<T>>> recipeListConsumers = new ArrayList<>();
        private final List<Supplier<? extends ItemStack>> catalysts = new ArrayList<>();

        public CategoryBuilder(Class<T> recipeClass) {
            this.recipeClass = recipeClass;
        }

        public CategoryBuilder<C, T> enableIf(Predicate<CRecipes> predicate) {
            this.predicate = predicate;
            return this;
        }

        public CategoryBuilder<C, T> enableWhen(Function<CRecipes, ConfigBase.ConfigBool> configValue) {
            predicate = c -> configValue.apply(c).get();
            return this;
        }

        public CategoryBuilder<C, T> addRecipeListConsumer(Consumer<List<T>> consumer) {
            recipeListConsumers.add(consumer);
            return this;
        }

        public CategoryBuilder<C, T> addRecipes(Supplier<Collection<T>> collection) {
            return addRecipeListConsumer(recipes -> recipes.addAll(collection.get()));
        }

        public CategoryBuilder<C, T> addAllRecipesIf(Predicate<Recipe<?>> pred) {
            return addRecipeListConsumer(recipes -> consumeAllRecipes(recipe -> {
                if (pred.test(recipe)) {
                    recipes.add((T) recipe);
                }
            }));
        }

        public CategoryBuilder<C, T> addAllRecipesIf(Predicate<Recipe<?>> pred, Function<Recipe<?>, T> converter) {
            return addRecipeListConsumer(recipes -> consumeAllRecipes(recipe -> {
                if (pred.test(recipe)) {
                    recipes.add(converter.apply(recipe));
                }
            }));
        }

        public CategoryBuilder<C, T> addTypedRecipes(IRecipeTypeInfo recipeTypeEntry) {
            return addTypedRecipes(recipeTypeEntry::getType);
        }

        public CategoryBuilder<C, T> addTypedRecipes(Supplier<RecipeType<T>> recipeType) {
            return addRecipeListConsumer(recipes -> CreateJEI.<T>consumeTypedRecipes(recipes::add, recipeType.get()));
        }

        public CategoryBuilder<C, T> addTypedRecipes(Supplier<RecipeType<T>> recipeType, Function<Recipe<?>, T> converter) {
            return addRecipeListConsumer(recipes -> CreateJEI.<T>consumeTypedRecipes(recipe -> recipes.add(converter.apply(recipe)), recipeType.get()));
        }

        public CategoryBuilder<C, T> addTypedRecipesIf(Supplier<RecipeType<T>> recipeType, Predicate<Recipe<?>> pred) {
            return addRecipeListConsumer(recipes -> CreateJEI.<T>consumeTypedRecipes(recipe -> {
                if (pred.test(recipe)) {
                    recipes.add(recipe);
                }
            }, recipeType.get()));
        }

        public CategoryBuilder<C, T> addTypedRecipesExcluding(Supplier<RecipeType<Recipe<C>>> recipeType,
                                                           Supplier<RecipeType<Recipe<C>>> excluded) {
            return addRecipeListConsumer(recipes -> {
                List<Recipe<C>> excludedRecipes = getTypedRecipes(excluded.get());
                CreateJEI.<T>consumeTypedRecipes(recipe -> {
                    for (Recipe<?> excludedRecipe : excludedRecipes) {
                        if (doInputsMatch(recipe, excludedRecipe)) {
                            return;
                        }
                    }
                    recipes.add(recipe);
                }, recipeType.get());
            });
        }

        public CategoryBuilder<C, T> removeRecipes(Supplier<RecipeType<Recipe<C>>> recipeType) {
            return addRecipeListConsumer(recipes -> {
                List<Recipe<C>> excludedRecipes = getTypedRecipes(recipeType.get());
                recipes.removeIf(recipe -> {
                    for (Recipe<C> excludedRecipe : excludedRecipes) {
                        if (doInputsMatch(recipe, excludedRecipe)) {
                            return true;
                        }
                    }
                    return false;
                });
            });
        }

        public CategoryBuilder<C, T> catalystStack(Supplier<ItemStack> supplier) {
            catalysts.add(supplier);
            return this;
        }

        public CategoryBuilder<C, T> catalyst(Supplier<ItemLike> supplier) {
            return catalystStack(() -> new ItemStack(supplier.get()
                    .asItem()));
        }

        public CategoryBuilder<C, T> icon(IDrawable icon) {
            this.icon = icon;
            return this;
        }

        public CategoryBuilder<C, T> itemIcon(ItemLike item) {
            icon(new ItemIcon(() -> new ItemStack(item)));
            return this;
        }

        public CategoryBuilder<C, T> doubleItemIcon(ItemLike item1, ItemLike item2) {
            icon(new DoubleItemIcon(() -> new ItemStack(item1), () -> new ItemStack(item2)));
            return this;
        }

        public CategoryBuilder<C, T> background(IDrawable background) {
            this.background = background;
            return this;
        }

        public CategoryBuilder<C, T> emptyBackground(int width, int height) {
            background(new EmptyBackground(width, height));
            return this;
        }

        public SkyCreateRecipeCategory<T> build(String name, SkyCreateRecipeCategory.Factory<T> factory) {
            Supplier<List<T>> recipesSupplier = () -> {
                List<T> recipes = new ArrayList<>();
                for (Consumer<List<T>> consumer : recipeListConsumers)
                    consumer.accept(recipes);
                return recipes;
            };

            SkyCreateRecipeCategory.Info<T> info = new SkyCreateRecipeCategory.Info<>(
                    new mezz.jei.api.recipe.RecipeType<>(SkyCreateMod.asResource(name), recipeClass),
                    Lang.translateDirect("recipe." + name), background, icon, recipesSupplier, catalysts);
            SkyCreateRecipeCategory<T> category = factory.create(info);
            scCategories.add(category);
            return category;
        }
    }

    public static  void consumeAllRecipes(Consumer<Recipe<?>> consumer) {
        Minecraft.getInstance()
                .getConnection()
                .getRecipeManager()
                .getRecipes()
                .forEach(consumer);
    }

    public static <C extends Container, T extends Recipe<C>> void consumeTypedRecipes(Consumer<T> consumer, RecipeType<T> type) {
        List<?> list = Minecraft.getInstance()
                .getConnection()
                .getRecipeManager().getAllRecipesFor(type);
        if (list != null) {
            list.forEach(recipe -> consumer.accept((T) recipe));
        }
    }

    public static <C extends Container> List<Recipe<C>> getTypedRecipes(RecipeType<Recipe<C>> type) {
        List<Recipe<C>> recipes = new ArrayList<>();
        consumeTypedRecipes(recipes::add, type);
        return recipes;
    }

    public static <C extends Container> List<Recipe<C>> getTypedRecipesExcluding(RecipeType<Recipe<C>> type, Predicate<Recipe<C>> exclusionPred) {
        List<Recipe<C>> recipes = getTypedRecipes(type);
        recipes.removeIf(exclusionPred);
        return recipes;
    }

    public static boolean doInputsMatch(Recipe<?> recipe1, Recipe<?> recipe2) {
        if (recipe1.getIngredients()
                .isEmpty()
                || recipe2.getIngredients()
                .isEmpty()) {
            return false;
        }
        ItemStack[] matchingStacks = recipe1.getIngredients()
                .get(0)
                .getItems();
        if (matchingStacks.length == 0) {
            return false;
        }
        return recipe2.getIngredients()
                .get(0)
                .test(matchingStacks[0]);
    }

}
