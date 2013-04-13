/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.api.power;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.core.SafeTimeTracker;

public abstract class PowerProvider implements IPowerProvider {

	protected int latency;
	protected int minEnergyReceived;
	protected int maxEnergyReceived;
	protected int maxEnergyStored;
	protected int minActivationEnergy;
	protected float energyStored = 0;

	protected int powerLoss = 1;
	protected int powerLossRegularity = 100;

	public SafeTimeTracker timeTracker = new SafeTimeTracker();
	public SafeTimeTracker energyLossTracker = new SafeTimeTracker();

	public int[] powerSources = { 0, 0, 0, 0, 0, 0 };

	@Override
	public SafeTimeTracker getTimeTracker() {
		return this.timeTracker;
	}

	@Override
	public int getLatency() {
		return this.latency;
	}

	@Override
	public int getMinEnergyReceived() {
		return this.minEnergyReceived;
	}

	@Override
	public int getMaxEnergyReceived() {
		return this.maxEnergyReceived;
	}

	@Override
	public int getMaxEnergyStored() {
		return this.maxEnergyStored;
	}

	@Override
	public int getActivationEnergy() {
		return this.minActivationEnergy;
	}

	@Override
	public float getEnergyStored() {
		return this.energyStored;
	}

	@Override
	public void configure(int latency, int minEnergyReceived, int maxEnergyReceived, int minActivationEnergy, int maxStoredEnergy) {
		this.latency = latency;
		this.minEnergyReceived = minEnergyReceived;
		this.maxEnergyReceived = maxEnergyReceived;
		this.maxEnergyStored = maxStoredEnergy;
		this.minActivationEnergy = minActivationEnergy;
	}

	@Override
	public void configurePowerPerdition(int powerLoss, int powerLossRegularity) {
		this.powerLoss = powerLoss;
		this.powerLossRegularity = powerLossRegularity;
	}

	@Override
	public boolean update(IPowerReceptor receptor) {
		if (!preConditions(receptor))
			return false;

		TileEntity tile = (TileEntity) receptor;
		boolean result = false;

		if (energyStored >= minActivationEnergy) {
			if (latency == 0) {
				receptor.doWork();
				result = true;
			} else {
				if (timeTracker.markTimeIfDelay(tile.worldObj, latency)) {
					receptor.doWork();
					result = true;
				}
			}
		}

		if (powerLoss > 0 && energyLossTracker.markTimeIfDelay(tile.worldObj, powerLossRegularity)) {

			energyStored -= powerLoss;
			if (energyStored < 0) {
				energyStored = 0;
			}
		}

		for (int i = 0; i < 6; ++i) {
			if (powerSources[i] > 0) {
				powerSources[i]--;
			}
		}

		return result;
	}

	@Override
	public boolean preConditions(IPowerReceptor receptor) {
		return true;
	}

	@Override
	public float useEnergy(float min, float max, boolean doUse) {
		float result = 0;

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

		return result;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		latency = nbttagcompound.getInteger("latency");
		minEnergyReceived = nbttagcompound.getInteger("minEnergyReceived");
		maxEnergyReceived = nbttagcompound.getInteger("maxEnergyReceived");
		maxEnergyStored = nbttagcompound.getInteger("maxStoreEnergy");
		minActivationEnergy = nbttagcompound.getInteger("minActivationEnergy");

		try {
			energyStored = nbttagcompound.getFloat("storedEnergy");
		} catch (Throwable c) {
			energyStored = 0;
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		nbttagcompound.setInteger("latency", latency);
		nbttagcompound.setInteger("minEnergyReceived", minEnergyReceived);
		nbttagcompound.setInteger("maxEnergyReceived", maxEnergyReceived);
		nbttagcompound.setInteger("maxStoreEnergy", maxEnergyStored);
		nbttagcompound.setInteger("minActivationEnergy", minActivationEnergy);
		nbttagcompound.setFloat("storedEnergy", energyStored);
	}

	@Override
	public void receiveEnergy(float quantity, ForgeDirection from) {
		powerSources[from.ordinal()] = 2;

		energyStored += quantity;

		if (energyStored > maxEnergyStored) {
			energyStored = maxEnergyStored;
		}
	}

	@Override
	public boolean isPowerSource(ForgeDirection from) {
		return powerSources[from.ordinal()] != 0;
	}
}
