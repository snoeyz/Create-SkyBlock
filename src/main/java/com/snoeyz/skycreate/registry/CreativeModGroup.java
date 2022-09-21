package com.snoeyz.skycreate.registry;

import com.snoeyz.skycreate.SkyCreateMod;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class CreativeModGroup extends CreativeModeTab {
    
    public static final String TAB_NAME = "main";

    public CreativeModGroup() {
        super(SkyCreateMod.MOD_ID + ":" + TAB_NAME);
    }

    @Override
    public ItemStack makeIcon() {
        return new ItemStack(SCBlocks.PULVERIZER.get());
    }
}
