package mekanism.common.integration.forgeenergy;

import mekanism.common.tile.transmitter.TileEntityUniversalCable;
import net.minecraft.util.Direction;
import net.minecraftforge.energy.IEnergyStorage;

public class ForgeEnergyCableIntegration implements IEnergyStorage {

    private final TileEntityUniversalCable tile;
    private final Direction side;

    public ForgeEnergyCableIntegration(TileEntityUniversalCable tile, Direction facing) {
        this.tile = tile;
        side = facing;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        return ForgeEnergyIntegration.toForge(tile.acceptEnergy(side, ForgeEnergyIntegration.fromForge(maxReceive), simulate));
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return 0;
    }

    @Override
    public int getEnergyStored() {
        return ForgeEnergyIntegration.toForge(tile.getEnergy());
    }

    @Override
    public int getMaxEnergyStored() {
        return ForgeEnergyIntegration.toForge(tile.getMaxEnergy());
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public boolean canReceive() {
        return tile.canReceiveEnergy(side);
    }
}