package mekanism.client.render.transmitter;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.client.render.FluidRenderMap;
import mekanism.client.render.MekanismRenderType;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.FluidType;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.tile.transmitter.TileEntityMechanicalPipe;
import mekanism.common.tile.transmitter.TileEntitySidedPipe.ConnectionType;
import mekanism.common.transmitters.TransmitterImpl;
import mekanism.common.transmitters.grid.FluidNetwork;
import mekanism.common.util.EnumUtils;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.profiler.IProfiler;
import net.minecraft.util.Direction;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

@ParametersAreNonnullByDefault
public class RenderMechanicalPipe extends RenderTransmitterBase<TileEntityMechanicalPipe> {

    private static final int stages = 100;
    private static final double height = 0.45;
    private static final double offset = 0.015;
    //Note: this is basically used as an enum map (Direction), but null key is possible, which EnumMap doesn't support. 6 is used for null side
    private static Int2ObjectMap<FluidRenderMap<Int2ObjectMap<Model3D>>> cachedLiquids = new Int2ObjectArrayMap<>(7);

    public RenderMechanicalPipe(TileEntityRendererDispatcher renderer) {
        super(renderer);
    }

    public static void onStitch() {
        cachedLiquids.clear();
    }

    @Override
    protected void render(TileEntityMechanicalPipe pipe, float partialTick, MatrixStack matrix, IRenderTypeBuffer renderer, int light, int overlayLight,
          IProfiler profiler) {
        TransmitterImpl<IFluidHandler, FluidNetwork, FluidStack> transmitter = pipe.getTransmitter();
        if (transmitter.hasTransmitterNetwork()) {
            FluidNetwork network = transmitter.getTransmitterNetwork();
            if (!network.fluidTank.isEmpty() && network.fluidScale > 0) {
                FluidStack fluidStack = network.fluidTank.getFluid();
                float fluidScale = network.fluidScale;
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
                IVertexBuilder buffer = renderer.getBuffer(MekanismRenderType.resizableCuboid());
                for (Direction side : EnumUtils.DIRECTIONS) {
                    ConnectionType connectionType = pipe.getConnectionType(side);
                    if (connectionType == ConnectionType.NORMAL) {
                        MekanismRenderer.renderObject(getModel(side, fluidStack, stage), matrix, buffer, color, glow);
                    } else if (connectionType != ConnectionType.NONE) {
                        connectionContents.add(side.getName() + connectionType.getName().toUpperCase());
                    }
                    if (model != null) {
                        model.setSideRender(side, connectionType == ConnectionType.NONE);
                    }
                }
                MekanismRenderer.renderObject(model, matrix, buffer, MekanismRenderer.getColorARGB(fluidStack, fluidScale), glow);
                if (!connectionContents.isEmpty()) {
                    matrix.push();
                    matrix.translate(0.5, 0.5, 0.5);
                    renderModel(pipe, matrix, renderer.getBuffer(MekanismRenderType.transmitterContents(AtlasTexture.LOCATION_BLOCKS_TEXTURE)),
                          MekanismRenderer.getRed(color), MekanismRenderer.getGreen(color), MekanismRenderer.getBlue(color), MekanismRenderer.getAlpha(color), glow,
                          overlayLight, MekanismRenderer.getFluidTexture(fluidStack, FluidType.STILL), connectionContents);
                    matrix.pop();
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
        double stageRatio = (stage / (double) stages) * height;
        switch (sideOrdinal) {
            case 0:
                model.minX = 0.5 - stageRatio / 2;
                model.minY = 0;
                model.minZ = 0.5 - stageRatio / 2;

                model.maxX = 0.5 + stageRatio / 2;
                model.maxY = 0.25 + offset;
                model.maxZ = 0.5 + stageRatio / 2;
                break;
            case 1:
                model.minX = 0.5 - stageRatio / 2;
                model.minY = 0.25 - offset + stageRatio;
                model.minZ = 0.5 - stageRatio / 2;

                model.maxX = 0.5 + stageRatio / 2;
                model.maxY = 1;
                model.maxZ = 0.5 + stageRatio / 2;
                break;
            case 2:
                model.minX = 0.25 + offset;
                model.minY = 0.25 + offset;
                model.minZ = 0;

                model.maxX = 0.75 - offset;
                model.maxY = 0.25 + offset + stageRatio;
                model.maxZ = 0.25 + offset;
                break;
            case 3:
                model.minX = 0.25 + offset;
                model.minY = 0.25 + offset;
                model.minZ = 0.75 - offset;

                model.maxX = 0.75 - offset;
                model.maxY = 0.25 + offset + stageRatio;
                model.maxZ = 1;
                break;
            case 4:
                model.minX = 0;
                model.minY = 0.25 + offset;
                model.minZ = 0.25 + offset;

                model.maxX = 0.25 + offset;
                model.maxY = 0.25 + offset + stageRatio;
                model.maxZ = 0.75 - offset;
                break;
            case 5:
                model.minX = 0.75 - offset;
                model.minY = 0.25 + offset;
                model.minZ = 0.25 + offset;

                model.maxX = 1;
                model.maxY = 0.25 + offset + stageRatio;
                model.maxZ = 0.75 - offset;
                break;
            case 6:
                //Null side
                model.minX = 0.25 + offset;
                model.minY = 0.25 + offset;
                model.minZ = 0.25 + offset;

                model.maxX = 0.75 - offset;
                model.maxY = 0.25 + offset + stageRatio;
                model.maxZ = 0.75 - offset;
                break;
        }
        if (cachedFluids.containsKey(fluid)) {
            cachedFluids.get(fluid).put(stage, model);
        } else {
            Int2ObjectMap<Model3D> map = new Int2ObjectOpenHashMap<>();
            map.put(stage, model);
            cachedFluids.put(fluid, map);
        }
        return model;
    }
}