/*package mekanism.common.integration.energy.fluxnetworks;

import mekanism.api.Action;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.math.FloatingLong;
import mekanism.common.integration.energy.EnergyCompatUtils.EnergyType;
import sonar.fluxnetworks.api.energy.IFNEnergyStorage;

public class FNIntegration implements IFNEnergyStorage {

    private final IStrictEnergyHandler handler;

    public FNIntegration(IStrictEnergyHandler handler) {
        this.handler = handler;
    }

    @Override
    public long receiveEnergyL(long maxReceive, boolean simulate) {
        if (maxReceive <= 0) {
            return 0;
        }
        FloatingLong toInsert = EnergyType.FORGE.convertFrom(maxReceive);
        return EnergyType.FORGE.convertToAsLong(toInsert.subtract(handler.insertEnergy(toInsert, Action.get(!simulate))));
    }

    @Override
    public long extractEnergyL(long maxExtract, boolean simulate) {
        return maxExtract <= 0 ? 0 : EnergyType.FORGE.convertToAsLong(handler.extractEnergy(EnergyType.FORGE.convertFrom(maxExtract), Action.get(!simulate)));
    }

    @Override
    public long getEnergyStoredL() {
        int containers = handler.getEnergyContainerCount();
        if (containers > 0) {
            long energy = 0;
            for (int container = 0; container < containers; container++) {
                long total = EnergyType.FORGE.convertToAsLong(handler.getEnergy(container));
                if (total > Long.MAX_VALUE - energy) {
                    //Ensure we don't overflow
                    energy = Long.MAX_VALUE;
                    break;
                } else {
                    energy += total;
                }
            }
            return energy;
        }
        return 0;
    }

    @Override
    public long getMaxEnergyStoredL() {
        int containers = handler.getEnergyContainerCount();
        if (containers > 0) {
            long maxEnergy = 0;
            for (int container = 0; container < containers; container++) {
                long max = EnergyType.FORGE.convertToAsLong(handler.getMaxEnergy(container));
                if (max > Long.MAX_VALUE - maxEnergy) {
                    //Ensure we don't overflow
                    maxEnergy = Long.MAX_VALUE;
                    break;
                } else {
                    maxEnergy += max;
                }
            }
            return maxEnergy;
        }
        return 0;
    }

    @Override
    public boolean canExtractL() {
        return !handler.extractEnergy(FloatingLong.ONE, Action.SIMULATE).isZero();
    }

    @Override
    public boolean canReceiveL() {
        return handler.insertEnergy(FloatingLong.ONE, Action.SIMULATE).smallerThan(FloatingLong.ONE);
    }
}*/