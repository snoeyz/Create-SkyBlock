package com.snoeyz.skycreate.components.pulverizer;

import static com.simibubi.create.content.contraptions.base.DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE;
import static com.simibubi.create.content.contraptions.base.DirectionalKineticBlock.FACING;

import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.api.instance.DynamicInstance;
import com.jozufozu.flywheel.core.materials.oriented.OrientedData;
import com.mojang.math.Quaternion;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.relays.encased.ShaftInstance;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.snoeyz.skycreate.registry.SCBlockPartials;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;

public class PulverizerInstance extends ShaftInstance implements DynamicInstance {

    final PulverizerTileEntity tile;
    final Direction facing;
    final float yRot;
    final float xRot;
    final float zRot;

    protected final OrientedData pole;

    protected OrientedData hand;

    float progress;

    public PulverizerInstance(MaterialManager dispatcher, KineticTileEntity tile) {
        super(dispatcher, tile);

        this.tile = (PulverizerTileEntity) super.blockEntity;
        facing = blockState.getValue(FACING);

        boolean rotatePole = blockState.getValue(AXIS_ALONG_FIRST_COORDINATE) ^ facing.getAxis() == Direction.Axis.Z;

        yRot = AngleHelper.horizontalAngle(facing);
        xRot = facing == Direction.UP ? 270 : facing == Direction.DOWN ? 90 : 0;
        zRot = rotatePole ? 90 : 0;

        pole = getOrientedMaterial().getModel(AllBlockPartials.DEPLOYER_POLE, blockState).createInstance();

        hand = getOrientedMaterial().getModel(SCBlockPartials.PULVERIZER, blockState).createInstance();

        progress = getProgress(AnimationTickHolder.getPartialTicks());
        updateRotation(pole, hand, yRot, xRot, zRot);
        updatePosition();
    }

    @Override
    public void beginFrame() {

        float newProgress = getProgress(AnimationTickHolder.getPartialTicks());

        if (Mth.equal(newProgress, progress)) return;

        progress = newProgress;

        updatePosition();
    }

    @Override
    public void updateLight() {
        super.updateLight();
        relight(pos, hand, pole);
    }

    @Override
    public void remove() {
        super.remove();
        hand.delete();
        pole.delete();
    }

    private float getProgress(float partialTicks) {
        if (tile.state == PulverizerTileEntity.State.EXPANDING) {
            float f = 1 - (tile.timer - partialTicks * tile.getTimerSpeed()) / 1000f;
            return f;
        }
        if (tile.state == PulverizerTileEntity.State.RETRACTING)
            return (tile.timer - partialTicks * tile.getTimerSpeed()) / 1000f;
        return 0;
    }

    private void updatePosition() {
        float handLength =  3 / 16f;
        float distance = Math.min(Mth.clamp(progress, 0, 1) * (tile.reach + handLength), 21 / 16f);
        Vec3i facingVec = facing.getNormal();
        BlockPos blockPos = getInstancePosition();

        float x = blockPos.getX() + ((float) facingVec.getX()) * distance;
        float y = blockPos.getY() + ((float) facingVec.getY()) * distance;
        float z = blockPos.getZ() + ((float) facingVec.getZ()) * distance;

        pole.setPosition(x, y, z);
        hand.setPosition(x, y, z);
    }

    static void updateRotation(OrientedData pole, OrientedData hand, float yRot, float xRot, float zRot) {

        Quaternion q = Direction.UP.step().rotationDegrees(yRot);
        q.mul(Direction.EAST.step().rotationDegrees(xRot));

        hand.setRotation(q);

        q.mul(Direction.SOUTH.step().rotationDegrees(zRot));

        pole.setRotation(q);
    }
}
