package mekanism.common.item;

import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;
import ic2.api.item.IElectricItemManager;
import ic2.api.item.ISpecialElectricItem;

import java.util.List;

import mekanism.api.EnumColor;
import mekanism.api.energy.EnergizedItemManager;
import mekanism.api.energy.IEnergizedItem;
import mekanism.api.gas.GasStack;
import mekanism.common.IElectricChest;
import mekanism.common.IFactory;
import mekanism.common.IInvConfiguration;
import mekanism.common.IRedstoneControl;
import mekanism.common.IRedstoneControl.RedstoneControl;
import mekanism.common.ISustainedInventory;
import mekanism.common.ISustainedTank;
import mekanism.common.IUpgradeManagement;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.block.BlockMachine.MachineType;
import mekanism.common.integration.IC2ItemManager;
import mekanism.common.inventory.InventoryElectricChest;
import mekanism.common.miner.MinerFilter;
import mekanism.common.network.PacketElectricChest;
import mekanism.common.network.PacketElectricChest.ElectricChestPacketType;
import mekanism.common.tile.*;
import mekanism.common.transporter.TransporterFilter;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.TransporterUtils;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.input.Keyboard;

import cofh.api.energy.IEnergyContainerItem;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Item class for handling multiple machine block IDs.
 * 0:0: Enrichment Chamber
 * 0:1: Osmium Compressor
 * 0:2: Combiner
 * 0:3: Crusher
 * 0:4: Digital Miner
 * 0:5: Basic Factory
 * 0:6: Advanced Factory
 * 0:7: Elite Factory
 * 0:8: Metallurgic Infuser
 * 0:9: Purification Chamber
 * 0:10: Energized Smelter
 * 0:11: Teleporter
 * 0:12: Electric Pump
 * 0:13: Electric Chest
 * 0:14: Chargepad
 * 0:15: Logistical Sorter
 * 1:0: Rotary Condensentrator
 * 1:1: Chemical Oxidizer
 * 1:2: Chemical Infuser
 * 1:3: Chemical Injection Chamber
 * 1:4: Electrolytic Separator
 * 1:5: Precision Sawmill
 * 1:6: Chemical Dissolution Chamber
 * 1:7: Chemical Washer
 * 1:8: Chemical Crystallizer
 * @author AidanBrady
 *
 */
public class ItemBlockMachine extends ItemBlock implements IEnergizedItem, ISpecialElectricItem, IUpgradeManagement, IFactory, ISustainedInventory, ISustainedTank, IElectricChest, IEnergyContainerItem
{
	public Block metaBlock;

	public ItemBlockMachine(Block block)
	{
		super(block);
		metaBlock = block;
		setHasSubtypes(true);
		setNoRepair();
		setMaxStackSize(1);
	}

	@Override
	public int getMetadata(int i)
	{
		return i;
	}

