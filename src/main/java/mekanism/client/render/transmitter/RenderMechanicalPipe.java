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
import mekanism.common.transmitters.grid.FluidNetwork;
import mekanism.common.util.EnumUtils;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.profiler.IProfiler;
import net.minecraft.util.Direction;
import net.minecraftforge.fluids.FluidStack;

@ParametersAreNonnullByDefault
public class RenderMechanicalPipe extends RenderTransmitterBase<TileEntityMechanicalPipe> {

    private static final int stages = 100;
    private static final double height = 0.45;
    private static final double offset = 0.015;
    //TODO: this is basically used as an enum map (Direction), but null key is possible, which EnumMap doesn't support. 6 is used for null side
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
        float targetScale;
        FluidStack fluidStack;
        if (pipe.getTransmitter().hasTransmitterNetwork()) {
            FluidNetwork network = pipe.getTransmitter().getTransmitterNetwork();
            targetScale = network.fluidScale;
            fluidStack = network.fluidTank.getFluid();
        } else {
            targetScale = (float) pipe.buffer.getFluidAmount() / (float) pipe.buffer.getCapacity();
            fluidStack = pipe.getBuffer();
        }
        if (Math.abs(pipe.currentScale - targetScale) > 0.01) {
            pipe.currentScale = (12 * pipe.currentScale + targetScale) / 13;
        } else {
            pipe.currentScale = targetScale;
        }
        float scale = Math.min(pipe.currentScale, 1);
        if (scale > 0.01 && !fluidStack.isEmpty()) {
            IVertexBuilder buffer = renderer.getBuffer(MekanismRenderType.resizableCuboid());
            int glow = MekanismRenderer.calculateGlowLight(light, fluidStack);
            boolean gas = fluidStack.getFluid().getAttributes().isGaseous(fluidStack);
            List<String> connectionContents = new ArrayList<>();
            for (Direction side : EnumUtils.DIRECTIONS) {
                //TODO: Make this mark the sides that shouldn't get rendered
                ConnectionType connectionType = pipe.getConnectionType(side);
                if (connectionType == ConnectionType.NORMAL) {
                    Model3D model = getModel(side, fluidStack, getStage(scale, gas));
                    if (model != null) {
                        //TODO: Only render part of back face?
                        MekanismRenderer.renderObject(model, matrix, buffer, MekanismRenderer.getColorARGB(fluidStack, scale), glow);
                    }
                } else if (connectionType != ConnectionType.NONE) {
                    connectionContents.add(side.getName() + connectionType.getName().toUpperCase());
                }
            }
            Model3D model = getModel(null, fluidStack, getStage(scale, gas));
            if (model != null) {
                //TODO: Make this only render faces that don't have a connection type
                MekanismRenderer.renderObject(model, matrix, buffer, MekanismRenderer.getColorARGB(fluidStack, scale), glow);
            }
            if (!connectionContents.isEmpty()) {
                matrix.push();
                matrix.translate(0.5, 0.5, 0.5);
                int color = MekanismRenderer.getColorARGB(fluidStack, pipe.currentScale);
                renderModel(pipe, matrix, renderer.getBuffer(MekanismRenderType.transmitterContents(AtlasTexture.LOCATION_BLOCKS_TEXTURE)),
                      MekanismRenderer.getRed(color), MekanismRenderer.getGreen(color), MekanismRenderer.getBlue(color), MekanismRenderer.getAlpha(color), glow,
                      overlayLight, MekanismRenderer.getFluidTexture(fluidStack, FluidType.STILL), connectionContents);
                matrix.pop();
            }
        }
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.MECHANICAL_PIPE;
    }

    private int getStage(float scale, boolean gas) {
        return gas ? stages - 1 : Math.max(3, (int) (scale * (stages - 1)));
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
        switch (sideOrdinal) {
            case 0:
                model.minX = 0.5 - (((float) stage / (float) stages) * height) / 2;
                model.minY = 0.0;
                model.minZ = 0.5 - (((float) stage / (float) stages) * height) / 2;

                model.maxX = 0.5 + (((float) stage / (float) stages) * height) / 2;
                model.maxY = 0.25 + offset;
                model.maxZ = 0.5 + (((float) stage / (float) stages) * height) / 2;
                break;
            case 1:
                model.minX = 0.5 - (((float) stage / (float) stages) * height) / 2;
                model.minY = 0.25 - offset + ((float) stage / (float) stages) * height;
                model.minZ = 0.5 - (((float) stage / (float) stages) * height) / 2;

                model.maxX = 0.5 + (((float) stage / (float) stages) * height) / 2;
                model.maxY = 1.0;
                model.maxZ = 0.5 + (((float) stage / (float) stages) * height) / 2;
                break;
            case 2:
                model.minX = 0.25 + offset;
                model.minY = 0.25 + offset;
                model.minZ = 0.0;

                model.maxX = 0.75 - offset;
                model.maxY = 0.25 + offset + ((float) stage / (float) stages) * height;
                model.maxZ = 0.25 + offset;
                break;
            case 3:
                model.minX = 0.25 + offset;
                model.minY = 0.25 + offset;
                model.minZ = 0.75 - offset;

                model.maxX = 0.75 - offset;
                model.maxY = 0.25 + offset + ((float) stage / (float) stages) * height;
                model.maxZ = 1.0;
                break;
            case 4:
                model.minX = 0.0;
                model.minY = 0.25 + offset;
                model.minZ = 0.25 + offset;

                model.maxX = 0.25 + offset;
                model.maxY = 0.25 + offset + ((float) stage / (float) stages) * height;
                model.maxZ = 0.75 - offset;
                break;
            case 5:
                model.minX = 0.75 - offset;
                model.minY = 0.25 + offset;
                model.minZ = 0.25 + offset;

                model.maxX = 1.0;
                model.maxY = 0.25 + offset + ((float) stage / (float) stages) * height;
                model.maxZ = 0.75 - offset;
                break;
            case 6:
                //Null side
                model.minX = 0.25 + offset;
                model.minY = 0.25 + offset;
                model.minZ = 0.25 + offset;

                model.maxX = 0.75 - offset;
                model.maxY = 0.25 + offset + ((float) stage / (float) stages) * height;
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