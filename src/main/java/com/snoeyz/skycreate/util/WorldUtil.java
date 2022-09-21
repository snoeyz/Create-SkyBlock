package com.snoeyz.skycreate.util;

import com.snoeyz.skycreate.world.chunkgenerators.SkyCreateNoiseBasedChunkGenerator;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public class WorldUtil {

    public static boolean isSkyCreate(Level level) {
        if (!(level instanceof ServerLevel)) return false;

        MinecraftServer server = ((ServerLevel) level).getServer();

        return server.overworld().getChunkSource().getGenerator() instanceof SkyCreateNoiseBasedChunkGenerator;
    }
}
