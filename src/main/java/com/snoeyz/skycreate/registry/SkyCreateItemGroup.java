package com.snoeyz.skycreate.registry;

import com.snoeyz.skycreate.SkyCreateMod;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class SkyCreateItemGroup extends CreativeModeTab {

    private static final String GROUP_ID = "main";

    public SkyCreateItemGroup() {
        super(SkyCreateMod.MOD_ID + "." + GROUP_ID);
    }

    @Override
    public ItemStack makeIcon() {
        return new ItemStack(SCBlocks.PULVERIZER.get());
    }
}
