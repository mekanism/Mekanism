package mekanism.generators.common.tile.turbine;

import mekanism.common.multiblock.TileEntityInternalMultiblock;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.content.turbine.SynchronizedTurbineData;
import mekanism.generators.common.registries.GeneratorsBlocks;

public class TileEntityRotationalComplex extends TileEntityInternalMultiblock {

    public TileEntityRotationalComplex() {
        super(GeneratorsBlocks.ROTATIONAL_COMPLEX);
    }

    @Override
    public void setMultiblock(String id) {
        if (id == null && multiblockUUID != null) {
            SynchronizedTurbineData.clientRotationMap.removeFloat(multiblockUUID);
        }

        super.setMultiblock(id);

        TileEntityTurbineRotor tile = MekanismUtils.getTileEntity(TileEntityTurbineRotor.class, getWorld(), getPos().down());
        if (tile != null) {
            tile.updateRotors();
        }
    }
}