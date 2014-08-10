package mekanism.common.base;

import mekanism.common.util.FluidContainerUtils.ContainerEditMode;

public interface IFluidContainerManager 
{
	public ContainerEditMode getContainerEditMode();
	
	public void setContainerEditMode(ContainerEditMode mode);
}
