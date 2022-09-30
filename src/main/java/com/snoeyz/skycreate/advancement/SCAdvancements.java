package com.snoeyz.skycreate.advancement;

// import static com.snoeyz.skycreate.advancement.SkyCreateAdvancement.TaskType.EXPERT;
import static com.snoeyz.skycreate.advancement.SkyCreateAdvancement.TaskType.NOISY;
// import static com.snoeyz.skycreate.advancement.SkyCreateAdvancement.TaskType.SECRET;
import static com.snoeyz.skycreate.advancement.SkyCreateAdvancement.TaskType.SILENT;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import org.slf4j.Logger;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.snoeyz.skycreate.SkyCreateMod;
import com.snoeyz.skycreate.advancement.SkyCreateAdvancement.Builder;
import com.snoeyz.skycreate.registry.SCBlocks;
import com.snoeyz.skycreate.util.NbtBuilder;

import net.minecraft.advancements.Advancement;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;

public class SCAdvancements implements DataProvider {

    public static final List<SkyCreateAdvancement> ENTRIES = new ArrayList<>();
    public static final SkyCreateAdvancement START = null,

    ROOT = skyCreate("root", b -> b.icon(Blocks.GRASS_BLOCK)
        .title("SkyBlock Create")
        .description("Here Be SkyBlock Contraptions!")
        .awardedForFree()
        .lootOnlyRewards(new ResourceLocation(SkyCreateMod.MOD_ID, "advancement_reward/grant_book_on_first_join"))
        .special(SILENT)),
    
    WELCOME = skyCreate("welcome", b -> b.icon(SCBlocks.PULVERIZER)
        .title("Getting Started")
        .description("Let's Get Pulverizing!")
        .whenItemCollected(ip -> ip.of(ForgeRegistries.ITEMS.getValue(new ResourceLocation("patchouli", "guide_book"))).hasNbt(
            NbtBuilder.builder()
                .putString("patchouli:book", "skycreate:skyblock_create_guide")
                .build()
        ))
        .after(ROOT)
        .lootOnlyRewards(new ResourceLocation(SkyCreateMod.MOD_ID, "advancement_reward/welcome"))
        .special(NOISY)),

    HUNGER = skyCreate("hunger", b -> b.icon(Items.APPLE)
        .title("Feeling a Bit Peckish?")
        .description("Try not to work yourself to death...")
        .after(WELCOME)
        .lootOnlyRewards(new ResourceLocation(SkyCreateMod.MOD_ID, "advancement_reward/hunger"))
        .special(NOISY)),

    MILLING = skyCreate("milling", b -> b.icon(Blocks.COBBLESTONE)
        .title("Time to Grind")
        .description("You're going to need some gravel to chop down those trees...")
        .after(WELCOME)
        .whenIconCollected()
        .lootOnlyRewards(new ResourceLocation(SkyCreateMod.MOD_ID, "advancement_reward/milling"))
        .special(NOISY)),

    FARMING = skyCreate("farming", b -> b.icon(ForgeRegistries.ITEMS.getValue(new ResourceLocation("notreepunching", "flint_hoe")))
        .title("Let's Get Farming!")
        .description("Farming is a great way to get food!")
        .whenIconCollected()
        .after(HUNGER)
        .special(NOISY)),

    //
    END = null;

private static SkyCreateAdvancement skyCreate(String id, UnaryOperator<Builder> b) {
    return new SkyCreateAdvancement(id, b);
}

// Datagen

private static final Logger LOGGER = LogUtils.getLogger();
private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting()
    .create();
private final DataGenerator generator;

public SCAdvancements(DataGenerator generatorIn) {
    this.generator = generatorIn;
}

@Override
public void run(HashCache cache) throws IOException {
    Path path = this.generator.getOutputFolder();
    Set<ResourceLocation> set = Sets.newHashSet();
    Consumer<Advancement> consumer = (p_204017_3_) -> {
        if (!set.add(p_204017_3_.getId()))
            throw new IllegalStateException("Duplicate advancement " + p_204017_3_.getId());

        Path path1 = getPath(path, p_204017_3_);

        try {
            DataProvider.save(GSON, cache, p_204017_3_.deconstruct()
                .serializeToJson(), path1);
        } catch (IOException ioexception) {
            LOGGER.error("Couldn't save advancement {}", path1, ioexception);
        }
    };

    for (SkyCreateAdvancement advancement : ENTRIES)
        advancement.save(consumer);
}

private static Path getPath(Path pathIn, Advancement advancementIn) {
    return pathIn.resolve("data/" + advancementIn.getId()
        .getNamespace() + "/advancements/"
        + advancementIn.getId()
            .getPath()
        + ".json");
}

@Override
public String getName() {
    return "SkyBlock Create's Advancements";
}

public static JsonObject provideLangEntries() {
    JsonObject object = new JsonObject();
    for (SkyCreateAdvancement advancement : ENTRIES)
        advancement.appendToLang(object);
    return object;
}

public static void register() {}

}