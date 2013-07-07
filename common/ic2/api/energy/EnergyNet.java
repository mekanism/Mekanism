package ic2.api.energy;

import java.lang.reflect.Method;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import ic2.api.energy.tile.IEnergySource;

/**
 * Provides access to the energy network.
 */
public final class EnergyNet {
	/**
	 * Gets the EnergyNet instance for the specified world.
	 *
	 * @param world world
	 * @return EnergyNet instance for the world
	 */
	public static EnergyNet getForWorld(World world) {
		try {
			if (EnergyNet_getForWorld == null) EnergyNet_getForWorld = Class.forName(getPackage() + ".core.EnergyNet").getMethod("getForWorld", World.class);

			return new EnergyNet(EnergyNet_getForWorld.invoke(null, world));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private EnergyNet(Object energyNetInstance) {
		this.energyNetInstance = energyNetInstance;
	}

	/**
	 * Add a tile entity to the energy network.
	 * The tile entity has to be valid and initialized.
	 *
	 * @param addedTileEntity tile entity to add
	 *
	 * @deprecated use EnergyTileLoadEvent instead
	 */
	@Deprecated
	public void addTileEntity(TileEntity addedTileEntity) {
		try {
			if (EnergyNet_addTileEntity == null) EnergyNet_addTileEntity = Class.forName(getPackage() + ".core.EnergyNet").getMethod("addTileEntity", TileEntity.class);

			EnergyNet_addTileEntity.invoke(energyNetInstance, addedTileEntity);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Removes a tile entity from the energy network.
	 * The tile entity has to be still valid.
	 *
	 * @param removedTileEntity tile entity to remove
	 *
	 * @deprecated use EnergyTileUnloadEvent instead
	 */
	@Deprecated
	public void removeTileEntity(TileEntity removedTileEntity) {
		try {
			if (EnergyNet_removeTileEntity == null) EnergyNet_removeTileEntity = Class.forName(getPackage() + ".core.EnergyNet").getMethod("removeTileEntity", TileEntity.class);

			EnergyNet_removeTileEntity.invoke(energyNetInstance, removedTileEntity);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Emit energy from an energy source to the energy network.
	 *
	 * @param energySource energy source to emit energy from
	 * @param amount amount of energy to emit in EU
	 * @return Leftover (unused) power
	 *
	 * @deprecated use EnergyTileSourceEvent instead
	 */
	@Deprecated
	public int emitEnergyFrom(IEnergySource energySource, int amount) {
		try {
			if (EnergyNet_emitEnergyFrom == null) EnergyNet_emitEnergyFrom = Class.forName(getPackage() + ".core.EnergyNet").getMethod("emitEnergyFrom", IEnergySource.class, Integer.TYPE);

			return ((Integer) EnergyNet_emitEnergyFrom.invoke(energyNetInstance, energySource, amount)).intValue();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Get the amount of energy currently being conducted by a conductor.
	 * Call this twice with a delay to get the average conducted power by doing (call2 - call1) / 2.
	 *
	 * @param tileEntity conductor
	 *
	 * @deprecated use getTotalEnergyEmitted and getTotalEnergySunken instead
	 */
	@Deprecated
	public long getTotalEnergyConducted(TileEntity tileEntity) {
		try {
			if (EnergyNet_getTotalEnergyConducted == null) EnergyNet_getTotalEnergyConducted = Class.forName(getPackage() + ".core.EnergyNet").getMethod("getTotalEnergyConducted", TileEntity.class);

			return ((Long) EnergyNet_getTotalEnergyConducted.invoke(energyNetInstance, tileEntity)).longValue();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * determine how much energy has been emitted by the EnergyEmitter specified
	 *
	 * @note call this twice with x ticks delay to get the avg. emitted power p = (call2 - call1) / x EU/tick
	 *
	 * @param tileEntity energy emitter
	 */
	public long getTotalEnergyEmitted(TileEntity tileEntity) {
		try {
			if (EnergyNet_getTotalEnergyEmitted == null) EnergyNet_getTotalEnergyEmitted = Class.forName(getPackage() + ".core.EnergyNet").getMethod("getTotalEnergyEmitted", TileEntity.class);

			return ((Long) EnergyNet_getTotalEnergyEmitted.invoke(energyNetInstance, tileEntity)).longValue();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * determine how much energy has been sunken by the EnergySink specified
	 *
	 * @note call this twice with x ticks delay to get the avg. sunken power p = (call2 - call1) / x EU/tick
	 *
	 * @param tileEntity energy emitter
	 */
	public long getTotalEnergySunken(TileEntity tileEntity) {
		try {
			if (EnergyNet_getTotalEnergySunken == null) EnergyNet_getTotalEnergySunken = Class.forName(getPackage() + ".core.EnergyNet").getMethod("getTotalEnergySunken", TileEntity.class);

			return ((Long) EnergyNet_getTotalEnergySunken.invoke(energyNetInstance, tileEntity)).longValue();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Get the base IC2 package name, used internally.
	 *
	 * @return IC2 package name, if unable to be determined defaults to ic2
	 */
	private static String getPackage() {
		Package pkg = EnergyNet.class.getPackage();

		if (pkg != null) {
			String packageName = pkg.getName();

			return packageName.substring(0, packageName.length() - ".api.energy".length());
		}

		return "ic2";
	}

	/**
	 * Instance of the energy network.
	 */
	Object energyNetInstance;

	private static Method EnergyNet_getForWorld;
	private static Method EnergyNet_addTileEntity;
	private static Method EnergyNet_removeTileEntity;
	private static Method EnergyNet_emitEnergyFrom;
	private static Method EnergyNet_getTotalEnergyConducted;
	private static Method EnergyNet_getTotalEnergyEmitted;
	private static Method EnergyNet_getTotalEnergySunken;
}

