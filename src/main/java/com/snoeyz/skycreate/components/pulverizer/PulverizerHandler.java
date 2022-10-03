package com.snoeyz.skycreate.components.pulverizer;

import java.util.List;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.foundation.utility.VecHelper;
import com.snoeyz.skycreate.SkyCreateMod;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.phys.Vec3;

public class PulverizerHandler {

    static enum PulverizeTarget {
        // Specific Blocks
        Cobblestone, Gravel, Ice, Stone, Tuff,

        // Block Categories
        LogVariant,

        // Invalid Target
        INVALID;
    }

    public static void activate(Level world, Vec3 center, BlockPos targetPos, Vec3 movementVector) {
        PulverizeTarget targetType = getTargetType(world, targetPos);
        if (targetType != PulverizeTarget.INVALID) {
            playSound(world, targetPos, targetType);
            spawnLootItems(world, targetPos, targetType);
            replacePulverizedBlock(world, targetPos, targetType);
        }
    }

    private static PulverizeTarget getTargetType(Level world, BlockPos targetPos) {
        BlockState target = world.getBlockState(targetPos);
        if (target.is(Blocks.COBBLESTONE)) return PulverizeTarget.Cobblestone;
        if (target.is(Blocks.GRAVEL)) return PulverizeTarget.Gravel;
        if (target.is(Blocks.ICE)) return PulverizeTarget.Ice;
        if (target.is(BlockTags.LOGS)) return PulverizeTarget.LogVariant;
        if (target.is(Blocks.STONE)) return PulverizeTarget.Stone;
        if (target.is(Blocks.TUFF)) return PulverizeTarget.Tuff;

        return PulverizeTarget.INVALID;
    }

    public static boolean shouldActivate(Level world, BlockPos targetPos) {
        return getTargetType(world, targetPos) != PulverizeTarget.INVALID;
    }

    private static void spawnLootItems(Level world, BlockPos targetPos, PulverizeTarget targetType) {
        if (world instanceof ServerLevel) {
            ResourceLocation lootTable;
            switch (targetType) {
                case Cobblestone:
                    lootTable = new ResourceLocation(SkyCreateMod.MOD_ID, "pulverizing/cobblestone");
                    break;
                case Gravel:
                    lootTable = new ResourceLocation(SkyCreateMod.MOD_ID, "pulverizing/gravel");
                    break;
                case Stone:
                    lootTable = new ResourceLocation(SkyCreateMod.MOD_ID, "pulverizing/stone");
                    break;
                case Tuff:
                    lootTable = new ResourceLocation(SkyCreateMod.MOD_ID, "pulverizing/tuff");
                    break;
                case LogVariant:
                    lootTable = new ResourceLocation(SkyCreateMod.MOD_ID, "pulverizing/logs");
                    break;
                default:
                    return;
            }
            List<ItemStack> loot = getLootItems((ServerLevel)world, lootTable);
            for (ItemStack eachItem : loot) {
                Vec3 vec = VecHelper.offsetRandomly(VecHelper.getCenterOf(targetPos), world.random, 0.125f);
                ItemEntity itemEntity = new ItemEntity(world, vec.x, vec.y, vec.z, eachItem);
                itemEntity.setDefaultPickUpDelay();
                itemEntity.setDeltaMovement(Vec3.ZERO);
                world.addFreshEntity(itemEntity);
            }
        }
    }

    private static List<ItemStack> getLootItems(ServerLevel world, ResourceLocation lootTable) {
        LootContext ctx = new LootContext.Builder(world)
            .withRandom(world.getRandom())
            .create(LootContextParamSet.builder().build());
        LootTable table = ctx.getLootTable(lootTable);
        return table.getRandomItems(ctx);
    }

    private static void replacePulverizedBlock(Level world, BlockPos targetPos, PulverizeTarget targetType) {
        FluidState fluidState = world.getFluidState(targetPos);
        switch (targetType) {
            case Cobblestone:
            case Tuff:
            case LogVariant:
                world.setBlock(targetPos, fluidState.createLegacyBlock(), 3);
                break;
            case Stone:
                world.setBlock(targetPos, Blocks.COBBLESTONE.defaultBlockState(), 3);
                break;
            case Gravel:
                world.setBlock(targetPos, Blocks.SAND.defaultBlockState(), Block.UPDATE_ALL);
                break;
            case Ice:
                world.setBlock(targetPos, Blocks.WATER.defaultBlockState(), Block.UPDATE_ALL);
                break;
            case INVALID:
            default:
                break;
        }
    }

    private static void playSound(Level world, BlockPos targetPos, PulverizeTarget targetType) {
        SoundEvent soundEvent;
        float volume = 0.5f;
        float pitch = 1.0f;
        switch (targetType) {
            case Cobblestone:
                soundEvent = SoundEvents.GRAVEL_BREAK;
                break;
            case Gravel:
                soundEvent = SoundEvents.SAND_BREAK;
                break;
            case Ice:
                soundEvent = SoundEvents.GLASS_BREAK;
                break;
            case LogVariant:
                soundEvent = SoundEvents.WOOD_BREAK;
                break;
            case Stone:
            case Tuff:
                soundEvent = SoundEvents.STONE_BREAK;
                break;
            case INVALID:
            default:
                return;
        }
        world.playSound(null, targetPos, soundEvent, SoundSource.BLOCKS, volume, pitch);
    }

    public static void playPulverizerHitSound(Level world, BlockPos targetPos) {
        AllSoundEvents.MECHANICAL_PRESS_ACTIVATION.playOnServer(world, targetPos);
    }
}
