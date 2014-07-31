/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.mj;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.minecraftforge.common.util.ForgeDirection;

/**
 * This annotation is used for tiles that need to interface with BuildCraft
 * energy framework, a.k.a MinecraftJoule or MJ. In order to receive power,
 * tiles, need to declare a double field, with the annotation MjBattery. MJ
 * provider machines able to provide power will then connect to these tiles, and
 * feed energy up to max capacity. It's the responsibility of the implementer to
 * manually decrease the value of the energy, as he simulates energy
 * consumption. On each cycle, per power input, machines can receive up to
 * "maxReceivedPerCycle" units of energy. As an optional behavior, the system
 * can have a minimum amount of energy consumed even if the system is at max
 * capacity, modelized by the "minimumConsumption" value.
 * 
 * If the field designated by MjBattery is an object, then it will be considered
 * as a nested battery, and will look for the field in the designated object.
 *
 * All the properties defined in this annotation are class wide. If you need to
 * change them on a tile by tile basis, you will need to use interfaces, either
 * {@link IBatteryProvider} or {@link ISidedBatteryProvider}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Inherited
public @interface MjBattery {
	/**
	 * @return Max energy capacity of battery
	 */
	double maxCapacity() default 100.0;

	/**
	 * @return Max energy received per one tick
	 */
	double maxReceivedPerCycle() default 10.0;

	/**
	 * @return Minimal energy for keep machine is active
	 */
	double minimumConsumption() default 0.1;

	/**
	 * @return The kind of battery stored. Specific power systems can be created
	 *         through this system, as several battery of different kind can
	 *         coexist in the same tile.
	 */
	String kind() default MjAPI.DEFAULT_POWER_FRAMEWORK;

	/**
	 * @return Sides on which this battery should works.
	 */
	ForgeDirection[] sides() default { ForgeDirection.UNKNOWN };

	/**
	 * @return Current battery input/output mode
	 */
	IOMode mode() default IOMode.Receive;
}