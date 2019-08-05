package mekanism.common.tile.base;

import mekanism.common.base.IBlockProvider;
import mekanism.common.block.interfaces.IBlockElectric;
import mekanism.common.util.MekanismUtils;

public abstract class TileEntityElectric extends TileEntityMekanism {

    //TODO
    private IBlockElectric electricBlock;

    private final double BASE_ENERGY_PER_TICK;

    private double energyPerTick;

    public TileEntityElectric(IBlockProvider blockProvider) {
        super(blockProvider);
        electricBlock = (IBlockElectric) blockProvider.getBlock();
        setMaxEnergy(getBaseStorage());
        setEnergyPerTick(getBaseUsage());
        BASE_ENERGY_PER_TICK = getBaseUsage();
    }

    @Override
    public void onChunkUnload() {
        if (MekanismUtils.useIC2()) {
            deregister();
        }
        super.onChunkUnload();
    }

    /**
     * Gets the scaled energy level for the GUI.
     *
     * @param i - multiplier
     *
     * @return scaled energy
     */
    public int getScaledEnergyLevel(int i) {
        return (int) (getEnergy() * i / getMaxEnergy());
    }

    public double getBaseUsage() {
        return electricBlock.getUsage();
    }

    public double getBaseStorage() {
        return electricBlock.getStorage();
    }

    public double getBaseEnergyPerTick() {
        return BASE_ENERGY_PER_TICK;
    }

    public double getEnergyPerTick() {
        return energyPerTick;
    }

    public void setEnergyPerTick(double energyPerTick) {
        this.energyPerTick = energyPerTick;
    }
}