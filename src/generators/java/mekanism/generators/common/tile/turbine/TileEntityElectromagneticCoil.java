package mekanism.generators.common.tile.turbine;

import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.generators.common.GeneratorsBlock;

public class TileEntityElectromagneticCoil extends TileEntityMekanism {

    public TileEntityElectromagneticCoil() {
        super(GeneratorsBlock.ELECTROMAGNETIC_COIL);
    }

    @Override
    public void onUpdate() {
    }
}