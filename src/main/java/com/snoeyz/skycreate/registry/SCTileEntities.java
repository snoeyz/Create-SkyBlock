package com.snoeyz.skycreate.registry;

import com.snoeyz.skycreate.SkyCreateMod;
import com.snoeyz.skycreate.components.pulverizer.PulverizerInstance;
import com.snoeyz.skycreate.components.pulverizer.PulverizerRenderer;
import com.snoeyz.skycreate.components.pulverizer.PulverizerTileEntity;
import com.tterrag.registrate.util.entry.BlockEntityEntry;

public class SCTileEntities {
    public static final BlockEntityEntry<PulverizerTileEntity> PULVERIZER = SkyCreateMod.registrate()
            .tileEntity("pulverizer", PulverizerTileEntity::new)
            .instance(() -> PulverizerInstance::new)
            .validBlocks(SCBlocks.PULVERIZER)
            .renderer(() -> PulverizerRenderer::new)
            .register();

    public static void register() {}
}
