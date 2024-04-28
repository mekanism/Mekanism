package mekanism.client.render.item.block;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.RenderResizableCuboid;
import mekanism.client.render.RenderResizableCuboid.FaceDisplay;
import mekanism.client.render.item.MekanismISTER;
import mekanism.client.render.tileentity.RenderFluidTank;
import mekanism.common.attachments.containers.ContainerType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public class RenderFluidTankItem extends MekanismISTER {

    public static final RenderFluidTankItem RENDERER = new RenderFluidTankItem();

    @Override
    public void onResourceManagerReload(@NotNull ResourceManager resourceManager) {
        //Note: We don't need to register this as a reload listener as we don't have an in code model or make use of this
        // reload in any way
    }

    @Override
    public void renderByItem(@NotNull ItemStack stack, @NotNull ItemDisplayContext displayContext, @NotNull PoseStack matrix, @NotNull MultiBufferSource renderer,
          int light, int overlayLight) {
        IMekanismFluidHandler attachment = ContainerType.FLUID.createHandler(stack);
        if (attachment != null) {
            FluidStack fluid = attachment.getFluidInTank(0);
            if (!fluid.isEmpty()) {
                float fluidScale = (float) fluid.getAmount() / attachment.getTankCapacity(0);
                if (fluidScale > 0) {
                    RenderResizableCuboid.renderCube(RenderFluidTank.getFluidModel(fluid, fluidScale), matrix, renderer.getBuffer(Sheets.translucentCullBlockSheet()), MekanismRenderer.getColorARGB(fluid, fluidScale), MekanismRenderer.calculateGlowLight(light, fluid), overlayLight, FaceDisplay.FRONT, getCamera(), null);
                }
            }
        }
        renderBlockItem(stack, displayContext, matrix, renderer, light, overlayLight, ModelData.EMPTY);
    }
}