package com.snoeyz.skycreate.world.chunkgenerators;

import java.io.IOException;
import java.util.Random;

import javax.annotation.Nonnull;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.snoeyz.skycreate.SkyCreateMod;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class SkyCreateChunkGenerator {
    private static final Logger LOGGER = LogUtils.getLogger();
    
    public static void buildSurface(@Nonnull ChunkAccess chunk, @Nonnull ServerLevelAccessor level) {
        ChunkPos chunkPos = chunk.getPos();
        if (chunkPos.x == 0 && chunkPos.z == 0) {
            loadSpawnIsland(chunk, level);
        }
    }

    private static void loadSpawnIsland(@Nonnull ChunkAccess chunk, @Nonnull ServerLevelAccessor level) {
        chunk.setBlockState(new BlockPos(0, 64, 0), Blocks.BEDROCK.defaultBlockState(), false);
        loadIsland(chunk, level, "/default.nbt");
    }

    private static void loadIsland(@Nonnull ChunkAccess chunk, @Nonnull ServerLevelAccessor level, String filePath) {
        StructureTemplate template = new StructureTemplate();
        CompoundTag nbt;
        try {
            nbt = NbtIo.readCompressed(SkyCreateMod.class.getResourceAsStream(filePath));
            template.load(nbt);
        } catch (IOException e) {
            LOGGER.error("Template with name " + filePath + " is incorrect.", e);
        }
        if (template != null) {
            StructurePlaceSettings settings = new StructurePlaceSettings().setKnownShape(true);
            template.placeInWorld(level, new BlockPos(0, 60, 0), new BlockPos(0, 60, 0), settings, new Random(), Block.UPDATE_CLIENTS);
        }
    }
}
