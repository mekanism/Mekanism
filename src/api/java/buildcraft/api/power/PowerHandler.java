/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.power;

import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.core.SafeTimeTracker;
import buildcraft.api.mj.BatteryObject;
import buildcraft.api.mj.IBatteryObject;
import buildcraft.api.mj.IBatteryProvider;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjBattery;

/**
 * The PowerHandler is similar to FluidTank in that it holds your power and
 * allows standardized interaction between machines.
 * <p/>
 * To receive power to your machine you needs create an instance of PowerHandler
 * and implement IPowerReceptor on the TileEntity.
 * <p/>
 * If you plan emit power, you need only implement IPowerEmitter. You do not
 * need a PowerHandler. Engines have a PowerHandler because they can also
 * receive power from other Engines.
 * <p/>
 * See TileRefinery for a simple example of a power using machine.
 *
 * @see IPowerReceptor
 * @see IPowerEmitter
 */
public final class PowerHandler implements IBatteryProvider {

	public static enum Type {

		ENGINE, GATE, MACHINE, PIPE, STORAGE;

		public boolean canReceiveFromPipes() {
			switch (this) {
				case MACHINE:
				case STORAGE:
					return true;
				default:
					return false;
			}
		}

		public boolean eatsEngineExcess() {
			switch (this) {
				case MACHINE:
				case STORAGE:
					return true;
				default:
					return false;
			}
		}
	}

	/**
	 * Extend this class to create custom Perdition algorithms (its not final).
	 * <p/>
	 * NOTE: It is not possible to create a Zero perdition algorithm.
	 */
	public static class PerditionCalculator {

		public static final float DEFAULT_POWERLOSS = 1F;
		public static final float MIN_POWERLOSS = 0.01F;
		private final double powerLoss;

		public PerditionCalculator() {
			powerLoss = DEFAULT_POWERLOSS;
		}

		/**
		 * Simple constructor for simple Perdition per tick.
		 *
		 * @param powerLoss power loss per tick
		 */
		public PerditionCalculator(double powerLoss) {
			if (powerLoss < MIN_POWERLOSS) {
				this.powerLoss = MIN_POWERLOSS;
			} else {
				this.powerLoss = powerLoss;
			}
		}

		/**
		 * Apply the perdition algorithm to the current stored energy. This
		 * function can only be called once per tick, but it might not be called
		 * every tick. It is triggered by any manipulation of the stored energy.
		 *
		 * @param powerHandler the PowerHandler requesting the perdition update
		 * @param current      the current stored energy
		 * @param ticksPassed  ticks since the last time this function was called
		 */
		public double applyPerdition(PowerHandler powerHandler, double current, long ticksPassed) {
			double newPower = current - powerLoss * ticksPassed;

			if (newPower < 0) {
				newPower = 0;
			}

			return newPower;
		}

		/**
		 * Taxes a flat rate on all incoming power.
		 * <p/>
		 * Defaults to 0% tax rate.
		 *
		 * @return percent of input to tax
		 */
		public double getTaxPercent() {
			return 0;
		}
	}

	public static final PerditionCalculator DEFAULT_PERDITION = new PerditionCalculator();
	public static final double ROLLING_AVERAGE_WEIGHT = 100.0;
	public static final double ROLLING_AVERAGE_NUMERATOR = ROLLING_AVERAGE_WEIGHT - 1;
	public static final double ROLLING_AVERAGE_DENOMINATOR = 1.0 / ROLLING_AVERAGE_WEIGHT;
	public final int[] powerSources = new int[6];
	public final IPowerReceptor receptor;

	private double activationEnergy;
	private final SafeTimeTracker doWorkTracker = new SafeTimeTracker(1);
	private final SafeTimeTracker sourcesTracker = new SafeTimeTracker(1);
	private final SafeTimeTracker perditionTracker = new SafeTimeTracker(1);
	private PerditionCalculator perdition;
	private final PowerReceiver receiver;
	private final Type type;
	private IBatteryObject battery;
	// Tracking
	private double averageLostPower = 0;
	private double averageReceivedPower = 0;
	private double averageUsedPower = 0;

	public PowerHandler(IPowerReceptor receptor, Type type) {
		this(receptor, type, null);
	}

	public PowerHandler(IPowerReceptor receptor, Type type, Object battery) {
		this.receptor = receptor;
		this.type = type;
		this.receiver = new PowerReceiver();
		this.perdition = DEFAULT_PERDITION;

		if (battery instanceof IBatteryObject) {
			this.battery = (BatteryObject) battery;
		} else if (battery != null) {
			this.battery = MjAPI.getMjBattery(battery);
		} else {
			this.battery = MjAPI.getMjBattery(new AnonymousBattery());
		}
	}

	public PowerReceiver getPowerReceiver() {
		return receiver;
	}

