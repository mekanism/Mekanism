package mekanism.client.render.transmitter;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.client.render.tileentity.MekanismTileEntityRenderer;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.network.transmitter.Transmitter;
import mekanism.common.lib.transmitter.ConnectionType;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.util.EnumUtils;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.Vec3;

public abstract class RenderTransmitterBase<TRANSMITTER extends TileEntityTransmitter, STATE extends TransmitterRenderState> extends MekanismTileEntityRenderer<TRANSMITTER, STATE> {

    protected RenderTransmitterBase(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    protected void setContentsModel(TRANSMITTER tile, STATE state, Identifier texture, int tint) {
        Transmitter<?, ?, ?> transmitter = tile.getTransmitter();
        ConnectionType[] connectionTypes = new ConnectionType[EnumUtils.DIRECTIONS.length];
        for (Direction side : EnumUtils.DIRECTIONS) {
            connectionTypes[side.ordinal()] = transmitter.getConnectionType(side);
        }
        state.contentsModel = TransmitterContentsManager.get().getBaked(connectionTypes, texture);
        state.modelTint = new int[]{tint};
    }

    @Override
    public void submit(STATE state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState camera) {
        if (!state.contentsModel.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.5, 0.5, 0.5);
            nodeCollector.submitBlockModel(poseStack, Sheets.translucentBlockItemSheet(), state.contentsModel, state.modelTint, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, EntityRenderState.NO_OUTLINE);
            poseStack.popPose();
        }
    }

    @Override
    public boolean shouldRender(TRANSMITTER tile, Vec3 camera) {
        return shouldRenderTransmitter(tile, camera) && super.shouldRender(tile, camera);
    }

    protected boolean shouldRenderTransmitter(TRANSMITTER tile, Vec3 camera) {
        return !MekanismConfig.client.opaqueTransmitters.get();
    }
}