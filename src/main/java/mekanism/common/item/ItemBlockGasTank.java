package mekanism.common.item;

import java.util.List;

import mekanism.api.EnumColor;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasItem;
import mekanism.client.MekKeyHandler;
import mekanism.client.MekanismKeyHandler;
import mekanism.common.Mekanism;
import mekanism.common.base.ISustainedInventory;
import mekanism.common.tile.TileEntityGasTank;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;

import net.minecraft.block.Block;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

public class ItemBlockGasTank extends ItemBlock implements IGasItem, ISustainedInventory
{
	public Block metaBlock;

	/** The maximum amount of gas this tank can hold. */
	public int MAX_GAS = 96000;

	/** How fast this tank can transfer gas. */
	public static final int TRANSFER_RATE = 256;

	public ItemBlockGasTank(Block block)
	{
		super(block);
		metaBlock = block;
		setHasSubtypes(true);
		setMaxStackSize(1);
		setMaxDamage(100);
		setNoRepair();
		setCreativeTab(Mekanism.tabMekanism);
	}

	@Override
	public int getMetadata(int i)
	{
		return i;
	}

	@Override
	public IIcon getIconFromDamage(int i)
	{
		return metaBlock.getIcon(2, i);
	}

	@Override
	public String getUnlocalizedName(ItemStack itemstack)
	{
		return getUnlocalizedName() + "." + "GasTank";
	}

	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int metadata)
	{
		boolean place = super.placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ, metadata);

		if(place)
		{
			TileEntityGasTank tileEntity = (TileEntityGasTank)world.getTileEntity(x, y, z);
			tileEntity.gasTank.setGas(getGas(stack));

			((ISustainedInventory)tileEntity).setInventory(getInventory(stack));
		}

		return place;
	}

	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List list, boolean flag)
	{
		GasStack gasStack = getGas(itemstack);

		if(gasStack == null)
		{
			list.add(MekanismUtils.localize("tooltip.noGas") + ".");
		}
		else {
			list.add(MekanismUtils.localize("tooltip.stored") + " " + gasStack.getGas().getLocalizedName() + ": " + gasStack.amount);
		}

		if(!MekKeyHandler.getIsKeyPressed(MekanismKeyHandler.sneakKey))
		{
			list.add(MekanismUtils.localize("tooltip.hold") + " " + EnumColor.AQUA + GameSettings.getKeyDisplayString(MekanismKeyHandler.sneakKey.getKeyCode()) + EnumColor.GREY + " " + MekanismUtils.localize("tooltip.forDetails"));
		}
		else {
			list.add(EnumColor.AQUA + MekanismUtils.localize("tooltip.inventory") + ": " + EnumColor.GREY + LangUtils.transYesNo(getInventory(itemstack) != null && getInventory(itemstack).tagCount() != 0));
		}
	}

	@Override
	public void onCreated(ItemStack itemstack, World world, EntityPlayer entityplayer)
	{
		itemstack = getEmptyItem();
	}

	@Override
	public GasStack getGas(ItemStack itemstack)
	{
		if(itemstack.stackTagCompound == null)
		{
			return null;
		}

		GasStack stored = GasStack.readFromNBT(itemstack.stackTagCompound.getCompoundTag("stored"));

		if(stored == null)
		{
			itemstack.setItemDamage(100);
		}
		else {
			itemstack.setItemDamage((int)Math.max(1, (Math.abs((((float)stored.amount/getMaxGas(itemstack))*100)-100))));
		}

		return stored;
	}

	@Override
	public void setGas(ItemStack itemstack, GasStack stack)
	{
		if(itemstack.stackTagCompound == null)
		{
			itemstack.setTagCompound(new NBTTagCompound());
		}

		if(stack == null || stack.amount == 0)
		{
			itemstack.setItemDamage(100);
			itemstack.stackTagCompound.removeTag("stored");
		}
		else {
			int amount = Math.max(0, Math.min(stack.amount, getMaxGas(itemstack)));
			GasStack gasStack = new GasStack(stack.getGas(), amount);

			itemstack.setItemDamage((int)Math.max(1, (Math.abs((((float)amount/getMaxGas(itemstack))*100)-100))));
			itemstack.stackTagCompound.setTag("stored", gasStack.write(new NBTTagCompound()));
		}
	}

	public ItemStack getEmptyItem()
	{
		ItemStack empty = new ItemStack(this);
		setGas(empty, null);
		empty.setItemDamage(100);
		return empty;
	}

	@Override
	public void getSubItems(Item item, CreativeTabs tabs, List list)
	{
		ItemStack empty = new ItemStack(this);
		setGas(empty, null);
		empty.setItemDamage(100);
		list.add(empty);

		for(Gas type : GasRegistry.getRegisteredGasses())
		{
			if(type.isVisible())
			{
				ItemStack filled = new ItemStack(this);
				setGas(filled, new GasStack(type, ((IGasItem)filled.getItem()).getMaxGas(filled)));
				list.add(filled);
			}
		}
	}

	@Override
	public int getMaxGas(ItemStack itemstack)
	{
		return MAX_GAS;
	}

	@Override
	public int getRate(ItemStack itemstack)
	{
		return TRANSFER_RATE;
	}

	@Override
	public int addGas(ItemStack itemstack, GasStack stack)
	{
		if(getGas(itemstack) != null && getGas(itemstack).getGas() != stack.getGas())
		{
			return 0;
		}

		int toUse = Math.min(getMaxGas(itemstack)-getStored(itemstack), Math.min(getRate(itemstack), stack.amount));
		setGas(itemstack, new GasStack(stack.getGas(), getStored(itemstack)+toUse));

		return toUse;
	}

	@Override
	public GasStack removeGas(ItemStack itemstack, int amount)
	{
		if(getGas(itemstack) == null)
		{
			return null;
		}

		Gas type = getGas(itemstack).getGas();

		int gasToUse = Math.min(getStored(itemstack), Math.min(getRate(itemstack), amount));
		setGas(itemstack, new GasStack(type, getStored(itemstack)-gasToUse));

		return new GasStack(type, gasToUse);
	}

	private int getStored(ItemStack itemstack)
	{
		return getGas(itemstack) != null ? getGas(itemstack).amount : 0;
	}

	@Override
	public boolean canReceiveGas(ItemStack itemstack, Gas type)
	{
		return getGas(itemstack) == null || getGas(itemstack).getGas() == type;
	}

	@Override
	public boolean canProvideGas(ItemStack itemstack, Gas type)
	{
		return getGas(itemstack) != null && (type == null || getGas(itemstack).getGas() == type);
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

			return itemStack.stackTagCompound.getTagList("Items", NBT.TAG_COMPOUND);
		}

		return null;
	}
	
	@Override
	public boolean isMetadataSpecific(ItemStack itemStack)
	{
		return false;
	}
}
