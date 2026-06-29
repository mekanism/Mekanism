package mekanism.client.render.transmitter;

import mekanism.api.chemical.ChemicalResource;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.content.network.ChemicalNetwork;
import mekanism.common.content.network.transmitter.PressurizedTube;
import mekanism.common.tile.transmitter.TileEntityPressurizedTube;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class RenderPressurizedTube extends RenderTransmitterBase<TileEntityPressurizedTube, TransmitterRenderState> {

    public RenderPressurizedTube(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public TransmitterRenderState createRenderState() {
        return new TransmitterRenderState();
    }

    @Override
    public void extractRenderState(TileEntityPressurizedTube tube, TransmitterRenderState state, float partialTick, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        super.extractRenderState(tube, state, partialTick, cameraPosition, breakProgress);
        ChemicalNetwork network = tube.getTransmitter().getTransmitterNetwork();
        if (network == null) {//TODO - 26.2: Does this race condition still exist?
            return;//race conditions, yay
        }
        ChemicalResource chemical = network.getLastType();
        float currentScale = Math.max(0.2F, network.currentScale);
        //TODO - 26.2: Figure out the tint better
        setContentsModel(tube, state, chemical.value().icon(), ARGB.color(currentScale, chemical.value().tint()));
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.PRESSURIZED_TUBE;
    }

    @Override
    protected boolean shouldRenderTransmitter(TileEntityPressurizedTube tile, Vec3 camera) {
        if (super.shouldRenderTransmitter(tile, camera)) {
            PressurizedTube tube = tile.getTransmitter();
            if (tube.hasTransmitterNetwork()) {
                ChemicalNetwork network = tube.getTransmitterNetworkNN();
                return !network.getLastType().isEmpty() && !network.getContainer().isEmpty() && network.currentScale > 0;
            }
        }
        return false;
    }
}