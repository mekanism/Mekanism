package universalelectricity.prefab;

import net.minecraft.src.Block;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.NetworkManager;
import net.minecraft.src.Packet250CustomPayload;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.electricity.ElectricityManager;
import universalelectricity.implement.IConductor;
import universalelectricity.implement.IConnector;
import universalelectricity.network.IPacketReceiver;
import universalelectricity.network.PacketManager;

import com.google.common.io.ByteArrayDataInput;

/**
 * This tile entity prefabricated for all conductors.
 * @author Calclavia
 *
 */
public abstract class TileEntityConductor extends AdvancedTile implements IConductor, IPacketReceiver
{
    private int connectionID = 0;

    /**
     * Stores information on the blocks that this conductor is connected to
     */
    public TileEntity[] connectedBlocks = {null, null, null, null, null, null};

    public TileEntityConductor()
    {
        this.reset();
    }
    
    @Override
    public int getConnectionID()
    {
    	return this.connectionID;
    }
    
    @Override
    public void setConnectionID(int ID)
    {
    	this.connectionID = ID;
    }
    
    @Override
    public TileEntity[] getConnectedBlocks()
    {
    	return connectedBlocks;
    }
    
    @Override
    public void initiate()
    {
    	this.refreshConnectedBlocks();
    }

    /**
     * Adds a connection between this conductor and a UE unit
     * @param tileEntity - Must be either a producer, consumer or a conductor
     * @param side - side in which the connection is coming from
     */
    public void updateConnection(TileEntity tileEntity, ForgeDirection side)
    {    	
		if(tileEntity != null)
		{
	        if(tileEntity instanceof IConnector)
	        {
	            this.connectedBlocks[side.ordinal()] = tileEntity;
	
	            if (tileEntity.getClass() == this.getClass())
	            {
	                ElectricityManager.instance.mergeConnection(this.connectionID, ((TileEntityConductor)tileEntity).connectionID);
	            }
	            
	            return;
	        }
		}
		
        if (this.connectedBlocks[side.ordinal()] != null)
        {
            if (this.connectedBlocks[side.ordinal()] instanceof IConductor)
            {
                ElectricityManager.instance.splitConnection(this, (IConductor)this.getConnectedBlocks()[side.ordinal()]);
            }
        }

        this.connectedBlocks[side.ordinal()] = null;
    }

    @Override
    public void updateConnectionWithoutSplit(TileEntity tileEntity, ForgeDirection side)
    {    	
    	if(tileEntity != null)
		{
	        if(tileEntity instanceof IConnector)
	        {
	            this.connectedBlocks[side.ordinal()] = tileEntity;
	
	            if (tileEntity.getClass() == this.getClass())
	            {
	                ElectricityManager.instance.mergeConnection(this.connectionID, ((TileEntityConductor)tileEntity).connectionID);
	            }
	            
	            return;
	        }
    	}
    	
    	 this.connectedBlocks[side.ordinal()] = null;
    }
    
    @Override
    public void handlePacketData(NetworkManager network, int type, Packet250CustomPayload packet, EntityPlayer player, ByteArrayDataInput dataStream)
    {
    	if(this.worldObj.isRemote)
    	{
    		this.refreshConnectedBlocks();
    	}
    }
    
    /**
     * Determines if this TileEntity requires update calls.
     * @return True if you want updateEntity() to be called, false if not
     */
    @Override
    public boolean canUpdate()
    {
        return false;
    }

    @Override
    public void reset()
    {
        this.connectionID = 0;
        
        if(ElectricityManager.instance != null)
        {
            ElectricityManager.instance.registerConductor(this);
        }
    }

    @Override
    public void refreshConnectedBlocks()
    {
        if (this.worldObj != null)
        {
            BlockConductor.updateConductorTileEntity(this.worldObj, this.xCoord, this.yCoord, this.zCoord);
        }
    }

    @Override
    public World getWorld()
    {
    	return this.worldObj;
    }
    
    @Override
	public boolean canConnect(ForgeDirection side) 
    {
		return true;
	}
    
    public Block getBlockType()
    {
        if (this.blockType == null)
        {
            this.blockType = Block.blocksList[this.worldObj.getBlockId(this.xCoord, this.yCoord, this.zCoord)];
        }

        return this.blockType;
    }
}
