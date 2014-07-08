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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.core.BCLog;
import buildcraft.api.core.JavaTools;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * The class MjAPI provides services to the Minecraft Joules power framework.
 * BuildCraft implements a default power model on top of this, the "kinesis"
 * power model. Third party mods may provide they own version of Minecraft
 * Joules batteries and provide different models.
 */
public final class MjAPI {

	public static final String DEFAULT_POWER_FRAMEWORK = "buildcraft.kinesis";
	private static Map<BatteryHolder, BatteryField> mjBatteryFields = new HashMap<BatteryHolder, BatteryField>();
	private static Map<String, Class<? extends IBatteryObject>> mjBatteryKinds = new HashMap<String, Class<? extends IBatteryObject>>();
	private static final BatteryField invalidBatteryField = new BatteryField();
	private static final MjReconfigurator reconfigurator = new MjReconfigurator();
	private static final Map<Object, BatteryCache> mjBatteryCache = new WeakHashMap<Object, BatteryCache>();

	/**
	 * Deactivate constructor
	 */
	private MjAPI() {
	}

	/**
	 * @see #getMjBattery(Object, String, ForgeDirection)
	 */
	public static IBatteryObject getMjBattery(Object o) {
		return getMjBattery(o, null, null);
	}

	/**
	 * @see #getMjBattery(Object, String, ForgeDirection)
	 */
	public static IBatteryObject getMjBattery(Object o, String kind) {
		return getMjBattery(o, kind, null);
	}

	/**
	 * @see #getMjBattery(Object, String, ForgeDirection)
	 */
	public static IBatteryObject getMjBattery(Object o, ForgeDirection side) {
		return getMjBattery(o, null, side);
	}

	/**
	 * Returns the battery related to the object given in parameter.
	 */
	public static IBatteryObject getMjBattery(Object o, String kindRaw, ForgeDirection sideRaw) {
		if (o == null) {
			return null;
		}
		String kind = kindRaw == null ? DEFAULT_POWER_FRAMEWORK : kindRaw;
		ForgeDirection side = sideRaw == null ? ForgeDirection.UNKNOWN : sideRaw;

		IBatteryObject battery;
		BatteryCache cache = mjBatteryCache.get(o);
		if (cache == null) {
			cache = new BatteryCache();
			mjBatteryCache.put(o, cache);
		} else {
			battery = cache.get(kind, side);
			if (isCacheable(battery)) {
				return battery;
			}
		}
		if (o instanceof ISidedBatteryProvider) {
			battery = ((ISidedBatteryProvider) o).getMjBattery(kind, side);
			if (battery == null && side != ForgeDirection.UNKNOWN) {
				battery = ((ISidedBatteryProvider) o).getMjBattery(kind, ForgeDirection.UNKNOWN);
			}
		} else if (o instanceof IBatteryProvider) {
			battery = ((IBatteryProvider) o).getMjBattery(kind);
		} else {
			battery = createBattery(o, kind, side);
		}
		if (battery == null && o instanceof IPowerReceptor) {
			PowerHandler.PowerReceiver receiver = ((IPowerReceptor) o).getPowerReceiver(side);
			if (receiver == null && side != ForgeDirection.UNKNOWN) {
				receiver = ((IPowerReceptor) o).getPowerReceiver(ForgeDirection.UNKNOWN);
			}
			if (receiver != null) {
				battery = receiver.getMjBattery();
			}
		}
		cache.put(kind, side, battery);
		return battery;
	}

	/**
	 * Create new battery instance.
	 * This method ignore all providers/caches and only create battery for given kind/side.
	 *
	 * @param o Object which contains {@link MjBattery}
	 * @param kind Kind of power
	 * @param side Side of block
	 * @return New {@link IBatteryObject} implementation registered for given kind of power
	 */
	public static IBatteryObject createBattery(Object o, String kind, ForgeDirection side) {
		if (o == null) {
			return null;
		}
		BatteryField f = getMjBatteryField(o.getClass(), kind, side);
		if (f == null && side != ForgeDirection.UNKNOWN) {
			f = getMjBatteryField(o.getClass(), kind, ForgeDirection.UNKNOWN);
		}
		if (f == null) {
			return null;
		} else if (!mjBatteryKinds.containsKey(kind)) {
			return null;
		} else if (f.kind == BatteryKind.Value) {
			try {
				IBatteryObject obj = mjBatteryKinds.get(kind).newInstance();
				obj.init(o, f.field, f.battery);
				return obj;
			} catch (InstantiationException e) {
				BCLog.logger.log(Level.WARNING, "can't instantiate class for energy kind \"" + kind + "\"");
				return null;
			} catch (IllegalAccessException e) {
				BCLog.logger.log(Level.WARNING, "can't instantiate class for energy kind \"" + kind + "\"");
				return null;
			}
		} else {
			try {
				return createBattery(f.field.get(o), kind, side);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				return null;
			}
		}
	}

