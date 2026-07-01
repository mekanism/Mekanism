package mekanism.client.render.transmitter;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Arrays;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.FluidTextureType;
import mekanism.client.render.ModelRenderer;
import mekanism.client.render.RenderResizableCuboid;
import mekanism.client.render.RenderResizableCuboid.SideRender;
import mekanism.client.render.transmitter.TransmitterRenderState.PipeRenderState;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.content.network.FluidNetwork;
import mekanism.common.content.network.transmitter.MechanicalPipe;
import mekanism.common.lib.transmitter.ConnectionType;
import mekanism.common.tile.transmitter.TileEntityMechanicalPipe;
import mekanism.common.util.EnumUtils;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.jspecify.annotations.Nullable;

public class RenderMechanicalPipe extends RenderTransmitterBase<TileEntityMechanicalPipe, PipeRenderState> {

    private static final int STAGES = 100;
    private static final float HEIGHT = 0.45F;
    private static final float OFFSET = 0.02F;

    public RenderMechanicalPipe(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public PipeRenderState createRenderState() {
        return new PipeRenderState();
    }

    @Override
    public void extractRenderState(TileEntityMechanicalPipe pipe, PipeRenderState state, float partialTick, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        super.extractRenderState(pipe, state, partialTick, cameraPosition, breakProgress);
        MechanicalPipe transmitter = pipe.getTransmitter();
        //Note: We validated in shouldRender(Transmitter) that the pipe has a network, which is two lines above the call to this method
        FluidNetwork network = transmitter.getTransmitterNetworkNN();
        FluidResource fluidType = network.getLastType();
        if (fluidType.isEmpty()) {
            return;//Shouldn't be the case but validate it
        }
        TextureAtlasSprite texture = MekanismRenderer.getFluidTexture(fluidType, FluidTextureType.STILL);
        state.fluidTexture = MekanismRenderer.getSinglePicker(texture);
        state.fluidTint = MekanismRenderer.getColorARGB(fluidType, network.currentScale);
        state.modelTint = new int[]{state.fluidTint};

        int stage = Math.max(3, ModelRenderer.getStage(fluidType, STAGES, network.currentScale));
        state.stage = stage;
        state.lightCoords = LightCoordsUtil.lightCoordsWithEmission(state.lightCoords, fluidType.getFluidType().getLightLevel());

        ConnectionType[] connectionContents = new ConnectionType[EnumUtils.DIRECTIONS.length];
        boolean[] renderSides = new boolean[6];
        boolean hasHorizontalSide = false;
        int verticalSides = 0;
        for (Direction side : EnumUtils.DIRECTIONS) {
            ConnectionType connectionType = transmitter.getConnectionType(side);
            //If it is normal we need to render it manually so to have it be the correct dimensions instead of too narrow
            if (connectionType == ConnectionType.PUSH || connectionType == ConnectionType.PULL) {
                connectionContents[side.ordinal()] = connectionType;
            }
            renderSides[side.ordinal()] = connectionType != ConnectionType.NORMAL;
            if (connectionType != ConnectionType.NONE) {
                if (side.getAxis().isHorizontal()) {
                    hasHorizontalSide = true;
                } else {
                    verticalSides++;
                }
            }
        }
        state.contentsModel = TransmitterContentsManager.get().getBaked(connectionContents, texture.contents().name());
        //Render the base part if there is a horizontal connection, or we only have one vertical connection
        boolean renderBase = hasHorizontalSide || verticalSides < 2;
        state.renderBase = renderBase;
        @SideRender.SideRenderFlags byte coreSideRender = 0;
        for (Direction side : EnumUtils.DIRECTIONS) {
            //Render the side if there is no connection on that side, or it is a vertical connection, we have at least one side, and we are not full
            // We also render for push and pull as they use slightly smaller fill models which then means we would have
            // small gaps if we didn't render
            if (renderSides[side.ordinal()] || (renderBase && stage != STAGES - 1 && side.getAxis().isVertical())) {
                coreSideRender |= SideRender.of(side);
            }
        }
        state.coreSideRender = coreSideRender;
        Arrays.fill(state.renderSideModel, false);
        for (Direction side : EnumUtils.DIRECTIONS) {
            ConnectionType connectionType = transmitter.getConnectionType(side);
            if (connectionType == ConnectionType.NORMAL) {
                //If it is normal we need to render it manually so to have it be the correct dimensions instead of too narrow
                state.renderSideModel[side.ordinal()] = true;
            }
        }
    }

    @Override
    public void submit(PipeRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState camera) {
        super.submit(state, poseStack, nodeCollector, camera);
        if (state.fluidTexture == null) {
            return;
        }

        float stageRatio = (state.stage / (float) STAGES) * HEIGHT;
        Vec3 lowerCorner = Vec3.atLowerCornerOf(state.blockPos);

        for (Direction side : EnumUtils.DIRECTIONS) {
            if (!state.renderSideModel[side.ordinal()]) {
                continue;
            }
            //all face except side and side-opposite
            //noinspection MagicConstant - hush
            @SideRender.SideRenderFlags
            byte sideRenderCheck = (byte) (SideRender.ALL_FACES ^ SideRender.of(side) ^ SideRender.of(side.getOpposite()));

            float minX, minY, minZ;
            float maxX, maxY, maxZ;

            if (side.getAxis().isHorizontal()) {
                minY = 0.25F + OFFSET;
                maxY = 0.25F + OFFSET + stageRatio;
                if (side.getAxis() == Axis.Z) {
                    minX = 0.25F + OFFSET;
                    maxX = 0.75F - OFFSET;
                    if (side.getAxisDirection() == AxisDirection.POSITIVE) {
                        minZ = 0.75F - OFFSET;
                        maxZ = 1;
                    } else {
                        minZ = 0;
                        maxZ = 0.25F + OFFSET;
                    }
                } else {
                    minZ = 0.25F + OFFSET;
                    maxZ = 0.75F - OFFSET;
                    if (side.getAxisDirection() == AxisDirection.POSITIVE) {
                        minX = 0.75F - OFFSET;
                        maxX = 1;
                    } else {
                        minX = 0;
                        maxX = 0.25F + OFFSET;
                    }
                }
            } else {
                float min = 0.5F - stageRatio / 2;
                float max = 0.5F + stageRatio / 2;
                minX = min;
                maxX = max;
                minZ = min;
                maxZ = max;
                if (side == Direction.DOWN) {
                    minY = 0;
                    maxY = 0.25F + OFFSET;
                } else {//Up
                    minY = 0.25F + OFFSET + stageRatio;
                    maxY = 1;
                }
            }
            RenderResizableCuboid.renderCube(sideRenderCheck, minX, minY, minZ, maxX, maxY, maxZ, poseStack, Sheets.translucentBlockItemSheet(), nodeCollector,
                  state.fluidTint, state.lightCoords, OverlayTexture.NO_OVERLAY, RenderResizableCuboid.FaceDisplay.FRONT, camera.pos, lowerCorner, state.fluidTexture);
        }

        {//render core cube
            float min;
            float max;
            if (state.renderBase) {
                min = 0.25F + OFFSET;
                max = 0.75F - OFFSET;
            } else {
                min = 0.5F - stageRatio / 2;
                max = 0.5F + stageRatio / 2;
            }
            RenderResizableCuboid.renderCube(state.coreSideRender, min, 0.25F + OFFSET, min, max, 0.25F + OFFSET + stageRatio, max, poseStack,
                  Sheets.translucentBlockItemSheet(), nodeCollector, state.fluidTint, state.lightCoords, OverlayTexture.NO_OVERLAY, RenderResizableCuboid.FaceDisplay.FRONT,
                  camera.pos, lowerCorner, state.fluidTexture);
        }
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.MECHANICAL_PIPE;
    }

    @Override
    protected boolean shouldRenderTransmitter(TileEntityMechanicalPipe tile, Vec3 camera) {
        if (super.shouldRenderTransmitter(tile, camera)) {
            MechanicalPipe pipe = tile.getTransmitter();
            if (pipe.hasTransmitterNetwork()) {
                FluidNetwork network = pipe.getTransmitterNetworkNN();
                return !network.getLastType().isEmpty() && !network.getContainer().isEmpty() && network.currentScale > 0;
            }
        }
        return false;
    }
}