package buildcraft.api.fuels;

import java.util.LinkedList;

import net.minecraftforge.liquids.LiquidStack;

public class IronEngineCoolant {

	public static LinkedList<IronEngineCoolant> coolants = new LinkedList<IronEngineCoolant>();

	public static IronEngineCoolant getCoolantForLiquid(LiquidStack liquid) {
		if (liquid == null)
			return null;
		if (liquid.itemID <= 0)
			return null;

		for (IronEngineCoolant coolant : coolants)
			if (coolant.liquid.isLiquidEqual(liquid))
				return coolant;

		return null;
	}

	public final LiquidStack liquid;
	public final float coolingPerUnit;

	public IronEngineCoolant(LiquidStack liquid, float coolingPerUnit) {
		this.liquid = liquid;
		this.coolingPerUnit = coolingPerUnit;
	}

}
