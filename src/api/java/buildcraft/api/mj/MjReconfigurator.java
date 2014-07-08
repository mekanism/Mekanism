/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.mj;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.core.BCLog;

/**
 * Reconfiguration helper.
 * Allow to change battery parameters in runtime.
 */
public class MjReconfigurator {
	private static final class ConfigurableMjBattery implements MjBattery {
		double maxCapacity, maxReceivedPerCycle, maxSendedPerCycle, minimumConsumption;
		String kind;
		ForgeDirection[] sides;
		IOMode mode;
		boolean cacheable;

		@Override
		public double maxCapacity() {
			return maxCapacity;
		}

		@Override
		public double maxReceivedPerCycle() {
			return maxReceivedPerCycle;
		}

		@Override
		public double minimumConsumption() {
			return minimumConsumption;
		}

		@Override
		public double maxSendedPerCycle() {
			return maxSendedPerCycle;
		}

		@Override
		public String kind() {
			return kind;
		}

		@Override
		public ForgeDirection[] sides() {
			return sides;
		}

		@Override
		public IOMode mode() {
			return mode;
		}

		@Override
		public boolean cacheable() {
			return cacheable;
		}

		@Override
		public Class<? extends Annotation> annotationType() {
			return MjBattery.class;
		}
	}

	/**
	 * Helper interface which should implement all configurable batteries.
	 */
	public interface IConfigurableBatteryObject extends IBatteryObject {
		MjBattery getMjBattery();

		void setMjBattery(MjBattery battery);
	}

	private ConfigurableMjBattery obtainConfigurableBattery(IBatteryObject battery) {
		if (!(battery instanceof IConfigurableBatteryObject)) {
			BCLog.logger.warning("Attempt to reconfigure unsupported battery: " + battery);
			return null;
		}
		IConfigurableBatteryObject configurableBattery = (IConfigurableBatteryObject) battery;
		MjBattery mjBattery = configurableBattery.getMjBattery();
		if (mjBattery instanceof ConfigurableMjBattery) {
			return (ConfigurableMjBattery) mjBattery;
		}
		ConfigurableMjBattery configurableMjBattery = new ConfigurableMjBattery();
		configurableMjBattery.maxCapacity = mjBattery.maxCapacity();
		configurableMjBattery.maxReceivedPerCycle = mjBattery.maxReceivedPerCycle();
		configurableMjBattery.maxSendedPerCycle = mjBattery.maxSendedPerCycle();
		configurableMjBattery.minimumConsumption = mjBattery.minimumConsumption();
		configurableMjBattery.kind = mjBattery.kind();
		configurableMjBattery.sides = mjBattery.sides();
		configurableMjBattery.mode = mjBattery.mode();
		configurableMjBattery.cacheable = mjBattery.cacheable();
		configurableBattery.setMjBattery(configurableMjBattery);
		return configurableMjBattery;
	}

	public void maxCapacity(IBatteryObject batteryObject, double maxCapacity) {
		ConfigurableMjBattery battery = obtainConfigurableBattery(batteryObject);
		if (battery != null) {
			battery.maxCapacity = maxCapacity;
		}
	}

	public void maxReceivedPerCycle(IBatteryObject batteryObject, double maxReceivedPerCycle) {
		ConfigurableMjBattery battery = obtainConfigurableBattery(batteryObject);
		if (battery != null) {
			battery.maxReceivedPerCycle = maxReceivedPerCycle;
		}
	}

	public void maxSendedPerCycle(IBatteryObject batteryObject, double maxSendedPerCycle) {
		ConfigurableMjBattery battery = obtainConfigurableBattery(batteryObject);
		if (battery != null) {
			battery.maxSendedPerCycle = maxSendedPerCycle;
		}
	}

	public void minimumConsumption(IBatteryObject batteryObject, double minimumConsumption) {
		ConfigurableMjBattery battery = obtainConfigurableBattery(batteryObject);
		if (battery != null) {
			battery.minimumConsumption = minimumConsumption;
		}
	}

	public void kind(IBatteryObject batteryObject, String kind) {
		ConfigurableMjBattery battery = obtainConfigurableBattery(batteryObject);
		if (battery != null) {
			battery.kind = kind;
			MjAPI.resetBatteriesCache(batteryObject);
		}
	}

	/**
	 * Reconfigure passed battery instance for working with passed sides only
	 * @param sides Enabled sides
	 */
	public void sides(IBatteryObject batteryObject, ForgeDirection... sides) {
		ConfigurableMjBattery battery = obtainConfigurableBattery(batteryObject);
		if (battery != null) {
			battery.sides = sides;
			MjAPI.resetBatteriesCache(batteryObject);
		}
	}

	/**
	 * Reconfigure passed battery instance for working with all sides exclude passed
	 * @param sides Disabled sides
	 */
	public void sidesExclude(IBatteryObject batteryObject, ForgeDirection... sides) {
		List<ForgeDirection> newSides = new ArrayList<ForgeDirection>(Arrays.asList(ForgeDirection.VALID_DIRECTIONS));
		for (ForgeDirection side : sides) {
			newSides.remove(side);
		}
		sides(batteryObject, newSides.toArray(new ForgeDirection[newSides.size()]));
	}

	public void mode(IBatteryObject batteryObject, IOMode mode) {
		ConfigurableMjBattery battery = obtainConfigurableBattery(batteryObject);
		if (battery != null) {
			battery.mode = mode;
		}
	}

	public void cacheable(IBatteryObject batteryObject, boolean cacheable) {
		ConfigurableMjBattery battery = obtainConfigurableBattery(batteryObject);
		if (battery != null) {
			battery.cacheable = cacheable;
		}
	}
}
