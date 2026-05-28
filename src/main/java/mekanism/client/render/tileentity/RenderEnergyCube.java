package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.model.ModelEnergyCore;
import mekanism.client.render.LateEffectQueue;
import mekanism.client.render.tileentity.RenderEnergyCube.EnergyCubeRenderState;
import mekanism.common.Mekanism;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.tile.TileEntityEnergyCube;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

@NothingNullByDefault
public class RenderEnergyCube extends MekanismTileEntityRenderer<TileEntityEnergyCube, EnergyCubeRenderState> {

    public static final ModelLayerLocation CORE_LAYER = new ModelLayerLocation(Mekanism.rl("energy_core"), "main");
    public static final Axis coreVec = Axis.of(new Vector3f(0.0F, MekanismUtils.ONE_OVER_ROOT_TWO, MekanismUtils.ONE_OVER_ROOT_TWO));

    public static LayerDefinition createCoreLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("core",
              CubeListBuilder.create().addBox(-8, -8, -8, 16, 16, 16),
              PartPose.ZERO
        );
        return LayerDefinition.create(mesh, 64, 64);
    }

    private final ModelPart energyCore;

    public RenderEnergyCube(BlockEntityRendererProvider.Context context) {
        super(context);
        this.energyCore = context.bakeLayer(CORE_LAYER);
    }

    @Override
    public EnergyCubeRenderState createRenderState() {
        return new EnergyCubeRenderState();
    }

    @Override
    public void extractRenderState(TileEntityEnergyCube cube, EnergyCubeRenderState state, float partialTick, Vec3 cameraPosition, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        super.extractRenderState(cube, state, partialTick, cameraPosition, breakProgress);
        state.coreTint = cube.getTier().getBaseTier().getPackedColor(ARGB.as8BitChannel(cube.getEnergyScale()));
        state.ticks = cube.getLevel().getGameTime() + partialTick;
    }

    @Override
    public void submit(EnergyCubeRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState camera) {
        // Queue core as late FX: entityCutout via SubmitNodeCollector may not render correctly in 26.1 BER context.
        // Using MultiBufferSource directly in AfterTranslucentParticles ensures the core is visible.
        BlockPos pos = state.blockPos;
        float ticks = state.ticks;
        int tint = state.coreTint;
        LateEffectQueue.add((fxPoseStack, bufferSource, fxCamera, gameTime, partialTicks) -> {
            fxPoseStack.pushPose();
            fxPoseStack.translate(pos.getX(), pos.getY(), pos.getZ());
            float scaledTicks = 4 * ticks;
            fxPoseStack.translate(0.5, 0.5, 0.5);
            fxPoseStack.scale(0.4F, 0.4F, 0.4F);
            fxPoseStack.translate(0, Math.sin(Math.toRadians(3 * ticks)) / 7, 0);
            fxPoseStack.mulPose(Axis.YP.rotationDegrees(scaledTicks));
            fxPoseStack.mulPose(coreVec.rotationDegrees(36F + scaledTicks));
            var buffer = bufferSource.getBuffer(ModelEnergyCore.RENDER_TYPE);
            energyCore.render(fxPoseStack, buffer, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, tint);
            fxPoseStack.popPose();
        });
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.ENERGY_CUBE;
    }

    @Override
    public boolean shouldRender(TileEntityEnergyCube tile, Vec3 camera) {
        return tile.getEnergyScale() > 0 && super.shouldRender(tile, camera);
    }

    public static class EnergyCubeRenderState extends BlockEntityRenderState {

        public int coreTint = 0xFFFFFFFF;
        public float ticks;
    }
}