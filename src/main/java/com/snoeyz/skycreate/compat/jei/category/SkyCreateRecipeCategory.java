package com.snoeyz.skycreate.compat.jei.category;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.contraptions.processing.ProcessingOutput;
import com.snoeyz.skycreate.datagen.Lang;
import com.snoeyz.skycreate.gui.SCGuiTextures;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotTooltipCallback;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

public abstract class SkyCreateRecipeCategory<T extends Recipe<?>> implements IRecipeCategory<T> {
    private static final IDrawable BASIC_SLOT = asDrawable(SCGuiTextures.JEI_SLOT);
    private static final IDrawable CHANCE_SLOT = asDrawable(SCGuiTextures.JEI_CHANCE_SLOT);

    protected final RecipeType<T> type;
    protected final Component title;
    protected final IDrawable background;
    protected final IDrawable icon;

    private final Supplier<List<T>> recipes;
    private final List<Supplier<? extends ItemStack>> catalysts;

    public SkyCreateRecipeCategory(Info<T> info) {
        this.type = info.recipeType();
        this.title = info.title();
        this.background = info.background();
        this.icon = info.icon();
        this.recipes = info.recipes();
        this.catalysts = info.catalysts();
    }

    @NotNull
    @Override
    public RecipeType<T> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return title;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    @Deprecated
    public final ResourceLocation getUid() {
        return type.getUid();
    }

    @Override
    @Deprecated
    public final Class<? extends T> getRecipeClass() {
        return type.getRecipeClass();
    }

    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(type, recipes.get());
    }

    public void registerCatalysts(IRecipeCatalystRegistration registration) {
        catalysts.forEach(s -> registration.addRecipeCatalyst(s.get(), type));
    }

    public static IDrawable getRenderedSlot() {
        return BASIC_SLOT;
    }

    public static IDrawable getRenderedSlot(ProcessingOutput output) {
        return getRenderedSlot(output.getChance());
    }

    public static IDrawable getRenderedSlot(float chance) {
        if (chance == 1)
            return BASIC_SLOT;

        return CHANCE_SLOT;
    }

    public static IRecipeSlotTooltipCallback addStochasticTooltip(ProcessingOutput output) {
        return (view, tooltip) -> {
            float chance = output.getChance();
            if (chance != 1)
                tooltip.add(1, Lang.translateDirect("recipe.processing.chance", chance < 0.01 ? "<1" : (int) (chance * 100))
                        .withStyle(ChatFormatting.GOLD));
        };
    }

    protected static IDrawable asDrawable(SCGuiTextures texture) {
        return new IDrawable() {
            @Override
            public int getWidth() {
                return texture.width;
            }

            @Override
            public int getHeight() {
                return texture.height;
            }

            @Override
            public void draw(PoseStack poseStack, int xOffset, int yOffset) {
                texture.render(poseStack, xOffset, yOffset);
            }
        };
    }

    public record Info<T extends Recipe<?>>(RecipeType<T> recipeType, Component title, IDrawable background, IDrawable icon, Supplier<List<T>> recipes, List<Supplier<? extends ItemStack>> catalysts) {
    }

    public interface Factory<T extends Recipe<?>> {
        SkyCreateRecipeCategory<T> create(Info<T> info);
    }
}
