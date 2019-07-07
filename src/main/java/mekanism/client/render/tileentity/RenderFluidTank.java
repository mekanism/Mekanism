package mekanism.client.render.tileentity;

import mekanism.client.render.FluidRenderMap;
import mekanism.client.render.GLSMHelper;
import mekanism.client.render.GLSMHelper.GlowInfo;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.DisplayInteger;
import mekanism.client.render.MekanismRenderer.FluidType;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.common.tier.FluidTankTier;
import mekanism.common.tile.TileEntityFluidTank;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.init.Blocks;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

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
            GlStateManager.pushMatrix();
            GlStateManager.enableCull();
            GlStateManager.disableLighting();
            GlStateManager.shadeModel(GL11.GL_SMOOTH);
            GlStateManager.disableAlpha();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
            bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            GlStateManager.translate((float) x, (float) y, (float) z);
            GlowInfo glowInfo = GLSMHelper.enableGlow(fluid);

            DisplayInteger[] displayList = getListAndRender(fluid);
            if (tier == FluidTankTier.CREATIVE) {
                fluidScale = 1;
            }

            GLSMHelper.color(fluid, fluidScale);
            if (fluid.getFluid().isGaseous(fluid)) {
                displayList[stages - 1].render();
            } else {
                displayList[Math.min(stages - 1, (int) (fluidScale * ((float) stages - 1)))].render();
            }
            GLSMHelper.resetColor();
            GLSMHelper.disableGlow(glowInfo);
            GlStateManager.disableBlend();
            GlStateManager.enableAlpha();
            GlStateManager.enableLighting();
            GlStateManager.disableCull();
            GlStateManager.popMatrix();
        }

        if (valveFluid != null && !valveFluid.getFluid().isGaseous(valveFluid)) {
            GlStateManager.pushMatrix();
            GlStateManager.enableCull();
            GlStateManager.disableLighting();
            GlStateManager.shadeModel(GL11.GL_SMOOTH);
            GlStateManager.disableAlpha();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
            bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            GlStateManager.translate((float) x, (float) y, (float) z);
            GlowInfo glowInfo = GLSMHelper.enableGlow(valveFluid);
            GLSMHelper.color(valveFluid);
            DisplayInteger[] valveList = getValveRender(valveFluid);
            valveList[Math.min(stages - 1, (int) (fluidScale * ((float) stages - 1)))].render();
            GLSMHelper.resetColor();
            GLSMHelper.disableGlow(glowInfo);
            GlStateManager.disableBlend();
            GlStateManager.enableAlpha();
            GlStateManager.enableLighting();
            GlStateManager.disableCull();
            GlStateManager.popMatrix();
        }
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

            DisplayInteger.endList();
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

            DisplayInteger.endList();
        }

        return displays;
    }
}