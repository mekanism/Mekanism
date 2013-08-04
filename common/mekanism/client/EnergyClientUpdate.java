package mekanism.client;

import java.util.ArrayList;
import java.util.List;

import mekanism.api.Object3D;
import mekanism.common.EnergyNetwork.NetworkFinder;
import mekanism.common.IUniversalCable;
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
		finder = new NetworkFinder(head.worldObj, Object3D.get(head));
	}
	
	public void clientUpdate()
	{
		List<Object3D> found = finder.exploreNetwork();
		
		for(Object3D object : found)
		{
			TileEntity tileEntity = object.getTileEntity(worldObj);
			if(tileEntity instanceof IUniversalCable)
			{
				((IUniversalCable)tileEntity).setEnergyScale(energyScale);
			}
		}
	}
}
