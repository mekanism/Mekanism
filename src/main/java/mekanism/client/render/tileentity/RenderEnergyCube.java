package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mekanism.client.model.MekanismModelCache;
import mekanism.client.render.tileentity.RenderEnergyCube.EnergyCubeRenderState;
import mekanism.common.tile.TileEntityEnergyCube;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

public class RenderEnergyCube extends MekanismTileEntityRenderer<TileEntityEnergyCube, EnergyCubeRenderState> {

    public static final Axis coreVec = Axis.of(new Vector3f(0.0F, MekanismUtils.ONE_OVER_ROOT_TWO, MekanismUtils.ONE_OVER_ROOT_TWO));

    public RenderEnergyCube(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public EnergyCubeRenderState createRenderState() {
        return new EnergyCubeRenderState();
    }

    @Override
    public void extractRenderState(TileEntityEnergyCube cube, EnergyCubeRenderState state, float partialTick, Vec3 cameraPosition,
          ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        super.extractRenderState(cube, state, partialTick, cameraPosition, breakProgress);
        state.coreTint = cube.getTier().getBaseTier().getPackedColor(ARGB.as8BitChannel(cube.getEnergyScale()));
        state.animationTime = getAnimationTime(cube, partialTick);
    }

    @Override
    public void submit(EnergyCubeRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.scale(0.4F, 0.4F, 0.4F);
        poseStack.translate(0, Math.sin(Math.toRadians(3 * state.animationTime)) / 7, 0);
        float scaledTime = 4 * state.animationTime;
        poseStack.rotateDegrees(Axis.YP, scaledTime);
        poseStack.rotateDegrees(coreVec, 36 + scaledTime);
        nodeCollector.submitBlockModel(poseStack, Sheets.translucentBlockItemSheet(), MekanismModelCache.INSTANCE.ENERGY_CORE.getBakedModel(),
              new int[]{state.coreTint}, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, EntityRenderState.NO_OUTLINE);
        poseStack.popPose();
    }

    @Override
    public boolean shouldRender(TileEntityEnergyCube tile, Vec3 camera) {
        return tile.getEnergyScale() > 0 && super.shouldRender(tile, camera);
    }

    public static class EnergyCubeRenderState extends BlockEntityRenderState {

        public int coreTint = CommonColors.WHITE;
        public float animationTime;
    }
}