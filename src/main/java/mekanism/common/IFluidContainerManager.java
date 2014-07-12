package mekanism.common;

import mekanism.common.util.FluidContainerUtils.ContainerEditMode;

public interface IFluidContainerManager 
{
	public ContainerEditMode getContainerEditMode();
	
	public void setContainerEditMode(ContainerEditMode mode);
}
