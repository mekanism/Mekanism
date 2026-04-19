package mekanism.generators.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.tileentity.IWireFrameRenderer;
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
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
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
          @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
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
            nodeCollector.submitModel(
                  this.model,
                  state.rotation,
                  poseStack,
                  //TODO - 26.1: Figure out/at least cleanup the name
                  this.model.RENDER_TYPE,
                  //TODO - 26.1: Do we need to do something for the light level similar to what double chests do of calculating the max of all the positions?
                  state.lightCoords,
                  OverlayTexture.NO_OVERLAY,
                  0,//No outline
                  state.breakProgress
            );
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
    public void renderWireFrame(BlockEntity tile, float partialTick, PoseStack poseStack, VertexConsumer buffer) {
        if (tile instanceof TileEntityWindGenerator generator) {
            poseStack.pushPose();
            poseStack.translate(0.5, 1.5, 0.5);
            MekanismRenderer.rotate(poseStack, generator.getDirection(), 0, 180, 90, 270);
            poseStack.mulPose(Axis.ZP.rotationDegrees(180));
            float angle = generator.getAngle();
            if (generator.getActive() && partialTick > 0) {
                angle = (angle + generator.getHeightSpeedRatio() * partialTick) % 360;
            }
            model.renderWireFrame(poseStack, buffer, angle);
            poseStack.popPose();
        }
    }

    public static class WindGeneratorRenderState extends BlockEntityRenderState {

        public WindGeneratorRotationRenderState rotation = new WindGeneratorRotationRenderState();
        @Nullable
        public Direction direction;
    }
}