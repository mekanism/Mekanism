package mekanism.client.render.transmitter;

import mekanism.client.render.MekanismRenderer;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.content.network.EnergyNetwork;
import mekanism.common.content.network.transmitter.UniversalCable;
import mekanism.common.tile.transmitter.TileEntityUniversalCable;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class RenderUniversalCable extends RenderTransmitterBase<TileEntityUniversalCable, TransmitterRenderState> {

    public RenderUniversalCable(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public TransmitterRenderState createRenderState() {
        return new TransmitterRenderState();
    }

    @Override
    public void extractRenderState(TileEntityUniversalCable cable, TransmitterRenderState state, float partialTick, Vec3 cameraPosition,
          ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        super.extractRenderState(cable, state, partialTick, cameraPosition, breakProgress);
        //Note: We validated in shouldRender(Transmitter) that the cable has a network, which is two lines above the call to this method
        EnergyNetwork network = cable.getTransmitter().getTransmitterNetworkNN();
        //TODO - 26.2: What threshold do we want to cut this off at?
        setContentsModel(cable, state, MekanismRenderer.ENERGY_ICON_LOCATION, ARGB.white(network.currentScale));
        //TODO - 26.2: What do we want to use for the light level
        state.lightCoords = LightCoordsUtil.FULL_BRIGHT;
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.UNIVERSAL_CABLE;
    }

    @Override
    protected boolean shouldRenderTransmitter(TileEntityUniversalCable tile, Vec3 camera) {
        if (super.shouldRenderTransmitter(tile, camera)) {
            UniversalCable cable = tile.getTransmitter();
            if (cable.hasTransmitterNetwork()) {
                EnergyNetwork network = cable.getTransmitterNetworkNN();
                //Note: We don't check if the network is empty as we don't actually ever sync the energy value to the client
                return network.currentScale > 0;
            }
        }
        return false;
    }
}