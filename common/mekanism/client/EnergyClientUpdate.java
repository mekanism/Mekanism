package mekanism.client;

import java.util.List;

import mekanism.api.DynamicNetwork.NetworkFinder;
import mekanism.api.Object3D;
import mekanism.common.EnergyNetwork;
import mekanism.common.TileEntityUniversalCable;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class EnergyClientUpdate
{	
	public NetworkFinder finder;
	
	public World worldObj;
	
	public double energyScale;
	
	public EnergyClientUpdate(TileEntity head, double power)
	{
		worldObj = head.worldObj;
		energyScale = power;
		finder = new NetworkFinder(head.worldObj, EnergyNetwork.class, Object3D.get(head));
	}
	
	public void clientUpdate()
	{
		List<Object3D> found = finder.exploreNetwork();
		
		for(Object3D object : found)
		{
			TileEntity tileEntity = object.getTileEntity(worldObj);
			
			if(tileEntity instanceof TileEntityUniversalCable)
			{
				((TileEntityUniversalCable)tileEntity).setCachedEnergy(energyScale);
			}
		}
	}
}
