package mekanism.common.tile;

import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.TileEntityMekanism;

public class TileEntityIndustrialAlarm extends TileEntityMekanism {

    public TileEntityIndustrialAlarm() {
        super(MekanismBlocks.INDUSTRIAL_ALARM);
        delaySupplier = () -> 3;
        this.onPowerChange();
    }

    @Override
    public void onPowerChange() {
        super.onPowerChange();
        if (getWorld() != null && !getWorld().isRemote()) {
            setActive(isPowered());
        }
    }

    @Override
    public boolean lightUpdate() {
        return true;
    }
}
