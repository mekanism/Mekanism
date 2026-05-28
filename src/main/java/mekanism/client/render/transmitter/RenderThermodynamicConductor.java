package mekanism.client.render.transmitter;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.RenderResizableCuboid;
import mekanism.client.render.transmitter.TransmitterRenderState.ConductorRenderState;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.content.network.transmitter.ThermodynamicConductor;
import mekanism.common.tile.transmitter.TileEntityThermodynamicConductor;
import mekanism.common.util.HeatUtils;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class RenderThermodynamicConductor extends RenderTransmitterBase<TileEntityThermodynamicConductor, ConductorRenderState> {

    public RenderThermodynamicConductor(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ConductorRenderState createRenderState() {
        return new ConductorRenderState();
    }

    @Override
    public void extractRenderState(TileEntityThermodynamicConductor conductor, ConductorRenderState state, float partialTick, Vec3 cameraPosition,
          @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        super.extractRenderState(conductor, state, partialTick, cameraPosition, breakProgress);
        ThermodynamicConductor transmitter = conductor.getTransmitter();
        state.tempColor = HeatUtils.getColorFromTemp(transmitter.getTotalTemperature(), transmitter.getBaseColor()).argb();
    }

    @Override
    public void submit(ConductorRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState camera) {
        float alpha = ARGB.alphaFloat(state.tempColor);
        if (alpha <= 0) {
            return;
        }
        float min = 0.3F;
        float max = 0.7F;
        RenderResizableCuboid.renderCube(
              RenderResizableCuboid.SideRender.ALL_FACES,
              min, min, min, max, max, max,
              poseStack, Sheets.translucentBlockSheet(), nodeCollector,
              state.tempColor, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
              RenderResizableCuboid.FaceDisplay.FRONT,
              camera.pos, Vec3.atLowerCornerOf(state.blockPos),
              MekanismRenderer.getSinglePicker(MekanismRenderer.heatIcon)
        );
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.THERMODYNAMIC_CONDUCTOR;
    }
}