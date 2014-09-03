package mekanism.common.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import mekanism.api.Coord4D;
import mekanism.api.MekanismConfig.general;
import mekanism.api.energy.EnergizedItemManager;
import mekanism.api.energy.IEnergizedItem;
import mekanism.common.Mekanism;
import mekanism.common.MekanismItems;
import mekanism.common.RobitAIFollow;
import mekanism.common.RobitAIPickup;
import mekanism.common.base.ISustainedInventory;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.item.ItemRobit;
import mekanism.common.tile.TileEntityChargepad;
import mekanism.common.util.MekanismUtils;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.Constants;
import cpw.mods.fml.common.Optional.Interface;

import cofh.api.energy.IEnergyContainerItem;
import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;
import micdoodle8.mods.galacticraft.api.entity.IEntityBreathable;

@Interface(iface = "micdoodle8.mods.galacticraft.api.entity.IEntityBreathable", modid = "Galacticraft API")
public class EntityRobit extends EntityCreature implements IInventory, ISustainedInventory, IEntityBreathable
{
	public double MAX_ELECTRICITY = 100000;

	public Coord4D homeLocation;

	public ItemStack[] inventory = new ItemStack[31];

	public int furnaceBurnTime = 0;
	public int currentItemBurnTime = 0;
	public int furnaceCookTime = 0;

	public boolean texTick;

