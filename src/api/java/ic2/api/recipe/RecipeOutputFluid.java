package ic2.api.recipe;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.fluids.FluidStack;

/**
 * @author estebes
 */
public class RecipeOutputFluid {
	public RecipeOutputFluid(NBTTagCompound metadata, List<FluidStack> outputs) {
		assert !outputs.contains(null);

		this.metadata = metadata;
		this.outputs = outputs;
	}

	public RecipeOutputFluid(NBTTagCompound metadata, FluidStack... outputs) {
		this(metadata, Arrays.asList(outputs));
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof RecipeOutputFluid) {
			RecipeOutputFluid ro = (RecipeOutputFluid) obj;

			if (outputs.size() == ro.outputs.size() &&
					(metadata == null && ro.metadata == null || metadata != null && ro.metadata != null && metadata.equals(ro.metadata))) {
				Iterator<FluidStack> itA = outputs.iterator();
				Iterator<FluidStack> itB = ro.outputs.iterator();

				while (itA.hasNext() && itB.hasNext()) {
					FluidStack stackA = itA.next();
					FluidStack stackB = itB.next();

					if (stackA.isFluidStackIdentical(stackB)) return false;
				}

				return true;
			}
		}

		return false;
	}

	@Override
	public String toString() {
		return "ROutput<"+outputs+","+metadata+">";
	}

	public final List<FluidStack> outputs;
	public final NBTTagCompound metadata;
}
