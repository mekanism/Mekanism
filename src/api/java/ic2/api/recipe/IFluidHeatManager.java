package ic2.api.recipe;

import java.util.Map;

import net.minecraftforge.fluids.Fluid;


public interface IFluidHeatManager extends ILiquidAcceptManager {
	/**
	 * Add a new fluid to the Fluid Heat Generator.
	 * 
	 * @param fluidName the fluid to burn
	 * @param amount amount of fluid to consume per tick
	 * @param heat amount of heat generated per tick
	 */
	void addFluid(String fluidName, int amount, int heat);

	BurnProperty getBurnProperty(Fluid fluid);

	Map<String, BurnProperty> getBurnProperties();


	public static class BurnProperty {
		public BurnProperty(int amount1, int heat1) {
			this.amount = amount1;
			this.heat = heat1;
		}

		public final int amount;
		public final int heat;
	}
}
