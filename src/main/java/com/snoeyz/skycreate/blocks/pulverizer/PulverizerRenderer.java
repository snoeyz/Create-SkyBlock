package com.snoeyz.skycreate.blocks.pulverizer;

import static com.simibubi.create.content.contraptions.base.DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE;
import static com.simibubi.create.content.contraptions.base.DirectionalKineticBlock.FACING;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.core.virtual.VirtualRenderWorld;
import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.base.IRotate;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionMatrices;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionRenderDispatcher;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.tileEntity.renderer.SafeTileEntityRenderer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.VecHelper;
import com.snoeyz.skycreate.registry.SCBlockPartials;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class PulverizerRenderer extends SafeTileEntityRenderer<PulverizerTileEntity> {

    public PulverizerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    protected void renderSafe(PulverizerTileEntity te, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        if (Backend.canUseInstancing(te.getLevel())) return;
        renderComponents(te, partialTicks, ms, buffer, light, overlay);
    }

    protected void renderComponents(PulverizerTileEntity te, float partialTicks, PoseStack ms, MultiBufferSource buffer,
        int light, int overlay) {
        VertexConsumer vb = buffer.getBuffer(RenderType.solid());
        if (!Backend.canUseInstancing(te.getLevel())) {
            KineticTileEntityRenderer.renderRotatingKineticBlock(te, getRenderedBlockState(te), ms, vb, light);
        }

        BlockState blockState = te.getBlockState();
        Vec3 offset = getHandOffset(te, partialTicks, blockState);

        SuperByteBuffer pole = CachedBufferer.partial(AllBlockPartials.DEPLOYER_POLE, blockState);
        // SuperByteBuffer hand = CachedBufferer.partial(te.getHandPose(), blockState);

        transform(pole.translate(offset.x, offset.y, offset.z), blockState, true).light(light).renderInto(ms, vb);
        // transform(hand.translate(offset.x, offset.y, offset.z), blockState, false).light(light).renderInto(ms, vb);
    }

    protected Vec3 getHandOffset(PulverizerTileEntity te, float partialTicks, BlockState blockState) {
        float distance = te.getHandOffset(partialTicks);
        return Vec3.atLowerCornerOf(blockState.getValue(FACING).getNormal()).scale(distance);
    }

    protected BlockState getRenderedBlockState(KineticTileEntity te) {
        return KineticTileEntityRenderer.shaft(KineticTileEntityRenderer.getRotationAxisOf(te));
    }

    private static SuperByteBuffer transform(SuperByteBuffer buffer, BlockState deployerState, boolean axisDirectionMatters) {
        Direction facing = deployerState.getValue(FACING);

        float yRot = AngleHelper.horizontalAngle(facing);
        float xRot = facing == Direction.UP ? 270 : facing == Direction.DOWN ? 90 : 0;
        float zRot = axisDirectionMatters && (deployerState.getValue(AXIS_ALONG_FIRST_COORDINATE) ^ facing.getAxis() == Axis.Z) ? 90 : 0;

        buffer.rotateCentered(Direction.UP, (float) ((yRot) / 180 * Math.PI));
        buffer.rotateCentered(Direction.EAST, (float) ((xRot) / 180 * Math.PI));
        buffer.rotateCentered(Direction.SOUTH, (float) ((zRot) / 180 * Math.PI));
        return buffer;
    }

    public static void renderInContraption(MovementContext context, VirtualRenderWorld renderWorld, ContraptionMatrices matrices, MultiBufferSource buffer) {
        VertexConsumer builder = buffer.getBuffer(RenderType.solid());
        BlockState blockState = context.state;

        float speed = (float) context.getAnimationSpeed();
        if (context.contraption.stalled)
            speed = 0;

        SuperByteBuffer shaft = CachedBufferer.block(AllBlocks.SHAFT.getDefaultState());
        SuperByteBuffer pole = CachedBufferer.partial(AllBlockPartials.DEPLOYER_POLE, blockState);
        SuperByteBuffer plate = CachedBufferer.partial(SCBlockPartials.PULVERIZER, blockState);

        double factor;
        if (context.contraption.stalled || context.position == null || context.data.contains("StationaryTimer")) {
            factor = Mth.sin(AnimationTickHolder.getRenderTime() * .5f) * .25f + .25f;
        } else {
            Vec3 center = VecHelper.getCenterOf(new BlockPos(context.position));
            double distance = context.position.distanceTo(center);
            double nextDistance = context.position.add(context.motion).distanceTo(center);
            factor = .5f - Mth.clamp(Mth.lerp(AnimationTickHolder.getPartialTicks(), distance, nextDistance), 0, 1);
        }

        Vec3 offset = Vec3.atLowerCornerOf(blockState.getValue(FACING)
            .getNormal()).scale(factor);

        PoseStack m = matrices.getModel();
        m.pushPose();

        m.pushPose();
        Axis axis = Axis.Y;
        if (context.state.getBlock() instanceof IRotate) {
            IRotate def = (IRotate) context.state.getBlock();
            axis = def.getRotationAxis(context.state);
        }

        float time = AnimationTickHolder.getRenderTime(context.world) / 20;
        float angle = (time * speed) % 360;

        TransformStack.cast(m).centre().rotateY(axis == Axis.Z ? 90 : 0).rotateZ(axis.isHorizontal() ? 90 : 0).unCentre();
        shaft.transform(m);
        shaft.rotateCentered(Direction.get(AxisDirection.POSITIVE, Axis.Y), angle);
        m.popPose();

        m.translate(offset.x, offset.y, offset.z);
        pole.transform(m);
        plate.transform(m);

        transform(pole, blockState, true);
        transform(plate, blockState, false);

        shaft.light(matrices.getWorld(), ContraptionRenderDispatcher.getContraptionWorldLight(context, renderWorld)).renderInto(matrices.getViewProjection(), builder);
        pole.light(matrices.getWorld(), ContraptionRenderDispatcher.getContraptionWorldLight(context, renderWorld)).renderInto(matrices.getViewProjection(), builder);
        plate.light(matrices.getWorld(), ContraptionRenderDispatcher.getContraptionWorldLight(context, renderWorld)).renderInto(matrices.getViewProjection(), builder);

        m.popPose();
    }
}
