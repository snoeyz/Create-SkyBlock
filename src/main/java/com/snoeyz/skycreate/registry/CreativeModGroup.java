package com.snoeyz.skycreate.registry;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class CreativeModGroup extends CreativeModeTab {

    public CreativeModGroup() {
        super("main");
    }

    @Override
    public ItemStack makeIcon() {
        return new ItemStack(SCBlocks.PULVERIZER.get());
    }
}
