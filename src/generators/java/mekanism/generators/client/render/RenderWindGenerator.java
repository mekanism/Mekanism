package mekanism.generators.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.Collection;
import java.util.Collections;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.outline.IWireFrameRenderer;
import mekanism.client.render.outline.Outlines.Line;
import mekanism.client.render.tileentity.MekanismTileEntityRenderer;
import mekanism.generators.client.model.ModelWindGenerator;
import mekanism.generators.client.model.ModelWindGenerator.WindGeneratorRotationRenderState;
import mekanism.generators.client.render.RenderWindGenerator.WindGeneratorRenderState;
import mekanism.generators.common.GeneratorsProfilerConstants;
import mekanism.generators.common.tile.TileEntityWindGenerator;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class RenderWindGenerator extends MekanismTileEntityRenderer<TileEntityWindGenerator, WindGeneratorRenderState> implements IWireFrameRenderer {

    private final ModelWindGenerator model;

    public RenderWindGenerator(BlockEntityRendererProvider.Context context) {
        super(context);
        this.model = new ModelWindGenerator(context.entityModelSet());
    }

    @Override
    public WindGeneratorRenderState createRenderState() {
        return new WindGeneratorRenderState();
    }

    @Override
    public void extractRenderState(TileEntityWindGenerator generator, WindGeneratorRenderState state, float partialTick, Vec3 cameraPosition,
          ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        super.extractRenderState(generator, state, partialTick, cameraPosition, breakProgress);
        state.direction = generator.getDirection();
        state.rotation.angle = generator.getAngle();
        if (generator.getActive() && partialTick > 0) {
            state.rotation.angle = (state.rotation.angle + generator.getHeightSpeedRatio() * partialTick) % 360;
        }
    }

    @Override
    public void submit(WindGeneratorRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState camera) {
        if (state.direction != null) {
            poseStack.pushPose();
            poseStack.translate(0.5, 1.5, 0.5);
            MekanismRenderer.rotate(poseStack, state.direction, 0, 180, 90, 270);
            poseStack.mulPose(Axis.ZP.rotationDegrees(180));
            //TODO - 26.2: Do we need to do something for the light level similar to what double chests do of calculating the max of all the positions?
            nodeCollector.submitModel(this.model, state.rotation, poseStack, ModelWindGenerator.RENDER_TYPE, state.lightCoords, OverlayTexture.NO_OVERLAY, 0, state.breakProgress);
            poseStack.popPose();
        }
    }

    @Override
    protected String getProfilerSection() {
        return GeneratorsProfilerConstants.WIND_GENERATOR;
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(TileEntityWindGenerator tile) {
        //Note: we just extend it to the max size (including blades) it could be ignoring what direction it is actually facing
        BlockPos pos = tile.getBlockPos();
        return AABB.encapsulatingFullBlocks(pos.offset(-2, 0, -2), pos.offset(2, 6, 2));
    }

    @Override
    public Collection<Line> applyTransformAndGetFrame(BlockEntity tile, float partialTick, PoseStack poseStack, LevelRenderState levelRenderState) {
        if (!(tile instanceof TileEntityWindGenerator generator)) {
            return Collections.emptyList();
        }
        poseStack.translate(0.5F, 1.5F, 0.5F);
        MekanismRenderer.rotate(poseStack, generator.getDirection(), 0, 180, 90, 270);
        poseStack.mulPose(Axis.ZP.rotationDegrees(180));
        float angle;
        if (generator.getActive() && partialTick > 0) {
            angle = (generator.getAngle() + generator.getHeightSpeedRatio() * partialTick) % 360;
        } else {
            angle = generator.getAngle();
        }
        //TODO: Can we somehow cache the wireframe?
        return model.getWireFrame(new WindGeneratorRotationRenderState(angle));
    }

    public static class WindGeneratorRenderState extends BlockEntityRenderState {

        public WindGeneratorRotationRenderState rotation = new WindGeneratorRotationRenderState(0);
        @Nullable
        public Direction direction;
    }
}