package mekanism.common.tile;

import mekanism.common.MekanismBlock;
import mekanism.common.tile.base.TileEntityMekanism;

public class TileEntityPressureDisperser extends TileEntityMekanism {

    public TileEntityPressureDisperser() {
        super(MekanismBlock.PRESSURE_DISPERSER);
    }

    @Override
    public void onUpdate() {
    }
}