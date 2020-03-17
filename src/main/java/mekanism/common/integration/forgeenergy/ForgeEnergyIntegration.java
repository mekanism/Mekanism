package mekanism.common.integration.forgeenergy;

import mekanism.api.Action;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.common.config.MekanismConfig;
import mekanism.common.util.MekanismUtils;
import net.minecraft.util.Direction;
import net.minecraftforge.energy.IEnergyStorage;

public class ForgeEnergyIntegration implements IEnergyStorage {

    private final IMekanismStrictEnergyHandler tile;
    private final Direction side;

    public ForgeEnergyIntegration(IMekanismStrictEnergyHandler tile, Direction facing) {
        this.tile = tile;
        side = facing;
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
        //Max energy that would be inserted
        double toInsert = fromForge(maxReceive);
        return toForge(toInsert - tile.insertEnergy(toInsert, side, Action.get(!simulate)));
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return toForge(tile.extractEnergy(fromForge(maxExtract), side, Action.get(!simulate)));
    }

    @Override
    public int getEnergyStored() {
        if (tile.getEnergyContainerCount(side) > 0) {
            //TODO: Improve on this
            return toForge(tile.getEnergy(0));
        }
        return 0;
    }

    @Override
    public int getMaxEnergyStored() {
        if (tile.getEnergyContainerCount(side) > 0) {
            //TODO: Improve on this
            return toForge(tile.getMaxEnergy(0));
        }
        return 0;
    }

    @Override
    public boolean canExtract() {
        return tile.extractEnergy(1, side, Action.SIMULATE) < 1;
    }

    @Override
    public boolean canReceive() {
        return tile.insertEnergy(1, side, Action.SIMULATE) > 0;
    }
}