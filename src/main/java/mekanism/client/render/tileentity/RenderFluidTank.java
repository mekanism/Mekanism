package mekanism.client.render.tileentity;

import mekanism.client.render.FluidRenderMap;
import mekanism.client.render.MekanismRenderHelper;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.DisplayInteger;
import mekanism.client.render.MekanismRenderer.FluidType;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.common.tier.FluidTankTier;
import mekanism.common.tile.TileEntityFluidTank;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.init.Blocks;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderFluidTank extends TileEntitySpecialRenderer<TileEntityFluidTank> {

    public static final RenderFluidTank INSTANCE = new RenderFluidTank();

    private static FluidRenderMap<DisplayInteger[]> cachedCenterFluids = new FluidRenderMap<>();
    private static FluidRenderMap<DisplayInteger[]> cachedValveFluids = new FluidRenderMap<>();

    private static int stages = 1400;

    public static void resetDisplayInts() {
        cachedCenterFluids.clear();
        cachedValveFluids.clear();
    }

    @Override
    public void render(TileEntityFluidTank tileEntity, double x, double y, double z, float partialTick, int destroyStage, float alpha) {
        FluidStack fluid = tileEntity.fluidTank.getFluid();
        render(tileEntity.tier, fluid, tileEntity.prevScale, tileEntity.isActive, tileEntity.valve > 0 ? tileEntity.valveFluid : null, x, y, z);
    }

    public void render(FluidTankTier tier, FluidStack fluid, float fluidScale, boolean active, FluidStack valveFluid, double x, double y, double z) {
        if (fluid != null && fluidScale > 0) {
            MekanismRenderHelper renderHelper = initHelper();
            bindTexture(MekanismRenderer.getBlocksTexture());
            GlStateManager.translate(x, y, z);
            MekanismRenderer.glowOn(fluid.getFluid().getLuminosity(fluid));
            renderHelper.color(fluid);

            DisplayInteger[] displayList = getListAndRender(fluid);
            if (tier == FluidTankTier.CREATIVE) {
                fluidScale = 1;
            }

            if (fluid.getFluid().isGaseous(fluid)) {
                GlStateManager.color(1F, 1F, 1F, Math.min(1, fluidScale + MekanismRenderer.GAS_RENDER_BASE));
                displayList[stages - 1].render();
            } else {
                displayList[Math.min(stages - 1, (int) (fluidScale * ((float) stages - 1)))].render();
            }
            MekanismRenderer.glowOff();
            renderHelper.cleanup();
        }

        if (valveFluid != null && !valveFluid.getFluid().isGaseous(valveFluid)) {
            MekanismRenderHelper renderHelper = initHelper();
            bindTexture(MekanismRenderer.getBlocksTexture());
            GlStateManager.translate(x, y, z);
            MekanismRenderer.glowOn(valveFluid.getFluid().getLuminosity(valveFluid));
            renderHelper.color(valveFluid);
            DisplayInteger[] valveList = getValveRender(valveFluid);
            valveList[Math.min(stages - 1, (int) (fluidScale * ((float) stages - 1)))].render();
            MekanismRenderer.glowOff();
            renderHelper.cleanup();
        }
    }

    private MekanismRenderHelper initHelper() {
        return MekanismRenderer.blendOn(new MekanismRenderHelper(true).enableCull().disableLighting());
    }

    private DisplayInteger[] getValveRender(FluidStack fluid) {
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

            if (fluid.getFluid().getStill(fluid) != null) {
                toReturn.minX = 0.3125 + .01;
                toReturn.minY = 0.0625 + ((float) i / (float) stages) * 0.875;
                toReturn.minZ = 0.3125 + .01;

                toReturn.maxX = 0.6875 - .01;
                toReturn.maxY = 0.9375 - .01;
                toReturn.maxZ = 0.6875 - .01;

                MekanismRenderer.renderObject(toReturn);
            }

            GlStateManager.glEndList();
        }

        return displays;
    }

    private DisplayInteger[] getListAndRender(FluidStack fluid) {
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

            if (fluid.getFluid().getStill(fluid) != null) {
                toReturn.minX = 0.125 + .01;
                toReturn.minY = 0.0625 + .01;
                toReturn.minZ = 0.125 + .01;

                toReturn.maxX = 0.875 - .01;
                toReturn.maxY = 0.0625 + ((float) i / (float) stages) * 0.875 - .01;
                toReturn.maxZ = 0.875 - .01;

                MekanismRenderer.renderObject(toReturn);
            }

            GlStateManager.glEndList();
        }

        return displays;
    }
}