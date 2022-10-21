package mekanism.client.render.item.block;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.RenderResizableCuboid.FaceDisplay;
import mekanism.client.render.item.MekanismISTER;
import mekanism.client.render.tileentity.RenderFluidTank;
import mekanism.common.item.block.machine.ItemBlockFluidTank;
import mekanism.common.tier.FluidTankTier;
import mekanism.common.util.StorageUtils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public class RenderFluidTankItem extends MekanismISTER {

    public static final RenderFluidTankItem RENDERER = new RenderFluidTankItem();

    @Override
    public void onResourceManagerReload(@NotNull ResourceManager resourceManager) {
        //Note: We don't need to register this as a reload listener as we don't have an in code model or make use of this
        // reload in any way
    }

    @Override
    public void renderByItem(@NotNull ItemStack stack, @NotNull TransformType transformType, @NotNull PoseStack matrix, @NotNull MultiBufferSource renderer,
          int light, int overlayLight) {
        FluidTankTier tier = ((ItemBlockFluidTank) stack.getItem()).getTier();
        FluidStack fluid = StorageUtils.getStoredFluidFromNBT(stack);
        if (!fluid.isEmpty()) {
            float fluidScale = (float) fluid.getAmount() / tier.getStorage();
            if (fluidScale > 0) {
                MekanismRenderer.renderObject(RenderFluidTank.getFluidModel(fluid, fluidScale), matrix, renderer.getBuffer(Sheets.translucentCullBlockSheet()),
                      MekanismRenderer.getColorARGB(fluid, fluidScale), MekanismRenderer.calculateGlowLight(light, fluid), overlayLight, FaceDisplay.FRONT, getCamera());
            }
        }
        renderBlockItem(stack, transformType, matrix, renderer, light, overlayLight, ModelData.EMPTY);
    }
}