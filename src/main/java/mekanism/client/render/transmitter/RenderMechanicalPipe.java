package mekanism.client.render.transmitter;

import com.mojang.blaze3d.matrix.MatrixStack;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import javax.annotation.Nonnull;
import mekanism.client.render.FluidRenderMap;
import mekanism.client.render.MekanismRenderer.DisplayInteger;
import mekanism.common.tile.transmitter.TileEntityMechanicalPipe;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;

public class RenderMechanicalPipe extends RenderTransmitterBase<TileEntityMechanicalPipe> {

    private static final int stages = 100;
    private static final double height = 0.45;
    private static final double offset = 0.015;
    //TODO this is basically used as an enum map (Direction), but null key is possible, which EnumMap doesn't support. 6 is used for null side
    private static Int2ObjectMap<FluidRenderMap<DisplayInteger[]>> cachedLiquids = new Int2ObjectArrayMap<>(7);

    public RenderMechanicalPipe(TileEntityRendererDispatcher renderer) {
        super(renderer);
    }

    public static void onStitch() {
        cachedLiquids.clear();
    }

    @Override
    public void func_225616_a_(@Nonnull TileEntityMechanicalPipe pipe, float partialTick, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light,
          int overlayLight) {
        //TODO: 1.15
        /*if (MekanismConfig.client.opaqueTransmitters.get()) {
            return;
        }

        float targetScale;
        FluidStack fluidStack;
        if (pipe.getTransmitter().hasTransmitterNetwork()) {
            FluidNetwork network = pipe.getTransmitter().getTransmitterNetwork();
            targetScale = network.fluidScale;
            fluidStack = network.buffer;
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
            RenderSystem.pushMatrix();
            RenderSystem.enableCull();
            RenderSystem.disableLighting();
            GlowInfo glowInfo = MekanismRenderer.enableGlow(fluidStack);
            MekanismRenderer.color(fluidStack);

            field_228858_b_.textureManager.bindTexture(PlayerContainer.field_226615_c_);
            RenderSystem.translatef((float) x, (float) y, (float) z);

            boolean gas = fluidStack.getFluid().getAttributes().isGaseous(fluidStack);
            for (Direction side : EnumUtils.DIRECTIONS) {
                if (pipe.getConnectionType(side) == ConnectionType.NORMAL) {
                    renderDisplayLists(getListAndRender(side, fluidStack), scale, gas);
                } else if (pipe.getConnectionType(side) != ConnectionType.NONE) {
                    RenderSystem.translatef(0.5F, 0.5F, 0.5F);
                    Tessellator tessellator = Tessellator.getInstance();
                    BufferBuilder worldRenderer = tessellator.getBuffer();
                    if (renderFluidInOut(worldRenderer, side, pipe)) {
                        tessellator.draw();
                    }
                    RenderSystem.translatef(-0.5F, -0.5F, -0.5F);
                }
            }
            renderDisplayLists(getListAndRender(null, fluidStack), scale, gas);
            MekanismRenderer.resetColor();
            MekanismRenderer.disableGlow(glowInfo);
            RenderSystem.enableLighting();
            RenderSystem.disableCull();
            RenderSystem.popMatrix();
        }*/
    }

