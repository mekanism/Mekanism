package mekanism.common.tileentity;

import java.util.HashSet;

import mekanism.api.Coord4D;
import mekanism.api.gas.GasNetwork;
import mekanism.api.gas.GasTransmission;
import mekanism.api.gas.IGasTransmitter;
import mekanism.api.gas.ITubeConnection;
import mekanism.api.transmitters.IGridTransmitter;
import mekanism.api.transmitters.TransmissionType;
import mekanism.api.transmitters.TransmitterNetworkRegistry;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

public class TileEntityPressurizedTube extends TileEntityTransmitter<GasNetwork> implements ITubeConnection, IGasTransmitter
{	
	@Override
	public TransmissionType getTransmissionType()
	{
		return TransmissionType.GAS;
	}
	
	@Override
	public GasNetwork getTransmitterNetwork(boolean createIfNull)
	{
		if(theNetwork == null && createIfNull)
		{
			TileEntity[] adjacentTubes = GasTransmission.getConnectedTubes(this);
			HashSet<GasNetwork> connectedNets = new HashSet<GasNetwork>();
			
			for(TileEntity tube : adjacentTubes)
			{
				if(TransmissionType.checkTransmissionType(tube, TransmissionType.GAS, this) && ((IGridTransmitter<GasNetwork>)tube).getTransmitterNetwork(false) != null)
				{
					connectedNets.add(((IGridTransmitter<GasNetwork>)tube).getTransmitterNetwork());
				}
			}
			
			if(connectedNets.size() == 0)
			{
				theNetwork = new GasNetwork(this);
			}
			else if(connectedNets.size() == 1)
			{
				theNetwork = (GasNetwork)connectedNets.iterator().next();
				theNetwork.transmitters.add(this);
			}
			else {
				theNetwork = new GasNetwork(connectedNets);
				theNetwork.transmitters.add(this);
			}
		}
		
		return theNetwork;
	}

	@Override
	public void fixTransmitterNetwork()
	{
		getTransmitterNetwork().fixMessedUpNetwork(this);
	}

	@Override
	public void onChunkUnload()
	{
		super.onChunkUnload();
		
		getTransmitterNetwork().split(this);
	}
	
	@Override
	public void invalidate()
	{
		getTransmitterNetwork().split(this);
		
		if(!worldObj.isRemote)
		{
			TransmitterNetworkRegistry.getInstance().pruneEmptyNetworks();
		}
		
		super.invalidate();
	}
	
	@Override
	public void removeFromTransmitterNetwork()
	{
		if(theNetwork != null)
		{
			theNetwork.removeTransmitter(this);
		}
	}
	
	@Override
	public void refreshTransmitterNetwork() 
	{
		for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
		{
			TileEntity tileEntity = Coord4D.get(this).getFromSide(side).getTileEntity(worldObj);
			
			if(TransmissionType.checkTransmissionType(tileEntity, TransmissionType.GAS, this))
			{
				getTransmitterNetwork().merge(((IGridTransmitter<GasNetwork>)tileEntity).getTransmitterNetwork());
			}
		}
		
		getTransmitterNetwork().refresh();
	}
	
	@Override
	public boolean canTubeConnect(ForgeDirection side)
	{
		return true;
	}
	
	@Override
	public int getTransmitterNetworkSize()
	{
		return getTransmitterNetwork().getSize();
	}

	@Override
	public int getTransmitterNetworkAcceptorSize()
	{
		return getTransmitterNetwork().getAcceptorSize();
	}

	@Override
	public String getTransmitterNetworkNeeded()
	{
		return getTransmitterNetwork().getNeeded();
	}

	@Override
	public String getTransmitterNetworkFlow()
	{
		return getTransmitterNetwork().getFlow();
	}

    @Override
    public boolean canTransferGasToTube(TileEntity tile)
    {
        return tile instanceof IGasTransmitter;
    }

    @Override
    public int getCapacity()
    {
        return 256;
    }
}
