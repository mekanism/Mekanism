package mekanism.common.item;

import java.util.ArrayList;
import java.util.List;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.Range4D;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasItem;
import mekanism.client.MekKeyHandler;
import mekanism.client.MekanismKeyHandler;
import mekanism.common.Mekanism;
import mekanism.common.Tier.BaseTier;
import mekanism.common.Tier.GasTankTier;
import mekanism.common.base.ISustainedInventory;
import mekanism.common.base.ITierItem;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.tile.TileEntityGasTank;
import mekanism.common.util.LangUtils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

public class ItemBlockGasTank extends ItemBlock implements IGasItem, ISustainedInventory, ITierItem
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
		setCreativeTab(Mekanism.tabMekanism);
	}

	@Override
	public int getMetadata(int i)
	{
		return i;
	}

	@Override
	public String getUnlocalizedName(ItemStack itemstack)
	{
		return LangUtils.localize("tile.GasTank" + getBaseTier(itemstack).getName() + ".name");
	}

	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState state)
	{
		boolean place = super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, state);

		if(place)
		{
			TileEntityGasTank tileEntity = (TileEntityGasTank)world.getTileEntity(pos);
			tileEntity.tier = GasTankTier.values()[getBaseTier(stack).ordinal()];
			tileEntity.gasTank.setMaxGas(tileEntity.tier.storage);
			tileEntity.gasTank.setGas(getGas(stack));

			((ISustainedInventory)tileEntity).setInventory(getInventory(stack));
			
			if(!world.isRemote)
			{
				Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(tileEntity), tileEntity.getNetworkedData(new ArrayList())), new Range4D(Coord4D.get(tileEntity)));
			}
		}

		return place;
	}

	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List<String> list, boolean flag)
	{
		GasStack gasStack = getGas(itemstack);

		if(gasStack == null)
		{
			list.add(LangUtils.localize("tooltip.noGas") + ".");
		}
		else {
			list.add(LangUtils.localize("tooltip.stored") + " " + gasStack.getGas().getLocalizedName() + ": " + gasStack.amount);
		}

		if(!MekKeyHandler.getIsKeyPressed(MekanismKeyHandler.sneakKey))
		{
			list.add(LangUtils.localize("tooltip.hold") + " " + EnumColor.AQUA + GameSettings.getKeyDisplayString(MekanismKeyHandler.sneakKey.getKeyCode()) + EnumColor.GREY + " " + LangUtils.localize("tooltip.forDetails") + ".");
		}
		else {
			list.add(EnumColor.AQUA + LangUtils.localize("tooltip.inventory") + ": " + EnumColor.GREY + LangUtils.transYesNo(getInventory(itemstack) != null && getInventory(itemstack).tagCount() != 0));
		}
	}

	@Override
	public GasStack getGas(ItemStack itemstack)
	{
		if(itemstack.getTagCompound() == null)
		{
			return null;
		}

		return GasStack.readFromNBT(itemstack.getTagCompound().getCompoundTag("stored"));
	}

	@Override
	public void setGas(ItemStack itemstack, GasStack stack)
	{
		if(itemstack.getTagCompound() == null)
		{
			itemstack.setTagCompound(new NBTTagCompound());
		}

		if(stack == null || stack.amount == 0)
		{
			itemstack.getTagCompound().removeTag("stored");
		}
		else {
			int amount = Math.max(0, Math.min(stack.amount, getMaxGas(itemstack)));
			GasStack gasStack = new GasStack(stack.getGas(), amount);

			itemstack.getTagCompound().setTag("stored", gasStack.write(new NBTTagCompound()));
		}
	}

	public ItemStack getEmptyItem(GasTankTier tier)
	{
		ItemStack empty = new ItemStack(this);
		setBaseTier(empty, tier.getBaseTier());
		setGas(empty, null);
		
		return empty;
	}

	@Override
	public void getSubItems(Item item, CreativeTabs tabs, List<ItemStack> list)
	{
		for(GasTankTier tier : GasTankTier.values())
		{
			ItemStack empty = new ItemStack(this);
			setBaseTier(empty, tier.getBaseTier());
			setGas(empty, null);
			list.add(empty);
		}

		for(Gas type : GasRegistry.getRegisteredGasses())
		{
			if(type.isVisible())
			{
				ItemStack filled = new ItemStack(this);
				setBaseTier(filled, BaseTier.ULTIMATE);
				setGas(filled, new GasStack(type, ((IGasItem)filled.getItem()).getMaxGas(filled)));
				list.add(filled);
			}
		}
	}
	
	@Override
	public BaseTier getBaseTier(ItemStack itemstack)
	{
		if(!itemstack.hasTagCompound())
		{
			return BaseTier.BASIC;
		}

		return BaseTier.values()[itemstack.getTagCompound().getInteger("tier")];
	}

	@Override
	public void setBaseTier(ItemStack itemstack, BaseTier tier)
	{
		if(!itemstack.hasTagCompound())
		{
			itemstack.setTagCompound(new NBTTagCompound());
		}

		itemstack.getTagCompound().setInteger("tier", tier.ordinal());
	}

	@Override
	public int getMaxGas(ItemStack itemstack)
	{
		return GasTankTier.values()[getBaseTier(itemstack).ordinal()].storage;
	}

	@Override
	public int getRate(ItemStack itemstack)
	{
		return GasTankTier.values()[getBaseTier(itemstack).ordinal()].output;
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

			if(!itemStack.hasTagCompound())
			{
				itemStack.setTagCompound(new NBTTagCompound());
			}

			itemStack.getTagCompound().setTag("Items", nbtTags);
		}
	}

	@Override
	public NBTTagList getInventory(Object... data)
	{
		if(data[0] instanceof ItemStack)
		{
			ItemStack itemStack = (ItemStack)data[0];

			if(!itemStack.hasTagCompound())
			{
				return null;
			}

			return itemStack.getTagCompound().getTagList("Items", NBT.TAG_COMPOUND);
		}

		return null;
	}
	
	@Override
	public boolean showDurabilityBar(ItemStack stack)
	{
		return true;
	}
	
	@Override
	public double getDurabilityForDisplay(ItemStack stack)
	{
		return 1D-((getGas(stack) != null ? (double)getGas(stack).amount : 0D)/(double)getMaxGas(stack));
	}
}
