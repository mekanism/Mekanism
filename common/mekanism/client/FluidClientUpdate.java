package mekanism.client;

import mekanism.api.transmitters.ITransmitter;
import mekanism.common.FluidNetwork;
import mekanism.common.tileentity.TileEntityMechanicalPipe;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

public class FluidClientUpdate
{
	public World worldObj;
	
	public FluidStack fluidStack;
	
	public TileEntity tileEntity;

	public FluidClientUpdate(TileEntity head, FluidStack fluid)
	{
		worldObj = head.worldObj;
		tileEntity = head;
		fluidStack = fluid;
	}
}
