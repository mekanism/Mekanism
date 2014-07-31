/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.mj;

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
	 * Can be overrided via {@link #reconfigure(double, double, double)}
	 *
	 * @return Maximal energy amount for this battery.
	 */
	double maxCapacity();

	/**
	 * Can be overrided via {@link #reconfigure(double, double, double)}
	 *
	 * @return Minimal energy amount for keep your machine in active state
	 */
	double minimumConsumption();

	/**
	 * Can be overrided via {@link #reconfigure(double, double, double)}
	 *
	 * @return Maximal energy received per one tick
	 */
	double maxReceivedPerCycle();

	/**
	 * Allow to dynamically reconfigure your battery.
	 * Usually it's not very good change battery parameters for already present machines, but if you want...
	 *
	 * @param maxCapacity         {@link #maxCapacity()}
	 * @param maxReceivedPerCycle {@link #maxReceivedPerCycle()}
	 * @param minimumConsumption  {@link #minimumConsumption()}
	 * @return Current battery object instance
	 */
	IBatteryObject reconfigure(double maxCapacity, double maxReceivedPerCycle, double minimumConsumption);

	/**
	 * @return kind of this energy battery
	 */
	String kind();
}
