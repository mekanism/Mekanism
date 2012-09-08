package net.uberkat.obsidian.common;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.asm.SideOnly;
import cpw.mods.fml.server.FMLServerHandler;

import net.minecraft.src.*;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.ISidedInventory;
import net.uberkat.obsidian.client.AudioManager;
import net.uberkat.obsidian.client.AudioSource;

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
    
    /** The direction this machine is facing */
    public int facing;
    
    /** How many ticks have passed since the last texture tick. */
    public int textureTick = 0;

    /** The amount of update ticks have passed since the game started */
    public byte updateTicks = 0;
    
    /** An integer that constantly cycles from 0 to 15. Used for animated textures. */
    public int textureIndex = 0;
    
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
		if(FMLCommonHandler.instance().getSidedDelegate().getSide().isClient())
		{
			updateTextureTick();
		}
		updateTick();
		
		if(machineCookTime == 0 || machineCookTime == maxBurnTime && currentItemBurnTime != 0)
		{
			currentItemBurnTime = 0;
		}
	}
	
	/**
	 * Update call for machines, called every tick. Use this instead of updateEntity().
	 */
	public void onUpdate() {}
	
	/**
	 * Synchronizes the client with the server on startup by sending two packets.
	 * Not exactly sure why it needs 5 packets, but it wouldn't work with only 4!
	 */
	public void updateTick()
	{
		if(updateTicks < 5)
		{
			PacketHandler.sendMachinePacket(this);
			updateTicks++;
		}
	}
	
	/**
	 * Check to see when to run updateTexture(). Called every tick, but functions every 3.
	 */
	@SideOnly(Side.CLIENT)
	public void updateTextureTick()
	{
		if(textureTick % 5 == 0)
		{
			updateTexture(worldObj, xCoord, yCoord, zCoord);
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
    
    public void setFacing(int direction)
    {
    	facing = direction;
    	
    	PacketHandler.sendMachinePacket(this);
    }
    
    public void updateTexture(World world, int x, int y, int z)
    {
    	if(textureIndex < 15) textureIndex++;
    	if(textureIndex == 15) textureIndex = 0;
    	
    	world.markBlockAsNeedsUpdate(x, y, z);
    }
    
    public void readFromNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.readFromNBT(par1NBTTagCompound);
        NBTTagList var2 = par1NBTTagCompound.getTagList("Items");
        machineItemStacks = new ItemStack[getSizeInventory()];

        for (int var3 = 0; var3 < var2.tagCount(); ++var3)
        {
            NBTTagCompound var4 = (NBTTagCompound)var2.tagAt(var3);
            byte var5 = var4.getByte("Slot");

            if (var5 >= 0 && var5 < machineItemStacks.length)
            {
                machineItemStacks[var5] = ItemStack.loadItemStackFromNBT(var4);
            }
        }

        machineBurnTime = par1NBTTagCompound.getInteger("machineBurnTime");
        machineCookTime = par1NBTTagCompound.getInteger("machineCookTime");
        currentItemBurnTime = par1NBTTagCompound.getInteger("currentItemBurnTime");
        isActive = par1NBTTagCompound.getBoolean("isActive");
        facing = par1NBTTagCompound.getInteger("facing");
    }

    public void writeToNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.writeToNBT(par1NBTTagCompound);
        par1NBTTagCompound.setInteger("machineBurnTime", machineBurnTime);
        par1NBTTagCompound.setInteger("machineCookTime", machineCookTime);
        par1NBTTagCompound.setInteger("currentItemBurnTime", currentItemBurnTime);
        par1NBTTagCompound.setBoolean("isActive", isActive);
        par1NBTTagCompound.setInteger("facing", facing);
        NBTTagList var2 = new NBTTagList();

        for (int var3 = 0; var3 < machineItemStacks.length; ++var3)
        {
            if (machineItemStacks[var3] != null)
            {
                NBTTagCompound var4 = new NBTTagCompound();
                var4.setByte("Slot", (byte)var3);
                machineItemStacks[var3].writeToNBT(var4);
                var2.appendTag(var4);
            }
        }

        par1NBTTagCompound.setTag("Items", var2);
    }
    
    public void getBurnTime() {}

	public void openChest() {}

	public void closeChest() {}
	
	public void handlePacketData(NetworkManager network, Packet250CustomPayload packet, EntityPlayer player, ByteArrayDataInput dataStream) 
	{
		try {
			facing = dataStream.readInt();
			isActive = dataStream.readByte() != 0;
			machineBurnTime = dataStream.readInt();
			machineCookTime = dataStream.readInt();
			currentItemBurnTime = dataStream.readInt();
			worldObj.markBlockAsNeedsUpdate(xCoord, yCoord, zCoord);
		} catch (Exception e)
		{
			System.out.println("[ObsidianIngots] Error while handling tile entity packet.");
			e.printStackTrace();
		}
	}
}
