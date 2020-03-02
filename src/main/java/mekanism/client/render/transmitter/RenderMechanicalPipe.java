package mekanism.client.render.transmitter;

import com.mojang.blaze3d.matrix.MatrixStack;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Collections;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.client.render.FluidRenderMap;
import mekanism.client.render.MekanismRenderType;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.FluidType;
import mekanism.client.render.MekanismRenderer.GlowInfo;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.common.config.MekanismConfig;
import mekanism.common.tile.transmitter.TileEntityMechanicalPipe;
import mekanism.common.tile.transmitter.TileEntitySidedPipe.ConnectionType;
import mekanism.common.transmitters.grid.FluidNetwork;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraftforge.fluids.FluidStack;

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
    public void render(@Nonnull TileEntityMechanicalPipe pipe, float partialTick, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light,
          int overlayLight) {
        if (MekanismConfig.client.opaqueTransmitters.get()) {
            return;
        }
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
            matrix.push();
            GlowInfo glowInfo = MekanismRenderer.enableGlow(fluidStack);
            boolean gas = fluidStack.getFluid().getAttributes().isGaseous(fluidStack);
            for (Direction side : EnumUtils.DIRECTIONS) {
                ConnectionType connectionType = pipe.getConnectionType(side);
                if (connectionType == ConnectionType.NORMAL) {
                    Model3D model = getModel(side, fluidStack, getStage(scale, gas));
                    if (model != null) {
                        MekanismRenderer.renderObject(model, matrix, renderer, MekanismRenderType.renderMechanicalPipeState(AtlasTexture.LOCATION_BLOCKS_TEXTURE),
                              MekanismRenderer.getColorARGB(fluidStack, scale));
                    }
                } else if (connectionType != ConnectionType.NONE) {
                    matrix.translate(0.5, 0.5, 0.5);
                    int color = MekanismRenderer.getColorARGB(fluidStack, pipe.currentScale);
                    float red = MekanismRenderer.getRed(color);
                    float green = MekanismRenderer.getGreen(color);
                    float blue = MekanismRenderer.getBlue(color);
                    float alpha = MekanismRenderer.getAlpha(color);
                    renderModel(pipe, matrix, renderer.getBuffer(MekanismRenderType.transmitterContents(AtlasTexture.LOCATION_BLOCKS_TEXTURE)), red, green, blue, alpha, light,
                          overlayLight, MekanismRenderer.getFluidTexture(fluidStack, FluidType.STILL), Collections.singletonList(side.getName() + connectionType.getName().toUpperCase()));
                    matrix.translate(-0.5, -0.5, -0.5);
                }
            }
            Model3D model = getModel(null, fluidStack, getStage(scale, gas));
            if (model != null) {
                MekanismRenderer.renderObject(model, matrix, renderer, MekanismRenderType.renderMechanicalPipeState(AtlasTexture.LOCATION_BLOCKS_TEXTURE),
                      MekanismRenderer.getColorARGB(fluidStack, scale));
            }
            MekanismRenderer.disableGlow(glowInfo);
            matrix.pop();
        }
    }

    private int getStage(float scale, boolean gas) {
        return gas ? stages - 1 : Math.max(3, (int) (scale * (stages - 1)));
    }

    @Nullable
    private Model3D getModel(Direction side, @Nonnull FluidStack fluid, int stage) {
        if (fluid.isEmpty()) {
            return null;
        }
        int sideOrdinal = side != null ? side.ordinal() : 6;
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
        BlockState state = MekanismUtils.getFlowingBlockState(fluid);
        //TODO: Check air better, given we don't have any position information
        model.baseBlock = state.isAir() ? Blocks.WATER : state.getBlock();
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