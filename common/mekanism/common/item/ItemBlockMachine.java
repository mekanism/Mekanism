package mekanism.common.item;

import ic2.api.item.ICustomElectricItem;

import java.util.List;

import mekanism.api.EnumColor;
import mekanism.api.IUpgradeManagement;
import mekanism.api.energy.IEnergizedItem;
import mekanism.common.IElectricChest;
import mekanism.common.IFactory;
import mekanism.common.ISustainedInventory;
import mekanism.common.ISustainedTank;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.IFactory.RecipeType;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.block.BlockMachine;
import mekanism.common.block.BlockMachine.MachineType;
import mekanism.common.inventory.InventoryElectricChest;
import mekanism.common.network.PacketElectricChest;
import mekanism.common.network.PacketElectricChest.ElectricChestPacketType;
import mekanism.common.tileentity.TileEntityElectricBlock;
import mekanism.common.tileentity.TileEntityElectricChest;
import mekanism.common.tileentity.TileEntityFactory;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.input.Keyboard;

import thermalexpansion.api.item.IChargeableItem;
import universalelectricity.core.electricity.ElectricityDisplay;
import universalelectricity.core.electricity.ElectricityDisplay.ElectricUnit;
import universalelectricity.core.item.IItemElectric;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Item class for handling multiple machine block IDs.
 * 0: Enrichment Chamber
 * 1: Osmium Compressor
 * 2: Combiner
 * 3: Crusher
 * 4: Theoretical Elementizer
 * 5: Basic Factory
 * 6: Advanced Factory
 * 7: Elite Factory
 * 8: Metallurgic Infuser
 * 9: Purification Chamber
 * 10: Energized Smelter
 * 11: Teleporter
 * 12: Electric Pump
 * 13: Electric Chest
 * 14: Chargepad
 * @author AidanBrady
 *
 */
public class ItemBlockMachine extends ItemBlock implements IEnergizedItem, IItemElectric, ICustomElectricItem, IUpgradeManagement, IFactory, ISustainedInventory, ISustainedTank, IElectricChest, IChargeableItem
{
	public Block metaBlock;
	
	public ItemBlockMachine(int id, Block block)
	{
		super(id);
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
		String name = "";
		switch(itemstack.getItemDamage())
		{
			case 0:
				name = "EnrichmentChamber";
				break;
			case 1:
				name = "OsmiumCompressor";
				break;
			case 2:
				name = "Combiner";
				break;
			case 3:
				name = "Crusher";
				break;
			case 4:
				name = "TheoreticalElementizer";
				break;
			case 5:
				name = "BasicFactory";
				break;
			case 6:
				name = "AdvancedFactory";
				break;
			case 7:
				name = "EliteFactory";
				break;
			case 8:
				name = "MetallurgicInfuser";
				break;
			case 9:
				name = "PurificationChamber";
				break;
			case 10:
				name = "EnergizedSmelter";
				break;
			case 11:
				name = "Teleporter";
				break;
			case 12:
				name = "ElectricPump";
				break;
			case 13:
				name = "ElectricChest";
				break;
			case 14:
				name = "Chargepad";
				break;
			default:
				name = "Unknown";
				break;
		}
		
		return getUnlocalizedName() + "." + name;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List list, boolean flag)
	{
		if(!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
		{
			list.add("Hold " + EnumColor.AQUA + "shift" + EnumColor.GREY + " for more details.");
		}
		else {
			if(isFactory(itemstack))
			{
				list.add(EnumColor.INDIGO + "Recipe Type: " + EnumColor.GREY + RecipeType.values()[getRecipeType(itemstack)].getName());
			}
			
			if(isElectricChest(itemstack))
			{
				list.add(EnumColor.INDIGO + "Authenticated: " + EnumColor.GREY + getAuthenticated(itemstack));
				list.add(EnumColor.INDIGO + "Locked: " + EnumColor.GREY + getLocked(itemstack));
			}
			
			list.add(EnumColor.BRIGHT_GREEN + "Stored Energy: " + EnumColor.GREY + ElectricityDisplay.getDisplayShort(getElectricityStored(itemstack), ElectricUnit.JOULES));
			list.add(EnumColor.BRIGHT_GREEN + "Voltage: " + EnumColor.GREY + getVoltage(itemstack) + "v");
			
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
			
			if(itemstack.getItemDamage() != 14)
			{
				list.add(EnumColor.AQUA + "Inventory: " + EnumColor.GREY + (getInventory(itemstack) != null && getInventory(itemstack).tagCount() != 0));
			}
		}
	}

	@Override
	public float getVoltage(ItemStack itemStack) 
	{
		return 120;
	}
	
	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int metadata)
    {
    	boolean place = super.placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ, metadata);
    	
