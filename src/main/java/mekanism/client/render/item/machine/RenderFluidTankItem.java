package mekanism.client.render.item.machine;

import javax.annotation.Nonnull;
import mekanism.client.render.tileentity.RenderFluidTank;
import mekanism.common.Tier.FluidTankTier;
import mekanism.common.item.ItemBlockMachine;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderFluidTankItem {

    public static void renderStack(@Nonnull ItemStack stack) {
        GlStateManager.pushMatrix();
        ItemBlockMachine itemMachine = (ItemBlockMachine) stack.getItem();
        float targetScale =
              (float) (itemMachine.getFluidStack(stack) != null ? itemMachine.getFluidStack(stack).amount : 0)
                    / itemMachine.getCapacity(stack);
        FluidTankTier tier = FluidTankTier.values()[itemMachine.getBaseTier(stack).ordinal()];
        Fluid fluid =
              itemMachine.getFluidStack(stack) != null ? itemMachine.getFluidStack(stack).getFluid() : null;
        RenderFluidTank.INSTANCE.render(tier, fluid, targetScale, false, null, -0.5, -0.5, -0.5);
        GlStateManager.popMatrix();
    }
}