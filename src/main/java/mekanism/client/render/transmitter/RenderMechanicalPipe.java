package mekanism.client.render.transmitter;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.render.FluidRenderMap;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.FluidTextureType;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.client.render.MekanismRenderer.Model3D.ModelBoundsSetter;
import mekanism.client.render.RenderResizableCuboid.FaceDisplay;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.content.network.FluidNetwork;
import mekanism.common.content.network.transmitter.MechanicalPipe;
import mekanism.common.lib.transmitter.ConnectionType;
import mekanism.common.tile.transmitter.TileEntityMechanicalPipe;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class RenderMechanicalPipe extends RenderTransmitterBase<TileEntityMechanicalPipe> {

    private static final int stages = 100;
    private static final float height = 0.45F;
    private static final float offset = 0.015F;
    //Note: this is basically used as an enum map (Direction), but null key is possible, which EnumMap doesn't support. 6 is used for null side
    private static final Int2ObjectMap<FluidRenderMap<Int2ObjectMap<Model3D>>> cachedLiquids = new Int2ObjectArrayMap<>(7);

    public RenderMechanicalPipe(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    public static void onStitch() {
        cachedLiquids.clear();
    }

    @Override
    protected void render(TileEntityMechanicalPipe tile, float partialTick, PoseStack matrix, MultiBufferSource renderer, int light, int overlayLight,
          ProfilerFiller profiler) {
        MechanicalPipe pipe = tile.getTransmitter();
        FluidNetwork network = pipe.getTransmitterNetwork();
        FluidStack fluidStack = network.lastFluid;
        float fluidScale = network.currentScale;
        int stage;
        if (MekanismUtils.lighterThanAirGas(fluidStack)) {
            stage = stages - 1;
        } else {
            stage = Math.max(3, (int) (fluidScale * (stages - 1)));
        }
        int glow = MekanismRenderer.calculateGlowLight(light, fluidStack);
        int color = MekanismRenderer.getColorARGB(fluidStack, fluidScale);
        List<String> connectionContents = new ArrayList<>();
        Model3D model = getModel(null, fluidStack, stage);
        VertexConsumer buffer = renderer.getBuffer(Sheets.translucentCullBlockSheet());
        for (Direction side : EnumUtils.DIRECTIONS) {
            ConnectionType connectionType = pipe.getConnectionType(side);
            if (connectionType == ConnectionType.NORMAL) {
                //If it is normal we need to render it manually so to have it be the correct dimensions instead of too narrow
                MekanismRenderer.renderObject(getModel(side, fluidStack, stage), matrix, buffer, color, glow, overlayLight, FaceDisplay.FRONT);
            } else if (connectionType != ConnectionType.NONE) {
                connectionContents.add(side.getSerializedName() + connectionType.getSerializedName().toUpperCase(Locale.ROOT));
            }
            if (model != null) {
                //Render the side if there is no connection on that side, or it is a vertical connection, and we are not full
                model.setSideRender(side, connectionType == ConnectionType.NONE || (side.getAxis().isVertical() && stage != stages - 1));
            }
        }
        MekanismRenderer.renderObject(model, matrix, buffer, color, glow, overlayLight, FaceDisplay.FRONT);
        if (!connectionContents.isEmpty()) {
            matrix.pushPose();
            matrix.translate(0.5, 0.5, 0.5);
            renderModel(tile, matrix, buffer, MekanismRenderer.getRed(color), MekanismRenderer.getGreen(color), MekanismRenderer.getBlue(color),
                  MekanismRenderer.getAlpha(color), glow, overlayLight, MekanismRenderer.getFluidTexture(fluidStack, FluidTextureType.STILL), connectionContents);
            matrix.popPose();
        }
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.MECHANICAL_PIPE;
    }

    @Override
    protected boolean shouldRenderTransmitter(TileEntityMechanicalPipe tile, Vec3 camera) {
        MechanicalPipe pipe = tile.getTransmitter();
        if (pipe.hasTransmitterNetwork()) {
            FluidNetwork network = pipe.getTransmitterNetwork();
            return !network.lastFluid.isEmpty() && !network.fluidTank.isEmpty() && network.currentScale > 0;
        }
        return false;
    }

    @Nullable
    private Model3D getModel(@Nullable Direction side, FluidStack fluid, int stage) {
        if (fluid.isEmpty()) {
            return null;
        }
        return cachedLiquids.computeIfAbsent(side == null ? 6 : side.ordinal(), s -> new FluidRenderMap<>())
              .computeIfAbsent(fluid, f -> new Int2ObjectOpenHashMap<>())
              .computeIfAbsent(stage, s -> {
                  float stageRatio = (s / (float) stages) * height;
                  Model3D model = new Model3D()
                        .setTexture(MekanismRenderer.getFluidTexture(fluid, FluidTextureType.STILL));
                  if (side == null) {
                      return model.xBounds(0.25F + offset, 0.75F - offset)
                            .yBounds(0.25F + offset, 0.25F + offset + stageRatio)
                            .zBounds(0.25F + offset, 0.75F - offset);
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
                  return side == Direction.DOWN ? model.yBounds(0, 0.25F + offset)
                                                : model.yBounds(0.25F - offset + stageRatio, 1);//Up
              });
    }

    private static Model3D setHorizontalBounds(Direction horizontal, ModelBoundsSetter axisBased, ModelBoundsSetter directionBased) {
        axisBased.set(0.25F + offset, 0.75F - offset);
        if (horizontal.getAxisDirection() == AxisDirection.POSITIVE) {
            return directionBased.set(0.75F - offset, 1);
        }
        return directionBased.set(0, 0.25F + offset);
    }
}