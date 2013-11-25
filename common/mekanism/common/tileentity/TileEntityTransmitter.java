package mekanism.common.tileentity;

import mekanism.api.transmitters.ITransmitter;
import mekanism.api.transmitters.TransmitterNetworkRegistry;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class TileEntityTransmitter<N> extends TileEntity implements ITransmitter<N>
{
	public N theNetwork;
	
	public int delayTicks = 0;
	
	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox()
	{
		return INFINITE_EXTENT_AABB;
	}
	
	@Override
	public boolean canUpdate()
	{
		return FMLCommonHandler.instance().getEffectiveSide().isClient();
	}
	
	@Override
	public void updateEntity()
	{
		super.updateEntity();
		
		if(worldObj.isRemote)
		{
			if(delayTicks == 3)
			{
				delayTicks++;
				refreshTransmitterNetwork();
			}
			else if(delayTicks < 3)
			{
				delayTicks++;
			}
		}
	}
	
	@Override
	public void onChunkUnload() 
	{
		super.onChunkUnload();
		
		if(!worldObj.isRemote)
		{
			TransmitterNetworkRegistry.getInstance().pruneEmptyNetworks();
		}
	}
	
	@Override
	public void setTransmitterNetwork(N network)
	{
		if(network != theNetwork)
		{
			removeFromTransmitterNetwork();
			theNetwork = network;
		}
	}
	
	@Override
	public boolean areTransmitterNetworksEqual(TileEntity tileEntity)
	{
		return tileEntity instanceof ITransmitter && getTransmissionType() == ((ITransmitter)tileEntity).getTransmissionType();
	}
	
	@Override
	public N getTransmitterNetwork()
	{
		return getTransmitterNetwork(true);
	}
	
	@Override
	public void chunkLoad() {}
}
