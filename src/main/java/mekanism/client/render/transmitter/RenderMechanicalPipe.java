package mekanism.client.render.transmitter;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.FluidTextureType;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.client.render.MekanismRenderer.Model3D.ModelBoundsSetter;
import mekanism.client.render.ModelRenderer;
import mekanism.client.render.transmitter.TransmitterRenderState.PipeRenderState;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.content.network.FluidNetwork;
import mekanism.common.content.network.transmitter.MechanicalPipe;
import mekanism.common.lib.transmitter.ConnectionType;
import mekanism.common.tile.transmitter.TileEntityMechanicalPipe;
import mekanism.common.util.EnumUtils;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidStackLinkedSet;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class RenderMechanicalPipe extends RenderTransmitterBase<TileEntityMechanicalPipe, PipeRenderState> {

    private static final int stages = 100;
    private static final float height = 0.45F;
    private static final float offset = 0.02F;
    //Note: this is basically used as an enum map (Direction), but null key is possible, which EnumMap doesn't support.
    // 6 is used for null side, and 7 is used for null side but flowing vertically
    private static final Int2ObjectMap<Map<FluidStack, Int2ObjectMap<Model3D>>> cachedLiquids = new Int2ObjectArrayMap<>(8);

    public RenderMechanicalPipe(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    public static void onStitch() {
        cachedLiquids.clear();
    }

    @Override
    public PipeRenderState createRenderState() {
        return new PipeRenderState();
    }

    @Override
    public void extractRenderState(TileEntityMechanicalPipe pipe, PipeRenderState state, float partialTick, Vec3 cameraPosition, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        super.extractRenderState(pipe, state, partialTick, cameraPosition, breakProgress);
        MechanicalPipe transmitter = pipe.getTransmitter();
        FluidNetwork network = transmitter.getTransmitterNetwork();
        if (network == null) {//TODO - 26.1: Does this race condition still exist?
            return;//race conditions, yay
        }
        FluidStack fluidStack = network.lastFluid;
        if (fluidStack.isEmpty()) {
            return;//Shouldn't be the case but validate it
        }
        state.currentScale = network.currentScale;
        state.fluidTexture = MekanismRenderer.getFluidTexture(fluidStack, FluidTextureType.STILL);
        state.fluidTint = MekanismRenderer.getColorARGB(fluidStack, state.currentScale);

        int stage = Math.max(3, ModelRenderer.getStage(fluidStack, stages, state.currentScale));
        //TODO - 26.1: Should we overwrite lightCoords with glow?
        int glow = MekanismRenderer.calculateGlowLight(state.lightCoords, fluidStack);


        List<String> connectionContents = new ArrayList<>();
        boolean[] renderSides = new boolean[6];
        boolean hasHorizontalSide = false;
        int verticalSides = 0;
        for (Direction side : EnumUtils.DIRECTIONS) {
            ConnectionType connectionType = transmitter.getConnectionType(side);
            //If it is normal we need to render it manually so to have it be the correct dimensions instead of too narrow
            if (connectionType == ConnectionType.PUSH || connectionType == ConnectionType.PULL) {
                connectionContents.add(side.getSerializedName() + connectionType.getSerializedName().toUpperCase(Locale.ROOT));
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
        //Render the base part if there is a horizontal connection, or we only have one vertical connection
        boolean renderBase = hasHorizontalSide || verticalSides < 2;
        Model3D model = getModel(fluidStack, stage, renderBase);
        for (Direction side : EnumUtils.DIRECTIONS) {
            //Render the side if there is no connection on that side, or it is a vertical connection, we have at least one side, and we are not full
            // We also render for push and pull as they use slightly smaller fill models which then means we would have
            // small gaps if we didn't render
            model.setSideRender(side, renderSides[side.ordinal()] || (side.getAxis().isVertical() && renderBase && stage != stages - 1));
        }
    }

    @Override
    public void submit(PipeRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState camera) {
        if (state.fluidTexture == null) {
            return;
        }
        //todo - 26.1: rendering
        /*for (Direction side : EnumUtils.DIRECTIONS) {
            ConnectionType connectionType = transmitter.getConnectionType(side);
            if (connectionType == ConnectionType.NORMAL) {
                //If it is normal we need to render it manually so to have it be the correct dimensions instead of too narrow
                MekanismRenderer.renderObject(getModel(side, fluidStack, stage), poseStack, buffer, state.fluidTint, glow, OverlayTexture.NO_OVERLAY, FaceDisplay.FRONT, camera.pos, state.blockPos);
            }
        }
        MekanismRenderer.renderObject(model, poseStack, buffer, state.fluidTint, glow, OverlayTexture.NO_OVERLAY, FaceDisplay.FRONT, camera.pos, state.blockPos);
        if (!connectionContents.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.5, 0.5, 0.5);
            renderModel(state, poseStack, buffer, ARGB.redFloat(state.fluidTint), ARGB.greenFloat(state.fluidTint), ARGB.blueFloat(state.fluidTint),
                  ARGB.alphaFloat(state.fluidTint), glow, OverlayTexture.NO_OVERLAY,
                  state.fluidTexture, connectionContents);
            poseStack.popPose();
        }*/
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
                FluidNetwork network = pipe.getTransmitterNetwork();
                return !network.lastFluid.isEmpty() && !network.fluidTank.isEmpty() && network.currentScale > 0;
            }
        }
        return false;
    }

    private Model3D getModel(FluidStack fluid, int stage, boolean hasSides) {
        return getModel(null, fluid, stage, hasSides);
    }

    private Model3D getModel(Direction side, FluidStack fluid, int stage) {
        return getModel(side, fluid, stage, false);
    }

    private Model3D getModel(@Nullable Direction side, FluidStack fluid, int stage, boolean renderBase) {
        int sideOrdinal;
        if (side == null) {
            sideOrdinal = renderBase ? 7 : 6;
        } else {
            sideOrdinal = side.ordinal();
        }
        Int2ObjectMap<Model3D> modelMap = cachedLiquids.computeIfAbsent(sideOrdinal, s -> new Object2ObjectOpenCustomHashMap<>(FluidStackLinkedSet.TYPE_AND_COMPONENTS))
              .computeIfAbsent(fluid, f -> new Int2ObjectOpenHashMap<>());
        Model3D model = modelMap.get(stage);
        if (model == null) {
            model = new Model3D().setTexture(MekanismRenderer.getFluidTexture(fluid, FluidTextureType.STILL));
            float stageRatio = (stage / (float) stages) * height;
            if (side == null) {
                float min;
                float max;
                if (renderBase) {
                    min = 0.25F + offset;
                    max = 0.75F - offset;
                } else {
                    min = 0.5F - stageRatio / 2;
                    max = 0.5F + stageRatio / 2;
                }
                return model.xBounds(min, max)
                      .yBounds(0.25F + offset, 0.25F + offset + stageRatio)
                      .zBounds(min, max);
            }
            model.setSideRender(side, false)
                  .setSideRender(side.getOpposite(), false);
            if (side.getAxis().isHorizontal()) {
                model.yBounds(0.25F + offset, 0.25F + offset + stageRatio);
                if (side.getAxis() == Axis.Z) {
                    return setHorizontalBounds(side, model::xBounds, model::zBounds);
                }
                return setHorizontalBounds(side, model::zBounds, model::xBounds);
            }
            float min = 0.5F - stageRatio / 2;
            float max = 0.5F + stageRatio / 2;
            model.xBounds(min, max)
                  .zBounds(min, max);
            if (side == Direction.DOWN) {
                model.yBounds(0, 0.25F + offset);
            } else {//Up
                model.yBounds(0.25F + offset + stageRatio, 1);
            }
            modelMap.put(stage, model);
        }
        return model;
    }

    private static Model3D setHorizontalBounds(Direction horizontal, ModelBoundsSetter axisBased, ModelBoundsSetter directionBased) {
        axisBased.set(0.25F + offset, 0.75F - offset);
        if (horizontal.getAxisDirection() == AxisDirection.POSITIVE) {
            return directionBased.set(0.75F - offset, 1);
        }
        return directionBased.set(0, 0.25F + offset);
    }
}