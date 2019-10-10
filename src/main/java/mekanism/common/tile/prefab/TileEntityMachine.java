package mekanism.common.tile.prefab;

import mekanism.api.providers.IBlockProvider;
import mekanism.common.tile.base.TileEntityMekanism;

//TODO: Remove
public abstract class TileEntityMachine extends TileEntityMekanism {

    public TileEntityMachine(IBlockProvider blockProvider) {
        super(blockProvider);
    }

    @Override
    public boolean renderUpdate() {
        return true;
    }

    @Override
    public boolean lightUpdate() {
        return true;
    }
}