package ic2.api.recipe;

import java.util.Collection;

import net.minecraft.item.ItemStack;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import ic2.api.recipe.IEmptyFluidContainerRecipeManager.Output;
import ic2.api.util.FluidContainerOutputMode;

public interface IEmptyFluidContainerRecipeManager extends IMachineRecipeManager<Void, Output, ItemStack> {
	MachineRecipeResult<Void, Output, ItemStack> apply(ItemStack input, Fluid requiredFluid, FluidContainerOutputMode outputMode, boolean acceptTest);

	public static class Output {
		public Output(Collection<ItemStack> container, FluidStack fluid) {
			this.container = container;
			this.fluid = fluid;
		}

		public final Collection<ItemStack> container;
		public final FluidStack fluid;
	}
}
