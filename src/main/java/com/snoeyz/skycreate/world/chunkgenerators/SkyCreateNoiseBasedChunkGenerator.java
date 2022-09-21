package com.snoeyz.skycreate.world.chunkgenerators;

import javax.annotation.Nonnull;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.core.*;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class SkyCreateNoiseBasedChunkGenerator extends NoiseBasedChunkGenerator {
    
    // [VanillaCopy] overworld chunk generator codec
    public static final Codec<SkyCreateNoiseBasedChunkGenerator> CODEC = RecordCodecBuilder.create(
            (instance) -> ChunkGenerator.commonCodec(instance)
                    .and(instance.group(
                            RegistryOps.retrieveRegistry(Registry.NOISE_REGISTRY).forGetter(generator -> generator.noises),
                            BiomeSource.CODEC.fieldOf("biome_source").forGetter(generator -> generator.biomeSource),
                            Codec.LONG.fieldOf("seed").stable().forGetter(generator -> generator.seed),
                            NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(generator -> generator.generatorSettings),
                            Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(generator -> generator.dimension)
                    )).apply(instance, instance.stable(SkyCreateNoiseBasedChunkGenerator::new)));

        public final long seed;
        public final Registry<NormalNoise.NoiseParameters> noises;
        public final Holder<NoiseGeneratorSettings> generatorSettings;
        public final ResourceKey<Level> dimension;
        protected final NoiseBasedChunkGenerator parent;
        protected final List<FlatLayerInfo> layerInfos;

    public SkyCreateNoiseBasedChunkGenerator(Registry<StructureSet> structureSets, Registry<NormalNoise.NoiseParameters> noises, BiomeSource biomeSource, long seed, Holder<NoiseGeneratorSettings> generatorSettings, ResourceKey<Level> dimension) {
        super(structureSets, noises, biomeSource, seed, generatorSettings);
        this.seed = seed;
        this.noises = noises;
        this.generatorSettings = generatorSettings;
        this.dimension = dimension;
        this.parent = new NoiseBasedChunkGenerator(structureSets, this.noises, biomeSource, seed, generatorSettings);
        this.layerInfos = Lists.newArrayList();
    }

    @Nonnull
    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return SkyCreateNoiseBasedChunkGenerator.CODEC;
    }

    @Nonnull
    @Override
    public ChunkGenerator withSeed(long seed) {
        return new SkyCreateNoiseBasedChunkGenerator(this.structureSets, this.noises, this.biomeSource.withSeed(seed), seed, this.generatorSettings, this.dimension);
    }

    @Override
    public void buildSurface(@Nonnull WorldGenRegion level, @Nonnull StructureFeatureManager structureManager, @Nonnull ChunkAccess chunk) {
        SkyCreateChunkGenerator.buildSurface(chunk, (ServerLevelAccessor)level);
    }

    @Nonnull
    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(@Nonnull Executor executor, @Nonnull Blender blender, @Nonnull StructureFeatureManager manager, @Nonnull ChunkAccess chunk) {
        return CompletableFuture.completedFuture(chunk);
    }

    @Nullable
    @Override
    public Pair<BlockPos, Holder<ConfiguredStructureFeature<?, ?>>> findNearestMapFeature(@Nonnull ServerLevel level, @Nonnull HolderSet<ConfiguredStructureFeature<?, ?>> structureSet, @Nonnull BlockPos pos, int searchRadius, boolean skipKnownStructures) {
        List<Holder<ConfiguredStructureFeature<?, ?>>> holders = structureSet.stream().filter(holder -> holder.unwrapKey().isPresent()).toList();
        HolderSet.Direct<ConfiguredStructureFeature<?, ?>> modifiedStructureSet = HolderSet.direct(holders);
        for (Holder<ConfiguredStructureFeature<?, ?>> holder : modifiedStructureSet) {
            if (holder.unwrapKey().isPresent()) {
                return super.findNearestMapFeature(level, modifiedStructureSet, pos, searchRadius, skipKnownStructures);
            }
        }

        return null;
    }

    @Override
    public int getBaseHeight(int x, int z, @Nonnull Heightmap.Types heightmapType, @Nonnull LevelHeightAccessor level) {

        return this.parent.getBaseHeight(x, z, heightmapType, level);
    }

    @Override
    public void applyCarvers(@Nonnull WorldGenRegion level, long seed, @Nonnull BiomeManager biomeManager, @Nonnull StructureFeatureManager structureManager, @Nonnull ChunkAccess chunk, @Nonnull GenerationStep.Carving carving) {

    }

    // [Vanilla copy]
    protected boolean tryGenerateStructure(@Nonnull StructureSet.StructureSelectionEntry structureEntry, @Nonnull StructureFeatureManager structureManager, @Nonnull RegistryAccess registry, @Nonnull StructureManager featureManager, long seed, @Nonnull ChunkAccess chunk, @Nonnull ChunkPos chunkPos, @Nonnull SectionPos sectionPos) {
        if (structureEntry.structure().unwrapKey().isEmpty() || structureEntry.structure().unwrapKey().get().location().getPath() != "minecraft:nether_fortress") {
            return false;
        }
        ConfiguredStructureFeature<?, ?> configuredstructurefeature = structureEntry.structure().value();
        int i = fetchReferences(structureManager, chunk, sectionPos, configuredstructurefeature);
        HolderSet<Biome> holderset = configuredstructurefeature.biomes();
        Predicate<Holder<Biome>> predicate = (p_211672_) -> {
            return holderset.contains(this.adjustBiome(p_211672_));
        };
        StructureStart structurestart = configuredstructurefeature.generate(registry, this, this.biomeSource,
                featureManager, seed, chunkPos, i, chunk, predicate);
        if (structurestart.isValid()) {
            structureManager.setStartForFeature(sectionPos, configuredstructurefeature, structurestart, chunk);
            return true;
        } else {
            return false;
        }
    }

    // [Vanilla copy]
    private static int fetchReferences(StructureFeatureManager p_207977_, ChunkAccess p_207978_, SectionPos p_207979_, ConfiguredStructureFeature<?, ?> p_207980_) {
       StructureStart structurestart = p_207977_.getStartForFeature(p_207979_, p_207980_, p_207978_);
       return structurestart != null ? structurestart.getReferences() : 0;
    }

    @Override
    public void createStructures(RegistryAccess p_62200_, StructureFeatureManager p_62201_, ChunkAccess p_62202_, StructureManager p_62203_, long p_62204_) {
       ChunkPos chunkpos = p_62202_.getPos();
       SectionPos sectionpos = SectionPos.bottomOf(p_62202_);
       this.possibleStructureSets().forEach((p_212264_) -> {
          StructurePlacement structureplacement = p_212264_.value().placement();
          List<StructureSet.StructureSelectionEntry> list = p_212264_.value().structures();
 
          for(StructureSet.StructureSelectionEntry structureset$structureselectionentry : list) {
             StructureStart structurestart = p_62201_.getStartForFeature(sectionpos, structureset$structureselectionentry.structure().value(), p_62202_);
             if (structurestart != null && structurestart.isValid()) {
                return;
             }
          }
 
          if (structureplacement.isFeatureChunk(this, p_62204_, chunkpos.x, chunkpos.z)) {
             if (list.size() == 1) {
                this.tryGenerateStructure(list.get(0), p_62201_, p_62200_, p_62203_, p_62204_, p_62202_, chunkpos, sectionpos);
             } else {
                ArrayList<StructureSet.StructureSelectionEntry> arraylist = new ArrayList<>(list.size());
                arraylist.addAll(list);
                WorldgenRandom worldgenrandom = new WorldgenRandom(new LegacyRandomSource(0L));
                worldgenrandom.setLargeFeatureSeed(p_62204_, chunkpos.x, chunkpos.z);
                int i = 0;
 
                for(StructureSet.StructureSelectionEntry structureset$structureselectionentry1 : arraylist) {
                   i += structureset$structureselectionentry1.weight();
                }
 
                while(!arraylist.isEmpty()) {
                   int j = worldgenrandom.nextInt(i);
                   int k = 0;
 
                   for(StructureSet.StructureSelectionEntry structureset$structureselectionentry2 : arraylist) {
                      j -= structureset$structureselectionentry2.weight();
                      if (j < 0) {
                         break;
                      }
 
                      ++k;
                   }
 
                   StructureSet.StructureSelectionEntry structureset$structureselectionentry3 = arraylist.get(k);
                   if (this.tryGenerateStructure(structureset$structureselectionentry3, p_62201_, p_62200_, p_62203_, p_62204_, p_62202_, chunkpos, sectionpos)) {
                      return;
                   }
 
                   arraylist.remove(k);
                   i -= structureset$structureselectionentry3.weight();
                }
 
             }
          }
       });
    }

    @Nonnull
    @Override
    public NoiseColumn getBaseColumn(int posX, int posZ, @Nonnull LevelHeightAccessor level) {
        return new NoiseColumn(0, new BlockState[0]);
    }

    // [Vanilla copy] to ignore some features
    @Override
    public void applyBiomeDecoration(@Nonnull WorldGenLevel level, @Nonnull ChunkAccess chunk, @Nonnull StructureFeatureManager structureFeatureManager) {
        ChunkPos chunkPos = chunk.getPos();
        SectionPos sectionPos = SectionPos.of(chunkPos, level.getMinSection());
        BlockPos blockPos = sectionPos.origin();

        Registry<ConfiguredStructureFeature<?, ?>> configuredStructureFeatureRegistry = level.registryAccess().registryOrThrow(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY);
        Map<Integer, List<ConfiguredStructureFeature<?, ?>>> featuresMap = configuredStructureFeatureRegistry.stream().collect(Collectors.groupingBy((feature) -> feature.feature.step().ordinal()));
        List<BiomeSource.StepFeatureData> stepFeatureDataList = this.biomeSource.featuresPerStep();
        WorldgenRandom worldgenRandom = new WorldgenRandom(new XoroshiroRandomSource(RandomSupport.seedUniquifier()));
        long decorationSeed = worldgenRandom.setDecorationSeed(level.getSeed(), blockPos.getX(), blockPos.getZ());

        Set<Biome> possibleBiomes = new ObjectArraySet<>();
        ChunkPos.rangeClosed(sectionPos.chunk(), 1).forEach((pos) -> {
            ChunkAccess chunkAccess = level.getChunk(pos.x, pos.z);

            for (LevelChunkSection chunkSection : chunkAccess.getSections()) {
                chunkSection.getBiomes().getAll((biomeHolder) -> {
                    possibleBiomes.add(biomeHolder.value());
                });
            }

        });
        possibleBiomes.retainAll(this.biomeSource.possibleBiomes().stream().map(Holder::value).collect(Collectors.toSet()));

        int dataSize = stepFeatureDataList.size();

        try {
            Registry<PlacedFeature> placedFeatureRegistry = level.registryAccess().registryOrThrow(Registry.PLACED_FEATURE_REGISTRY);
            int maxDecorations = Math.max(GenerationStep.Decoration.values().length, dataSize);

            for (int i = 0; i < maxDecorations; ++i) {
                int index = 0;
                if (structureFeatureManager.shouldGenerateFeatures()) {
                    for (ConfiguredStructureFeature<?, ?> feature : featuresMap.getOrDefault(i, Collections.emptyList())) {
                        worldgenRandom.setFeatureSeed(decorationSeed, index, i);
                        Supplier<String> supplier = () -> configuredStructureFeatureRegistry.getResourceKey(feature).map(Object::toString).orElseGet(feature::toString);

                        try {
                            level.setCurrentlyGenerating(supplier);
                            structureFeatureManager.startsForFeature(sectionPos, feature).forEach((structureStart) -> {
                                structureStart.placeInChunk(level, structureFeatureManager, this, worldgenRandom, getWritableArea(chunk), chunkPos);
                            });
                        } catch (Exception exception) {
                            CrashReport report = CrashReport.forThrowable(exception, "Feature placement");
                            report.addCategory("Feature").setDetail("Description", supplier::get);
                            throw new ReportedException(report);
                        }

                        ++index;
                    }
                }

                if (i < dataSize) {
                    IntSet mapping = new IntArraySet();

                    for (Biome biome : possibleBiomes) {
                        List<HolderSet<PlacedFeature>> holderSets = biome.getGenerationSettings().features();
                        if (i < holderSets.size()) {
                            HolderSet<PlacedFeature> featureHolderSet = holderSets.get(i);
                            BiomeSource.StepFeatureData stepFeatureData = stepFeatureDataList.get(i);
                            //noinspection CodeBlock2Expr
                            featureHolderSet.stream().map(Holder::value).forEach((feature) -> {
                                mapping.add(stepFeatureData.indexMapping().applyAsInt(feature));
                            });
                        }
                    }

                    int mappingSize = mapping.size();
                    int[] array = mapping.toIntArray();
                    Arrays.sort(array);
                    BiomeSource.StepFeatureData stepFeatureData = stepFeatureDataList.get(i);

                    for (int j = 0; j < mappingSize; ++j) {
                        int featureIndex = array[j];
                        PlacedFeature placedfeature = stepFeatureData.features().get(featureIndex);
                        // The only reason why I needed to copy the code - checking if it should be placed
                        ResourceLocation registryName = placedfeature.feature().value().feature().getRegistryName();
                        if (registryName != null) {
                            continue;
                        }

                        Supplier<String> currentlyGenerating = () -> placedFeatureRegistry.getResourceKey(placedfeature).map(Object::toString).orElseGet(placedfeature::toString);
                        worldgenRandom.setFeatureSeed(decorationSeed, featureIndex, i);

                        try {
                            level.setCurrentlyGenerating(currentlyGenerating);
                            placedfeature.placeWithBiomeCheck(level, this, worldgenRandom, blockPos);
                        } catch (Exception exception1) {
                            CrashReport report = CrashReport.forThrowable(exception1, "Feature placement");
                            report.addCategory("Feature").setDetail("Description", currentlyGenerating::get);
                            throw new ReportedException(report);
                        }
                    }
                }
            }

            level.setCurrentlyGenerating(null);
        } catch (Exception exception2) {
            CrashReport report = CrashReport.forThrowable(exception2, "Biome decoration");
            report.addCategory("Generation").setDetail("CenterX", chunkPos.x).setDetail("CenterZ", chunkPos.z).setDetail("Seed", decorationSeed);
            throw new ReportedException(report);
        }
    }

    // [Vanilla Copy]
    private static BoundingBox getWritableArea(ChunkAccess p_187718_) {
       ChunkPos chunkpos = p_187718_.getPos();
       int i = chunkpos.getMinBlockX();
       int j = chunkpos.getMinBlockZ();
       LevelHeightAccessor levelheightaccessor = p_187718_.getHeightAccessorForGeneration();
       int k = levelheightaccessor.getMinBuildHeight() + 1;
       int l = levelheightaccessor.getMaxBuildHeight() - 1;
       return new BoundingBox(i, k, j, i + 15, l, j + 15);
    }

    @Override
    public CompletableFuture<ChunkAccess> createBiomes(Registry<Biome> biomes, Executor p_197006_, Blender p_197007_, StructureFeatureManager p_197008_, ChunkAccess chunk) {
        ChunkPos chunkpos = chunk.getPos();
        if (chunkpos.x == 0 && chunkpos.z == 0) {
            return CompletableFuture.supplyAsync(Util.wrapThreadWithTaskName("init_biomes", () -> {
                this.doCreateBiomes(chunk, biomes.getHolderOrThrow(Biomes.BIRCH_FOREST));
                return chunk;
            }), Util.backgroundExecutor());
        }
        return super.createBiomes(biomes, p_197006_, p_197007_, p_197008_, chunk);
    }
 
    // [Vanilla Copy]
    private void doCreateBiomes(ChunkAccess chunk, Holder<Biome> biome) {
        LevelHeightAccessor levelheightaccessor = chunk.getHeightAccessorForGeneration();
  
        for(int k = levelheightaccessor.getMinSection(); k < levelheightaccessor.getMaxSection(); ++k) {
           LevelChunkSection levelchunksection = chunk.getSection(chunk.getSectionIndexFromSectionY(k));

           PalettedContainer<Holder<Biome>> palettedcontainer = levelchunksection.getBiomes();
           palettedcontainer.acquire();
     
           try {
              for(int t = 0; t < 4; ++t) {
                 for(int l = 0; l < 4; ++l) {
                    for(int i1 = 0; i1 < 4; ++i1) {
                       palettedcontainer.getAndSetUnchecked(t, l, i1, biome);
                    }
                 }
              }
           } finally {
              palettedcontainer.release();
           }
        }
    }
}
