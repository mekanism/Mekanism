package mekanism.generators.common.tile.turbine;

import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.generators.common.GeneratorsBlock;

public class TileEntitySaturatingCondenser extends TileEntityMekanism {

    public TileEntitySaturatingCondenser() {
        super(GeneratorsBlock.SATURATING_CONDENSER);
    }

    @Override
    public void onUpdate() {
    }
}