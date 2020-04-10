package mekanism.common.base;

import mekanism.common.tile.interfaces.IHasMode;

public interface IFluidContainerManager extends IHasMode {

    ContainerEditMode getContainerEditMode();
}