package mekanism.common.lib.multiblock;

import java.util.Map;

public interface IStructuralMultiblock extends IMultiblockBase {

    boolean canInterface(MultiblockManager<?> manager);

    Map<MultiblockManager<?>, Structure> getStructureMap();
}