    //TODO: 1.15
    /*private void renderDisplayLists(DisplayInteger[] displayLists, float scale, boolean gas) {
        if (displayLists != null) {
            if (gas) {
                RenderSystem.color4f(1, 1, 1, scale);
                displayLists[stages - 1].render();
                MekanismRenderer.resetColor();
            } else {
                displayLists[Math.max(3, (int) (scale * (stages - 1)))].render();
            }
        }
    }

    private DisplayInteger[] getListAndRender(Direction side, @Nonnull FluidStack fluid) {
        if (fluid.isEmpty()) {
            return null;
        }

        int sideOrdinal = side != null ? side.ordinal() : 6;

        if (cachedLiquids.containsKey(sideOrdinal) && cachedLiquids.get(sideOrdinal).containsKey(fluid)) {
            return cachedLiquids.get(sideOrdinal).get(fluid);
        }

        Model3D toReturn = new Model3D();
        toReturn.baseBlock = Blocks.WATER;
        toReturn.setTexture(MekanismRenderer.getFluidTexture(fluid, FluidType.STILL));

        if (side != null) {
            toReturn.setSideRender(side, false);
            toReturn.setSideRender(side.getOpposite(), false);
        }

        DisplayInteger[] displays = new DisplayInteger[stages];

        if (cachedLiquids.containsKey(sideOrdinal)) {
            cachedLiquids.get(sideOrdinal).put(fluid, displays);
        } else {
            FluidRenderMap<DisplayInteger[]> map = new FluidRenderMap<>();
            map.put(fluid, displays);
            cachedLiquids.put(sideOrdinal, map);
        }

        for (int i = 0; i < stages; i++) {
            displays[i] = DisplayInteger.createAndStart();

            switch (sideOrdinal) {
                case 6:
                    toReturn.minX = 0.25 + offset;
                    toReturn.minY = 0.25 + offset;
                    toReturn.minZ = 0.25 + offset;

                    toReturn.maxX = 0.75 - offset;
                    toReturn.maxY = 0.25 + offset + ((float) i / (float) stages) * height;
                    toReturn.maxZ = 0.75 - offset;
                    break;
                case 0:
                    toReturn.minX = 0.5 - (((float) i / (float) stages) * height) / 2;
                    toReturn.minY = 0.0;
                    toReturn.minZ = 0.5 - (((float) i / (float) stages) * height) / 2;

                    toReturn.maxX = 0.5 + (((float) i / (float) stages) * height) / 2;
                    toReturn.maxY = 0.25 + offset;
                    toReturn.maxZ = 0.5 + (((float) i / (float) stages) * height) / 2;
                    break;
                case 1:
                    toReturn.minX = 0.5 - (((float) i / (float) stages) * height) / 2;
                    toReturn.minY = 0.25 - offset + ((float) i / (float) stages) * height;
                    toReturn.minZ = 0.5 - (((float) i / (float) stages) * height) / 2;

                    toReturn.maxX = 0.5 + (((float) i / (float) stages) * height) / 2;
                    toReturn.maxY = 1.0;
                    toReturn.maxZ = 0.5 + (((float) i / (float) stages) * height) / 2;
                    break;
                case 2:
                    toReturn.minX = 0.25 + offset;
                    toReturn.minY = 0.25 + offset;
                    toReturn.minZ = 0.0;

                    toReturn.maxX = 0.75 - offset;
                    toReturn.maxY = 0.25 + offset + ((float) i / (float) stages) * height;
                    toReturn.maxZ = 0.25 + offset;
                    break;
                case 3:
                    toReturn.minX = 0.25 + offset;
                    toReturn.minY = 0.25 + offset;
                    toReturn.minZ = 0.75 - offset;

                    toReturn.maxX = 0.75 - offset;
                    toReturn.maxY = 0.25 + offset + ((float) i / (float) stages) * height;
                    toReturn.maxZ = 1.0;
                    break;
                case 4:
                    toReturn.minX = 0.0;
                    toReturn.minY = 0.25 + offset;
                    toReturn.minZ = 0.25 + offset;

                    toReturn.maxX = 0.25 + offset;
                    toReturn.maxY = 0.25 + offset + ((float) i / (float) stages) * height;
                    toReturn.maxZ = 0.75 - offset;
                    break;
                case 5:
                    toReturn.minX = 0.75 - offset;
                    toReturn.minY = 0.25 + offset;
                    toReturn.minZ = 0.25 + offset;

                    toReturn.maxX = 1.0;
                    toReturn.maxY = 0.25 + offset + ((float) i / (float) stages) * height;
                    toReturn.maxZ = 0.75 - offset;
                    break;
            }

            MekanismRenderer.renderObject(toReturn);
            GlStateManager.endList();
        }

        return displays;
    }

    public boolean renderFluidInOut(BufferBuilder renderer, Direction side, @Nonnull TileEntityMechanicalPipe pipe) {
        if (pipe.getTransmitter().hasTransmitterNetwork()) {
            field_228858_b_.textureManager.bindTexture(PlayerContainer.field_226615_c_);
            FluidNetwork fn = pipe.getTransmitter().getTransmitterNetwork();
            TextureAtlasSprite tex = MekanismRenderer.getFluidTexture(fn.buffer, FluidType.STILL);
            int color = fn.buffer.getFluid().getAttributes().getColor(fn.buffer);
            ColorRGBA c = new ColorRGBA(1.0, 1.0, 1.0, pipe.currentScale);
            if (color != 0xFFFFFFFF) {
                c.setRGBFromInt(color);
            }
            renderTransparency(renderer, tex, getModelForSide(pipe, side), c, pipe.getBlockState(), pipe.getModelData());
            return true;
        }
        return false;
    }*/
}