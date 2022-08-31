package mekanism.client.render.item.block;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.client.model.ModelFluidTank;
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
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public class RenderFluidTankItem extends MekanismISTER {

    public static final RenderFluidTankItem RENDERER = new RenderFluidTankItem();

    private ModelFluidTank modelFluidTank;

    @Override
    public void onResourceManagerReload(@NotNull ResourceManager resourceManager) {
        modelFluidTank = new ModelFluidTank(getEntityModels());
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
        matrix.pushPose();
        //TODO: Eventually move more of this to the model json
        matrix.translate(0.5, -0.4, 0.5);
        matrix.scale(0.9F, 0.8F, 0.9F);
        //Scale to the size of item
        matrix.scale(1.168F, 1.168F, 1.168F);
        //Shift the fluid slightly so that is visible with the min amount in
        matrix.translate(0, -0.06, 0);
        modelFluidTank.render(matrix, renderer, light, overlayLight, tier, stack.hasFoil());
        matrix.popPose();
    }
}