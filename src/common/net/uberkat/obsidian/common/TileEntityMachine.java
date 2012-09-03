package net.uberkat.obsidian.common;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.server.FMLServerHandler;

import net.minecraft.src.*;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.ISidedInventory;

public class TileEntityMachine extends TileEntity implements IInventory, ISidedInventory, INetworkedMachine
{
     /** The ItemStacks that hold the items currently being used in the furnace */
    protected ItemStack[] machineItemStacks = new ItemStack[3];

    /** The number of ticks that the furnace will keep burning */
    public int machineBurnTime = 0;

    /** The number of ticks that a fresh copy of the currently-burning item would keep the furnace burning for */
    public int currentItemBurnTime = 0;

    /** The number of ticks that the current item has been cooking for */
    public int machineCookTime = 0;
    
    /** The number of ticks it takes to cook an item */
    public int maxBurnTime = 0;
    
    /** The full name of this tile entity */
    public String fullName;
    
    /** Whether the machine is in it's active state or not */
    public boolean isActive;
    
    /** The machine's previous active state */
    public boolean prevActive;
    
    /** How many ticks have passed since the last texture tick. */
    public int textureTick = 0;
    
    /**
     * Instance of TileEntityMachine. Extend this for a head start on machine making.
     * @param time - time it takes to smelt an item
     * @param name - full display name of the item
     */
    public TileEntityMachine(int time, String name)
    {
    	maxBurnTime = time;
    	fullName = name;
    	isActive = false;
    }
	
	public int getStartInventorySide(ForgeDirection side) 
	{
        if (side == ForgeDirection.DOWN) return 1;
        if (side == ForgeDirection.UP) return 0; 
        return 2;
	}

	public int getSizeInventorySide(ForgeDirection side)
	{
		return 1;
	}

	public int getSizeInventory() 
	{
		return machineItemStacks.length;
	}

	public ItemStack getStackInSlot(int var1) 
	{
		return machineItemStacks[var1];
	}
	
	public void updateEntity()
	{
		onUpdate();
		updateTextureTick();
	}
	
	/**
	 * Update call for machines, called every tick. Use this instead of updateEntity().
	 */
	public void onUpdate() {}
	
	/**
	 * Texture update call for machines. Use this to switch to a different texture. Called every 3 ticks.
	 */
	public void updateTexture() 
	{
		BlockMachine.updateTexture(worldObj, xCoord, yCoord, zCoord);
	}
	
	/**
	 * Constant check to see when to run updateTexture(). Called every tick, but functions every 3.
	 */
	public void updateTextureTick()
	{
		if(textureTick % 3 == 0)
		{
			updateTexture();
		}
		textureTick++;
	}
	
    public ItemStack decrStackSize(int par1, int par2)
    {
        if (machineItemStacks[par1] != null)
        {
            ItemStack var3;

            if (machineItemStacks[par1].stackSize <= par2)
            {
                var3 = machineItemStacks[par1];
                machineItemStacks[par1] = null;
                return var3;
            }
            else
            {
                var3 = machineItemStacks[par1].splitStack(par2);

                if (machineItemStacks[par1].stackSize == 0)
                {
                    machineItemStacks[par1] = null;
                }

                return var3;
            }
        }
        else
        {
            return null;
        }
    }

    public ItemStack getStackInSlotOnClosing(int par1)
    {
        if (machineItemStacks[par1] != null)
        {
            ItemStack var2 = machineItemStacks[par1];
            machineItemStacks[par1] = null;
            return var2;
        }
        else
        {
            return null;
        }
    }

    public void setInventorySlotContents(int par1, ItemStack par2ItemStack)
    {
        machineItemStacks[par1] = par2ItemStack;

        if (par2ItemStack != null && par2ItemStack.stackSize > getInventoryStackLimit())
        {
            par2ItemStack.stackSize = getInventoryStackLimit();
        }
    }

	public String getInvName()
	{
		return fullName;
	}

	public int getInventoryStackLimit() 
	{
		return 64;
	}

    public boolean isUseableByPlayer(EntityPlayer par1EntityPlayer)
    {
        return worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) != this ? false : par1EntityPlayer.getDistanceSq((double)xCoord + 0.5D, (double)yCoord + 0.5D, (double)zCoord + 0.5D) <= 64.0D;
    }
    
    public int getBurnTimeRemainingScaled(int par1)
    {
        if (currentItemBurnTime == 0)
        {
            currentItemBurnTime = maxBurnTime;
        }

        return machineBurnTime * par1 / currentItemBurnTime;
    }
    
    public boolean isBurning()
    {
        return machineBurnTime > 0;
    }
    
    public int getCookProgressScaled(int par1)
    {
        return machineCookTime * par1 / maxBurnTime;
    }
    
    /**
     * Use this method to change a machine's active/inactive state. It will send a packet to the client with the update.
     * @param active
     */
    public void setActive(boolean active)
    {
    	isActive = active;
    	
    	if(prevActive != active)
    	{
    		PacketHandler.sendMachinePacket(this);
    	}
    	
    	prevActive = active;
    }

	public void openChest() {}

	public void closeChest() {}
	
	public void handlePacketData(NetworkManager network, Packet250CustomPayload packet, EntityPlayer player, ByteArrayDataInput dataStream) 
	{
		try {
			isActive = dataStream.readByte() != 0;
			machineBurnTime = dataStream.readInt();
			machineCookTime = dataStream.readInt();
			currentItemBurnTime = dataStream.readInt();
		} catch (Exception e)
		{
			System.out.println("[ObsidianIngots] Error while handling tile entity packet.");
			e.printStackTrace();
		}
	}
}
