/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.fuels;

import java.util.Collection;

import net.minecraftforge.fluids.Fluid;

import buildcraft.api.core.StackKey;

public interface ICoolantManager {
	ICoolant addCoolant(ICoolant coolant);

	ICoolant addCoolant(Fluid fluid, float degreesCoolingPerMB);

	ISolidCoolant addSolidCoolant(ISolidCoolant solidCoolant);

	ISolidCoolant addSolidCoolant(StackKey solid, StackKey liquid, float multiplier);

	Collection<ICoolant> getCoolants();

	Collection<ISolidCoolant> getSolidCoolants();

	ICoolant getCoolant(Fluid fluid);

	ISolidCoolant getSolidCoolant(StackKey solid);
}
