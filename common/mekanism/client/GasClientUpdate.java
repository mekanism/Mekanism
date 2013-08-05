package mekanism.client;

import java.util.List;

import mekanism.api.EnumGas;
import mekanism.api.GasNetwork.NetworkFinder;
import mekanism.api.IPressurizedTube;
import mekanism.api.Object3D;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class GasClientUpdate
{
	public NetworkFinder finder;
	
	public World worldObj;
	
	public EnumGas gasType;

	public GasClientUpdate(TileEntity head, EnumGas type)
	{
		worldObj = head.worldObj;
		gasType = type;
		finder = new NetworkFinder(head.worldObj, Object3D.get(head));
	}
	
	public void clientUpdate()
	{
		List<Object3D> found = finder.exploreNetwork();
		
		for(Object3D object : found)
		{
			TileEntity tileEntity = object.getTileEntity(worldObj);
			
			if(tileEntity instanceof IPressurizedTube)
			{
				((IPressurizedTube)tileEntity).onTransfer(gasType);
			}
		}
	}
}
