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
        return toForge(tile.acceptEnergy(side, fromForge(maxReceive), simulate));
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return toForge(tile.pullEnergy(side, fromForge(maxExtract), simulate));
    }

    @Override
    public int getEnergyStored() {
        return toForge(tile.getEnergy());
    }

    @Override
    public int getMaxEnergyStored() {
        return toForge(tile.getMaxEnergy());
    }

    @Override
    public boolean canExtract() {
        return tile.insertEnergy(1, side, Action.SIMULATE) < 1;
    }

    @Override
    public boolean canReceive() {
        return tile.extractEnergy(1, side, Action.SIMULATE) > 0;
    }
}