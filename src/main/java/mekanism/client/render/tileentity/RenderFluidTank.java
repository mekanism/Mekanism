package mekanism.client.render.tileentity;

import java.util.HashMap;
import java.util.Map;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.DisplayInteger;
import mekanism.client.render.MekanismRenderer.FluidType;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.common.Tier.FluidTankTier;
import mekanism.common.tile.TileEntityFluidTank;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.init.Blocks;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class RenderFluidTank extends TileEntitySpecialRenderer<TileEntityFluidTank> {

    public static final RenderFluidTank INSTANCE = new RenderFluidTank();

    private static Map<Fluid, DisplayInteger[]> cachedCenterFluids = new HashMap<>();
    private static Map<Fluid, DisplayInteger[]> cachedValveFluids = new HashMap<>();

    private static int stages = 1400;

    public static void resetDisplayInts() {
        cachedCenterFluids.clear();
        cachedValveFluids.clear();
    }

    @Override
    public void render(TileEntityFluidTank tileEntity, double x, double y, double z, float partialTick,
          int destroyStage, float alpha) {
        Fluid fluid = tileEntity.fluidTank.getFluid() != null ? tileEntity.fluidTank.getFluid().getFluid() : null;
        render(tileEntity.tier, fluid, tileEntity.prevScale, tileEntity.isActive,
              tileEntity.valve > 0 ? tileEntity.valveFluid : null, x, y, z);
    }

    public void render(FluidTankTier tier, Fluid fluid, float fluidScale, boolean active, Fluid valveFluid, double x,
          double y, double z) {
        if (fluid != null && fluidScale > 0) {
            push();

            bindTexture(MekanismRenderer.getBlocksTexture());
            GL11.glTranslated(x, y, z);

            MekanismRenderer.glowOn(fluid.getLuminosity());
            MekanismRenderer.colorFluid(fluid);

            DisplayInteger[] displayList = getListAndRender(fluid);

            if (tier == FluidTankTier.CREATIVE) {
                fluidScale = 1;
            }

            if (fluid.isGaseous()) {
                GL11.glColor4f(1F, 1F, 1F, Math.min(1, fluidScale + MekanismRenderer.GAS_RENDER_BASE));
                displayList[stages - 1].render();
            } else {
                displayList[Math.min(stages - 1, (int) (fluidScale * ((float) stages - 1)))].render();
            }

            MekanismRenderer.resetColor();
            MekanismRenderer.glowOff();

            pop();
        }

        if (valveFluid != null && !valveFluid.isGaseous()) {
            push();

            bindTexture(MekanismRenderer.getBlocksTexture());
            GL11.glTranslated(x, y, z);

            MekanismRenderer.glowOn(valveFluid.getLuminosity());
            MekanismRenderer.colorFluid(valveFluid);

            DisplayInteger[] valveList = getValveRender(valveFluid);

            valveList[Math.min(stages - 1, (int) (fluidScale * ((float) stages - 1)))].render();

            MekanismRenderer.glowOff();
            MekanismRenderer.resetColor();

            pop();
        }
    }

    private void pop() {
        GL11.glPopAttrib();
        MekanismRenderer.blendOff();
        GlStateManager.popMatrix();
    }

    private void push() {
        GlStateManager.pushMatrix();
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_LIGHTING);
        MekanismRenderer.blendOn();
    }

    private DisplayInteger[] getValveRender(Fluid fluid) {
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

            if (fluid.getStill() != null) {
                toReturn.minX = 0.3125 + .01;
                toReturn.minY = 0.0625 + ((float) i / (float) stages) * 0.875;
                toReturn.minZ = 0.3125 + .01;

                toReturn.maxX = 0.6875 - .01;
                toReturn.maxY = 0.9375 - .01;
                toReturn.maxZ = 0.6875 - .01;

                MekanismRenderer.renderObject(toReturn);
            }

            GL11.glEndList();
        }

        return displays;
    }

    private DisplayInteger[] getListAndRender(Fluid fluid) {
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

            if (fluid.getStill() != null) {
                toReturn.minX = 0.125 + .01;
                toReturn.minY = 0.0625 + .01;
                toReturn.minZ = 0.125 + .01;

                toReturn.maxX = 0.875 - .01;
                toReturn.maxY = 0.0625 + ((float) i / (float) stages) * 0.875 - .01;
                toReturn.maxZ = 0.875 - .01;

                MekanismRenderer.renderObject(toReturn);
            }

            GL11.glEndList();
        }

        return displays;
    }
}
