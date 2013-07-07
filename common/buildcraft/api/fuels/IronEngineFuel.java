/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.api.fuels;

import java.util.LinkedList;

import net.minecraftforge.liquids.LiquidContainerRegistry;
import net.minecraftforge.liquids.LiquidStack;

public class IronEngineFuel {

	public static LinkedList<IronEngineFuel> fuels = new LinkedList<IronEngineFuel>();

	public static IronEngineFuel getFuelForLiquid(LiquidStack liquid) {
		if (liquid == null)
			return null;
		if (liquid.itemID <= 0)
			return null;

		for (IronEngineFuel fuel : fuels)
			if (fuel.liquid.isLiquidEqual(liquid))
				return fuel;

		return null;
	}

	public final LiquidStack liquid;
	public final float powerPerCycle;
	public final int totalBurningTime;

	public IronEngineFuel(int liquidId, float powerPerCycle, int totalBurningTime) {
		this(new LiquidStack(liquidId, LiquidContainerRegistry.BUCKET_VOLUME, 0), powerPerCycle, totalBurningTime);
	}

	public IronEngineFuel(LiquidStack liquid, float powerPerCycle, int totalBurningTime) {
		this.liquid = liquid;
		this.powerPerCycle = powerPerCycle;
		this.totalBurningTime = totalBurningTime;
	}
}
