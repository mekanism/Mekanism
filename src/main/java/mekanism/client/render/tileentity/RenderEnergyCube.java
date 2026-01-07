package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.model.ModelEnergyCore;
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
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ARGB;
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
        //TODO - 1.21.11: Do we want to use game time as a basis or some other value?
        state.ticks = cube.getLevel().getGameTime() + partialTick;
    }

    @Override
    public void submit(EnergyCubeRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState camera) {
        float scaledTicks = 4 * state.ticks;
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.scale(0.4F, 0.4F, 0.4F);
        poseStack.translate(0, Math.sin(Math.toRadians(3 * state.ticks)) / 7, 0);
        poseStack.mulPose(Axis.YP.rotationDegrees(scaledTicks));
        poseStack.mulPose(coreVec.rotationDegrees(36F + scaledTicks));
        nodeCollector.submitModelPart(
              this.energyCore,
              poseStack,
              //TODO - 1.21.11: Figure out the render type
              ModelEnergyCore.RENDER_TYPE,
              //TODO - 1.21.11: Do we want to be using the state's light level instead?
              LightTexture.FULL_BRIGHT,
              OverlayTexture.NO_OVERLAY,
              null,//TODO - 1.21.11: Do we need to specify the texture or is doing so in the render type good enough?
              state.coreTint,
              null//No break overlay for the core
        );
        poseStack.popPose();
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