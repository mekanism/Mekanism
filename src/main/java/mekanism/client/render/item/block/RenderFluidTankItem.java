package mekanism.client.render.item.block;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import javax.annotation.Nonnull;
import mekanism.client.model.ModelFluidTank;
import mekanism.client.render.FluidRenderMap;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.DisplayInteger;
import mekanism.client.render.MekanismRenderer.FluidType;
import mekanism.client.render.MekanismRenderer.GlowInfo;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.client.render.item.ItemLayerWrapper;
import mekanism.client.render.item.MekanismItemStackRenderer;
import mekanism.common.item.block.machine.ItemBlockFluidTank;
import mekanism.common.tier.FluidTankTier;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;

@OnlyIn(Dist.CLIENT)
public class RenderFluidTankItem extends MekanismItemStackRenderer {

    public static ItemLayerWrapper model;

    private static ModelFluidTank fluidTank = new ModelFluidTank();
    private static FluidRenderMap<DisplayInteger[]> cachedCenterFluids = new FluidRenderMap<>();
    private static int stages = 1400;

    @Override
    public void renderBlockSpecific(@Nonnull ItemStack stack, TransformType transformType) {
        ItemBlockFluidTank itemFluidTank = (ItemBlockFluidTank) stack.getItem();
        FluidTankTier tier = itemFluidTank.getTier(stack);
        if (tier == null) {
            return;
        }
        FluidStack fluid = itemFluidTank.getFluidStack(stack);
        float fluidScale = (float) fluid.getAmount() / itemFluidTank.getCapacity(stack);

        GlStateManager.pushMatrix();
        if (!fluid.isEmpty() && fluidScale > 0) {
            GlStateManager.pushMatrix();
            GlStateManager.enableCull();
            GlStateManager.disableLighting();
            GlStateManager.shadeModel(GL11.GL_SMOOTH);
            GlStateManager.disableAlphaTest();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);

            MekanismRenderer.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
            GlStateManager.translatef(-0.5F, -0.5F, -0.5F);
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
            GlStateManager.disableBlend();
            GlStateManager.enableAlphaTest();
            GlStateManager.enableLighting();
            GlStateManager.disableCull();
            GlStateManager.popMatrix();
        }

        GlStateManager.translatef(0, -0.9F, 0);
        GlStateManager.scalef(0.9F, 0.8F, 0.9F);
        MekanismRenderer.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "fluid_tank.png"));
        fluidTank.render(0.073F, tier);
        GlStateManager.popMatrix();
    }

    private static DisplayInteger[] getListAndRender(@Nonnull FluidStack fluid) {
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

            if (fluid.getFluid().getAttributes().getStill(fluid) != null) {
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
    }

    @Override
    protected void renderItemSpecific(@Nonnull ItemStack stack, TransformType transformType) {
    }

    @Nonnull
    @Override
    protected TransformType getTransform(@Nonnull ItemStack stack) {
        return model.getTransform();
    }
}