	public double getMinEnergyReceived() {
		return battery.minimumConsumption();
	}

	public double getMaxEnergyReceived() {
		return battery.getEnergyRequested();
	}

	public double getMaxEnergyStored() {
		return battery.maxCapacity();
	}

	public double getActivationEnergy() {
		return activationEnergy;
	}

	public double getEnergyStored() {
		return battery.getEnergyStored();
	}

	@Override
	public IBatteryObject getMjBattery(String kind) {
		return battery.kind().equals(kind) ? battery : null;
	}

	/**
	 * Setup your PowerHandler's settings.
	 *
	 * @param minEnergyReceived
	 *            This is the minimum about of power that will be accepted by
	 *            the PowerHandler. This should generally be greater than the
	 *            activationEnergy if you plan to use the doWork() callback.
	 *            Anything greater than 1 will prevent Redstone Engines from
	 *            powering this Provider.
	 * @param maxEnergyReceived
	 *            The maximum amount of power accepted by the PowerHandler. This
	 *            should generally be less than 500. Too low and larger engines
	 *            will overheat while trying to power the machine. Too high, and
	 *            the engines will never warm up. Greater values also place
	 *            greater strain on the power net.
	 * @param activationEnergy
	 *            If the stored energy is greater than this value, the doWork()
	 *            callback is called (once per tick).
	 * @param maxStoredEnergy
	 *            The maximum amount of power this PowerHandler can store.
	 *            Values tend to range between 100 and 5000. With 1000 and 1500
	 *            being common.
	 */
	public void configure(double minEnergyReceived, double maxEnergyReceived, double activationEnergy,
						  double maxStoredEnergy) {
		double localMaxEnergyReceived = maxEnergyReceived;

		if (minEnergyReceived > localMaxEnergyReceived) {
			localMaxEnergyReceived = minEnergyReceived;
		}
		this.activationEnergy = activationEnergy;

		battery.reconfigure(maxStoredEnergy, localMaxEnergyReceived, minEnergyReceived);
	}

	/**
	 * Allows you define perdition in terms of loss/ticks.
	 * <p/>
	 * This function is mostly for legacy implementations. See
	 * PerditionCalculator for more complex perdition formulas.
	 *
	 * @param powerLoss
	 * @param powerLossRegularity
	 * @see PerditionCalculator
	 */
	public void configurePowerPerdition(int powerLoss, int powerLossRegularity) {
		if (powerLoss == 0 || powerLossRegularity == 0) {
			perdition = new PerditionCalculator(0);
			return;
		}
		perdition = new PerditionCalculator((float) powerLoss / (float) powerLossRegularity);
	}

	/**
	 * Allows you to define a new PerditionCalculator class to handler perdition
	 * calculations.
	 * <p/>
	 * For example if you want exponentially increasing loss based on amount
	 * stored.
	 *
	 * @param perdition
	 */
	public void setPerdition(PerditionCalculator perdition) {
		if (perdition == null) {
			this.perdition = DEFAULT_PERDITION;
		} else {
			this.perdition = perdition;
		}
	}

	public PerditionCalculator getPerdition() {
		if (perdition == null) {
			return DEFAULT_PERDITION;
		} else {
			return perdition;
		}
	}

	/**
	 * Ticks the power handler. You should call this if you can, but its not
	 * required.
	 * <p/>
	 * If you don't call it, the possibility exists for some weirdness with the
	 * perdition algorithm and work callback as its possible they will not be
	 * called on every tick they otherwise would be. You should be able to
	 * design around this though if you are aware of the limitations.
	 */
	public void update() {
		applyPerdition();
		applyWork();
		validateEnergy();
	}

	private void applyPerdition() {
		double energyStored = getEnergyStored();
		if (perditionTracker.markTimeIfDelay(receptor.getWorld()) && energyStored > 0) {
			double prev = energyStored;
			double newEnergy = getPerdition().applyPerdition(this, energyStored, perditionTracker.durationOfLastDelay());
			if (newEnergy == 0 || newEnergy < energyStored) {
				battery.setEnergyStored(energyStored = newEnergy);
			} else {
				battery.setEnergyStored(energyStored = DEFAULT_PERDITION.applyPerdition(this, energyStored, perditionTracker.durationOfLastDelay()));
			}
			validateEnergy();

			averageLostPower = (averageLostPower * ROLLING_AVERAGE_NUMERATOR + (prev - energyStored)) * ROLLING_AVERAGE_DENOMINATOR;
		}
	}

	private void applyWork() {
		if (getEnergyStored() >= activationEnergy) {
			if (doWorkTracker.markTimeIfDelay(receptor.getWorld())) {
				receptor.doWork(this);
			}
		}
	}

