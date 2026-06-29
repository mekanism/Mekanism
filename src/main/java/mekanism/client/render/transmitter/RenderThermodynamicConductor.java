package mekanism.client.render.transmitter;

import mekanism.common.Mekanism;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.content.network.transmitter.ThermodynamicConductor;
import mekanism.common.tile.transmitter.TileEntityThermodynamicConductor;
import mekanism.common.util.HeatUtils;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class RenderThermodynamicConductor extends RenderTransmitterBase<TileEntityThermodynamicConductor, TransmitterRenderState> {

    private static final Identifier HEAT_ICON_LOCATION = Mekanism.rl("mek_liquid/heat");

    public RenderThermodynamicConductor(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public TransmitterRenderState createRenderState() {
        return new TransmitterRenderState();
    }

    @Override
    public void extractRenderState(TileEntityThermodynamicConductor conductor, TransmitterRenderState state, float partialTick, Vec3 cameraPosition,
          ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        super.extractRenderState(conductor, state, partialTick, cameraPosition, breakProgress);
        ThermodynamicConductor transmitter = conductor.getTransmitter();
        setContentsModel(conductor, state, HEAT_ICON_LOCATION, HeatUtils.getColorFromTemp(transmitter.getTemperature(), transmitter.getBaseColor()).argb());
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.THERMODYNAMIC_CONDUCTOR;
    }
}