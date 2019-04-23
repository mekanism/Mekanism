package mekanism.common.integration.forgeenergy;

import mekanism.common.config.MekanismConfig;
import mekanism.common.tile.transmitter.TileEntityUniversalCable;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.energy.IEnergyStorage;

public class ForgeEnergyCableIntegration implements IEnergyStorage {

    public TileEntityUniversalCable tileEntity;

    public EnumFacing side;

    public ForgeEnergyCableIntegration(TileEntityUniversalCable tile, EnumFacing facing) {
        tileEntity = tile;
        side = facing;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        return (int) Math
              .round(tileEntity
                    .acceptEnergy(side, maxReceive * MekanismConfig.current().general.FROM_FORGE.val(), simulate)
                    * MekanismConfig.current().general.TO_FORGE.val());
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return 0;
    }

    @Override
    public int getEnergyStored() {
        return (int) Math.round(
              Math.min(Integer.MAX_VALUE, tileEntity.getEnergy() * MekanismConfig.current().general.TO_FORGE.val()));
    }

    @Override
    public int getMaxEnergyStored() {
        return (int) Math.round(
              Math.min(Integer.MAX_VALUE, tileEntity.getMaxEnergy() * MekanismConfig.current().general.TO_FORGE.val()));
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
