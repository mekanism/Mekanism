package mekanism.client.render.transmitter;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.client.render.FluidRenderMap;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.FluidType;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.client.render.RenderResizableCuboid.FaceDisplay;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.content.network.FluidNetwork;
import mekanism.common.content.network.transmitter.MechanicalPipe;
import mekanism.common.lib.transmitter.ConnectionType;
import mekanism.common.tile.transmitter.TileEntityMechanicalPipe;
import mekanism.common.util.EnumUtils;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.profiler.IProfiler;
import net.minecraft.util.Direction;
import net.minecraftforge.fluids.FluidStack;

@ParametersAreNonnullByDefault
public class RenderMechanicalPipe extends RenderTransmitterBase<TileEntityMechanicalPipe> {

    private static final int stages = 100;
    private static final float height = 0.45F;
    private static final float offset = 0.015F;
    //Note: this is basically used as an enum map (Direction), but null key is possible, which EnumMap doesn't support. 6 is used for null side
    private static final Int2ObjectMap<FluidRenderMap<Int2ObjectMap<Model3D>>> cachedLiquids = new Int2ObjectArrayMap<>(7);

    public RenderMechanicalPipe(TileEntityRendererDispatcher renderer) {
        super(renderer);
    }

    public static void onStitch() {
        cachedLiquids.clear();
    }

    @Override
    protected void render(TileEntityMechanicalPipe tile, float partialTick, MatrixStack matrix, IRenderTypeBuffer renderer, int light, int overlayLight,
          IProfiler profiler) {
        MechanicalPipe pipe = tile.getTransmitter();
        if (pipe.hasTransmitterNetwork()) {
            FluidNetwork network = pipe.getTransmitterNetwork();
            if (!network.lastFluid.isEmpty() && !network.fluidTank.isEmpty() && network.currentScale > 0) {
                FluidStack fluidStack = network.lastFluid;
                float fluidScale = network.currentScale;
                int stage;
                if (fluidStack.getFluid().getAttributes().isGaseous(fluidStack)) {
                    stage = stages - 1;
                } else {
                    stage = Math.max(3, (int) (fluidScale * (stages - 1)));
                }
                int glow = MekanismRenderer.calculateGlowLight(light, fluidStack);
                int color = MekanismRenderer.getColorARGB(fluidStack, fluidScale);
                List<String> connectionContents = new ArrayList<>();
                Model3D model = getModel(null, fluidStack, stage);
                IVertexBuilder buffer = renderer.getBuffer(Atlases.translucentCullBlockSheet());
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
                MekanismRenderer.renderObject(model, matrix, buffer, MekanismRenderer.getColorARGB(fluidStack, fluidScale), glow, overlayLight, FaceDisplay.FRONT);
                if (!connectionContents.isEmpty()) {
                    matrix.pushPose();
                    matrix.translate(0.5, 0.5, 0.5);
                    renderModel(tile, matrix, buffer, MekanismRenderer.getRed(color), MekanismRenderer.getGreen(color), MekanismRenderer.getBlue(color),
                          MekanismRenderer.getAlpha(color), glow, overlayLight, MekanismRenderer.getFluidTexture(fluidStack, FluidType.STILL), connectionContents);
                    matrix.popPose();
                }
            }
        }
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.MECHANICAL_PIPE;
    }

    @Nullable
    private Model3D getModel(@Nullable Direction side, FluidStack fluid, int stage) {
        if (fluid.isEmpty()) {
            return null;
        }
        int sideOrdinal = side == null ? 6 : side.ordinal();
        FluidRenderMap<Int2ObjectMap<Model3D>> cachedFluids;
        if (cachedLiquids.containsKey(sideOrdinal)) {
            cachedFluids = cachedLiquids.get(sideOrdinal);
            if (cachedFluids.containsKey(fluid) && cachedFluids.get(fluid).containsKey(stage)) {
                return cachedFluids.get(fluid).get(stage);
            }
        } else {
            cachedLiquids.put(sideOrdinal, cachedFluids = new FluidRenderMap<>());
        }
        Model3D model = new Model3D();
        model.setTexture(MekanismRenderer.getFluidTexture(fluid, FluidType.STILL));
        if (side != null) {
            model.setSideRender(side, false);
            model.setSideRender(side.getOpposite(), false);
        }
        float stageRatio = (stage / (float) stages) * height;
        switch (sideOrdinal) {
            case 0:
                model.minX = 0.5F - stageRatio / 2;
                model.minY = 0;
                model.minZ = 0.5F - stageRatio / 2;

                model.maxX = 0.5F + stageRatio / 2;
                model.maxY = 0.25F + offset;
                model.maxZ = 0.5F + stageRatio / 2;
                break;
            case 1:
                model.minX = 0.5F - stageRatio / 2;
                model.minY = 0.25F - offset + stageRatio;
                model.minZ = 0.5F - stageRatio / 2;

                model.maxX = 0.5F + stageRatio / 2;
                model.maxY = 1;
                model.maxZ = 0.5F + stageRatio / 2;
                break;
            case 2:
                model.minX = 0.25F + offset;
                model.minY = 0.25F + offset;
                model.minZ = 0;

                model.maxX = 0.75F - offset;
                model.maxY = 0.25F + offset + stageRatio;
                model.maxZ = 0.25F + offset;
                break;
            case 3:
                model.minX = 0.25F + offset;
                model.minY = 0.25F + offset;
                model.minZ = 0.75F - offset;

                model.maxX = 0.75F - offset;
                model.maxY = 0.25F + offset + stageRatio;
                model.maxZ = 1;
                break;
            case 4:
                model.minX = 0;
                model.minY = 0.25F + offset;
                model.minZ = 0.25F + offset;

                model.maxX = 0.25F + offset;
                model.maxY = 0.25F + offset + stageRatio;
                model.maxZ = 0.75F - offset;
                break;
            case 5:
                model.minX = 0.75F - offset;
                model.minY = 0.25F + offset;
                model.minZ = 0.25F + offset;

                model.maxX = 1;
                model.maxY = 0.25F + offset + stageRatio;
                model.maxZ = 0.75F - offset;
                break;
            case 6:
                //Null side
                model.minX = 0.25F + offset;
                model.minY = 0.25F + offset;
                model.minZ = 0.25F + offset;

                model.maxX = 0.75F - offset;
                model.maxY = 0.25F + offset + stageRatio;
                model.maxZ = 0.75F - offset;
                break;
        }
        cachedFluids.computeIfAbsent(fluid, f -> new Int2ObjectOpenHashMap<>()).putIfAbsent(stage, model);
        return model;
    }
}