	/**
	 * @return All non-sided batteries for passed object
	 */
	public static IBatteryObject[] getAllMjBatteries(Object o) {
		return getAllMjBatteries(o, ForgeDirection.UNKNOWN);
	}

	/**
	 * @param direction Side of block
	 * @return All sided batteries for passed object
	 */
	public static IBatteryObject[] getAllMjBatteries(Object o, ForgeDirection direction) {
		IBatteryObject[] result = new IBatteryObject[mjBatteryFields.size()];

		int id = 0;

		for (String kind : mjBatteryKinds.keySet()) {
			result[id] = getMjBattery(o, kind, direction);
			if (result[id] != null) {
				id++;
			}
		}

		return Arrays.copyOfRange(result, 0, id);
	}

	/**
	 * Register new battery kind implementation.
	 * Allowing to have a custom power types alongside default "kinesis" kind
	 * @param kind Kind name
	 * @param clazz Battery implementation class
	 */
	public static void registerMJBatteryKind(String kind, Class<? extends IBatteryObject> clazz) {
		if (!mjBatteryKinds.containsKey(kind)) {
			mjBatteryKinds.put(kind, clazz);
		} else {
			BCLog.logger.log(Level.WARNING,
					"energy kind \"" + kind + "\" already registered with " + clazz.getCanonicalName());
		}
	}

	/**
	 * @see IOMode#canReceive
	 */
	public static boolean canReceive(IBatteryObject battery) {
		return battery != null && (!(battery instanceof IBatteryIOObject) || ((IBatteryIOObject) battery).canReceive());
	}

	/**
	 * @see IOMode#canSend
	 */
	public static boolean canSend(IBatteryObject battery) {
		return battery != null && battery instanceof IBatteryIOObject && ((IBatteryIOObject) battery).canSend();
	}

	/**
	 * @see IOMode#active
	 */
	public static boolean isActive(IBatteryObject battery) {
		return battery != null && battery instanceof IBatteryIOObject && ((IBatteryIOObject) battery).isActive();
	}

	/**
	 * @see MjBattery#cacheable()
	 */
	public static boolean isCacheable(IBatteryObject battery) {
		return battery != null && battery instanceof IBatteryIOObject && ((IBatteryIOObject) battery).isCacheable();
	}

	/**
	 * @see MjBattery#maxReceivedPerCycle()
	 * @see MjBattery#maxSendedPerCycle()
	 * @return Actual IO limit for passed mode (only send/receive supported)
	 */
	public static double getIOLimit(IBatteryObject batteryObject, IOMode mode) {
		if (mode == IOMode.Receive && canReceive(batteryObject)) {
			return batteryObject.maxReceivedPerCycle();
		} else if (mode == IOMode.Send && canSend(batteryObject)) {
			return ((IBatteryIOObject) batteryObject).maxSendedPerCycle();
		}
		return 0;
	}

	/**
	 * Obtain battery parameters reconfigurator.
	 * Usage:<br />
	 * <code>
	 *     MjAPI.reconfigure().maxCapacity(battery, 15000);
	 * </code>
	 * @return Reconfigurator instance
	 */
	public static MjReconfigurator reconfigure() {
		return reconfigurator;
	}

	/**
	 * Transfer mj energy amount from battery "fromBattery" to "toBattery"
	 * @param fromBattery Source battery
	 * @param toBattery Target battery
	 * @param mj Amount of energy
	 * @return Transferred amount
	 */
	public static double transferEnergy(IBatteryObject fromBattery, IBatteryObject toBattery, double mj) {
		if (!canSend(fromBattery) || !canReceive(toBattery)) {
			return 0;
		}
		IBatteryIOObject from = (IBatteryIOObject) fromBattery;
		double attemptToTransfer = Math.min(getIOLimit(from, IOMode.Send), mj);
		attemptToTransfer = Math.min(attemptToTransfer, getIOLimit(toBattery, IOMode.Receive));
		double extracted = from.extractEnergy(attemptToTransfer);
		double received = toBattery.addEnergy(extracted);
		if (extracted > received) {
			from.addEnergy(extracted - received);
		}
		return received;
	}

