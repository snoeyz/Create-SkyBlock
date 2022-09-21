package com.snoeyz.skycreate.blocks.pulverizer;

import javax.annotation.ParametersAreNonnullByDefault;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.contraptions.base.DirectionalAxisKineticBlock;
import com.simibubi.create.content.contraptions.components.AssemblyOperatorUseContext;
import com.simibubi.create.foundation.block.ITE;
import com.snoeyz.skycreate.registry.SCTileEntities;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PulverizerBlock extends DirectionalAxisKineticBlock implements ITE<PulverizerTileEntity> {

    public PulverizerBlock(Properties properties) {
        super(properties);
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.NORMAL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return AllShapes.CASING_12PX.get(state.getValue(FACING));
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        ItemStack heldByPlayer = player.getItemInHand(handIn).copy();
        if (AllItems.WRENCH.isIn(heldByPlayer))
            return InteractionResult.PASS;

        if (hit.getDirection() != state.getValue(FACING))
            return InteractionResult.PASS;
        if (worldIn.isClientSide)
            return InteractionResult.SUCCESS;

        return InteractionResult.SUCCESS;
    }

    @Override
    public Class<PulverizerTileEntity> getTileEntityClass() {
        return PulverizerTileEntity.class;
    }

    @Override
    public BlockEntityType<? extends PulverizerTileEntity> getTileEntityType() {
        return SCTileEntities.PULVERIZER.get();
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter reader, BlockPos pos, PathComputationType type) {
        return false;
    }

    @Override
    protected Direction getFacingForPlacement(BlockPlaceContext context) {
        if (context instanceof AssemblyOperatorUseContext)
            return Direction.DOWN;
        else
            return super.getFacingForPlacement(context);
    }
}