	public EntityRobit(World world)
	{
		super(world);

		setSize(0.5F, 0.5F);

		getNavigator().setAvoidsWater(true);

		tasks.addTask(1, new RobitAIPickup(this, 1.0F));
		tasks.addTask(2, new RobitAIFollow(this, 1.0F, 4.0F, 2.0F));
		tasks.addTask(3, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
		tasks.addTask(3, new EntityAILookIdle(this));
		tasks.addTask(4, new EntityAISwimming(this));

		setAlwaysRenderNameTag(true);
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
	protected void applyEntityAttributes()
	{
		super.applyEntityAttributes();

		getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.3);
		getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(1);
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

		dataWatcher.addObject(12, new String("")); /* Electricity */
		dataWatcher.addObject(13, new String("")); /* Owner */
		dataWatcher.addObject(14, new Byte((byte)0)); /* Follow */
		dataWatcher.addObject(15, new String("")); /* Name */
		dataWatcher.addObject(16, new Byte((byte)0)); /* Drop Pickup */
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
			if(getDropPickup())
			{
				collectItems();
			}

			if(homeLocation == null)
			{
				setDead();

				return;
			}

			if(!(homeLocation.getTileEntity(MinecraftServer.getServer().worldServerForDimension(homeLocation.dimensionId)) instanceof TileEntityChargepad))
			{
				drop();
				setDead();

				return;
			}

			if(getEnergy() == 0 && !isOnChargepad())
			{
				goHome();
			}

			if(inventory[27] != null && getEnergy() < MAX_ELECTRICITY)
			{
				if(inventory[27].getItem() instanceof IEnergizedItem)
				{
					setEnergy(getEnergy() + EnergizedItemManager.discharge(inventory[27], MAX_ELECTRICITY - getEnergy()));
				}
				else if(MekanismUtils.useIC2() && inventory[27].getItem() instanceof IElectricItem)
				{
					IElectricItem item = (IElectricItem)inventory[27].getItem();

					if(item.canProvideEnergy(inventory[27]))
					{
						double gain = ElectricItem.manager.discharge(inventory[27], (MAX_ELECTRICITY - getEnergy())* general.TO_IC2, 4, true, true, false)* general.FROM_IC2;
						setEnergy(getEnergy() + gain);
					}
				}
				else if(MekanismUtils.useRF() && inventory[27].getItem() instanceof IEnergyContainerItem)
				{
					ItemStack itemStack = inventory[27];
					IEnergyContainerItem item = (IEnergyContainerItem)inventory[27].getItem();

					int itemEnergy = (int)Math.round(Math.min(Math.sqrt(item.getMaxEnergyStored(itemStack)), item.getEnergyStored(itemStack)));
					int toTransfer = (int)Math.round(Math.min(itemEnergy, ((MAX_ELECTRICITY - getEnergy())* general.TO_TE)));

					setEnergy(getEnergy() + (item.extractEnergy(itemStack, toTransfer, false)* general.FROM_TE));
				}
				else if(inventory[27].getItem() == Items.redstone && getEnergy()+ general.ENERGY_PER_REDSTONE <= MAX_ELECTRICITY)
				{
					setEnergy(getEnergy() + general.ENERGY_PER_REDSTONE);
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
								inventory[29] = inventory[29].getItem().getContainerItem(inventory[29]);
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

	private void collectItems()
	{
		List<EntityItem> items = worldObj.getEntitiesWithinAABB(EntityItem.class, boundingBox.expand(1.5, 1.5, 1.5));

		if(items != null && !items.isEmpty())
		{
			for(EntityItem item : items)
			{
				if(item.delayBeforeCanPickup > 0 || item.getEntityItem().getItem() instanceof ItemRobit)
				{
					continue;
				}

				for(int i = 0; i < 27; i++)
				{
					ItemStack itemStack = inventory[i];

					if(itemStack == null)
					{
						inventory[i] = item.getEntityItem();
						onItemPickup(item, item.getEntityItem().stackSize);
						item.setDead();

						playSound("random.pop", 1.0F, ((rand.nextFloat() - rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);

						break;
					}
					else if(itemStack.isItemEqual(item.getEntityItem()) && itemStack.stackSize < itemStack.getMaxStackSize())
					{
						int needed = itemStack.getMaxStackSize() - itemStack.stackSize;
						int toAdd = Math.min(needed, item.getEntityItem().stackSize);

						itemStack.stackSize += toAdd;
						item.getEntityItem().stackSize -= toAdd;

						onItemPickup(item, toAdd);

						if(item.getEntityItem().stackSize == 0)
						{
							item.setDead();
						}

						playSound("random.pop", 1.0F, ((rand.nextFloat() - rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);

						break;
					}
				}
			}
		}
	}

	public void goHome()
	{
		setFollowing(false);

		if(worldObj.provider.dimensionId != homeLocation.dimensionId)
		{
			travelToDimension(homeLocation.dimensionId);
		}

		setPositionAndUpdate(homeLocation.xCoord+0.5, homeLocation.yCoord+0.3, homeLocation.zCoord+0.5);

		motionX = 0;
		motionY = 0;
		motionZ = 0;
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

		if(worldObj.getTileEntity(x, y, z) instanceof TileEntityChargepad)
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
			entityplayer.openGui(Mekanism.instance, 21, worldObj, getEntityId(), 0, 0);
		}

		return false;
	}

	public void drop()
	{
		EntityItem entityItem = new EntityItem(worldObj, posX, posY+0.3, posZ, new ItemStack(MekanismItems.Robit));

		ItemRobit item = (ItemRobit)entityItem.getEntityItem().getItem();
		item.setEnergy(entityItem.getEntityItem(), getEnergy());
		item.setInventory(getInventory(), entityItem.getEntityItem());
		item.setName(entityItem.getEntityItem(), getCommandSenderName());

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

		nbtTags.setString("name", getName());

		if(getOwnerName() != null)
		{
			nbtTags.setString("owner", getOwnerName());
		}

		nbtTags.setBoolean("follow", getFollowing());

		nbtTags.setBoolean("dropPickup", getDropPickup());

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

		setName(nbtTags.getString("name"));

		if(nbtTags.hasKey("owner"))
		{
			setOwner(nbtTags.getString("owner"));
		}

		setFollowing(nbtTags.getBoolean("follow"));

		setDropPickup(nbtTags.getBoolean("dropPickup"));

		homeLocation = Coord4D.read(nbtTags);

		NBTTagList tagList = nbtTags.getTagList("Items", Constants.NBT.TAG_COMPOUND);
		inventory = new ItemStack[getSizeInventory()];

		for(int tagCount = 0; tagCount < tagList.tagCount(); tagCount++)
		{
			NBTTagCompound tagCompound = (NBTTagCompound)tagList.getCompoundTagAt(tagCount);
			byte slotID = tagCompound.getByte("Slot");

			if(slotID >= 0 && slotID < inventory.length)
			{
				inventory[slotID] = ItemStack.loadItemStackFromNBT(tagCompound);
			}
		}
	}

	@Override
	protected void damageEntity(DamageSource damageSource, float amount)
	{
		amount = ForgeHooks.onLivingHurt(this, damageSource, amount);

		if(amount <= 0)
		{
			return;
		}

		amount = applyArmorCalculations(damageSource, amount);
		amount = applyPotionDamageCalculations(damageSource, amount);
		float j = getHealth();

		setEnergy(Math.max(0, getEnergy() - (amount*1000)));
		func_110142_aN().func_94547_a(damageSource, j, amount);
	}

	@Override
	protected void onDeathUpdate() {}

	public void setHome(Coord4D home)
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
		try {
			return Double.parseDouble(dataWatcher.getWatchableObjectString(12));
		} catch(Exception e) {
			return 0;
		}
	}

	public void setEnergy(double energy)
	{
		dataWatcher.updateObject(12, Double.toString(Math.max(Math.min(energy, MAX_ELECTRICITY), 0)));
	}

	public EntityPlayer getOwner()
	{
		return worldObj.getPlayerEntityByName(getOwnerName());
	}

	public String getOwnerName()
	{
		return dataWatcher.getWatchableObjectString(13);
	}

	public void setOwner(String username)
	{
		dataWatcher.updateObject(13, username);
	}

	public boolean getFollowing()
	{
		return dataWatcher.getWatchableObjectByte(14) == 1;
	}

	public void setFollowing(boolean follow)
	{
		dataWatcher.updateObject(14, follow ? (byte)1 : (byte)0);
	}

	public String getName()
	{
		return dataWatcher.getWatchableObjectString(15);
	}

	public void setName(String name)
	{
		dataWatcher.updateObject(15, name);
	}

	public boolean getDropPickup()
	{
		return dataWatcher.getWatchableObjectByte(16) == 1;
	}

	public void setDropPickup(boolean pickup)
	{
		dataWatcher.updateObject(16, pickup ? (byte)1 : (byte)0);
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
	public String getInventoryName()
	{
		return "Robit";
	}
	
	@Override
	public boolean hasCustomInventoryName()
	{
	    return true;
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 64;
	}

	@Override
	public void markDirty() {}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer)
	{
		return true;
	}

	@Override
	public void openInventory() {}

	@Override
	public void closeInventory() {}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack)
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
			NBTTagCompound tagCompound = (NBTTagCompound)nbtTags.getCompoundTagAt(slots);
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
    public String getCommandSenderName()
	{
		return getName().isEmpty() ? "Robit" : getName();
	}

	@Override
	public float getShadowSize()
	{
		return 0.25F;
	}

	@Override
	public boolean canBreath()
	{
		return true;
	}
}
