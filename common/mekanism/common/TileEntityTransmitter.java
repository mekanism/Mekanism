package mekanism.common;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mekanism.api.ITransmitter;
import mekanism.api.TransmitterNetworkRegistry;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;

public abstract class TileEntityTransmitter<N> extends TileEntity implements ITransmitter<N>
{
	public N theNetwork;
	
	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox()
	{
		return INFINITE_EXTENT_AABB;
	}
	
	@Override
	public void onChunkUnload() 
	{
		invalidate();
		TransmitterNetworkRegistry.getInstance().pruneEmptyNetworks();
	}
	
	@Override
	public void setNetwork(N network)
	{
		if(network != theNetwork)
		{
			removeFromNetwork();
			theNetwork = network;
		}
	}
	
	@Override
	public boolean areNetworksEqual(TileEntity tileEntity)
	{
		return tileEntity instanceof ITransmitter && getTransmissionType() == ((ITransmitter)tileEntity).getTransmissionType();
	}
	
	@Override
	public N getNetwork()
	{
		return getNetwork(true);
	}
}