	/**
	 * Transfer maximal energy amount from battery "fromBattery" to "toBattery"
	 * @param fromBattery Source battery
	 * @param toBattery Target battery
	 * @return Transferred amount
	 */
	public static double transferEnergy(IBatteryObject fromBattery, IBatteryObject toBattery) {
		return transferEnergy(fromBattery, toBattery, Math.min(
				getIOLimit(fromBattery, IOMode.Send),
				getIOLimit(toBattery, IOMode.Receive)));
	}

	/**
	 * Helper method which you should invoke in every game tick for supporting Active IO modes
	 * @param tile Tile which contains active battery
	 */
	public static void updateEntity(TileEntity tile) {
		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
			IBatteryObject batteryObject = getMjBattery(tile, direction);
			TileEntity anotherTile = tile.getWorldObj().getTileEntity(tile.xCoord + direction.offsetX, tile.yCoord + direction.offsetY, tile.zCoord + direction.offsetZ);
			IBatteryObject anotherBattery = getMjBattery(anotherTile, direction.getOpposite());
			if (batteryObject == null || anotherBattery == null) {
				continue;
			}
			if (canSend(batteryObject) && canReceive(anotherBattery) && isActive(batteryObject)) {
				transferEnergy(batteryObject, anotherBattery);
			}
			if (canReceive(batteryObject) && canSend(anotherBattery) && isActive(anotherBattery) && !isActive(batteryObject)) {
				transferEnergy(anotherBattery, batteryObject);
			}
		}
	}

	/**
	 * Reset all caches for passed tile
	 */
	public static void resetBatteriesCache(TileEntity tile) {
		mjBatteryCache.remove(tile);
	}

	/**
	 * Remove cached instance of passed battery
	 */
	public static void resetBatteriesCache(IBatteryObject battery) {
		for (BatteryCache cache : mjBatteryCache.values()) {
			cache.reset(battery);
		}
	}

	private enum BatteryKind {
		Value, Container
	}

	private static final class BatteryCache {
		TIntObjectMap<IBatteryObject> cache = new TIntObjectHashMap<IBatteryObject>();

		IBatteryObject get(String kind, ForgeDirection side) {
			return cache.get(hash(kind, side));
		}

		void put(String kind, ForgeDirection side, IBatteryObject battery) {
			cache.put(hash(kind, side), battery);
		}

		void reset(IBatteryObject battery) {
			for (int key : cache.keys()) {
				if (cache.get(key) == battery) {
					cache.remove(key);
				}
			}
		}

		private int hash(String kind, ForgeDirection side) {
			return kind.hashCode() * 31 + side.hashCode();
		}
	}

	private static final class BatteryHolder {
		private String kind;
		private ForgeDirection side;
		private Class<? extends Object> clazz;

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}

			BatteryHolder that = (BatteryHolder) o;

			return kind.equals(that.kind) && clazz.equals(that.clazz) && side.equals(that.side);
		}

		@Override
		public int hashCode() {
			int result = kind.hashCode();
			result = 31 * result + clazz.hashCode();
			result = 31 * result + side.hashCode();
			return result;
		}
	}

	private static class BatteryField {
		public Field field;
		public MjBattery battery;
		public BatteryKind kind;
	}

	private static BatteryField getMjBatteryField(Class<?> c, String kind, ForgeDirection side) {
		BatteryHolder holder = new BatteryHolder();
		holder.clazz = c;
		holder.kind = kind;
		holder.side = side;

		BatteryField bField = mjBatteryFields.get(holder);

		if (bField == null) {
			for (Field f : JavaTools.getAllFields(c)) {
				MjBattery battery = f.getAnnotation(MjBattery.class);

				if (battery != null && kind.equals(battery.kind())) {
					if (!contains(battery.sides(), side) && !contains(battery.sides(), ForgeDirection.UNKNOWN)) {
						continue;
					}
					f.setAccessible(true);
					bField = new BatteryField();
					bField.field = f;
					bField.battery = battery;

					if (double.class.equals(f.getType())) {
						bField.kind = BatteryKind.Value;
					} else if (f.getType().isPrimitive()) {
						throw new RuntimeException(
								"MJ battery needs to be object or double type");
					} else {
						bField.kind = BatteryKind.Container;
					}

					mjBatteryFields.put(holder, bField);

					return bField;
				}
			}
			mjBatteryFields.put(holder, invalidBatteryField);
		}

		return bField == invalidBatteryField ? null : bField;
	}

	private static <T> boolean contains(T[] array, T value) {
		for (T t : array) {
			if (t == value) {
				return true;
			}
		}
		return false;
	}

	static {
		registerMJBatteryKind(DEFAULT_POWER_FRAMEWORK, BatteryObject.class);
	}
}
