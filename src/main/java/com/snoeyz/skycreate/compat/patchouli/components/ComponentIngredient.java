package com.snoeyz.skycreate.compat.patchouli.components;

import java.util.Optional;
import java.util.function.UnaryOperator;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import vazkii.patchouli.api.IComponentRenderContext;
import vazkii.patchouli.api.IVariable;

public class ComponentIngredient extends TemplateComponentBase {

    private transient static final int HEIGHT = 16;
    private transient static final int WIDTH = 16;
    private transient static final int CYCLETIME = 1000;

    public IVariable input;

    private transient Ingredient ingredient;
    private transient long startTime;
    private transient long drawTime;
    private transient long pausedDuration = 0;

    @Override
    public void build(int componentX, int componentY, int pageNum) {
        super.build(componentX, componentY, pageNum);
        long time = System.currentTimeMillis();
        startTime = time;
        drawTime = time;
    }

    @Override
    public void onVariablesAvailable(UnaryOperator<IVariable> lookup) {
        ingredient = lookup.apply(input).as(Ingredient.class);
    }

    @Override
    public void render(PoseStack ms, IComponentRenderContext context, float pticks, int mouseX, int mouseY) {
        onDraw();
        Optional<ItemStack> stack = getCycledItem();
        if (stack.isPresent()) {
            ms.pushPose();
            {
                ms.translate(x, y, 0);
                RenderSystem.enableBlend();
                drawItem(ms, stack.get());
                if (context.isAreaHovered(mouseX, mouseY, x, y, WIDTH, HEIGHT)) {
                    setTooltip(context, stack.get());
                }
                RenderSystem.disableBlend();
            }
            ms.popPose();
        }
    }

    private Optional<ItemStack> getCycledItem() {
        if (ingredient == null || ingredient.isEmpty()) {
            return Optional.empty();
        }
        long index = ((drawTime - startTime) / CYCLETIME) % ingredient.getItems().length;
        return Optional.of(ingredient.getItems()[Math.toIntExact(index)]);
    }

    private void onDraw() {
        if (!Screen.hasShiftDown()) {
            if (pausedDuration > 0) {
                startTime += pausedDuration;
                pausedDuration = 0;
            }
            drawTime = System.currentTimeMillis();
        } else {
            pausedDuration = System.currentTimeMillis() - drawTime;
        }
    }
}
