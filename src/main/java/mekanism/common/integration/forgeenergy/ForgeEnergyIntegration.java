package mekanism.common.integration.forgeenergy;

import mekanism.api.Action;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.common.config.MekanismConfig;
import mekanism.common.util.MekanismUtils;
import net.minecraftforge.energy.IEnergyStorage;

public class ForgeEnergyIntegration implements IEnergyStorage {

    private final IStrictEnergyHandler handler;

    public ForgeEnergyIntegration(IStrictEnergyHandler handler) {
        this.handler = handler;
    }

    public static double fromForge(int forge) {
        return forge * MekanismConfig.general.FROM_FORGE.get();
    }

    public static double fromForge(double forge) {
        return forge * MekanismConfig.general.FROM_FORGE.get();
    }

    public static int toForge(double joules) {
        return MekanismUtils.clampToInt(joules * MekanismConfig.general.TO_FORGE.get());
    }

    public static double toForgeAsDouble(double joules) {
        return joules * MekanismConfig.general.TO_FORGE.get();
    }

    public static long toForgeAsLong(long joules) {
        return Math.round(joules * MekanismConfig.general.TO_FORGE.get());
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (maxReceive <= 0) {
            return 0;
        }
        double toInsert = fromForge(maxReceive);
        return toForge(toInsert - handler.insertEnergy(toInsert, Action.get(!simulate)));
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        if (maxExtract <= 0) {
            return 0;
        }
        return toForge(handler.extractEnergy(fromForge(maxExtract), Action.get(!simulate)));
    }

    @Override
    public int getEnergyStored() {
        if (handler.getEnergyContainerCount() > 0) {
            //TODO: Improve on this
            return toForge(handler.getEnergy(0));
        }
        return 0;
    }

    @Override
    public int getMaxEnergyStored() {
        if (handler.getEnergyContainerCount() > 0) {
            //TODO: Improve on this
            return toForge(handler.getMaxEnergy(0));
        }
        return 0;
    }

    @Override
    public boolean canExtract() {
        //TODO: Should this always return true
        return handler.extractEnergy(1, Action.SIMULATE) > 0;
    }

    @Override
    public boolean canReceive() {
        //TODO: Should this always return true
        return handler.insertEnergy(1, Action.SIMULATE) < 1;
    }
}