    	if(place)
    	{
    		TileEntityElectricBlock tileEntity = (TileEntityElectricBlock)world.getBlockTileEntity(x, y, z);
    		
    		if(tileEntity instanceof IUpgradeManagement)
    		{
    			((IUpgradeManagement)tileEntity).setEnergyMultiplier(getEnergyMultiplier(stack));
    			((IUpgradeManagement)tileEntity).setSpeedMultiplier(getSpeedMultiplier(stack));
    		}
    		
    		if(tileEntity instanceof TileEntityFactory)
    		{
    			((TileEntityFactory)tileEntity).recipeType = getRecipeType(stack);
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
    		
    		((ISustainedInventory)tileEntity).setInventory(getInventory(stack));
    		
    		tileEntity.electricityStored = getEnergy(stack);
    	}
    	
    	return place;
    }
	
	@Override
	public int charge(ItemStack itemStack, int amount, int tier, boolean ignoreTransferLimit, boolean simulate)
	{
		if(canReceive(itemStack))
		{
			double energyNeeded = getMaxEnergy(itemStack)-getEnergy(itemStack);
			double energyToStore = Math.min(Math.min(amount*Mekanism.FROM_IC2, getMaxEnergy(itemStack)*0.01), energyNeeded);
			
			if(!simulate)
			{
				setEnergy(itemStack, getEnergy(itemStack) + energyToStore);
			}
			
			return (int)(energyToStore*Mekanism.TO_IC2);
		}
		
		return 0;
	}
	
	@Override
	public int discharge(ItemStack itemStack, int amount, int tier, boolean ignoreTransferLimit, boolean simulate)
	{
		return 0;
	}

	@Override
	public boolean canUse(ItemStack itemStack, int amount)
	{
		return getEnergy(itemStack) >= amount*Mekanism.FROM_IC2;
	}
	
	@Override
	public boolean canShowChargeToolTip(ItemStack itemStack)
	{
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
		return 3;
	}

	@Override
	public int getTransferLimit(ItemStack itemStack)
	{
		return 0;
	}
	
