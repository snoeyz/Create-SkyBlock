package com.snoeyz.skycreate.components.pulverizer;

import java.util.List;
import java.util.Optional;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.foundation.utility.VecHelper;
import com.snoeyz.skycreate.recipe.PulverizingRecipe;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;

public class PulverizerHandler {

    public static void activate(Level world, Vec3 center, BlockPos targetPos, Vec3 movementVector) {
        Optional<PulverizingRecipe> recipe = getRecipe(world, targetPos);
        if (recipe.isPresent()) {
            world.playSound(null, targetPos, SoundEvents.GRAVEL_BREAK, SoundSource.BLOCKS, .5f, 1f);
            replacePulverizedBlock(world, targetPos, recipe.get());
            spawnItemDrops(world, targetPos, recipe.get());
        }
    }

    private static Optional<PulverizingRecipe> getRecipe(Level world, BlockPos targetPos) {
        BlockState target = world.getBlockState(targetPos);
        return PulverizingRecipe.getRecipe(world, target);
    }

    public static boolean shouldActivate(Level world, BlockPos targetPos) {
        return getRecipe(world, targetPos).isPresent();
    }

    private static void spawnItemDrops(Level world, BlockPos targetPos, PulverizingRecipe recipe) {
        if (world instanceof ServerLevel) {
            List<ItemStack> drops = recipe.rollResultItems();
            for (ItemStack eachItem : drops) {
                Vec3 vec = VecHelper.offsetRandomly(VecHelper.getCenterOf(targetPos), world.random, 0.125f);
                ItemEntity itemEntity = new ItemEntity(world, vec.x, vec.y, vec.z, eachItem);
                itemEntity.setDefaultPickUpDelay();
                itemEntity.setDeltaMovement(Vec3.ZERO);
                world.addFreshEntity(itemEntity);
            }
        }
    }

    private static void replacePulverizedBlock(Level world, BlockPos targetPos, PulverizingRecipe recipe) {
        FluidState fluidState = world.getFluidState(targetPos);
        if (recipe.getOutputBlocks().isEmpty()) {
            world.setBlock(targetPos, fluidState.createLegacyBlock(), 3);
        } else {
            world.setBlock(targetPos, recipe.rollResultBlockState(), 3);
        }
    }

    // private static void playSound(Level world, BlockPos targetPos, PulverizeTarget targetType) {
    //     SoundEvent soundEvent;
    //     float volume = 0.5f;
    //     float pitch = 1.0f;
    //     switch (targetType) {
    //         case Cobblestone:
    //             soundEvent = SoundEvents.GRAVEL_BREAK;
    //             break;
    //         case Gravel:
    //             soundEvent = SoundEvents.SAND_BREAK;
    //             break;
    //         case Ice:
    //             soundEvent = SoundEvents.GLASS_BREAK;
    //             break;
    //         case LogVariant:
    //             soundEvent = SoundEvents.WOOD_BREAK;
    //             break;
    //         case Stone:
    //         case Tuff:
    //             soundEvent = SoundEvents.STONE_BREAK;
    //             break;
    //         case INVALID:
    //         default:
    //             return;
    //     }
    //     world.playSound(null, targetPos, soundEvent, SoundSource.BLOCKS, volume, pitch);
    // }

    public static void playPulverizerHitSound(Level world, BlockPos targetPos) {
        AllSoundEvents.MECHANICAL_PRESS_ACTIVATION.playOnServer(world, targetPos);
    }
}
