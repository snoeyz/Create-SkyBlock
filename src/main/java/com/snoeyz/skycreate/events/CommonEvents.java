package com.snoeyz.skycreate.events;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllTags.AllBlockTags;
import com.simibubi.create.content.palettes.AllPaletteBlocks;
import com.simibubi.create.content.palettes.AllPaletteStoneTypes;
import com.snoeyz.skycreate.advancement.SCAdvancements;
import com.snoeyz.skycreate.util.WorldUtil;

import net.minecraft.advancements.Advancement;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class CommonEvents {

    @SubscribeEvent
    public static void onAdvancement(AdvancementEvent event) {
        if (!event.getPlayer().getLevel().isClientSide()) { // Server-side check
            Advancement adv = event.getAdvancement();
            if (adv.getId().compareTo(new ResourceLocation("create", "hand_crank_000")) == 0) {
                SCAdvancements.HUNGER.awardTo(event.getPlayer());
            }
        }
    }

    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.CreateSpawnPosition event) {
        if (!event.getWorld().isClientSide()) { // Server-side check
            ServerLevel overworld = event.getWorld().getServer().overworld();
            if (WorldUtil.isSkyCreate(overworld)) {
                BlockPos spawnPos = new BlockPos(6, 64, 6);
                event.setCanceled(true);
                overworld.setDefaultSpawnPos(spawnPos, 0f);
            }
        }
    }

    @SubscribeEvent
    public static void onFluidPlaceBlock(BlockEvent.FluidPlaceBlockEvent event) {
        if (!event.getWorld().isClientSide()) { // Server-side check
            if (WorldUtil.isSkyCreate(event.getWorld().getServer().overworld())) {
                //BlockState liquidState = event.getWorld().getBlockState(event.getLiquidPos());
                BlockState blockBelow = event.getWorld().getBlockState(event.getPos().below());
                BlockState newBlock = null;
                if (event.getNewState().is(Blocks.COBBLESTONE)) {
                    if (blockBelow.is(Blocks.BONE_BLOCK)) {
                        newBlock = Blocks.CALCITE.defaultBlockState();
                    } else if (blockBelow.is(Blocks.LAPIS_BLOCK)) {
                        newBlock = AllPaletteStoneTypes.ASURINE.baseBlock.get().defaultBlockState();
                    }
                }
                if (newBlock != null) {
                    event.setNewState(newBlock);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerRightClickBlocktEvent(PlayerInteractEvent.RightClickBlock event) {
        if (event.getPlayer() instanceof ServerPlayer) {  // Is server-side player
            ServerPlayer player = (ServerPlayer)event.getPlayer();
            if (event.getItemStack().is(Items.POTION)) {
                Potion potion = PotionUtils.getPotion(event.getItemStack());
                if (potion.equals(Potions.WATER)) { // Used water bottle
                    Level level = event.getWorld();
                    BlockPos blockPos = event.getPos();
                    BlockState blockstate = level.getBlockState(blockPos);
                    if (blockstate.is(Blocks.SAND)) {
                        level.setBlock(blockPos, Blocks.CLAY.defaultBlockState(), Block.UPDATE_ALL);
                        player.setItemInHand(event.getHand(), new ItemStack(Items.GLASS_BOTTLE));

                        level.playSound(null, blockPos, SoundEvents.SAND_STEP, SoundSource.BLOCKS, 0.5f, 1f);

                        event.setCanceled(true);
                    }
                }
            }
        }
    }
}
