package mekanism.common;

import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;

import java.math.BigDecimal;
import java.math.RoundingMode;

import mekanism.api.EnergizedItemManager;
import mekanism.api.IEnergizedItem;
import mekanism.api.Object3D;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import thermalexpansion.api.item.IChargeableItem;
import universalelectricity.core.item.ElectricItemHelper;
import universalelectricity.core.item.IItemElectric;
import codechicken.core.alg.MathHelper;

public class EntityRobit extends EntityCreature implements IInventory, ISustainedInventory
{
	public double MAX_ELECTRICITY = 100000;
	
	public Object3D homeLocation;
	
	public ItemStack[] inventory = new ItemStack[31];
	
    public int furnaceBurnTime = 0;
    public int currentItemBurnTime = 0;
    public int furnaceCookTime = 0;
	
	public EntityRobit(World world) 
	{
		super(world);
		
		setSize(1, 1);
		moveSpeed = 0.35F;
		texture = "/mods/mekanism/render/Robit.png";
		
		getNavigator().setAvoidsWater(true);
		
		tasks.addTask(1, new RobitAIFollow(this, moveSpeed, 5.0F, 2.0F));
		tasks.addTask(2, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
		tasks.addTask(2, new EntityAILookIdle(this));
		tasks.addTask(3, new EntityAISwimming(this));
	}
	
	public EntityRobit(World world, double x, double y, double z)
	{
		this(world);
		
		setPosition(x, y, z);
		
		prevPosX = x; 
		prevPosY = y;
		prevPosZ = z;
	}
	
	@Override
	public boolean isAIEnabled()
	{
		return true;
	}
	
	@Override
	protected boolean canDespawn()
	{
		return false;
	}
	
	@Override
	protected void updateAITick() {}
	
	@Override
	protected void entityInit()
	{
		super.entityInit();
		
		dataWatcher.addObject(11, new String("")); /* Electricity */
		dataWatcher.addObject(12, new String("")); /* Owner */
		dataWatcher.addObject(13, new Byte((byte)0)); /* Follow */
	}
	
	public double getRoundedTravelEnergy()
	{
		return new BigDecimal(getDistance(prevPosX, prevPosY, prevPosZ)*1.5).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
	}
	
	@Override
	public void onEntityUpdate()
	{
		if(!worldObj.isRemote)
		{
			if(getFollowing() && getOwner() != null && getDistanceSqToEntity(getOwner()) > 4 && !getNavigator().noPath() && getEnergy() > 0)
			{
				setEnergy(getEnergy() - getRoundedTravelEnergy());
			}
		}
		
		super.onEntityUpdate();
		
		if(!worldObj.isRemote)
		{
			if(homeLocation == null)
			{
				setDead();
				
				return;
			}
			
			if(!(homeLocation.getTileEntity(worldObj) instanceof TileEntityChargepad))
			{
				drop();
				setDead();
				
				return;
			}
			
			if(getEnergy() == 0 && !isOnChargepad())
			{
				setFollowing(false);
				setPositionAndUpdate(homeLocation.xCoord+0.5, homeLocation.yCoord+0.3, homeLocation.zCoord+0.5);
				
				motionX = 0;
				motionY = 0;
				motionZ = 0;
			}
			
			if(inventory[27] != null && getEnergy() < MAX_ELECTRICITY)
			{
				if(inventory[27].getItem() instanceof IEnergizedItem)
				{
					setEnergy(getEnergy() + EnergizedItemManager.discharge(inventory[27], MAX_ELECTRICITY - getEnergy()));
				}
				else if(inventory[27].getItem() instanceof IItemElectric)
				{
					setEnergy(getEnergy() + ElectricItemHelper.dechargeItem(inventory[27], MAX_ELECTRICITY - getEnergy(), 120/*VOLTAGE*/));
				}
				else if(Mekanism.hooks.IC2Loaded && inventory[27].getItem() instanceof IElectricItem)
				{
					IElectricItem item = (IElectricItem)inventory[27].getItem();
					
					if(item.canProvideEnergy(inventory[27]))
					{
						double gain = ElectricItem.discharge(inventory[27], (int)((MAX_ELECTRICITY - getEnergy())*Mekanism.TO_IC2), 3, false, false)*Mekanism.FROM_IC2;
						setEnergy(getEnergy() + gain);
					}
				}
				else if(inventory[27].getItem() instanceof IChargeableItem)
				{
					ItemStack itemStack = inventory[27];
					IChargeableItem item = (IChargeableItem)inventory[27].getItem();
					
					float itemEnergy = (float)Math.min(Math.sqrt(item.getMaxEnergyStored(itemStack)), item.getEnergyStored(itemStack));
					float toTransfer = (float)Math.min(itemEnergy, ((MAX_ELECTRICITY - getEnergy())*Mekanism.TO_BC));
					
					item.transferEnergy(itemStack, toTransfer, true);
					setEnergy(getEnergy() + (toTransfer*Mekanism.FROM_BC));
				}
				else if(inventory[27].itemID == Item.redstone.itemID && getEnergy()+1000 <= MAX_ELECTRICITY)
				{
					setEnergy(getEnergy() + 1000);
					inventory[27].stackSize--;
					
		            if(inventory[27].stackSize <= 0)
		            {
		                inventory[27] = null;
		            }
				}
			}

	        if(furnaceBurnTime > 0)
	        {
	            furnaceBurnTime--;
	        }

	        if(!worldObj.isRemote)
	        {
	            if(furnaceBurnTime == 0 && canSmelt())
	            {
	                currentItemBurnTime = furnaceBurnTime = TileEntityFurnace.getItemBurnTime(inventory[29]);

	                if(furnaceBurnTime > 0)
	                {
	                    if(inventory[29] != null)
	                    {
	                        inventory[29].stackSize--;

	                        if(inventory[29].stackSize == 0)
	                        {
	                            inventory[29] = inventory[29].getItem().getContainerItemStack(inventory[29]);
	                        }
	                    }
	                }
	            }

	            if(furnaceBurnTime > 0 && canSmelt())
	            {
	                furnaceCookTime++;

	                if(furnaceCookTime == 200)
	                {
	                    furnaceCookTime = 0;
	                    smeltItem();
	                }
	            }
	            else {
	                furnaceCookTime = 0;
	            }
	        }
		}
	}

    private boolean canSmelt()
    {
        if(inventory[28] == null)
        {
            return false;
        }
        else {
            ItemStack itemstack = FurnaceRecipes.smelting().getSmeltingResult(inventory[28]);
            if(itemstack == null) return false;
            if(inventory[30] == null) return true;
            if(!inventory[30].isItemEqual(itemstack)) return false;
            int result = inventory[30].stackSize + itemstack.stackSize;
            return (result <= getInventoryStackLimit() && result <= itemstack.getMaxStackSize());
        }
    }

    public void smeltItem()
    {
        if(canSmelt())
        {
            ItemStack itemstack = FurnaceRecipes.smelting().getSmeltingResult(inventory[28]);

            if(inventory[30] == null)
            {
                inventory[30] = itemstack.copy();
            }
            else if(inventory[30].isItemEqual(itemstack))
            {
                inventory[30].stackSize += itemstack.stackSize;
            }

            inventory[28].stackSize--;

            if(inventory[28].stackSize <= 0)
            {
                inventory[28] = null;
            }
        }
    }
	
	public boolean isOnChargepad()
	{
		int x = MathHelper.floor_double(posX);
		int y = MathHelper.floor_double(posY);
		int z = MathHelper.floor_double(posZ);
		
		if(worldObj.getBlockTileEntity(x, y, z) instanceof TileEntityChargepad)
		{
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean interact(EntityPlayer entityplayer)
	{
		if(entityplayer.isSneaking())
		{
			ItemStack itemStack = entityplayer.getCurrentEquippedItem();
			
			if(itemStack != null && itemStack.getItem() instanceof ItemConfigurator)
			{
				if(!worldObj.isRemote)
				{
					drop();
				}
				
				setDead();
				
				entityplayer.swingItem();
				return true;
			}
		}
		else {
			entityplayer.openGui(Mekanism.instance, 21, worldObj, entityId, 0, 0);
		}
		
		return false;
	}
	
	public void drop()
	{
		EntityItem entityItem = new EntityItem(worldObj, posX, posY+0.3, posZ, new ItemStack(Mekanism.Robit));
		
		ItemRobit item = (ItemRobit)entityItem.getEntityItem().getItem();
		item.setEnergy(entityItem.getEntityItem(), getEnergy());
		item.setInventory(getInventory(), entityItem.getEntityItem());
		
	    float k = 0.05F;
	    entityItem.motionX = 0;
        entityItem.motionY = rand.nextGaussian() * k + 0.2F;
        entityItem.motionZ = 0;
        
        worldObj.spawnEntityInWorld(entityItem);
	}

	@Override
    public void writeEntityToNBT(NBTTagCompound nbtTags)
    {
    	super.writeEntityToNBT(nbtTags);
    	
    	nbtTags.setDouble("electricityStored", getEnergy());
    	
    	if(getOwnerName() != null)
    	{
    		nbtTags.setString("owner", getOwnerName());
    	}
    	
    	nbtTags.setBoolean("follow", getFollowing());
    	
    	homeLocation.write(nbtTags);
    	
        NBTTagList tagList = new NBTTagList();

        for(int slotCount = 0; slotCount < inventory.length; slotCount++)
        {
            if(inventory[slotCount] != null)
            {
                NBTTagCompound tagCompound = new NBTTagCompound();
                tagCompound.setByte("Slot", (byte)slotCount);
                inventory[slotCount].writeToNBT(tagCompound);
                tagList.appendTag(tagCompound);
            }
        }

        nbtTags.setTag("Items", tagList);
    }

	@Override
    public void readEntityFromNBT(NBTTagCompound nbtTags)
    {
    	super.readEntityFromNBT(nbtTags);
    	
    	setEnergy(nbtTags.getDouble("electricityStored"));
    	
    	if(nbtTags.hasKey("owner"))
    	{
    		setOwner(nbtTags.getString("owner"));
    	}
    	
    	setFollowing(nbtTags.getBoolean("follow"));
    	
    	homeLocation = Object3D.read(nbtTags);
    	
        NBTTagList tagList = nbtTags.getTagList("Items");
        inventory = new ItemStack[getSizeInventory()];

        for(int tagCount = 0; tagCount < tagList.tagCount(); tagCount++)
        {
            NBTTagCompound tagCompound = (NBTTagCompound)tagList.tagAt(tagCount);
            byte slotID = tagCompound.getByte("Slot");

            if(slotID >= 0 && slotID < inventory.length)
            {
                inventory[slotID] = ItemStack.loadItemStackFromNBT(tagCompound);
            }
        }
    }
	
	@Override
    protected void damageEntity(DamageSource damageSource, int amount)
    {
        amount = ForgeHooks.onLivingHurt(this, damageSource, amount);
        
        if(amount <= 0)
        {
            return;
        }
        
        amount = applyArmorCalculations(damageSource, amount);
        amount = applyPotionDamageCalculations(damageSource, amount);
        int j = getHealth();
        
        setEnergy(Math.max(0, getEnergy() - (amount*1000)));
        field_94063_bt.func_94547_a(damageSource, j, amount);
    }
	
	@Override
	protected void onDeathUpdate() {}
	
	public void setHome(Object3D home)
	{
		homeLocation = home;
	}
	
	@Override
	public boolean canBePushed()
	{
		return getEnergy() > 0;
	}
	
	public double getEnergy()
	{
		return Double.parseDouble(dataWatcher.getWatchableObjectString(11));
	}
	
	public void setEnergy(double energy)
	{
		dataWatcher.updateObject(11, Double.toString(Math.max(Math.min(energy, MAX_ELECTRICITY), 0)));
	}
	
	public EntityPlayer getOwner()
	{
		return worldObj.getPlayerEntityByName(getOwnerName());
	}
	
	public String getOwnerName()
	{
		return dataWatcher.getWatchableObjectString(12);
	}
	
	public void setOwner(String username)
	{
		dataWatcher.updateObject(12, username);
	}
	
	public boolean getFollowing()
	{
		return dataWatcher.getWatchableObjectByte(13) == 1;
	}
	
	public void setFollowing(boolean follow)
	{
		dataWatcher.updateObject(13, follow ? (byte)1 : (byte)0);
	}

	@Override
	public int getMaxHealth() 
	{
		return 1;
	}

	@Override
	public int getSizeInventory()
	{
		return inventory.length;
	}

	@Override
	public ItemStack getStackInSlot(int slotID) 
	{
		return inventory[slotID];
	}

	@Override
	public ItemStack decrStackSize(int slotID, int amount)
	{
		if(getStackInSlot(slotID) != null)
        {
            ItemStack tempStack;

            if(getStackInSlot(slotID).stackSize <= amount)
            {
                tempStack = getStackInSlot(slotID);
                setInventorySlotContents(slotID, null);
                return tempStack;
            }
            else {
                tempStack = getStackInSlot(slotID).splitStack(amount);

                if(getStackInSlot(slotID).stackSize == 0)
                {
                	setInventorySlotContents(slotID, null);
                }

                return tempStack;
            }
        }
        else {
            return null;
        }
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slotID) 
	{
        if(getStackInSlot(slotID) != null)
        {
            ItemStack tempStack = getStackInSlot(slotID);
            setInventorySlotContents(slotID, null);
            return tempStack;
        }
        else {
            return null;
        }
	}

	@Override
    public void setInventorySlotContents(int slotID, ItemStack itemstack)
    {
        inventory[slotID] = itemstack;

        if(itemstack != null && itemstack.stackSize > getInventoryStackLimit())
        {
            itemstack.stackSize = getInventoryStackLimit();
        }
    }

	@Override
	public String getInvName() 
	{
		return "Robit";
	}

	@Override
	public boolean isInvNameLocalized() 
	{
		return true;
	}

	@Override
	public int getInventoryStackLimit() 
	{
		return 64;
	}

	@Override
	public void onInventoryChanged() {}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) 
	{
		return true;
	}

	@Override
	public void openChest() {}

	@Override
	public void closeChest() {}

	@Override
	public boolean isStackValidForSlot(int i, ItemStack itemstack) 
	{
		return true;
	}
	
	@Override
	public void setInventory(NBTTagList nbtTags, Object... data) 
	{
		if(nbtTags == null || nbtTags.tagCount() == 0)
		{
			return;
		}
		
        inventory = new ItemStack[getSizeInventory()];

        for(int slots = 0; slots < nbtTags.tagCount(); slots++)
        {
            NBTTagCompound tagCompound = (NBTTagCompound)nbtTags.tagAt(slots);
            byte slotID = tagCompound.getByte("Slot");

            if(slotID >= 0 && slotID < inventory.length)
            {
                inventory[slotID] = ItemStack.loadItemStackFromNBT(tagCompound);
            }
        }
	}

	@Override
	public NBTTagList getInventory(Object... data)
	{
        NBTTagList tagList = new NBTTagList();

        for(int slots = 0; slots < inventory.length; slots++)
        {
            if(inventory[slots] != null)
            {
                NBTTagCompound tagCompound = new NBTTagCompound();
                tagCompound.setByte("Slot", (byte)slots);
                inventory[slots].writeToNBT(tagCompound);
                tagList.appendTag(tagCompound);
            }
        }

        return tagList;
	}
	
    @Override
    public float getShadowSize()
    {
        return 0.25F;
    }
}
