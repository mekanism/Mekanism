package ic2.api.recipe;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.util.EnumFacing;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

/**
 * Electrolyzer recipe manager, similar to {@link ISemiFluidFuelManager} and {@link IFermenterRecipeManager},
 * but more sided to fit how the Electrolyzer now works
 *
 * @author Chocohead
 */
public interface IElectrolyzerRecipeManager extends ILiquidAcceptManager {
	/**
	 * Add a new recipe to the electrolyzer.
	 *
	 * @param input The name of the fluid being electrolyzed
	 * @param inputAmount The amount of input fluid used per operation (in millibuckets)
	 * @param EUaTick The EU used per tick
	 * @param outputs The output fluids, with a given direction for the tank to fill
	 */
	void addRecipe(@Nonnull String input, int inputAmount, int EUaTick, @Nonnull ElectrolyzerOutput... outputs);

	/**
	 * Add a new recipe to the electrolyzer.
	 *
	 * @param input The name of the fluid being electrolyzed
	 * @param inputAmount The amount of input fluid used per operation (in millibuckets)
	 * @param EUaTick The EU used per tick
	 * @param ticksNeeded The number of ticks each operation takes, will default to 200 if not given
	 * @param outputs The output fluids, with a given direction for the tank to fill
	 */
	void addRecipe(@Nonnull String input, int inputAmount, int EUaTick, int ticksNeeded, @Nonnull ElectrolyzerOutput... outputs);

	/**
	 * Get an electrolyzer recipe for the given fluid
	 *
	 * @param Input fluid
	 * @return The found recipe or null if no recipe is found
	 */
	ElectrolyzerRecipe getElectrolysisInformation(Fluid fluid);

	/**
	 * Get the {@link ElectrolyzerOutput}s for the given input fluid
	 *
	 * @param Input fluid
	 * @return Outputs and tank directions
	 */
	ElectrolyzerOutput[] getOutput(Fluid input);

	/**
	 * Gets the whole current fluid mappings
	 * @return The current fluid map
	 */
	Map<String, ElectrolyzerRecipe> getRecipeMap();

	@ParametersAreNonnullByDefault
	public static final class ElectrolyzerOutput {
		/**
		 * @param fluidName The name of the output fluid
		 * @param fluidAmount The amount of the fluid produced per operation (in millibuckets)
		 * @param tankDirection The direction relative to the electrolyzer that the output tank should be
		 */
		public ElectrolyzerOutput(String fluidName, int fluidAmount, EnumFacing tankDirection) {
			this.fluidName = fluidName;
			this.fluidAmount = fluidAmount;
			this.tankDirection = tankDirection;
		}

		public FluidStack getOutput() {
			return FluidRegistry.getFluid(fluidName) == null ? null : new FluidStack(FluidRegistry.getFluid(this.fluidName), this.fluidAmount);
		}

		public Pair<FluidStack, EnumFacing> getFullOutput() {
			return Pair.of(getOutput(), tankDirection);
		}

		public final String fluidName;
		public final int fluidAmount;
		public final EnumFacing tankDirection;
	}

	public static final class ElectrolyzerRecipe {
		public ElectrolyzerRecipe(int inputAmount, int EUaTick, int ticksNeeded, ElectrolyzerOutput... outputs) {
			this.inputAmount = inputAmount;
			this.EUaTick = EUaTick;
			this.ticksNeeded = ticksNeeded;
			this.outputs = validateOutputs(outputs);
		}

		private ElectrolyzerOutput[] validateOutputs(ElectrolyzerOutput[] outputs) {
			if (outputs.length < 1 || outputs.length > 5) {
				throw new RuntimeException("Cannot have "+outputs.length+" outputs of an Electrolzer recipe, must be between 1 and 5");
			}
			Set<EnumFacing> directions = new HashSet<EnumFacing>(outputs.length * 2, 0.5F);
			for (ElectrolyzerOutput output : outputs) {
				if (!directions.add(output.tankDirection)) {
					throw new RuntimeException("Duplicate direction in Electrolzer outputs ("+output.tankDirection+')');
				}
			}
			return outputs;
		}

		public final int inputAmount;
		public final int EUaTick;
		public final int ticksNeeded;
		public final ElectrolyzerOutput[] outputs;
	}
}