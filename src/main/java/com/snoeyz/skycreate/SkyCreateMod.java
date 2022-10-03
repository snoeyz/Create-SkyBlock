package com.snoeyz.skycreate;

import com.mojang.logging.LogUtils;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.snoeyz.skycreate.advancement.SCAdvancements;
import com.snoeyz.skycreate.advancement.SCTriggers;
import com.snoeyz.skycreate.datagen.LangMerger;
import com.snoeyz.skycreate.datagen.recipe.PulverizingRecipeGen;
import com.snoeyz.skycreate.datagen.recipe.StandardRecipeGen;
import com.snoeyz.skycreate.registry.*;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(SkyCreateMod.MOD_ID)
public class SkyCreateMod
{
    private static final NonNullSupplier<CreateRegistrate> registrate = CreateRegistrate.lazy(SkyCreateMod.MOD_ID);
    public static final CreativeModeTab MAIN_CREATIVE_TAB = new CreativeModGroup();

    public static final String MOD_ID = "skycreate";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public SkyCreateMod()
    {
        IEventBus modbus = FMLJavaModLoadingContext.get().getModEventBus();
        // Register the setup method for modloading
        modbus.addListener(this::setup);
        Registration.WORLD_TYPES.register(modbus);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
		modbus.addListener(EventPriority.LOWEST, SkyCreateMod::gatherData);

        SCBlocks.register();
        SCTileEntities.register();
        SCRecipeTypes.register(modbus);
        
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            SCBlockPartials.init();
            modbus.addListener(SkyCreateMod::clientInit);
        });
    }

    public static void clientInit(final FMLClientSetupEvent event) {
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        LOGGER.info("Registering SkyCreate Mod...");
        Registration.init();

        event.enqueueWork(() -> {
            SCAdvancements.register();
            SCTriggers.register();
        });
    }

    public static void gatherData(GatherDataEvent event) {
        DataGenerator gen = event.getGenerator();
        if (event.includeClient()) {
            gen.addProvider(new LangMerger(gen));
        }
        if (event.includeServer()) {
            gen.addProvider(new SCAdvancements(gen));
            gen.addProvider(new PulverizingRecipeGen(gen));
            gen.addProvider(new StandardRecipeGen(gen));
        }
    }
    
    public static CreateRegistrate registrate() {
        return registrate.get();
    }

    public static ResourceLocation asResource(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
