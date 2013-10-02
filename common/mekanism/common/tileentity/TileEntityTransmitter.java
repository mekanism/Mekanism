package mekanism.common.tileentity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mekanism.api.Object3D;
import mekanism.api.transmitters.DynamicNetwork;
import mekanism.api.transmitters.ITransmitter;
import mekanism.api.transmitters.TransmitterNetworkRegistry;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.ForgeDirection;

public abstract class TileEntityTransmitter<N extends DynamicNetwork<?, N, D>, D> extends TileEntity implements ITransmitter<N, D>
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
	public boolean canConnectMutual(ForgeDirection side)
	{
		if(!canConnect(side)) return false;
		
		TileEntity tile = Object3D.get(this).getFromSide(side).getTileEntity(worldObj);
		return (!(tile instanceof ITransmitter) || ((ITransmitter<?, ?>)tile).canConnect(side.getOpposite()));
	}
	
	@Override
	public boolean canConnect(ForgeDirection side)
	{
		return true;
	}
}
