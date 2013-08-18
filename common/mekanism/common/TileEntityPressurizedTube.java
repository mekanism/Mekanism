package mekanism.common;

import java.util.HashSet;

import mekanism.api.EnumGas;
import mekanism.api.GasNetwork;
import mekanism.api.GasTransmission;
import mekanism.api.IPressurizedTube;
import mekanism.api.ITubeConnection;
import mekanism.api.Object3D;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

public class TileEntityPressurizedTube extends TileEntityTransmitter<GasNetwork> implements IPressurizedTube, ITubeConnection
{
	/** The gas currently displayed in this tube. */
	public EnumGas refGas = null;
	
	/** The scale of the gas (0F -> 1F) currently inside this tube. */
	public float gasScale;
	
	@Override
	public GasNetwork getNetwork(boolean createIfNull)
	{
		if(theNetwork == null && createIfNull)
		{
			TileEntity[] adjacentTubes = GasTransmission.getConnectedTubes(this);
			HashSet<GasNetwork> connectedNets = new HashSet<GasNetwork>();
			
			for(TileEntity tube : adjacentTubes)
			{
				if(tube instanceof IPressurizedTube && ((IPressurizedTube)tube).getNetwork(false) != null)
				{
					connectedNets.add(((IPressurizedTube)tube).getNetwork());
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
	public void fixNetwork()
	{
		getNetwork().fixMessedUpNetwork(this);
	}

	@Override
	public void invalidate()
	{
		if(!worldObj.isRemote)
		{
			getNetwork().split(this);
		}
		
		super.invalidate();
	}
	
	@Override
	public void removeFromNetwork()
	{
		if(theNetwork != null)
		{
			theNetwork.removeTransmitter(this);
		}
	}
	
	@Override
	public void refreshNetwork() 
	{
		if(!worldObj.isRemote)
		{
			if(canTransferGas())
			{
				for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
				{
					TileEntity tileEntity = Object3D.get(this).getFromSide(side).getTileEntity(worldObj);
					
					if(tileEntity instanceof IPressurizedTube && ((IPressurizedTube)tileEntity).canTransferGas())
					{
						getNetwork().merge(((IPressurizedTube)tileEntity).getNetwork());
					}
				}
				
				getNetwork().refresh();
			}
			else {
				getNetwork().split(this);
			}
		}
	}
	
	@Override
	public boolean canTransferGas()
	{
		return worldObj.getBlockPowerInput(xCoord, yCoord, zCoord) == 0;
	}

    @Override
    public boolean canTransferGasToTube(TileEntity tile)
    {
        return canTransferGas();
    }
	
	@Override
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
		return canTransferGas();
	}
	
	@Override
	public boolean canUpdate()
	{
		return false;
	}
}
