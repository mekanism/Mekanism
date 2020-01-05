package ic2.api.recipe;

import java.util.Map;

import net.minecraftforge.fluids.Fluid;

public interface ILiquidHeatExchangerManager extends ILiquidAcceptManager {

	/**
	 * Add a new fluid heatup/cooldown recipe.
	 *
	 * @param fluidName the fluid to heat up/cool down
	 * @param fluidOutput the fluid the above fluid turns into
	 * @param huPerMB the Thermal Energy difference in hU for the conversion of one mB fluid
	 */
	void addFluid(String fluidName, String fluidOutput, int huPerMB);

	HeatExchangeProperty getHeatExchangeProperty(Fluid fluid);

	Map<String, HeatExchangeProperty> getHeatExchangeProperties();

	/**
	 * This returns an ILiquidAcceptManager that only accepts fluids, that can be heated up / cooled down, but not both.
	 * You can basically use this to check if the liquid resulting from this conversion can be reprocessed into the first one.
	 * @return Returns the SingleDirectionManager.
	 */
	ILiquidAcceptManager getSingleDirectionLiquidManager();

	public static class HeatExchangeProperty {
		public HeatExchangeProperty(Fluid outputFluid, int huPerMB) {
			this.outputFluid = outputFluid;
			this.huPerMB = huPerMB;
		}

		public final Fluid outputFluid;
		public final int huPerMB;
	}

}
