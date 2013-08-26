package mekanism.common.tileentity;

import java.util.HashSet;

import mekanism.api.Object3D;
import mekanism.api.gas.EnumGas;
import mekanism.api.gas.GasNetwork;
import mekanism.api.gas.GasTransmission;
import mekanism.api.gas.IGasTransmitter;
import mekanism.api.gas.ITubeConnection;
import mekanism.api.transmitters.ITransmitter;
import mekanism.api.transmitters.TransmissionType;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

public class TileEntityPressurizedTube extends TileEntityTransmitter<GasNetwork> implements ITubeConnection, IGasTransmitter
{
	/** The gas currently displayed in this tube. */
	public EnumGas refGas = null;
	
	/** The scale of the gas (0F -> 1F) currently inside this tube. */
	public float gasScale;
	
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
				if(TransmissionType.checkTransmissionType(tube, TransmissionType.GAS, this) && ((ITransmitter<GasNetwork>)tube).getTransmitterNetwork(false) != null)
				{
					connectedNets.add(((ITransmitter<GasNetwork>)tube).getTransmitterNetwork());
				}
			}
			
			if(connectedNets.size() == 0 || worldObj.isRemote)
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
	public void invalidate()
	{
		if(!worldObj.isRemote)
		{
			getTransmitterNetwork().split(this);
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
		if(!worldObj.isRemote)
		{
			for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
			{
				TileEntity tileEntity = Object3D.get(this).getFromSide(side).getTileEntity(worldObj);
				
				if(TransmissionType.checkTransmissionType(tileEntity, TransmissionType.GAS, this))
				{
					getTransmitterNetwork().merge(((ITransmitter<GasNetwork>)tileEntity).getTransmitterNetwork());
				}
			}
			
			getTransmitterNetwork().refresh();
		}
	}
	
	public void onTransfer(EnumGas type)
	{
		if(type == refGas)
		{
			gasScale = Math.min(1, gasScale+.02F);
		}
		else if(refGas == null)
		{
			refGas = type;
			gasScale += Math.min(1, gasScale+.02F);
		}
	}
	
	@Override
	public boolean canTubeConnect(ForgeDirection side)
	{
		return true;
	}
	
	@Override
	public boolean canUpdate()
	{
		return false;
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
}
