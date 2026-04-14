package mekanism.client.render.transmitter;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.transmitter.TransmitterRenderState.TubeRenderState;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.content.network.ChemicalNetwork;
import mekanism.common.content.network.transmitter.PressurizedTube;
import mekanism.common.tile.transmitter.TileEntityPressurizedTube;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class RenderPressurizedTube extends RenderTransmitterBase<TileEntityPressurizedTube, TubeRenderState> {

    public RenderPressurizedTube(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public TubeRenderState createRenderState() {
        return new TubeRenderState();
    }

    @Override
    public void extractRenderState(TileEntityPressurizedTube tube, TubeRenderState state, float partialTick, Vec3 cameraPosition, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        super.extractRenderState(tube, state, partialTick, cameraPosition, breakProgress);
        ChemicalNetwork network = tube.getTransmitter().getTransmitterNetwork();
        if (network == null) {//TODO - 1.21.11: Does this race condition still exist?
            return;//race conditions, yay
        }
        state.currentScale = Math.max(0.2F, network.currentScale);
        state.chemicalTexture = MekanismRenderer.getChemicalTexture(network.lastChemical);
        //TODO - 1.21.11: Do we need to make this ARGB as technically alpha is currently empty
        state.chemicalTint = MekanismRenderer.getTint(network.lastChemical);
    }

    @Override
    public void submit(TubeRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState camera) {
        if (state.chemicalTexture != null) {
            poseStack.pushPose();
            poseStack.translate(0.5, 0.5, 0.5);
            renderModel(state, poseStack, renderer.getBuffer(Sheets.translucentCullBlockSheet()), state.chemicalTint, state.currentScale,
                  LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, state.chemicalTexture);
            poseStack.popPose();
        }
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
                ChemicalNetwork network = tube.getTransmitterNetwork();
                return !network.lastChemical.is(MekanismAPI.EMPTY_CHEMICAL_KEY) && !network.getChemicalTank().isEmpty() && network.currentScale > 0;
            }
        }
        return false;
    }
}