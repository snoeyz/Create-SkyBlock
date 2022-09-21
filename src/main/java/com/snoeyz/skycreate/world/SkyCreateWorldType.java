package com.snoeyz.skycreate.world;

import javax.annotation.Nonnull;

import com.mojang.serialization.Lifecycle;
import com.snoeyz.skycreate.Registration;
import com.snoeyz.skycreate.world.chunkgenerators.SkyCreateNoiseBasedChunkGenerator;

import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraftforge.common.ForgeConfig;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.world.ForgeWorldPreset;

public class SkyCreateWorldType extends ForgeWorldPreset {

    public SkyCreateWorldType() {
        super(SkyCreateWorldType::configuredOverworldChunkGenerator);
    }

    @Override
    public ChunkGenerator createChunkGenerator(RegistryAccess dynamicRegistries, long seed, String generatorSettings) {
        return SkyCreateWorldType.configuredOverworldChunkGenerator(dynamicRegistries, seed);
    }

    @Override
    public WorldGenSettings createSettings(RegistryAccess dynamicRegistries, long seed, boolean generateStructures, boolean generateLoot, String generatorSettings) {
        Registry<DimensionType> dimensionTypeRegistry = dynamicRegistries.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);

        Registry<LevelStem> dimensions = WorldGenSettings.withOverworld(
                dimensionTypeRegistry,
                voidDimensions(dynamicRegistries, seed),
                this.createChunkGenerator(dynamicRegistries, seed, null)
        );

        return new WorldGenSettings(seed, generateStructures, generateLoot, dimensions);
    }

    public static MappedRegistry<LevelStem> voidDimensions(RegistryAccess dynamicRegistries, long seed) {
        dynamicRegistries.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY);
        dynamicRegistries.registryOrThrow(Registry.STRUCTURE_SET_REGISTRY);
        dynamicRegistries.registryOrThrow(Registry.NOISE_REGISTRY);

        MappedRegistry<LevelStem> levelStems = new MappedRegistry<>(Registry.LEVEL_STEM_REGISTRY, Lifecycle.experimental(), null);
        Registry<DimensionType> dimensionTypes = dynamicRegistries.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
        dynamicRegistries.registryOrThrow(Registry.BIOME_REGISTRY);

        levelStems.register(LevelStem.OVERWORLD, new LevelStem(dimensionTypes.getOrCreateHolder(DimensionType.OVERWORLD_LOCATION),
                configuredOverworldChunkGenerator(dynamicRegistries, seed)), Lifecycle.stable());

        levelStems.register(LevelStem.NETHER, new LevelStem(dimensionTypes.getOrCreateHolder(DimensionType.NETHER_LOCATION), defaultNetherGenerator(dynamicRegistries, seed)), Lifecycle.stable());

        levelStems.register(LevelStem.END, new LevelStem(dimensionTypes.getOrCreateHolder(DimensionType.END_LOCATION), defaultEndGenerator(dynamicRegistries, seed)), Lifecycle.stable());

        return levelStems;
    }

    public static ChunkGenerator configuredOverworldChunkGenerator(RegistryAccess dynamicRegistries, long seed) {
        Registry<StructureSet> structureSets = dynamicRegistries.registryOrThrow(Registry.STRUCTURE_SET_REGISTRY);
        Registry<NormalNoise.NoiseParameters> noises = dynamicRegistries.registryOrThrow(Registry.NOISE_REGISTRY);
        Registry<Biome> biomes = dynamicRegistries.registryOrThrow(Registry.BIOME_REGISTRY);
        Registry<NoiseGeneratorSettings> noiseGeneratorSettings = dynamicRegistries.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY);

        return overworldChunkGenerator(dynamicRegistries, structureSets, noises, biomes, noiseGeneratorSettings, seed);
    }

    public static ChunkGenerator overworldChunkGenerator(RegistryAccess dynamicRegistries, Registry<StructureSet> structureSets, Registry<NormalNoise.NoiseParameters> noises, @Nonnull Registry<Biome> biomeRegistry, @Nonnull Registry<NoiseGeneratorSettings> dimensionSettingsRegistry, long seed) {
        MultiNoiseBiomeSource biomeSource = (MultiNoiseBiomeSource) MultiNoiseBiomeSource.Preset.OVERWORLD.biomeSource(biomeRegistry, false);
        Holder<NoiseGeneratorSettings> settings = dimensionSettingsRegistry.getOrCreateHolder(NoiseGeneratorSettings.OVERWORLD);

        return new SkyCreateNoiseBasedChunkGenerator(structureSets, noises, biomeSource, seed, settings, Level.OVERWORLD);
    }

    private static ChunkGenerator defaultNetherGenerator(RegistryAccess dynamicRegistries, long seed) {
        Registry<NoiseGeneratorSettings> noiseGeneratorSettings = dynamicRegistries.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY);
        Registry<StructureSet> structureSets = dynamicRegistries.registryOrThrow(Registry.STRUCTURE_SET_REGISTRY);
        Registry<NormalNoise.NoiseParameters> noises = dynamicRegistries.registryOrThrow(Registry.NOISE_REGISTRY);

        MultiNoiseBiomeSource biomeSource = MultiNoiseBiomeSource.Preset.NETHER.biomeSource(dynamicRegistries.registryOrThrow(Registry.BIOME_REGISTRY));
        Holder<NoiseGeneratorSettings> settings = noiseGeneratorSettings.getOrCreateHolder(NoiseGeneratorSettings.NETHER);

        return new NoiseBasedChunkGenerator(structureSets, noises, biomeSource, seed, settings);
    }

    private static ChunkGenerator defaultEndGenerator(RegistryAccess dynamicRegistries, long seed) {
        Registry<NoiseGeneratorSettings> noiseGeneratorSettings = dynamicRegistries.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY);
        Registry<StructureSet> structureSets = dynamicRegistries.registryOrThrow(Registry.STRUCTURE_SET_REGISTRY);
        Registry<NormalNoise.NoiseParameters> noises = dynamicRegistries.registryOrThrow(Registry.NOISE_REGISTRY);

        TheEndBiomeSource biomeSource = new TheEndBiomeSource(dynamicRegistries.registryOrThrow(Registry.BIOME_REGISTRY), seed);
        Holder<NoiseGeneratorSettings> settings = noiseGeneratorSettings.getOrCreateHolder(NoiseGeneratorSettings.END);

        return new NoiseBasedChunkGenerator(structureSets, noises, biomeSource, seed, settings);
    }

    /**
     * Override the default world type, in a safe, mixin free, and API providing manner :D
     * Thank you gigahertz!
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void overrideDefaultWorldType()
    {
        if (ForgeConfig.COMMON.defaultWorldType.get().equals("default"))
        {
            ((ForgeConfigSpec.ConfigValue) ForgeConfig.COMMON.defaultWorldType).set(Registration.SKYCREATE.getId().toString());
        }
    }
    
}
