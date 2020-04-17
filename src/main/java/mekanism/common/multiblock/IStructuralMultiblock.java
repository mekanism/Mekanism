package mekanism.common.multiblock;

import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import net.minecraft.tileentity.TileEntity;

public interface IStructuralMultiblock extends IMultiblockBase {

    boolean canInterface(TileEntity controller);

    void setController(Coord4D coord);

    void onPlace();

    @Nullable
    Coord4D getController();
}