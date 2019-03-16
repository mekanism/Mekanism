package mekanism.client.render.item.machine;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import mekanism.client.model.ModelFluidTank;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.DisplayInteger;
import mekanism.client.render.MekanismRenderer.FluidType;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.common.Tier.FluidTankTier;
import mekanism.common.item.ItemBlockMachine;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class RenderFluidTankItem {

    private static ModelFluidTank fluidTank = new ModelFluidTank();

    private static Map<Fluid, DisplayInteger[]> cachedCenterFluids = new HashMap<>();

    private static int stages = 1400;

    public static void renderStack(@Nonnull ItemStack stack, TransformType transformType) {
        ItemBlockMachine itemMachine = (ItemBlockMachine) stack.getItem();
        float fluidScale =
              (float) (itemMachine.getFluidStack(stack) != null ? itemMachine.getFluidStack(stack).amount : 0)
                    / itemMachine.getCapacity(stack);
        FluidTankTier tier = FluidTankTier.values()[itemMachine.getBaseTier(stack).ordinal()];
        Fluid fluid =
              itemMachine.getFluidStack(stack) != null ? itemMachine.getFluidStack(stack).getFluid() : null;

        GlStateManager.pushMatrix();
        if (fluid != null && fluidScale > 0) {
            GlStateManager.pushMatrix();
            GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
            GL11.glEnable(GL11.GL_CULL_FACE);
            GL11.glDisable(GL11.GL_LIGHTING);
            MekanismRenderer.blendOn();

            MekanismRenderer.bindTexture(MekanismRenderer.getBlocksTexture());
            GL11.glTranslated(-0.5, -0.5, -0.5);

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

            GL11.glPopAttrib();
            MekanismRenderer.blendOff();
            GlStateManager.popMatrix();
        }

        GlStateManager.translate(0F, -0.9F, 0F);
        GlStateManager.scale(0.9F, 0.8F, 0.9F);
        MekanismRenderer.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "FluidTank.png"));
        fluidTank.render(0.073F, tier);
        GlStateManager.popMatrix();
    }

    private static DisplayInteger[] getListAndRender(Fluid fluid) {
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