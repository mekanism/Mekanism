package mekanism.common;

import java.util.HashSet;

import mekanism.api.EnumGas;
import mekanism.api.GasNetwork;
import mekanism.api.IPressurizedTube;
import mekanism.api.ITubeConnection;
import mekanism.api.Object3D;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityPressurizedTube extends TileEntity implements IPressurizedTube, ITubeConnection
{
	/** The gas currently displayed in this tube. */
	public EnumGas refGas = null;
	
	/** The scale of the gas (0F -> 1F) currently inside this tube. */
	public float gasScale;
	
	/** The gas network currently in use by this tube segment. */
	public GasNetwork gasNetwork;
	
	@Override
	public GasNetwork getNetwork()
	{
		return getNetwork(true);
	}
	
	@Override
	public GasNetwork getNetwork(boolean createIfNull)
	{
		if(gasNetwork == null && createIfNull)
		{
			TileEntity[] adjacentPipes = PipeUtils.getConnectedPipes(this);
			HashSet<GasNetwork> connectedNets = new HashSet<GasNetwork>();
			
			for(TileEntity cable : adjacentPipes)
			{
				if(cable instanceof IPressurizedTube && ((IPressurizedTube)cable).getNetwork(false) != null)
				{
					connectedNets.add(((IPressurizedTube)cable).getNetwork());
				}
			}
			
			if(connectedNets.size() == 0 || worldObj.isRemote)
			{
				gasNetwork = new GasNetwork(this);
			}
			else if(connectedNets.size() == 1)
			{
				gasNetwork = (GasNetwork)connectedNets.iterator().next();
				gasNetwork.tubes.add(this);
			}
			else {
				gasNetwork = new GasNetwork(connectedNets);
				gasNetwork.tubes.add(this);
			}
		}
		
		return gasNetwork;
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
	public void setNetwork(GasNetwork network)
	{
		if(network != gasNetwork)
		{
			removeFromNetwork();
			gasNetwork = network;			
		}
	}
	
	@Override
	public void removeFromNetwork()
	{
		if(gasNetwork != null)
		{
			gasNetwork.removeTube(this);
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
	
	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox()
	{
		return INFINITE_EXTENT_AABB;
	}
}
