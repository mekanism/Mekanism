package mekanism.generators.common.tile.turbine;

import mekanism.api.Coord4D;
import mekanism.common.multiblock.TileEntityInternalMultiblock;
import mekanism.generators.common.GeneratorsBlock;
import mekanism.generators.common.content.turbine.SynchronizedTurbineData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;

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

        Coord4D coord = Coord4D.get(this).offset(Direction.DOWN);
        TileEntity tile = coord.getTileEntity(world);
        if (tile instanceof TileEntityTurbineRotor) {
            ((TileEntityTurbineRotor) tile).updateRotors();
        }
    }
}