package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.List;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.model.MekanismModelCache;
import mekanism.client.render.RenderTickHandler;
import mekanism.client.render.lib.Outlines;
import mekanism.client.render.lib.Outlines.Line;
import mekanism.client.render.tileentity.RenderPigmentMixer.PigmentMixerRenderState;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.tile.machine.TileEntityPigmentMixer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.util.Unit;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class RenderPigmentMixer extends MekanismTileEntityRenderer<TileEntityPigmentMixer, PigmentMixerRenderState> implements IWireFrameRenderer {

    private static final float SHAFT_SPEED = 5F;
    @Nullable
    private static List<Line> lines;

    public static void resetCached() {
        lines = null;
    }

    public RenderPigmentMixer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public PigmentMixerRenderState createRenderState() {
        return new PigmentMixerRenderState();
    }

    @Override
    public void extractRenderState(TileEntityPigmentMixer mixer, PigmentMixerRenderState state, float partialTick, Vec3 cameraPosition, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        super.extractRenderState(mixer, state, partialTick, cameraPosition, breakProgress);
        state.direction = mixer.getDirection();
        state.rotation = (mixer.getLevel().getGameTime() + partialTick) * SHAFT_SPEED % 360;
    }

    @Override
    public void submit(PigmentMixerRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState camera) {
        if (state.direction == null) {
            return;
        }
        poseStack.pushPose();
        switch (state.direction) {
            case NORTH -> poseStack.translate(7 / 16F, 0, 6 / 16F);
            case SOUTH -> poseStack.translate(7 / 16F, 0, 0.5F);
            case WEST -> poseStack.translate(6 / 16F, 0, 7 / 16F);
            case EAST -> poseStack.translate(0.5F, 0, 7 / 16F);
        }
        float shift = 1 / 16F;
        poseStack.translate(shift, 0, shift);
        poseStack.mulPose(Axis.YN.rotationDegrees(state.rotation));
        poseStack.translate(-shift, 0, -shift);
        nodeCollector.submitModel(
              MekanismModelCache.INSTANCE.PIGMENT_MIXER_SHAFT.getBakedModel(),
              Unit.INSTANCE,
              poseStack,
              renderType,
              state.lightCoords,
              OverlayTexture.NO_OVERLAY,
              0,//TODO - 1.21.11: Test that this works as no outline, and if it doesn't fix all the other places we pass zero for the outline color
              state.breakProgress
        );
        poseStack.popPose();
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.PIGMENT_MIXER;
    }

    @Override
    public boolean shouldRender(TileEntityPigmentMixer tile, Vec3 camera) {
        //We only actually need to do rendering if the tile is active as that means we are using the active model which has no shaft
        return tile.getActive() && super.shouldRender(tile, camera);
    }

    @Override
    public boolean hasSelectionBox(BlockState state) {
        return Attribute.isActive(state);
    }

    @Override
    public boolean isCombined() {
        return true;
    }

    @Override
    public void renderWireFrame(BlockEntity tile, float partialTick, PoseStack poseStack, VertexConsumer buffer) {
        if (tile instanceof TileEntityPigmentMixer mixer) {
            if (lines == null) {
                lines = Outlines.extract(tile.getLevel(), tile.getBlockPos(), state, MekanismModelCache.INSTANCE.PIGMENT_MIXER_SHAFT.getBakedModel());
            }
            poseStack.pushPose();
            switch (mixer.getDirection()) {
                case NORTH -> poseStack.translate(7 / 16F, 0, 6 / 16F);
                case SOUTH -> poseStack.translate(7 / 16F, 0, 0.5F);
                case WEST -> poseStack.translate(6 / 16F, 0, 7 / 16F);
                case EAST -> poseStack.translate(0.5F, 0, 7 / 16F);
            }
            float shift = 1 / 16F;
            poseStack.translate(shift, 0, shift);
            poseStack.mulPose(Axis.YN.rotationDegrees((tile.getLevel().getGameTime() + partialTick) * SHAFT_SPEED % 360));
            poseStack.translate(-shift, 0, -shift);
            Pose pose = poseStack.last();
            RenderTickHandler.renderVertexWireFrame(lines, buffer, pose.pose(), pose.normal());
            poseStack.popPose();
        }
    }

    @Override
    public AABB getRenderBoundingBox(TileEntityPigmentMixer tile) {
        //We only care about the position that is above because we only use the BER to render the shaft which is in the upper block
        return new AABB(tile.getBlockPos().above());
    }

    public static class PigmentMixerRenderState extends BlockEntityRenderState {

        @Nullable
        public Direction direction;
        /**
         * Rotation of the shaft in degrees
         */
        public float rotation;
    }
}