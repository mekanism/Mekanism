package mekanism.client.render.transmitter;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.client.render.transmitter.TransmitterRenderState.ConductorRenderState;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.content.network.transmitter.ThermodynamicConductor;
import mekanism.common.tile.transmitter.TileEntityThermodynamicConductor;
import mekanism.common.util.HeatUtils;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

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
          ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        super.extractRenderState(conductor, state, partialTick, cameraPosition, breakProgress);
        ThermodynamicConductor transmitter = conductor.getTransmitter();
        state.tempColor = HeatUtils.getColorFromTemp(transmitter.getTotalTemperature(), transmitter.getBaseColor()).argb();
    }

    @Override
    public void submit(ConductorRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        //TODO - 26.2: What submit do we want to be using
        /*nodeCollector.submitModelPart(
              this.model,
              poseStack,
              //TODO - 26.2: Is this the correct render type to be using? It used to be translucent cull
              RenderTypes.entityTranslucent(MekanismRenderer.heatIcon.contents().name()),
              //TODO - 26.2: I believe in the past we used LightTexture.FULL_BRIGHT for the model box, check which looks better state.lightCoords
              LightCoordsUtil.FULL_BRIGHT,
              OverlayTexture.NO_OVERLAY,
              //TODO - 26.2: Do we need to pass the texture here as well, or not?
              MekanismRenderer.heatIcon,
              state.tempColor,
              state.breakProgress//TODO - 26.2: Should we be rendering the crumbling overlay here?
        );*/
        poseStack.popPose();
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.THERMODYNAMIC_CONDUCTOR;
    }
}