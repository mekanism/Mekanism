package mekanism.generators.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mekanism.api.text.EnumColor;
import mekanism.client.render.tileentity.MultiblockTileEntityRenderer;
import mekanism.client.render.tileentity.RenderEnergyCube;
import mekanism.generators.client.render.RenderFusionReactor.FusionRenderState;
import mekanism.generators.common.GeneratorsProfilerConstants;
import mekanism.generators.common.content.fusion.FusionReactorMultiblockData;
import mekanism.generators.common.tile.fusion.TileEntityFusionReactorController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class RenderFusionReactor extends MultiblockTileEntityRenderer<FusionReactorMultiblockData, TileEntityFusionReactorController, FusionRenderState> {

    private static final double SCALE = 100_000_000;

    private final ModelPart energyCore;

    public RenderFusionReactor(BlockEntityRendererProvider.Context context) {
        super(context);
        this.energyCore = context.bakeLayer(RenderEnergyCube.CORE_LAYER);
    }

    @Override
    public FusionRenderState createRenderState() {
        return new FusionRenderState();
    }

    @Override
    public void extractRenderState(TileEntityFusionReactorController controller, FusionRenderState state, float partialTick, Vec3 cameraPosition,
          ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        super.extractRenderState(controller, state, partialTick, cameraPosition, breakProgress);
        FusionReactorMultiblockData multiblock = controller.getMultiblock();
        state.scaledTemp = Math.round(multiblock.getLastPlasmaTemp() / SCALE);
        //TODO - 26.2: Is this what we should be using in BERs or should we use the game time?
        state.ticks = Minecraft.getInstance().gameRenderer.gameRenderState().levelRenderState.gameTime + partialTick;
    }

    @Override
    public void submit(FusionRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0.5, -1.5, 0.5);
        float scale = 1 + 0.7F * sinDegrees(3.14F * state.scaledTemp + 135);
        renderPart(state, poseStack, nodeCollector, EnumColor.RED, scale, -6, -7, 0, 36);

        scale = 1 + 0.8F * sinDegrees(3 * state.scaledTemp);
        renderPart(state, poseStack, nodeCollector, EnumColor.PINK, scale, 4, 4, 0, 36);

        scale = 1 - 0.9F * sinDegrees(4 * state.scaledTemp + 90);
        renderPart(state, poseStack, nodeCollector, EnumColor.ORANGE, scale, 5, -3, -35, 106);

        poseStack.popPose();
    }

    private static float sinDegrees(float degrees) {
        return Mth.sin((degrees % 360) * Mth.DEG_TO_RAD);
    }

    @Override
    protected String getProfilerSection() {
        return GeneratorsProfilerConstants.FUSION_REACTOR;
    }

    private void renderPart(FusionRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, EnumColor color, float scale, int mult1, int mult2,
          int shift1, int shift2) {
        poseStack.pushPose();
        poseStack.scale(scale, scale, scale);
        poseStack.mulPose(Axis.YP.rotationDegrees(state.ticks * mult1 + shift1));
        poseStack.mulPose(RenderEnergyCube.coreVec.rotationDegrees(state.ticks * mult2 + shift2));
        nodeCollector.submitModelPart(
              this.energyCore,
              poseStack,
              RenderEnergyCube.RENDER_TYPE,
              LightCoordsUtil.FULL_BRIGHT,
              OverlayTexture.NO_OVERLAY,
              null,
              color.getPackedColor(),
              null//No break overlay for the core
        );
        poseStack.popPose();
    }

    @Override
    protected boolean shouldRender(TileEntityFusionReactorController tile, FusionReactorMultiblockData multiblock, Vec3 camera) {
        return multiblock.isBurning();
    }

    public static class FusionRenderState extends BlockEntityRenderState {

        public long scaledTemp;
        public float ticks;
    }
}