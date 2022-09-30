package com.snoeyz.skycreate.components.pulverizer;

import static com.simibubi.create.content.contraptions.base.DirectionalKineticBlock.FACING;

import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.curiosities.tools.SandPaperItem;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import com.snoeyz.skycreate.registry.SCBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PulverizerTileEntity extends KineticTileEntity {

    protected State state;
    protected int timer;
	protected float reach;
    protected int loopCount;
    private LerpedFloat animatedOffset;

    enum State {
        WAITING, EXPANDING, RETRACTING;
    }
    
    public PulverizerTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.state = State.WAITING;
        animatedOffset = LerpedFloat.linear().startWithValue(0);
    }

    protected int getTimerSpeed() {
        return (int) (getSpeed() == 0 ? 0 : Mth.clamp(Math.abs(getSpeed() * 8), 8, 512));
    }

    @Override
    public void tick() {
        super.tick();

        if (getSpeed() == 0)
            return;
        if (timer > 0) {
            timer -= getTimerSpeed();
            return;
        }
        if (level.isClientSide)
            return;
        if (state == State.WAITING) {

            start();
            return;
        }

        if (state == State.EXPANDING) {
            activate();

            state = State.RETRACTING;
            timer = 1000;
            sendData();
            return;
        }

        if (state == State.RETRACTING) {
            state = State.WAITING;
            timer = 500;
            sendData();
            return;
        }
    }

    protected void start() {
        state = State.EXPANDING;
        Vec3 movementVector = getMovementVector();
        Vec3 rayOrigin = VecHelper.getCenterOf(worldPosition).add(movementVector.scale(3 / 2f));
        Vec3 rayTarget = VecHelper.getCenterOf(worldPosition).add(movementVector.scale(5 / 2f));
        ClipContext rayTraceContext = new ClipContext(rayOrigin, rayTarget, Block.OUTLINE, Fluid.NONE, null);
        BlockHitResult result = level.clip(rayTraceContext);
        reach = (float) (.5f + Math.min(result.getLocation().subtract(rayOrigin).length(), .75f));
        timer = 1000;
        if (loopCount == 0) loopCount = 5;
        sendData();
    }

    protected void activate() {
        Vec3 movementVector = getMovementVector();
        Direction direction = getBlockState().getValue(FACING);
        Vec3 center = VecHelper.getCenterOf(worldPosition);
        BlockPos targetPos = worldPosition.relative(direction, 2);

        if (!PulverizerHandler.shouldActivate(level, targetPos)) {
            loopCount = 0;
            return;
        }

        loopCount--;
        if (loopCount > 0) {
            PulverizerHandler.playPulverizerHitSound(level, targetPos);
        } else {
            PulverizerHandler.activate(level, center, targetPos, movementVector);
        }
    }

    protected Vec3 getMovementVector() {
        if (!SCBlocks.PULVERIZER.has(getBlockState()))
            return Vec3.ZERO;
        return Vec3.atLowerCornerOf(getBlockState().getValue(FACING).getNormal());
    }

    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        state = NBTHelper.readEnum(compound, "State", State.class);
        timer = compound.getInt("Timer");
        loopCount = compound.getInt("LoopCount");
        super.read(compound, clientPacket);

        if (!clientPacket)
            return;
        reach = compound.getFloat("Reach");
        if (compound.contains("Particle")) {
            ItemStack particleStack = ItemStack.of(compound.getCompound("Particle"));
            SandPaperItem.spawnParticles(VecHelper.getCenterOf(worldPosition).add(getMovementVector().scale(reach + 1)), particleStack, this.level);
        }
    }

    @Override
    public void write(CompoundTag compound, boolean clientPacket) {
        NBTHelper.writeEnum(compound, "State", state);
        compound.putInt("Timer", timer);
        compound.putInt("LoopCount", loopCount);

        super.write(compound, clientPacket);

        if (!clientPacket)
            return;
        compound.putFloat("Reach", reach);
        // if (player.spawnedItemEffects != null) {
        //     compound.put("Particle", player.spawnedItemEffects.serializeNBT());
        //     player.spawnedItemEffects = null;
        // }
    }

    @OnlyIn(Dist.CLIENT)
    public float getHandOffset(float partialTicks) {
        if (isVirtual())
            return animatedOffset.getValue(partialTicks);

        float progress = 0;
        int timerSpeed = getTimerSpeed();

        if (state == State.EXPANDING) {
            progress = 1 - (timer - partialTicks * timerSpeed) / 1000f;
        }
        if (state == State.RETRACTING)
            progress = (timer - partialTicks * timerSpeed) / 1000f;
        float handLength =  3 / 16f;
        float distance = Math.min(Mth.clamp(progress, 0, 1) * (reach + handLength), 21 / 16f);

        return distance;
    }

    public void setAnimatedOffset(float offset) {
        animatedOffset.setValue(offset);
    }
}