	@Override
	public String getUnlocalizedName(ItemStack itemstack)
	{
		if(MachineType.get(itemstack) != null)
		{
			return getUnlocalizedName() + "." + MachineType.get(itemstack).name;
		}

		return "null";
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List list, boolean flag)
	{
		MachineType type = MachineType.get(itemstack);

		if(!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
		{
			list.add("Hold " + EnumColor.AQUA + EnumColor.INDIGO + "shift" + EnumColor.GREY + " for more details.");
			list.add("Hold " + EnumColor.AQUA + "shift" + EnumColor.GREY + " and " + EnumColor.AQUA + "M" + EnumColor.GREY + " for a description.");
		}
		else if(!Keyboard.isKeyDown(Keyboard.KEY_M))
		{
			if(type == MachineType.BASIC_FACTORY || type == MachineType.ADVANCED_FACTORY || type == MachineType.ELITE_FACTORY)
			{
				list.add(EnumColor.INDIGO + "Recipe Type: " + EnumColor.GREY + RecipeType.values()[getRecipeType(itemstack)].getName());
			}

			if(type == MachineType.ELECTRIC_CHEST)
			{
				list.add(EnumColor.INDIGO + "Authenticated: " + EnumColor.GREY + getAuthenticated(itemstack));
				list.add(EnumColor.INDIGO + "Locked: " + EnumColor.GREY + getLocked(itemstack));
			}

			if(type != MachineType.LOGISTICAL_SORTER)
			{
				list.add(EnumColor.BRIGHT_GREEN + "Stored Energy: " + EnumColor.GREY + MekanismUtils.getEnergyDisplay(getEnergyStored(itemstack)));
			}

			if(hasTank(itemstack))
			{
				if(getFluidStack(itemstack) != null)
				{
					list.add(EnumColor.PINK + FluidRegistry.getFluidName(getFluidStack(itemstack)) + ": " + EnumColor.GREY + getFluidStack(itemstack).amount + "mB");
				}
			}

			if(supportsUpgrades(itemstack))
			{
				list.add(EnumColor.PURPLE + "Energy: " + EnumColor.GREY + "x" + (getEnergyMultiplier(itemstack)+1));
				list.add(EnumColor.PURPLE + "Speed: " + EnumColor.GREY + "x" + (getSpeedMultiplier(itemstack)+1));
			}

			if(type != MachineType.CHARGEPAD && type != MachineType.LOGISTICAL_SORTER)
			{
				list.add(EnumColor.AQUA + "Inventory: " + EnumColor.GREY + (getInventory(itemstack) != null && getInventory(itemstack).tagCount() != 0));
			}
		}
		else {
			list.addAll(MekanismUtils.splitLines(type.getDescription()));
		}
	}

	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int metadata)
	{
		boolean place = true;

		if(MachineType.get(stack) == MachineType.DIGITAL_MINER)
		{
			for(int xPos = x-1; xPos <= x+1; xPos++)
			{
				for(int yPos = y; yPos <= y+1; yPos++)
				{
					for(int zPos = z-1; zPos <= z+1; zPos++)
					{
						Block b = world.getBlock(xPos, yPos, zPos);

						if(yPos > 255)
							place = false;

						if(!b.isAir(world, xPos, yPos, zPos) && !b.isReplaceable(world, xPos, yPos, zPos))
							return false;
					}
				}
			}
		}

		if(place && super.placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ, metadata))
		{
			TileEntityElectricBlock tileEntity = (TileEntityElectricBlock)world.getTileEntity(x, y, z);

			if(tileEntity instanceof IUpgradeManagement)
			{
				((IUpgradeManagement)tileEntity).setEnergyMultiplier(getEnergyMultiplier(stack));
				((IUpgradeManagement)tileEntity).setSpeedMultiplier(getSpeedMultiplier(stack));
			}

			if(tileEntity instanceof IInvConfiguration)
			{
				IInvConfiguration config = (IInvConfiguration)tileEntity;

				if(stack.stackTagCompound != null && stack.stackTagCompound.hasKey("hasSideData"))
				{
					config.getEjector().setEjecting(stack.stackTagCompound.getBoolean("ejecting"));

					for(int i = 0; i < 6; i++)
					{
						config.getConfiguration()[i] = stack.stackTagCompound.getByte("config"+i);
					}
				}
			}

			if(tileEntity instanceof TileEntityDigitalMiner)
			{
				TileEntityDigitalMiner miner = (TileEntityDigitalMiner)tileEntity;

				if(stack.stackTagCompound != null && stack.stackTagCompound.hasKey("hasMinerConfig"))
				{
					miner.radius = stack.stackTagCompound.getInteger("radius");
					miner.minY = stack.stackTagCompound.getInteger("minY");
					miner.maxY = stack.stackTagCompound.getInteger("maxY");
					miner.doEject = stack.stackTagCompound.getBoolean("doEject");
					miner.doPull = stack.stackTagCompound.getBoolean("doPull");
					miner.silkTouch = stack.stackTagCompound.getBoolean("silkTouch");

					if(stack.stackTagCompound.hasKey("replaceStack"))
					{
						miner.replaceStack = ItemStack.loadItemStackFromNBT(stack.stackTagCompound.getCompoundTag("replaceStack"));
					}

					if(stack.stackTagCompound.hasKey("filters"))
					{
						NBTTagList tagList = stack.stackTagCompound.getTagList("filters");

						for(int i = 0; i < tagList.tagCount(); i++)
						{
							miner.filters.add(MinerFilter.readFromNBT((NBTTagCompound)tagList.tagAt(i)));
						}
					}
				}
			}

			if(tileEntity instanceof TileEntityLogisticalSorter)
			{
				TileEntityLogisticalSorter sorter = (TileEntityLogisticalSorter)tileEntity;

				if(stack.stackTagCompound != null && stack.stackTagCompound.hasKey("hasSorterConfig"))
				{
					if(stack.stackTagCompound.hasKey("color"))
					{
						sorter.color = TransporterUtils.colors.get(stack.stackTagCompound.getInteger("color"));
					}

					sorter.autoEject = stack.stackTagCompound.getBoolean("autoEject");
					sorter.roundRobin = stack.stackTagCompound.getBoolean("roundRobin");

					if(stack.stackTagCompound.hasKey("filters"))
					{
						NBTTagList tagList = stack.stackTagCompound.getTagList("filters");

						for(int i = 0; i < tagList.tagCount(); i++)
						{
							sorter.filters.add(TransporterFilter.readFromNBT((NBTTagCompound)tagList.tagAt(i)));
						}
					}
				}
			}

			if(tileEntity instanceof IRedstoneControl)
			{
				if(stack.stackTagCompound != null && stack.stackTagCompound.hasKey("controlType"))
				{
					((IRedstoneControl)tileEntity).setControlType(RedstoneControl.values()[stack.stackTagCompound.getInteger("controlType")]);
				}
			}

			if(tileEntity instanceof TileEntityFactory)
			{
				((TileEntityFactory)tileEntity).recipeType = getRecipeType(stack);
				world.notifyBlocksOfNeighborChange(x, y, z, tileEntity.getBlockType());
			}

			if(tileEntity instanceof ISustainedTank)
			{
				if(hasTank(stack) && getFluidStack(stack) != null)
				{
					((ISustainedTank)tileEntity).setFluidStack(getFluidStack(stack));
				}
			}

			if(tileEntity instanceof TileEntityElectricChest)
			{
				((TileEntityElectricChest)tileEntity).authenticated = getAuthenticated(stack);
				((TileEntityElectricChest)tileEntity).locked = getLocked(stack);
				((TileEntityElectricChest)tileEntity).password = getPassword(stack);
			}

			if(tileEntity instanceof TileEntityRotaryCondensentrator)
			{
				if(stack.stackTagCompound != null && stack.stackTagCompound.hasKey("gasStack"))
				{
					GasStack gasStack = GasStack.readFromNBT(stack.stackTagCompound.getCompoundTag("gasStack"));
					((TileEntityRotaryCondensentrator)tileEntity).gasTank.setGas(gasStack);
				}
			}

			if(tileEntity instanceof TileEntityChemicalOxidizer)
			{
				if(stack.stackTagCompound != null)
				{
					((TileEntityChemicalOxidizer)tileEntity).gasTank.setGas(GasStack.readFromNBT(stack.stackTagCompound.getCompoundTag("gasTank")));
				}
			}

			if(tileEntity instanceof TileEntityChemicalInfuser)
			{
				if(stack.stackTagCompound != null)
				{
					((TileEntityChemicalInfuser)tileEntity).leftTank.setGas(GasStack.readFromNBT(stack.stackTagCompound.getCompoundTag("leftTank")));
					((TileEntityChemicalInfuser)tileEntity).rightTank.setGas(GasStack.readFromNBT(stack.stackTagCompound.getCompoundTag("rightTank")));
					((TileEntityChemicalInfuser)tileEntity).centerTank.setGas(GasStack.readFromNBT(stack.stackTagCompound.getCompoundTag("centerTank")));
				}
			}

			((ISustainedInventory)tileEntity).setInventory(getInventory(stack));

			tileEntity.electricityStored = getEnergy(stack);

			return true;
		}

