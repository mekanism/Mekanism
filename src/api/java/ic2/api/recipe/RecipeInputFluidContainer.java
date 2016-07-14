package ic2.api.recipe;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidContainerRegistry.FluidContainerData;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;

import ic2.api.item.IC2Items;

public class RecipeInputFluidContainer implements IRecipeInput {
	public RecipeInputFluidContainer(Fluid fluid) {
		this(fluid, Fluid.BUCKET_VOLUME);
	}

	public RecipeInputFluidContainer(Fluid fluid, int amount) {
		this.fluid = fluid;
		this.amount = amount;
	}

	@Override
	public boolean matches(ItemStack subject) {
		FluidStack fs = FluidContainerRegistry.getFluidForFilledItem(subject);

		if (fs == null && subject.getItem() instanceof IFluidContainerItem) {
			IFluidContainerItem item = (IFluidContainerItem)subject.getItem();
			fs = item.getFluid(subject);
		}

		// match amount precisely to avoid having to deal with leftover
		return fs == null && fluid == null ||
				fs != null && fs.getFluid() == fluid && fs.amount >= amount;
	}

	@Override
	public int getAmount() {
		return amount;
	}

	@Override
	public List<ItemStack> getInputs() {
		List<ItemStack> ret = new ArrayList<ItemStack>();

		for (FluidContainerData data : FluidContainerRegistry.getRegisteredFluidContainerData()) {
			if (data.fluid.getFluid() == fluid) ret.add(data.filledContainer);
		}
		ret.add(IC2Items.getItem("fluid_cell", fluid.getName()));

		return ret;
	}

	@Override
	public String toString() {
		return "RInputFluidContainer<"+amount+"x"+fluid.getName()+">";
	}

	public final Fluid fluid;
	public final int amount;
}
