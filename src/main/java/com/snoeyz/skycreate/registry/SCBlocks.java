package com.snoeyz.skycreate.registry;

import static com.simibubi.create.AllTags.axeOrPickaxe;
import static com.simibubi.create.foundation.data.ModelGen.customItemModel;

import com.simibubi.create.AllTags.AllBlockTags;
import com.simibubi.create.foundation.block.BlockStressDefaults;
import com.simibubi.create.foundation.data.BlockStateGen;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.SharedProperties;
import com.snoeyz.skycreate.SkyCreateMod;
import com.snoeyz.skycreate.components.pulverizer.PulverizerBlock;
import com.tterrag.registrate.util.entry.BlockEntry;

public class SCBlocks {
    private static final CreateRegistrate REGISTRATE = SkyCreateMod.registrate().creativeModeTab(() -> SkyCreateMod.MAIN_CREATIVE_TAB);
    
    public static final BlockEntry<PulverizerBlock> PULVERIZER = REGISTRATE.block("pulverizer", PulverizerBlock::new)
        .initialProperties(SharedProperties::stone)
        .transform(axeOrPickaxe())
        .blockstate(BlockStateGen.directionalAxisBlockProvider())
        .transform(BlockStressDefaults.setImpact(4.0))
        .tag(AllBlockTags.SAFE_NBT.tag) //Dono what this tag means (contraption safe?).
        .item()
        .transform(customItemModel())
        .register();

    public static void register() {}
}
