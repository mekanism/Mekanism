package mekanism.common.multiblock;

import net.minecraft.tileentity.TileEntity;

public interface IStructuralMultiblock extends IMultiblockBase {

    boolean canInterface(TileEntity controller);

    void onPlace();

    void setMultiblock(MultiblockData multiblock);
}