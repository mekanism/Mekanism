package mekanism.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.client.render.FluidRenderMap;
import mekanism.client.render.MekanismRenderer.DisplayInteger;
import mekanism.common.tile.TileEntityFluidTank;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraftforge.fluids.FluidStack;

public class RenderFluidTank extends MekanismTileEntityRenderer<TileEntityFluidTank> {

    private static FluidRenderMap<DisplayInteger[]> cachedCenterFluids = new FluidRenderMap<>();
    private static FluidRenderMap<DisplayInteger[]> cachedValveFluids = new FluidRenderMap<>();

    private static int stages = 1400;

    public static void resetDisplayInts() {
        cachedCenterFluids.clear();
        cachedValveFluids.clear();
    }

    @Override
    public void func_225616_a_(@Nonnull TileEntityFluidTank tile, float partialTick, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int otherLight) {
        FluidStack fluid = tile.fluidTank.getFluid();
        //TODO: 1.15
        //render(tile.tier, fluid, tile.prevScale, tile.valve > 0 ? tile.valveFluid : FluidStack.EMPTY, x, y, z);
    }

    //TODO: 1.15
    /*public void render(FluidTankTier tier, @Nonnull FluidStack fluid, float fluidScale, @Nonnull FluidStack valveFluid, double x, double y, double z) {
        boolean glChanged = false;
        if (!fluid.isEmpty() && fluidScale > 0) {
            RenderSystem.pushMatrix();
            glChanged = enableGL();
            field_228858_b_.textureManager.bindTexture(PlayerContainer.field_226615_c_);
            RenderSystem.translatef((float) x, (float) y, (float) z);
            GlowInfo glowInfo = MekanismRenderer.enableGlow(fluid);

            DisplayInteger[] displayList = getListAndRender(fluid);
            if (tier == FluidTankTier.CREATIVE) {
                fluidScale = 1;
            }
            MekanismRenderer.color(fluid, fluidScale);
            if (fluid.getFluid().getAttributes().isGaseous(fluid)) {
                displayList[stages - 1].render();
            } else {
                displayList[Math.min(stages - 1, (int) (fluidScale * ((float) stages - 1)))].render();
            }
            MekanismRenderer.resetColor();
            MekanismRenderer.disableGlow(glowInfo);
            RenderSystem.popMatrix();
        }

        if (!valveFluid.isEmpty() && !valveFluid.getFluid().getAttributes().isGaseous(valveFluid)) {
            RenderSystem.pushMatrix();
            glChanged = enableGL();
            field_228858_b_.textureManager.bindTexture(PlayerContainer.field_226615_c_);
            RenderSystem.translatef((float) x, (float) y, (float) z);
            GlowInfo glowInfo = MekanismRenderer.enableGlow(valveFluid);
            MekanismRenderer.color(valveFluid);
            DisplayInteger[] valveList = getValveRender(valveFluid);
            valveList[Math.min(stages - 1, (int) (fluidScale * ((float) stages - 1)))].render();
            MekanismRenderer.resetColor();
            MekanismRenderer.disableGlow(glowInfo);
            RenderSystem.popMatrix();
        }

        if (glChanged) {
            RenderSystem.disableBlend();
            RenderSystem.enableAlphaTest();
            RenderSystem.enableLighting();
            RenderSystem.disableCull();
        }
    }

    private boolean enableGL() {
        RenderSystem.enableCull();
        RenderSystem.disableLighting();
        RenderSystem.shadeModel(GL11.GL_SMOOTH);
        RenderSystem.disableAlphaTest();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        return true;
    }

    private DisplayInteger[] getValveRender(@Nonnull FluidStack fluid) {
        if (cachedValveFluids.containsKey(fluid)) {
            return cachedValveFluids.get(fluid);
        }

        Model3D toReturn = new Model3D();
        toReturn.baseBlock = Blocks.WATER;
        MekanismRenderer.prepFlowing(toReturn, fluid);

        DisplayInteger[] displays = new DisplayInteger[stages];
        cachedValveFluids.put(fluid, displays);

        for (int i = 0; i < stages; i++) {
            displays[i] = DisplayInteger.createAndStart();
            if (fluid.getFluid().getAttributes().getStillTexture(fluid) != null) {
                toReturn.minX = 0.3125 + .01;
                toReturn.minY = 0.0625 + ((float) i / (float) stages) * 0.875;
                toReturn.minZ = 0.3125 + .01;

                toReturn.maxX = 0.6875 - .01;
                toReturn.maxY = 0.9375 - .01;
                toReturn.maxZ = 0.6875 - .01;

                MekanismRenderer.renderObject(toReturn);
            }
            GlStateManager.endList();
        }
        return displays;
    }

    private DisplayInteger[] getListAndRender(@Nonnull FluidStack fluid) {
        if (cachedCenterFluids.containsKey(fluid)) {
            return cachedCenterFluids.get(fluid);
        }

        Model3D toReturn = new Model3D();
        toReturn.baseBlock = Blocks.WATER;
        toReturn.setTexture(MekanismRenderer.getFluidTexture(fluid, FluidType.STILL));

        DisplayInteger[] displays = new DisplayInteger[stages];
        cachedCenterFluids.put(fluid, displays);

        for (int i = 0; i < stages; i++) {
            displays[i] = DisplayInteger.createAndStart();
            if (fluid.getFluid().getAttributes().getStillTexture(fluid) != null) {
                toReturn.minX = 0.125 + .01;
                toReturn.minY = 0.0625 + .01;
                toReturn.minZ = 0.125 + .01;

                toReturn.maxX = 0.875 - .01;
                toReturn.maxY = 0.0625 + ((float) i / (float) stages) * 0.875 - .01;
                toReturn.maxZ = 0.875 - .01;

                MekanismRenderer.renderObject(toReturn);
            }
            GlStateManager.endList();
        }
        return displays;
    }*/
}