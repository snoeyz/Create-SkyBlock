package com.snoeyz.skycreate.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.gui.UIRenderHelper;
import com.simibubi.create.foundation.gui.element.ScreenElement;
import com.simibubi.create.foundation.utility.Color;
import com.snoeyz.skycreate.SkyCreateMod;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public enum SCGuiTextures implements ScreenElement {
    // JEI
    JEI_SLOT("jei/widgets", 18, 18),
    JEI_CHANCE_SLOT("jei/widgets", 20, 156, 18, 18),
    JEI_CATALYST_SLOT("jei/widgets", 0, 156, 18, 18),
    JEI_ARROW("jei/widgets", 19, 10, 42, 10),
    JEI_LONG_ARROW("jei/widgets", 19, 0, 71, 10),
    JEI_DOWN_ARROW("jei/widgets", 0, 21, 18, 14),
    JEI_UP_ARROW("jei/widgets", 18, 21, 18, 14),
    JEI_LIGHT("jei/widgets", 0, 42, 52, 11),
    JEI_QUESTION_MARK("jei/widgets", 0, 178, 12, 16),
    JEI_SHADOW("jei/widgets", 0, 56, 52, 11),
    BLOCKZAPPER_UPGRADE_RECIPE("jei/widgets", 0, 75, 144, 66),
    JEI_HEAT_BAR("jei/widgets", 0, 201, 169, 19),
    JEI_NO_HEAT_BAR("jei/widgets", 0, 221, 169, 19);

    public static final int FONT_COLOR = 0x575F7A;

    public final ResourceLocation location;
    public int width, height;
    public int startX, startY;

    private SCGuiTextures(String location, int width, int height) {
        this(location, 0, 0, width, height);
    }

    private SCGuiTextures(int startX, int startY) {
        this("icons", startX * 16, startY * 16, 16, 16);
    }

    private SCGuiTextures(String location, int startX, int startY, int width, int height) {
        this(SkyCreateMod.MOD_ID, location, startX, startY, width, height);
    }

    private SCGuiTextures(String namespace, String location, int startX, int startY, int width, int height) {
        this.location = new ResourceLocation(namespace, "textures/gui/" + location + ".png");
        this.width = width;
        this.height = height;
        this.startX = startX;
        this.startY = startY;
    }

    @OnlyIn(Dist.CLIENT)
    public void bind() {
        RenderSystem.setShaderTexture(0, location);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void render(PoseStack ms, int x, int y) {
        bind();
        GuiComponent.blit(ms, x, y, 0, startX, startY, width, height, 256, 256);
    }

    @OnlyIn(Dist.CLIENT)
    public void render(PoseStack ms, int x, int y, GuiComponent component) {
        bind();
        component.blit(ms, x, y, startX, startY, width, height);
    }

    @OnlyIn(Dist.CLIENT)
    public void render(PoseStack ms, int x, int y, Color c) {
        bind();
        UIRenderHelper.drawColoredTexture(ms, c, x, y, startX, startY, width, height);
    }

}
