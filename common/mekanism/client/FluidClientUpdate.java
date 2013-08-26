package mekanism.client;

import java.util.List;

import mekanism.api.transmitters.TransmissionType;
import mekanism.api.transmitters.DynamicNetwork.NetworkFinder;
import mekanism.api.Object3D;
import mekanism.common.tileentity.TileEntityMechanicalPipe;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

public class FluidClientUpdate
{
	public NetworkFinder finder;
	
	public World worldObj;
	
	public FluidStack fluidStack;

	public FluidClientUpdate(TileEntity head, FluidStack fluid)
	{
		worldObj = head.worldObj;
		fluidStack = fluid;
		finder = new NetworkFinder(head.worldObj, TransmissionType.FLUID, Object3D.get(head));
	}
	
	public void clientUpdate()
	{
		List<Object3D> found = finder.exploreNetwork();
		
		for(Object3D object : found)
		{
			TileEntity tileEntity = object.getTileEntity(worldObj);
			
			if(tileEntity instanceof TileEntityMechanicalPipe)
			{
				((TileEntityMechanicalPipe)tileEntity).onTransfer(fluidStack);
			}
		}
	}
}