		return false;
	}

	@Override
	public boolean canProvideEnergy(ItemStack itemStack)
	{
		return false;
	}

	@Override
	public int getChargedItemId(ItemStack itemStack)
	{
		return itemID;
	}

	@Override
	public int getEmptyItemId(ItemStack itemStack)
	{
		return itemID;
	}

	@Override
	public int getMaxCharge(ItemStack itemStack)
	{
		return 0;
	}

	@Override
	public int getTier(ItemStack itemStack)
	{
		return 4;
	}

	@Override
	public int getTransferLimit(ItemStack itemStack)
	{
		return 0;
	}

	@Override
	public void onUpdate(ItemStack itemstack, World world, Entity entity, int i, boolean flag)
	{
		if(MachineType.get(itemstack) == MachineType.ELECTRIC_CHEST)
		{
			if(world != null && !world.isRemote)
			{
				InventoryElectricChest inv = new InventoryElectricChest(itemstack);

				if(inv.getStackInSlot(54) != null && getEnergy(itemstack) < getMaxEnergy(itemstack))
				{
					if(inv.getStackInSlot(54).getItem() instanceof IEnergizedItem)
					{
						setEnergy(itemstack, getEnergy(itemstack) + EnergizedItemManager.discharge(inv.getStackInSlot(54), getMaxEnergy(itemstack) - getEnergy(itemstack)));
					}
					else if(Mekanism.hooks.IC2Loaded && inv.getStackInSlot(54).getItem() instanceof IElectricItem)
					{
						IElectricItem item = (IElectricItem)inv.getStackInSlot(54).getItem();

						if(item.canProvideEnergy(inv.getStackInSlot(54)))
						{
							double gain = ElectricItem.manager.discharge(inv.getStackInSlot(54), (int)((getMaxEnergy(itemstack) - getEnergy(itemstack))*Mekanism.TO_IC2), 3, false, false)*Mekanism.FROM_IC2;
							setEnergy(itemstack, getEnergy(itemstack) + gain);
						}
					}
					else if(inv.getStackInSlot(54).getItem() instanceof IEnergyContainerItem)
					{
						ItemStack itemStack = inv.getStackInSlot(54);
						IEnergyContainerItem item = (IEnergyContainerItem)inv.getStackInSlot(54).getItem();

						int itemEnergy = (int)Math.round(Math.min(Math.sqrt(item.getMaxEnergyStored(itemStack)), item.getEnergyStored(itemStack)));
						int toTransfer = (int)Math.round(Math.min(itemEnergy, ((getMaxEnergy(itemstack) - getEnergy(itemstack))*Mekanism.TO_TE)));

						setEnergy(itemstack, getEnergy(itemstack) + (item.extractEnergy(itemStack, toTransfer, false)*Mekanism.FROM_TE));
					}
					else if(inv.getStackInSlot(54).getItem() == Items.redstone && getEnergy(itemstack)+Mekanism.ENERGY_PER_REDSTONE <= getMaxEnergy(itemstack))
					{
						setEnergy(itemstack, getEnergy(itemstack) + Mekanism.ENERGY_PER_REDSTONE);
						inv.getStackInSlot(54).stackSize--;

						if(inv.getStackInSlot(54).stackSize <= 0)
						{
							inv.setInventorySlotContents(54, null);
						}
					}

					inv.write();
				}
			}
		}
	}

	@Override
	public boolean onEntityItemUpdate(EntityItem entityItem)
	{
		onUpdate(entityItem.getEntityItem(), null, entityItem, 0, false);

		return false;
	}

	@Override
	public int getEnergyMultiplier(Object... data)
	{
		if(data[0] instanceof ItemStack)
		{
			ItemStack itemStack = (ItemStack)data[0];

			if(itemStack.stackTagCompound == null)
			{
				return 0;
			}

			return itemStack.stackTagCompound.getInteger("energyMultiplier");
		}

		return 0;
	}

	@Override
	public void setEnergyMultiplier(int multiplier, Object... data)
	{
		if(data[0] instanceof ItemStack)
		{
			ItemStack itemStack = (ItemStack)data[0];

			if(itemStack.stackTagCompound == null)
			{
				itemStack.setTagCompound(new NBTTagCompound());
			}

			itemStack.stackTagCompound.setInteger("energyMultiplier", multiplier);
		}
	}

	@Override
	public int getSpeedMultiplier(Object... data)
	{
		if(data[0] instanceof ItemStack)
		{
			ItemStack itemStack = (ItemStack)data[0];

			if(itemStack.stackTagCompound == null)
			{
				return 0;
			}

			return itemStack.stackTagCompound.getInteger("speedMultiplier");
		}

		return 0;
	}

	@Override
	public void setSpeedMultiplier(int multiplier, Object... data)
	{
		if(data[0] instanceof ItemStack)
		{
			ItemStack itemStack = (ItemStack)data[0];

			if(itemStack.stackTagCompound == null)
			{
				itemStack.setTagCompound(new NBTTagCompound());
			}

			itemStack.stackTagCompound.setInteger("speedMultiplier", multiplier);
		}
	}

	@Override
	public boolean supportsUpgrades(Object... data)
	{
		if(data[0] instanceof ItemStack)
		{
			MachineType type = MachineType.get((ItemStack)data[0]);

			return type.supportsUpgrades;
		}

		return false;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer entityplayer)
	{
		if(!world.isRemote)
		{
			if(MachineType.get(itemstack) == MachineType.ELECTRIC_CHEST)
			{
				if(!getAuthenticated(itemstack))
				{
					Mekanism.packetPipeline.sendTo(new PacketElectricChest(ElectricChestPacketType.CLIENT_OPEN, 2, 0, false), entityplayer);
				}
				else if(getLocked(itemstack) && getEnergy(itemstack) > 0)
				{
					Mekanism.packetPipeline.sendTo(new PacketElectricChest(ElectricChestPacketType.CLIENT_OPEN, 1, 0, false), entityplayer);
				}
				else {
					InventoryElectricChest inventory = new InventoryElectricChest(entityplayer);
					MekanismUtils.openElectricChestGui((EntityPlayerMP)entityplayer, null, inventory, false);
				}
			}
		}

		return itemstack;
	}

	@Override
	public int getRecipeType(ItemStack itemStack)
	{
		if(itemStack.stackTagCompound == null)
		{
			return 0;
		}

		return itemStack.stackTagCompound.getInteger("recipeType");
	}

	@Override
	public void setRecipeType(int type, ItemStack itemStack)
	{
		if(itemStack.stackTagCompound == null)
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}

		itemStack.stackTagCompound.setInteger("recipeType", type);
	}

	@Override
	public void setInventory(NBTTagList nbtTags, Object... data)
	{
		if(data[0] instanceof ItemStack)
		{
			ItemStack itemStack = (ItemStack)data[0];

			if(itemStack.stackTagCompound == null)
			{
				itemStack.setTagCompound(new NBTTagCompound());
			}

			itemStack.stackTagCompound.setTag("Items", nbtTags);
		}
	}

	@Override
	public NBTTagList getInventory(Object... data)
	{
		if(data[0] instanceof ItemStack)
		{
			ItemStack itemStack = (ItemStack)data[0];

			if(itemStack.stackTagCompound == null)
			{
				return null;
			}

			return itemStack.stackTagCompound.getTagList("Items");
		}

		return null;
	}

	@Override
	public void setFluidStack(FluidStack fluidStack, Object... data)
	{
		if(fluidStack == null || fluidStack.amount == 0 || fluidStack.fluidID == 0)
		{
			return;
		}

		if(data[0] instanceof ItemStack)
		{
			ItemStack itemStack = (ItemStack)data[0];

			if(itemStack.stackTagCompound == null)
			{
				itemStack.setTagCompound(new NBTTagCompound());
			}

			itemStack.stackTagCompound.setTag("fluidTank", fluidStack.writeToNBT(new NBTTagCompound()));
		}
	}

	@Override
	public FluidStack getFluidStack(Object... data)
	{
		if(data[0] instanceof ItemStack)
		{
			ItemStack itemStack = (ItemStack)data[0];

			if(itemStack.stackTagCompound == null)
			{
				return null;
			}

			if(itemStack.stackTagCompound.hasKey("fluidTank"))
			{
				return FluidStack.loadFluidStackFromNBT(itemStack.stackTagCompound.getCompoundTag("fluidTank"));
			}
		}

		return null;
	}

	@Override
	public boolean hasTank(Object... data)
	{
		return data[0] instanceof ItemStack && ((ItemStack)data[0]).getItem() instanceof ISustainedTank && (MachineType.get((ItemStack)data[0]) == MachineType.ELECTRIC_PUMP
				|| MachineType.get((ItemStack)data[0]) == MachineType.ROTARY_CONDENSENTRATOR);
	}

	@Override
	public void setAuthenticated(ItemStack itemStack, boolean auth)
	{
		if(itemStack.stackTagCompound == null)
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}

		itemStack.stackTagCompound.setBoolean("authenticated", auth);
	}

	@Override
	public boolean getAuthenticated(ItemStack itemStack)
	{
		if(itemStack.stackTagCompound == null)
		{
			return false;
		}

		return itemStack.stackTagCompound.getBoolean("authenticated");
	}

	@Override
	public void setPassword(ItemStack itemStack, String pass)
	{
		if(itemStack.stackTagCompound == null)
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}

		itemStack.stackTagCompound.setString("password", pass);
	}

	@Override
	public String getPassword(ItemStack itemStack)
	{
		if(itemStack.stackTagCompound == null)
		{
			return "";
		}

		return itemStack.stackTagCompound.getString("password");
	}

	@Override
	public void setLocked(ItemStack itemStack, boolean locked)
	{
		if(itemStack.stackTagCompound == null)
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}

		itemStack.stackTagCompound.setBoolean("locked", locked);
	}

	@Override
	public boolean getLocked(ItemStack itemStack)
	{
		if(itemStack.stackTagCompound == null)
		{
			return false;
		}

		return itemStack.stackTagCompound.getBoolean("locked");
	}

	@Override
	public void setOpen(ItemStack itemStack, boolean open)
	{
		if(itemStack.stackTagCompound == null)
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}

		itemStack.stackTagCompound.setBoolean("open", open);
	}

	@Override
	public boolean getOpen(ItemStack itemStack)
	{
		if(itemStack.stackTagCompound == null)
		{
			return false;
		}

		return itemStack.stackTagCompound.getBoolean("open");
	}

	@Override
	public void setLidAngle(ItemStack itemStack, float lidAngle)
	{
		if(itemStack.stackTagCompound == null)
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}

		itemStack.stackTagCompound.setFloat("lidAngle", lidAngle);
	}

	@Override
	public float getLidAngle(ItemStack itemStack)
	{
		if(itemStack.stackTagCompound == null)
		{
			return 0.0F;
		}

		return itemStack.stackTagCompound.getFloat("lidAngle");
	}

	@Override
	public void setPrevLidAngle(ItemStack itemStack, float prevLidAngle)
	{
		if(itemStack.stackTagCompound == null)
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}

		itemStack.stackTagCompound.setFloat("prevLidAngle", prevLidAngle);
	}

	@Override
	public float getPrevLidAngle(ItemStack itemStack)
	{
		if(itemStack.stackTagCompound == null)
		{
			return 0.0F;
		}

		return itemStack.stackTagCompound.getFloat("prevLidAngle");
	}

	@Override
	public double getEnergy(ItemStack itemStack)
	{
		if(itemStack.stackTagCompound == null)
		{
			return 0;
		}

		return itemStack.stackTagCompound.getDouble("electricity");
	}

	@Override
	public void setEnergy(ItemStack itemStack, double amount)
	{
		if(itemStack.stackTagCompound == null)
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}

		double electricityStored = Math.max(Math.min(amount, getMaxEnergy(itemStack)), 0);
		itemStack.stackTagCompound.setDouble("electricity", electricityStored);
	}

	@Override
	public double getMaxEnergy(ItemStack itemStack)
	{
		return MekanismUtils.getMaxEnergy(getEnergyMultiplier(itemStack), MachineType.get(itemStack.itemID, itemStack.getItemDamage()).baseEnergy);
	}

	@Override
	public double getMaxTransfer(ItemStack itemStack)
	{
		return getMaxEnergy(itemStack)*0.005;
	}

	@Override
	public boolean canReceive(ItemStack itemStack)
	{
		return true;
	}

	@Override
	public boolean canSend(ItemStack itemStack)
	{
		return false;
	}

	@Override
	public int receiveEnergy(ItemStack theItem, int energy, boolean simulate)
	{
		if(canReceive(theItem))
		{
			double energyNeeded = getMaxEnergy(theItem)-getEnergy(theItem);
			double toReceive = Math.min(energy*Mekanism.FROM_TE, energyNeeded);

			if(!simulate)
			{
				setEnergy(theItem, getEnergy(theItem) + toReceive);
			}

			return (int)Math.round(toReceive*Mekanism.TO_TE);
		}

		return 0;
	}

	@Override
	public int extractEnergy(ItemStack theItem, int energy, boolean simulate)
	{
		if(canSend(theItem))
		{
			double energyRemaining = getEnergy(theItem);
			double toSend = Math.min((energy*Mekanism.FROM_TE), energyRemaining);

			if(!simulate)
			{
				setEnergy(theItem, getEnergy(theItem) - toSend);
			}

			return (int)Math.round(toSend*Mekanism.TO_TE);
		}

		return 0;
	}

	@Override
	public int getEnergyStored(ItemStack theItem)
	{
		return (int)(getEnergy(theItem)*Mekanism.TO_TE);
	}

	@Override
	public int getMaxEnergyStored(ItemStack theItem)
	{
		return (int)(getMaxEnergy(theItem)*Mekanism.TO_TE);
	}

	@Override
	public boolean isMetadataSpecific()
	{
		return true;
	}

	@Override
	public IElectricItemManager getManager(ItemStack itemStack)
	{
		return IC2ItemManager.getManager(this);
	}
}
