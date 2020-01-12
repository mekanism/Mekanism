package mekanism.common.tile;

import mekanism.api.energy.IStrictEnergyStorage;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.block.basic.BlockInductionCell;
import mekanism.common.tier.InductionCellTier;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.util.Direction;

public class TileEntityInductionCell extends TileEntityMekanism implements IStrictEnergyStorage {

    public InductionCellTier tier;

    public TileEntityInductionCell(IBlockProvider blockProvider) {
        super(blockProvider);
    }

    @Override
    protected void presetVariables() {
        tier = ((BlockInductionCell) getBlockType()).getTier();
    }

    @Override
    public void onUpdate() {
    }

    @Override
    public boolean canReceiveEnergy(Direction side) {
        return false;
    }

    @Override
    public boolean canOutputEnergy(Direction side) {
        return false;
    }
}
