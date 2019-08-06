package mekanism.common.integration.forgeenergy;

import mekanism.common.tile.transmitter.universal_cable.TileEntityUniversalCable;
import net.minecraft.util.Direction;
import net.minecraftforge.energy.IEnergyStorage;

public class ForgeEnergyCableIntegration implements IEnergyStorage {

    public TileEntityUniversalCable tileEntity;

    public Direction side;

    public ForgeEnergyCableIntegration(TileEntityUniversalCable tile, Direction facing) {
        tileEntity = tile;
        side = facing;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        return ForgeEnergyIntegration.toForge(tileEntity.acceptEnergy(side, ForgeEnergyIntegration.fromForge(maxReceive), simulate));
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return 0;
    }

    @Override
    public int getEnergyStored() {
        return ForgeEnergyIntegration.toForge(tileEntity.getEnergy());
    }

    @Override
    public int getMaxEnergyStored() {
        return ForgeEnergyIntegration.toForge(tileEntity.getMaxEnergy());
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public boolean canReceive() {
        return tileEntity.canReceiveEnergy(side);
    }
}