	@Override
	public void onUpdate(ItemStack itemstack, World world, Entity entity, int i, boolean flag)
	{
		if(isElectricChest(itemstack))
		{
			setPrevLidAngle(itemstack, getLidAngle(itemstack));
			float increment = 0.1F;
			
		    if((!getOpen(itemstack) && getLidAngle(itemstack) > 0.0F) || (getOpen(itemstack) && getLidAngle(itemstack) < 1.0F))
		    {
		    	float angle = getLidAngle(itemstack);
	
		    	if(getOpen(itemstack))
		    	{
		    		setLidAngle(itemstack, getLidAngle(itemstack)+increment);
		    	}
		    	else {
		    		setLidAngle(itemstack, getLidAngle(itemstack)-increment);
		    	}
	
		    	if(getLidAngle(itemstack) > 1.0F)
		    	{
		    		setLidAngle(itemstack, 1.0F);
		    	}
	
		     	float split = 0.5F;
	
		     	if(getLidAngle(itemstack) < 0.0F)
		     	{
		     		setLidAngle(itemstack, 0.0F);
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
			ItemStack itemStack = (ItemStack) data[0];

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
			ItemStack itemStack = (ItemStack) data[0];

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
			if(((ItemStack)data[0]).getItemDamage() != 11 && ((ItemStack)data[0]).getItemDamage() != 12 && ((ItemStack)data[0]).getItemDamage() != 13 && ((ItemStack)data[0]).getItemDamage() != 14)
			{
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer entityplayer)
	{
		if(!world.isRemote)
		{
			if(isElectricChest(itemstack))
			{
		 		if(!getAuthenticated(itemstack))
		 		{
		 			PacketHandler.sendPacket(Transmission.SINGLE_CLIENT, new PacketElectricChest().setParams(ElectricChestPacketType.CLIENT_OPEN, 2, 0, false), entityplayer);
		 		}
		 		else if(getLocked(itemstack) && getEnergy(itemstack) > 0)
		 		{
		 			PacketHandler.sendPacket(Transmission.SINGLE_CLIENT, new PacketElectricChest().setParams(ElectricChestPacketType.CLIENT_OPEN, 1, 0, false), entityplayer);
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
	public boolean isFactory(ItemStack itemStack)
	{
		return itemStack.getItem() instanceof ItemBlockMachine && itemStack.getItemDamage() >= 5 && itemStack.getItemDamage() <= 7;
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
		return data[0] instanceof ItemStack && ((ItemStack)data[0]).getItem() instanceof ISustainedTank && ((ItemStack)data[0]).getItemDamage() == 12;
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
	public boolean isElectricChest(ItemStack itemStack) 
	{
		return itemStack.getItemDamage() == 13;
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
		return MekanismUtils.getEnergy(getEnergyMultiplier(itemStack), MachineType.getFromMetadata(itemStack.getItemDamage()).baseEnergy);
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
	public float receiveEnergy(ItemStack theItem, float energy, boolean doReceive)
	{
		return (float)(recharge(theItem, (float)((energy*Mekanism.FROM_BC)*Mekanism.TO_UE), doReceive)*Mekanism.TO_BC);
	}

	@Override
	public float transferEnergy(ItemStack theItem, float energy, boolean doTransfer) 
	{
		return (float)(discharge(theItem, (float)((energy*Mekanism.FROM_BC)*Mekanism.TO_UE), doTransfer)*Mekanism.TO_BC);
	}

	@Override
	public float getEnergyStored(ItemStack theItem)
	{
		return (float)(getEnergy(theItem)*Mekanism.TO_BC);
	}

	@Override
	public float getMaxEnergyStored(ItemStack theItem)
	{
		return (float)(getMaxEnergy(theItem)*Mekanism.TO_BC);
	}
	
	@Override
	public boolean isMetadataSpecific()
	{
		return true;
	}
	
	@Override
	public float recharge(ItemStack itemStack, float energy, boolean doRecharge) 
	{
		if(canReceive(itemStack))
		{
			double energyNeeded = getMaxEnergy(itemStack)-getEnergy(itemStack);
			double toReceive = Math.min(energy*Mekanism.FROM_UE, energyNeeded);
			
			if(doRecharge)
			{
				setEnergy(itemStack, getEnergy(itemStack) + toReceive);
			}
			
			return (float)(toReceive*Mekanism.TO_UE);
		}
		
		return 0;
	}

	@Override
	public float discharge(ItemStack itemStack, float energy, boolean doDischarge) 
	{
		if(canSend(itemStack))
		{
			double energyRemaining = getEnergy(itemStack);
			double toSend = Math.min((energy*Mekanism.FROM_UE), energyRemaining);
			
			if(doDischarge)
			{
				setEnergy(itemStack, getEnergy(itemStack) - toSend);
			}
			
			return (float)(toSend*Mekanism.TO_UE);
		}
		
		return 0;
	}

	@Override
	public float getElectricityStored(ItemStack theItem) 
	{
		return (float)(getEnergy(theItem)*Mekanism.TO_UE);
	}

	@Override
	public float getMaxElectricityStored(ItemStack theItem) 
	{
		return (float)(getMaxEnergy(theItem)*Mekanism.TO_UE);
	}

	@Override
	public void setElectricity(ItemStack itemStack, float joules) 
	{
		setEnergy(itemStack, joules*Mekanism.TO_UE);
	}

	@Override
	public float getTransfer(ItemStack itemStack)
	{
		return (float)(getMaxTransfer(itemStack)*Mekanism.TO_UE);
	}
}
