package mekanism.common.base;

import mekanism.common.util.FluidContainerUtils.ContainerEditMode;

public interface IFluidContainerManager {

    ContainerEditMode getContainerEditMode();

    void setContainerEditMode(ContainerEditMode mode);
}