	private void updateSources(ForgeDirection source) {
		if (sourcesTracker.markTimeIfDelay(receptor.getWorld())) {
			for (int i = 0; i < 6; ++i) {
				powerSources[i] -= sourcesTracker.durationOfLastDelay();
				if (powerSources[i] < 0) {
					powerSources[i] = 0;
				}
			}
		}

		if (source != null) {
			powerSources[source.ordinal()] = 10;
		}
	}

	/**
	 * Extract energy from the PowerHandler. You must call this even if doWork()
	 * triggers.
	 *
	 * @param min
	 * @param max
	 * @param doUse
	 * @return amount used
	 */
	public double useEnergy(double min, double max, boolean doUse) {
		applyPerdition();

		double result = 0;

		double energyStored = getEnergyStored();
		if (energyStored >= min) {
			if (energyStored <= max) {
				result = energyStored;
				if (doUse) {
					energyStored = 0;
				}
			} else {
				result = max;
				if (doUse) {
					energyStored -= max;
				}
			}
		}
		if (energyStored != getEnergyStored()) {
			battery.setEnergyStored(energyStored);
		}

		validateEnergy();

		if (doUse) {
			averageUsedPower = (averageUsedPower * ROLLING_AVERAGE_NUMERATOR + result) * ROLLING_AVERAGE_DENOMINATOR;
		}

		return result;
	}

	public void readFromNBT(NBTTagCompound data) {
		readFromNBT(data, "powerProvider");
	}

	public void readFromNBT(NBTTagCompound data, String tag) {
		NBTTagCompound nbt = data.getCompoundTag(tag);
		battery.setEnergyStored(nbt.getDouble("energyStored"));
	}

	public void writeToNBT(NBTTagCompound data) {
		writeToNBT(data, "powerProvider");
	}

	public void writeToNBT(NBTTagCompound data, String tag) {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setDouble("energyStored", battery.getEnergyStored());
		data.setTag(tag, nbt);
	}

	public final class PowerReceiver {

		private PowerReceiver() {
		}

		public double getMinEnergyReceived() {
			return PowerHandler.this.getMinEnergyReceived();
		}

		public double getMaxEnergyReceived() {
			return PowerHandler.this.getMaxEnergyReceived();
		}

		public double getMaxEnergyStored() {
			return PowerHandler.this.getMaxEnergyStored();
		}

		public double getActivationEnergy() {
			return activationEnergy;
		}

		public double getEnergyStored() {
			return PowerHandler.this.getEnergyStored();
		}

		public double getAveragePowerReceived() {
			return averageReceivedPower;
		}

		public double getAveragePowerUsed() {
			return averageUsedPower;
		}

		public double getAveragePowerLost() {
			return averageLostPower;
		}

		public Type getType() {
			return type;
		}

		public void update() {
			PowerHandler.this.update();
		}

		/**
		 * The amount of power that this PowerHandler currently needs.
		 */
		public double powerRequest() {
			update();
			return battery.getEnergyRequested();
		}

		/**
		 * Add power to the PowerReceiver from an external source.
		 * <p/>
		 * IPowerEmitters are responsible for calling this themselves.
		 *
		 * @param quantity
		 * @param from
		 * @return the amount of power used
		 */
		public double receiveEnergy(Type source, final double quantity, ForgeDirection from) {
			double used = quantity;
			if (source == Type.ENGINE) {
				if (used < getMinEnergyReceived()) {
					return 0;
				} else if (used > getMaxEnergyReceived()) {
					used = getMaxEnergyReceived();
				}
			}

			updateSources(from);

			used -= used * getPerdition().getTaxPercent();

			used = addEnergy(used);

			applyWork();

			if (source == Type.ENGINE && type.eatsEngineExcess()) {
				used = Math.min(quantity, getMaxEnergyReceived());
			}

			averageReceivedPower = (averageReceivedPower * ROLLING_AVERAGE_NUMERATOR + used) * ROLLING_AVERAGE_DENOMINATOR;

			return used;
		}
	}

	/**
	 * @return the amount the power changed by
	 */
	public double addEnergy(double quantity) {
		final double used = battery.addEnergy(quantity);
		applyPerdition();
		return used;
	}

	public void setEnergy(double quantity) {
		battery.setEnergyStored(quantity);
		validateEnergy();
	}

	public boolean isPowerSource(ForgeDirection from) {
		return powerSources[from.ordinal()] != 0;
	}

	private void validateEnergy() {
		double energyStored = getEnergyStored();
		double maxEnergyStored = getMaxEnergyStored();
		if (energyStored < 0) {
			energyStored = 0;
		}
		if (energyStored > maxEnergyStored) {
			energyStored = maxEnergyStored;
		}
		if (energyStored != battery.getEnergyStored()) {
			battery.setEnergyStored(energyStored);
		}
	}

	private static class AnonymousBattery {
		@MjBattery
		public double mjStored;
	}
}
