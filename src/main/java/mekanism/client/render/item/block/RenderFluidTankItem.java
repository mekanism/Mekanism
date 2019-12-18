package mekanism.client.render.item.block;

import javax.annotation.Nonnull;
import mekanism.client.model.ModelFluidTank;
import mekanism.client.render.FluidRenderMap;
import mekanism.client.render.MekanismRenderer.DisplayInteger;
import mekanism.client.render.item.ItemLayerWrapper;
import mekanism.client.render.item.MekanismItemStackRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.ItemStack;

public class RenderFluidTankItem extends MekanismItemStackRenderer {

    public static ItemLayerWrapper model;

    private static ModelFluidTank fluidTank = new ModelFluidTank();
    private static FluidRenderMap<DisplayInteger[]> cachedCenterFluids = new FluidRenderMap<>();
    private static int stages = 1400;

    @Override
    public void renderBlockSpecific(@Nonnull ItemStack stack, TransformType transformType) {
        //TODO: 1.15
        /*ItemBlockFluidTank itemFluidTank = (ItemBlockFluidTank) stack.getItem();
        FluidTankTier tier = itemFluidTank.getTier(stack);
        if (tier == null) {
            return;
        }
        FluidStack fluid = itemFluidTank.getFluidStack(stack);
        float fluidScale = (float) fluid.getAmount() / itemFluidTank.getCapacity(stack);

        RenderSystem.pushMatrix();
        if (!fluid.isEmpty() && fluidScale > 0) {
            RenderSystem.pushMatrix();
            RenderSystem.enableCull();
            RenderSystem.disableLighting();
            RenderSystem.shadeModel(GL11.GL_SMOOTH);
            RenderSystem.disableAlphaTest();
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);

            MekanismRenderer.bindTexture(PlayerContainer.field_226615_c_);
            RenderSystem.translatef(-0.5F, -0.5F, -0.5F);
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
            RenderSystem.disableBlend();
            RenderSystem.enableAlphaTest();
            RenderSystem.enableLighting();
            RenderSystem.disableCull();
            RenderSystem.popMatrix();
        }

        RenderSystem.translatef(0, -0.9F, 0);
        RenderSystem.scalef(0.9F, 0.8F, 0.9F);
        MekanismRenderer.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "fluid_tank.png"));
        fluidTank.render(0.073F, tier);
        RenderSystem.popMatrix();*/
    }

    //TODO: 1.15
    /*private static DisplayInteger[] getListAndRender(@Nonnull FluidStack fluid) {
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

    @Override
    protected void renderItemSpecific(@Nonnull ItemStack stack, TransformType transformType) {
    }

    @Nonnull
    @Override
    protected TransformType getTransform(@Nonnull ItemStack stack) {
        return model.getTransform();
    }
}