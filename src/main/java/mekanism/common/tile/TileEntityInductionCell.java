package mekanism.common.tile;

import mekanism.api.energy.IStrictEnergyStorage;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeTier;
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
        tier = (InductionCellTier) Attribute.get(getBlockType(), AttributeTier.class).getTier();
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
