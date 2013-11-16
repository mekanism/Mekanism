package mekanism.common.item;

import ic2.api.item.IElectricItemManager;
import ic2.api.item.ISpecialElectricItem;

import java.util.ArrayList;
import java.util.List;

import mekanism.api.EnumColor;
import mekanism.api.Object3D;
import mekanism.api.energy.IEnergizedItem;
import mekanism.common.IEnergyCube;
import mekanism.common.ISustainedInventory;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.Tier.EnergyCubeTier;
import mekanism.common.integration.IC2ItemManager;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.tileentity.TileEntityEnergyCube;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;

import org.lwjgl.input.Keyboard;

import universalelectricity.core.electricity.ElectricityDisplay;
import universalelectricity.core.electricity.ElectricityDisplay.ElectricUnit;
import universalelectricity.core.item.IItemElectric;
import cofh.api.energy.IEnergyContainerItem;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemBlockEnergyCube extends ItemBlock implements IEnergizedItem, IItemElectric, IEnergyCube, ISpecialElectricItem, ISustainedInventory, IEnergyContainerItem
{
	public Block metaBlock;
	
	public ItemBlockEnergyCube(int id, Block block)
	{
		super(id);
		metaBlock = block;
		setMaxStackSize(1);
		setMaxDamage(100);
		setNoRepair();
		setCreativeTab(Mekanism.tabMekanism);
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
			list.add(EnumColor.BRIGHT_GREEN + "Stored Energy: " + EnumColor.GREY + ElectricityDisplay.getDisplayShort(getElectricityStored(itemstack), ElectricUnit.JOULES));
			list.add(EnumColor.BRIGHT_GREEN + "Voltage: " + EnumColor.GREY + getVoltage(itemstack) + "v");
			list.add(EnumColor.AQUA + "Inventory: " + EnumColor.GREY + (getInventory(itemstack) != null && getInventory(itemstack).tagCount() != 0));
		}
	}
	
	public ItemStack getUnchargedItem(EnergyCubeTier tier)
	{
		ItemStack charged = new ItemStack(this);
		setEnergyCubeTier(charged, tier);
		charged.setItemDamage(100);
		return charged;
	}

	@Override
	public float getVoltage(ItemStack itemStack) 
	{
		return getEnergyCubeTier(itemStack).VOLTAGE;
	}
	
	@Override
	public String getUnlocalizedName(ItemStack itemstack)
	{
		return getUnlocalizedName() + "." + getEnergyCubeTier(itemstack).name;
	}
	
	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int metadata)
    {
    	boolean place = super.placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ, metadata);
    	
    	if(place)
    	{
    		TileEntityEnergyCube tileEntity = (TileEntityEnergyCube)world.getBlockTileEntity(x, y, z);
    		tileEntity.tier = ((IEnergyCube)stack.getItem()).getEnergyCubeTier(stack);
    		tileEntity.electricityStored = getEnergy(stack);
    		
    		((ISustainedInventory)tileEntity).setInventory(getInventory(stack));
    		
    		if(!world.isRemote)
    		{
    			PacketHandler.sendPacket(Transmission.ALL_CLIENTS, new PacketTileEntity().setParams(Object3D.get(tileEntity), tileEntity.getNetworkedData(new ArrayList())));
    		}
    	}
    	
    	return place;
    }

	@Override
	public EnergyCubeTier getEnergyCubeTier(ItemStack itemstack)
	{
		if(itemstack.stackTagCompound == null)
		{ 
			return EnergyCubeTier.BASIC; 
		}
		
		if(itemstack.stackTagCompound.getString("tier") == null)
		{
			return EnergyCubeTier.BASIC;
		}
		
		return EnergyCubeTier.getFromName(itemstack.stackTagCompound.getString("tier"));
	}

	@Override
	public void setEnergyCubeTier(ItemStack itemstack, EnergyCubeTier tier) 
	{
		if(itemstack.stackTagCompound == null)
		{
			itemstack.setTagCompound(new NBTTagCompound());
		}

		itemstack.stackTagCompound.setString("tier", tier.name);
	}
	
	@Override
	public boolean canProvideEnergy(ItemStack itemStack)
	{
		return true;
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
	public double getEnergy(ItemStack itemStack) 
	{
		if(itemStack.stackTagCompound == null) 
		{ 
			return 0; 
		}
		
		double electricityStored = itemStack.stackTagCompound.getDouble("electricity");
		itemStack.setItemDamage((int)Math.max(1, (Math.abs(((electricityStored/getMaxEnergy(itemStack))*100)-100))));
		
		return electricityStored;
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
		itemStack.setItemDamage((int)Math.max(1, (Math.abs(((electricityStored/getMaxEnergy(itemStack))*100)-100))));
	}

	@Override
	public double getMaxEnergy(ItemStack itemStack) 
	{
		return getEnergyCubeTier(itemStack).MAX_ELECTRICITY;
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
		return true;
	}
	
	@Override
	public int receiveEnergy(ItemStack theItem, int energy, boolean doReceive)
	{
		return (int)(recharge(theItem, (int)((energy*Mekanism.FROM_TE)*Mekanism.TO_UE), !doReceive)*Mekanism.TO_TE);
	}

	@Override
	public int extractEnergy(ItemStack theItem, int energy, boolean doTransfer) 
	{
		return (int)(discharge(theItem, (int)((energy*Mekanism.FROM_TE)*Mekanism.TO_UE), !doTransfer)*Mekanism.TO_TE);
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
		return false;
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
	
	@Override
	public IElectricItemManager getManager(ItemStack itemStack) 
	{
		return IC2ItemManager.getManager(this);
	}
}
