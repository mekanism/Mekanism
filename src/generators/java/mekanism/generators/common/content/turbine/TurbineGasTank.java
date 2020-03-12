package mekanism.generators.common.content.turbine;

import mekanism.common.capabilities.chemical.MultiblockGasTank;
import mekanism.common.registries.MekanismGases;
import mekanism.generators.common.tile.turbine.TileEntityTurbineCasing;

public class TurbineGasTank extends MultiblockGasTank<TileEntityTurbineCasing> {

    private int lastStored;

    public TurbineGasTank(TileEntityTurbineCasing tile) {
        super(tile, () -> tile.structure == null ? 0 : tile.structure.getSteamCapacity(), gas -> gas == MekanismGases.STEAM.getGas());
        lastStored = getStored();
    }

    @Override
    public void onContentsChanged() {
        super.onContentsChanged();
        int amount = getStored();
        if (multiblock.structure != null) {
            if (amount >= lastStored) {
                multiblock.structure.lastSteamInput = amount - lastStored;
            } else {
                multiblock.structure.lastSteamInput = 0;
            }
        }
        lastStored = amount;
    }
}