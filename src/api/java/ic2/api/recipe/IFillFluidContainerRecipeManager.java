package ic2.api.recipe;

import java.util.Collection;

import net.minecraft.item.ItemStack;

import net.minecraftforge.fluids.FluidStack;

import ic2.api.recipe.IFillFluidContainerRecipeManager.Input;
import ic2.api.util.FluidContainerOutputMode;

public interface IFillFluidContainerRecipeManager extends IMachineRecipeManager<Void, Collection<ItemStack>, Input> {
	MachineRecipeResult<Void, Collection<ItemStack>, Input> apply(Input input, FluidContainerOutputMode outputMode, boolean acceptTest);

	public static class Input {
		public Input(ItemStack container, FluidStack fluid) {
			this.container = container;
			this.fluid = fluid;
		}

		public final ItemStack container;
		public final FluidStack fluid;
	}
}
