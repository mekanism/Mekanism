package mekanism.common;

import java.util.ArrayList;
import java.util.HashSet;

import mekanism.api.Object3D;
import mekanism.api.TransmitterNetworkRegistry;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.network.PacketDataRequest;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.ForgeDirection;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityLogisticalTransporter extends TileEntity implements ILogisticalTransporter, ITileNetwork
{
	/** The inventory network currently in use by this transporter segment. */
	public InventoryNetwork inventoryNetwork;
	
	/** This transporter's active state. */
	public boolean isActive = false;
	
	@Override
	public InventoryNetwork getNetwork()
	{
		if(inventoryNetwork == null)
		{
			inventoryNetwork = new InventoryNetwork(this);
		}
		
		return inventoryNetwork;
	}
	
	@Override
	public InventoryNetwork getNetwork(boolean createIfNull)
	{
		if(inventoryNetwork == null && createIfNull)
		{
			TileEntity[] adjacentTransporters = CableUtils.getConnectedCables(this);
			HashSet<InventoryNetwork> connectedNets = new HashSet<InventoryNetwork>();
			
			for(TileEntity transporter : adjacentTransporters)
			{
				if(transporter instanceof ILogisticalTransporter && ((ILogisticalTransporter)transporter).getNetwork(false) != null)
				{
					connectedNets.add(((ILogisticalTransporter)transporter).getNetwork());
				}
			}
			if(connectedNets.size() == 0 || worldObj.isRemote)
			{
				inventoryNetwork = new InventoryNetwork(this);
			}
			else if(connectedNets.size() == 1)
			{
				inventoryNetwork = connectedNets.iterator().next();
				inventoryNetwork.transmitters.add(this);
			}
			else {
				inventoryNetwork = new InventoryNetwork(connectedNets);
				inventoryNetwork.transmitters.add(this);
			}
		}
		
		return inventoryNetwork;
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
	public void setNetwork(InventoryNetwork network)
	{
		if(network != inventoryNetwork)
		{
			removeFromNetwork();
			inventoryNetwork = network;
		}
	}
	
	@Override
	public void removeFromNetwork()
	{
		if(inventoryNetwork != null)
		{
			inventoryNetwork.removeTransmitter(this);
		}
	}

	@Override
	public void refreshNetwork() 
	{
		if(!worldObj.isRemote)
		{
			for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
			{
				TileEntity tileEntity = Object3D.get(this).getFromSide(side).getTileEntity(worldObj);
				
				if(tileEntity instanceof ILogisticalTransporter)
				{
					getNetwork().merge(((ILogisticalTransporter)tileEntity).getNetwork());
				}
			}
			
			getNetwork().refresh();
		}
	}
	
	@Override
	public void onChunkUnload() 
	{
		invalidate();
		TransmitterNetworkRegistry.getInstance().pruneEmptyNetworks();
	}
	
	@Override
	public boolean canUpdate()
	{
		return true;
	}
	
	@Override
	public void validate()
	{
		super.validate();
		
		if(worldObj.isRemote)
		{
			PacketHandler.sendPacket(Transmission.SERVER, new PacketDataRequest().setParams(Object3D.get(this)));
		}
	}
	
	@Override
	public void handlePacketData(ByteArrayDataInput dataStream)
	{
		isActive = dataStream.readBoolean();
	}
	
	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		data.add(isActive);
		return data;
	}
	
	@Override
    public void readFromNBT(NBTTagCompound nbtTags)
    {
        super.readFromNBT(nbtTags);

        isActive = nbtTags.getBoolean("isActive");
    }

	@Override
    public void writeToNBT(NBTTagCompound nbtTags)
    {
        super.writeToNBT(nbtTags);
        
        nbtTags.setBoolean("isActive", isActive);
    }
	
	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox()
	{
		return INFINITE_EXTENT_AABB;
	}
}
