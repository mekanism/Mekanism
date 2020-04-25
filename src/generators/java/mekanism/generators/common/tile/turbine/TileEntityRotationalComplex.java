package mekanism.generators.common.tile.turbine;

import java.util.UUID;
import mekanism.common.tile.prefab.TileEntityInternalMultiblock;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.content.turbine.TurbineMultiblockData;
import mekanism.generators.common.registries.GeneratorsBlocks;

public class TileEntityRotationalComplex extends TileEntityInternalMultiblock {

    public TileEntityRotationalComplex() {
        super(GeneratorsBlocks.ROTATIONAL_COMPLEX);
    }

    @Override
    public void setMultiblock(UUID id) {
        if (id == null && multiblockUUID != null) {
            TurbineMultiblockData.clientRotationMap.removeFloat(multiblockUUID);
        }
        super.setMultiblock(id);
        if (!isRemote()) {
            TileEntityTurbineRotor tile = MekanismUtils.getTileEntity(TileEntityTurbineRotor.class, getWorld(), getPos().down());
            if (tile != null) {
                tile.updateRotors();
            }
        }
    }
}