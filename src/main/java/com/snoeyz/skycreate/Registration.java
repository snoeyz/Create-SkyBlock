package com.snoeyz.skycreate;

import com.snoeyz.skycreate.world.SkyCreateWorldType;
import com.snoeyz.skycreate.world.chunkgenerators.SkyCreateNoiseBasedChunkGenerator;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.world.ForgeWorldPreset;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class Registration {
    public static final DeferredRegister<ForgeWorldPreset> WORLD_TYPES = DeferredRegister.create(ForgeRegistries.Keys.WORLD_TYPES, SkyCreateMod.MOD_ID);

    public static final RegistryObject<ForgeWorldPreset> SKYCREATE = WORLD_TYPES.register("skycreate", SkyCreateWorldType::new);

    public static void init() {
        Registry.register(Registry.CHUNK_GENERATOR, new ResourceLocation(SkyCreateMod.MOD_ID, "skycreate"), SkyCreateNoiseBasedChunkGenerator.CODEC);
        SkyCreateWorldType.overrideDefaultWorldType();
    }
}
