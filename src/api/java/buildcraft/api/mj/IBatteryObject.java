/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.mj;

import java.lang.reflect.Field;

public interface IBatteryObject {
	/**
	 * @return Current energy requirement for keeping machine state
	 */
	double getEnergyRequested();

	/**
	 * Add energy to this battery
	 *
	 * @param mj Energy amount
	 * @return Used energy
	 */
	double addEnergy(double mj);

	/**
	 * Add energy to this battery
	 *
	 * @param mj               Energy amount
	 * @param ignoreCycleLimit Force add all energy even if "maxReceivedPerCycle" limit is reached
	 * @return Used energy
	 */
	double addEnergy(double mj, boolean ignoreCycleLimit);

	/**
	 * @return Current stored energy amount in this battery
	 */
	double getEnergyStored();

	/**
	 * Set current stored energy amount.
	 * Doesn't use it for your machines! Decrease your battery field directly.
	 *
	 * @param mj New energy amount
	 */
	void setEnergyStored(double mj);

	/**
	 * @return Maximal energy amount for this battery.
	 */
	double maxCapacity();

	/**
	 * @return Minimal energy amount for keep your machine in active state
	 */
	double minimumConsumption();

	/**
	 * @return Maximal energy received per one tick
	 */
	double maxReceivedPerCycle();

	/**
	 * @return kind of this energy battery
	 */
	String kind();

	/**
	 * Basic initialization method
	 *
	 * @param object      Basic object which hold a battery field
	 * @param storedField Field for energy storing
	 * @param battery     Battery data
	 */
	void init(Object object, Field storedField, MjBattery battery);
}
