package mekanism.client.render.item.machine;

import javax.annotation.Nonnull;
import mekanism.client.model.ModelFluidTank;
import mekanism.client.render.FluidRenderMap;
import mekanism.client.render.MekanismRenderHelper;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.DisplayInteger;
import mekanism.client.render.MekanismRenderer.FluidType;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.common.item.ItemBlockMachine;
import mekanism.common.tier.FluidTankTier;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderFluidTankItem {

    private static ModelFluidTank fluidTank = new ModelFluidTank();

    private static FluidRenderMap<DisplayInteger[]> cachedCenterFluids = new FluidRenderMap<>();

    private static int stages = 1400;

    public static void renderStack(@Nonnull ItemStack stack, TransformType transformType) {
        ItemBlockMachine itemMachine = (ItemBlockMachine) stack.getItem();
        float fluidScale = (float) (itemMachine.getFluidStack(stack) != null ? itemMachine.getFluidStack(stack).amount : 0) / itemMachine.getCapacity(stack);
        FluidTankTier tier = FluidTankTier.values()[itemMachine.getBaseTier(stack).ordinal()];
        FluidStack fluid = itemMachine.getFluidStack(stack);

        GlStateManager.pushMatrix();
        if (fluid != null && fluidScale > 0) {
            MekanismRenderHelper fluidRenderHelper = new MekanismRenderHelper(true).enableCull().disableLighting().enableBlendPreset();
            MekanismRenderer.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            GlStateManager.translate(-0.5, -0.5, -0.5);
            fluidRenderHelper.enableGlow(fluid);

            DisplayInteger[] displayList = getListAndRender(fluid);
            if (tier == FluidTankTier.CREATIVE) {
                fluidScale = 1;
            }

            fluidRenderHelper.color(fluid, fluidScale);
            if (fluid.getFluid().isGaseous(fluid)) {
                displayList[stages - 1].render();
            } else {
                displayList[Math.min(stages - 1, (int) (fluidScale * ((float) stages - 1)))].render();
            }
            fluidRenderHelper.cleanup();
        }

        GlStateManager.translate(0, -0.9F, 0);
        GlStateManager.scale(0.9F, 0.8F, 0.9F);
        MekanismRenderer.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "FluidTank.png"));
        fluidTank.render(0.073F, tier);
        GlStateManager.popMatrix();
    }

    private static DisplayInteger[] getListAndRender(FluidStack fluid) {
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