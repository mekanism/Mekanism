package mekanism.common.entity;

import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;

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
import micdoodle8.mods.galacticraft.api.entity.IEntityBreathable;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Optional.Interface;
import cofh.api.energy.IEnergyContainerItem;

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
	
    private static final DataParameter<Float> ELECTRICITY = EntityDataManager.<Float>createKey(EntityRobit.class, DataSerializers.FLOAT);
    private static final DataParameter<String> OWNER = EntityDataManager.<String>createKey(EntityRobit.class, DataSerializers.STRING);
    private static final DataParameter<Boolean> FOLLOW = EntityDataManager.<Boolean>createKey(EntityRobit.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> DROP_PICKUP = EntityDataManager.<Boolean>createKey(EntityRobit.class, DataSerializers.BOOLEAN);

	public EntityRobit(World world)
	{
		super(world);

		setSize(0.5F, 0.5F);
		
		getNavigator().setCanSwim(false);

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
	public PathNavigateGround getNavigator()
	{
		return (PathNavigateGround)navigator;
	}

	@Override
	protected void applyEntityAttributes()
	{
		super.applyEntityAttributes();

		getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3);
		getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(1);
	}

	@Override
	protected boolean canDespawn()
	{
		return false;
	}

	@Override
	protected void entityInit()
	{
		super.entityInit();

		dataManager.register(ELECTRICITY, 0F);
		dataManager.register(OWNER, "");
		dataManager.register(FOLLOW, false);
		dataManager.register(DROP_PICKUP, false);
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

			if(!(homeLocation.getTileEntity(FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(homeLocation.dimensionId)) instanceof TileEntityChargepad))
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
						double gain = ElectricItem.manager.discharge(inventory[27], (MAX_ELECTRICITY - getEnergy())* general.TO_IC2, 4, true, true, false)*general.FROM_IC2;
						setEnergy(getEnergy() + gain);
					}
				}
				else if(MekanismUtils.useRF() && inventory[27].getItem() instanceof IEnergyContainerItem)
				{
					ItemStack itemStack = inventory[27];
					IEnergyContainerItem item = (IEnergyContainerItem)inventory[27].getItem();

					int itemEnergy = (int)Math.round(Math.min(Math.sqrt(item.getMaxEnergyStored(itemStack)), item.getEnergyStored(itemStack)));
					int toTransfer = (int)Math.round(Math.min(itemEnergy, ((MAX_ELECTRICITY - getEnergy())*general.TO_RF)));

					setEnergy(getEnergy() + (item.extractEnergy(itemStack, toTransfer, false)* general.FROM_RF));
				}
				else if(inventory[27].getItem() == Items.REDSTONE && getEnergy()+ general.ENERGY_PER_REDSTONE <= MAX_ELECTRICITY)
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
		List<EntityItem> items = worldObj.getEntitiesWithinAABB(EntityItem.class, getEntityBoundingBox().expand(1.5, 1.5, 1.5));

		if(items != null && !items.isEmpty())
		{
			for(EntityItem item : items)
			{
				if(item.cannotPickup() || item.getEntityItem().getItem() instanceof ItemRobit)
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

						playSound(SoundEvents.ENTITY_ITEM_PICKUP, 1.0F, ((rand.nextFloat() - rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);

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

						playSound(SoundEvents.ENTITY_ITEM_PICKUP, 1.0F, ((rand.nextFloat() - rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);

						break;
					}
				}
			}
		}
	}

	public void goHome()
	{
		setFollowing(false);

		if(worldObj.provider.getDimension() != homeLocation.dimensionId)
		{
			changeDimension(homeLocation.dimensionId);
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
			ItemStack itemstack = FurnaceRecipes.instance().getSmeltingResult(inventory[28]);
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
			ItemStack itemstack = FurnaceRecipes.instance().getSmeltingResult(inventory[28]);

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
		BlockPos pos = new BlockPos(this);

		if(worldObj.getTileEntity(pos) instanceof TileEntityChargepad)
		{
			return true;
		}

		return false;
	}

	@Override
	public EnumActionResult applyPlayerInteraction(EntityPlayer entityplayer, Vec3d vec, ItemStack stack, EnumHand hand)
	{
		if(entityplayer.isSneaking())
		{
			if(stack != null && stack.getItem() instanceof ItemConfigurator)
			{
				if(!worldObj.isRemote)
				{
					drop();
				}

				setDead();

				entityplayer.swingArm(hand);
				return EnumActionResult.SUCCESS;
			}
		}
		else {
			entityplayer.openGui(Mekanism.instance, 21, worldObj, getEntityId(), 0, 0);
			return EnumActionResult.SUCCESS;
		}

		return EnumActionResult.PASS;
	}

	public void drop()
	{
		EntityItem entityItem = new EntityItem(worldObj, posX, posY+0.3, posZ, new ItemStack(MekanismItems.Robit));

		ItemRobit item = (ItemRobit)entityItem.getEntityItem().getItem();
		item.setEnergy(entityItem.getEntityItem(), getEnergy());
		item.setInventory(((ISustainedInventory)this).getInventory(), entityItem.getEntityItem());
		item.setName(entityItem.getEntityItem(), getName());

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

		if(homeLocation != null)
		{
			homeLocation.write(nbtTags);
		}

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

		setCustomNameTag(nbtTags.getString("name"));

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
			NBTTagCompound tagCompound = tagList.getCompoundTagAt(tagCount);
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
		getCombatTracker().trackDamage(damageSource, j, amount);
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
		return (float)dataManager.get(ELECTRICITY);
	}

	public void setEnergy(double energy)
	{
		dataManager.set(ELECTRICITY, (float)Math.max(Math.min(energy, MAX_ELECTRICITY), 0));
	}

	public EntityPlayer getOwner()
	{
		return worldObj.getPlayerEntityByName(getOwnerName());
	}

	public String getOwnerName()
	{
		return dataManager.get(OWNER);
	}

	public void setOwner(String username)
	{
		dataManager.set(OWNER, username);
	}

	public boolean getFollowing()
	{
		return dataManager.get(FOLLOW);
	}

	public void setFollowing(boolean follow)
	{
		dataManager.set(FOLLOW, follow);
	}

	public boolean getDropPickup()
	{
		return dataManager.get(DROP_PICKUP);
	}

	public void setDropPickup(boolean pickup)
	{
		dataManager.set(DROP_PICKUP, pickup);
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
	public ItemStack removeStackFromSlot(int slotID)
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
	public void openInventory(EntityPlayer player) {}

	@Override
	public void closeInventory(EntityPlayer player) {}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack)
	{
		return true;
	}

	@Override
	public int getField(int id)
	{
		return 0;
	}

	@Override
	public void setField(int id, int value) {}

	@Override
	public int getFieldCount()
	{
		return 0;
	}

	@Override
	public void clear() {}

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
	public boolean canBreath()
	{
		return true;
	}
}
