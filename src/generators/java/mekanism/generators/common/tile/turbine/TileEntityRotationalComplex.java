package mekanism.generators.common.tile.turbine;

import mekanism.common.multiblock.TileEntityInternalMultiblock;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.GeneratorsBlock;
import mekanism.generators.common.content.turbine.SynchronizedTurbineData;

public class TileEntityRotationalComplex extends TileEntityInternalMultiblock {

    public TileEntityRotationalComplex() {
        super(GeneratorsBlock.ROTATIONAL_COMPLEX);
    }

    @Override
    public void setMultiblock(String id) {
        if (id == null && multiblockUUID != null) {
            SynchronizedTurbineData.clientRotationMap.remove(multiblockUUID);
        }

        super.setMultiblock(id);

        TileEntityTurbineRotor tile = MekanismUtils.getTileEntity(TileEntityTurbineRotor.class, getWorld(), getPos().down());
        if (tile != null) {
            tile.updateRotors();
        }
    }
}