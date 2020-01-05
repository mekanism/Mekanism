package mekanism.common.item;

import java.util.ArrayList;
import java.util.List;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.Range4D;
import mekanism.api.energy.IEnergizedItem;
import mekanism.api.energy.IStrictEnergyStorage;
import mekanism.client.MekKeyHandler;
import mekanism.client.MekanismKeyHandler;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlocks;
import mekanism.common.Tier.BaseTier;
import mekanism.common.Tier.BinTier;
import mekanism.common.Tier.InductionCellTier;
import mekanism.common.Tier.InductionProviderTier;
import mekanism.common.base.ITierItem;
import mekanism.common.block.BlockBasic.BasicType;
import mekanism.common.inventory.InventoryBin;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.tile.TileEntityBin;
import mekanism.common.tile.TileEntityInductionCell;
import mekanism.common.tile.TileEntityInductionProvider;
import mekanism.common.tile.TileEntityMultiblock;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Item class for handling multiple metal block IDs.
 * 0:0: Osmium Block
 * 0:1: Bronze Block
 * 0:2: Refined Obsidian
 * 0:3: Charcoal Block
 * 0:4: Refined Glowstone
 * 0:5: Steel Block
 * 0:6: Bin
 * 0:7: Teleporter Frame
 * 0:8: Steel Casing
 * 0:9: Dynamic Tank
 * 0:10: Structural Glass
 * 0:11: Dynamic Valve
 * 0:12: Copper Block
 * 0:13: Tin Block
 * 0:14: Thermal Evaporation Controller
 * 0:15: Thermal Evaporation Valve
 * 1:0: Thermal Evaporation Block
 * 1:1: Induction Casing
 * 1:2: Induction Port
 * 1:3: Induction Cell
 * 1:4: Induction Provider
 * 1:5: Superheating Element
 * 1:6: Pressure Disperser
 * 1:7: Boiler Casing
 * 1:8: Boiler Valve
 * 1:9: Security Desk
 * @author AidanBrady
 *
 */
public class ItemBlockBasic extends ItemBlock implements IEnergizedItem, ITierItem
{
	public Block metaBlock;

	public ItemBlockBasic(Block block)
	{
		super(block);
		metaBlock = block;
		setHasSubtypes(true);
	}
	
	@Override
	public int getItemStackLimit(ItemStack stack)
    {
		if(BasicType.get(stack) == BasicType.BIN)
		{
			return new InventoryBin(stack).getItemCount() == 0 ? super.getItemStackLimit(stack) : 1;
		}
		
		return super.getItemStackLimit(stack);
    }
	
	public ItemStack getUnchargedCell(InductionCellTier tier)
	{
		ItemStack stack = new ItemStack(MekanismBlocks.BasicBlock2, 1, 3);
		setBaseTier(stack, tier.getBaseTier());
		
		return stack;
	}
	
	public ItemStack getUnchargedProvider(InductionProviderTier tier)
	{
		ItemStack stack = new ItemStack(MekanismBlocks.BasicBlock2, 1, 4);
		setBaseTier(stack, tier.getBaseTier());
		
		return stack;
	}
	
	@Override
	public BaseTier getBaseTier(ItemStack itemstack)
	{
		if(itemstack.stackTagCompound == null)
		{
			return BaseTier.BASIC;
		}

		return BaseTier.values()[itemstack.stackTagCompound.getInteger("tier")];
	}

	@Override
	public void setBaseTier(ItemStack itemstack, BaseTier tier)
	{
		if(itemstack.stackTagCompound == null)
		{
			itemstack.setTagCompound(new NBTTagCompound());
		}

		itemstack.stackTagCompound.setInteger("tier", tier.ordinal());
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
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List list, boolean flag)
	{
		BasicType type = BasicType.get(itemstack);
		
		if(type.hasDescription)
		{
			if(!MekKeyHandler.getIsKeyPressed(MekanismKeyHandler.sneakKey))
			{
				if(type == BasicType.BIN)
				{
					InventoryBin inv = new InventoryBin(itemstack);
		
					if(inv.getItemCount() > 0)
					{
						list.add(EnumColor.BRIGHT_GREEN + inv.getItemType().getDisplayName());
						list.add(EnumColor.PURPLE + LangUtils.localize("tooltip.itemAmount") + ": " + EnumColor.GREY + inv.getItemCount());
					}
					else {
						list.add(EnumColor.DARK_RED + LangUtils.localize("gui.empty"));
					}
					
					list.add(EnumColor.INDIGO + LangUtils.localize("tooltip.capacity") + ": " + EnumColor.GREY + BinTier.values()[getBaseTier(itemstack).ordinal()].storage + " " + LangUtils.localize("transmission.Items"));
				}
				else if(type == BasicType.INDUCTION_CELL)
				{
					InductionCellTier tier = InductionCellTier.values()[getBaseTier(itemstack).ordinal()];
					
					list.add(tier.getBaseTier().getColor() + LangUtils.localize("tooltip.capacity") + ": " + EnumColor.GREY + MekanismUtils.getEnergyDisplay(tier.maxEnergy));
				}
				else if(type == BasicType.INDUCTION_PROVIDER)
				{
					InductionProviderTier tier = InductionProviderTier.values()[getBaseTier(itemstack).ordinal()];
					
					list.add(tier.getBaseTier().getColor() + LangUtils.localize("tooltip.outputRate") + ": " + EnumColor.GREY + MekanismUtils.getEnergyDisplay(tier.output));
				}
				
				if(getMaxEnergy(itemstack) > 0)
				{
					list.add(EnumColor.BRIGHT_GREEN + LangUtils.localize("tooltip.storedEnergy") + ": " + EnumColor.GREY + MekanismUtils.getEnergyDisplay(getEnergy(itemstack)));
				}
				
				list.add(LangUtils.localize("tooltip.hold") + " " + EnumColor.INDIGO + GameSettings.getKeyDisplayString(MekanismKeyHandler.sneakKey.getKeyCode()) + EnumColor.GREY + " " + LangUtils.localize("tooltip.forDetails") + ".");
			}
			else {
				list.addAll(MekanismUtils.splitTooltip(type.getDescription(), itemstack));
			}
		}
	}

	@Override
	public boolean hasContainerItem(ItemStack stack)
	{
		return BasicType.get(stack) == BasicType.BIN && stack.stackTagCompound != null && stack.stackTagCompound.hasKey("newCount");
	}

	@Override
	public boolean doesContainerItemLeaveCraftingGrid(ItemStack stack)
	{
		if(BasicType.get(stack) != BasicType.BIN)
		{
			return true;
		}

		return false;
	}

	@Override
	public ItemStack getContainerItem(ItemStack stack)
	{
		if(BasicType.get(stack) == BasicType.BIN)
		{
			if(stack.stackTagCompound == null || !stack.stackTagCompound.hasKey("newCount"))
			{
				return null;
			}
			
			int newCount = stack.stackTagCompound.getInteger("newCount");
			stack.stackTagCompound.removeTag("newCount");

            ItemStack ret = stack.copy();
            ret.stackTagCompound.setInteger("itemCount", newCount);

            return ret;
		}

		return null;
	}

	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int metadata)
	{
		boolean place = true;
		
		BasicType type = BasicType.get(stack);
		
		if(type == BasicType.SECURITY_DESK)
		{
			if(y+1 > 255 || !world.getBlock(x, y+1, z).isReplaceable(world, x, y+1, z))
			{
				place = false;
			}
		}

		if(place && super.placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ, metadata))
		{
			if(type == BasicType.BIN && stack.stackTagCompound != null)
			{
				TileEntityBin tileEntity = (TileEntityBin)world.getTileEntity(x, y, z);
				InventoryBin inv = new InventoryBin(stack);
				
				tileEntity.tier = BinTier.values()[getBaseTier(stack).ordinal()];

				if(inv.getItemType() != null)
				{
					tileEntity.setItemType(inv.getItemType());
				}

				tileEntity.setItemCount(inv.getItemCount());
			}
			else if(type == BasicType.INDUCTION_CELL)
			{
				TileEntityInductionCell tileEntity = (TileEntityInductionCell)world.getTileEntity(x, y, z);
				tileEntity.tier = InductionCellTier.values()[getBaseTier(stack).ordinal()];
				
				if(!world.isRemote)
				{
					Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(tileEntity), tileEntity.getNetworkedData(new ArrayList())), new Range4D(Coord4D.get(tileEntity)));
				}
			}
			else if(type == BasicType.INDUCTION_PROVIDER)
			{
				TileEntityInductionProvider tileEntity = (TileEntityInductionProvider)world.getTileEntity(x, y, z);
				tileEntity.tier = InductionProviderTier.values()[getBaseTier(stack).ordinal()];
				
				if(!world.isRemote)
				{
					Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(tileEntity), tileEntity.getNetworkedData(new ArrayList())), new Range4D(Coord4D.get(tileEntity)));
				}
			}
			
			TileEntity tileEntity = world.getTileEntity(x, y, z);
			
			if(tileEntity instanceof IStrictEnergyStorage && !(tileEntity instanceof TileEntityMultiblock<?>))
			{
				((IStrictEnergyStorage)tileEntity).setEnergy(getEnergy(stack));
			}
		}

		return place;
	}
	
	@Override
	public String getUnlocalizedName(ItemStack itemstack)
	{
		BasicType type = BasicType.get(itemstack);
		
		if(type != null)
		{
			String name = getUnlocalizedName() + "." + BasicType.get(itemstack).name;
			
			if(type == BasicType.BIN || type == BasicType.INDUCTION_CELL || type == BasicType.INDUCTION_PROVIDER)
			{
				name += getBaseTier(itemstack).getName();
			}
			
			return name;
		}

		return "null";
	}
	
	@Override
	public double getEnergy(ItemStack itemStack)
	{
		if(BasicType.get(itemStack) == BasicType.INDUCTION_CELL)
		{
			if(itemStack.stackTagCompound == null)
			{
				return 0;
			}
	
			return itemStack.stackTagCompound.getDouble("energyStored");
		}
		
		return 0;
	}

	@Override
	public void setEnergy(ItemStack itemStack, double amount)
	{
		if(BasicType.get(itemStack) == BasicType.INDUCTION_CELL)
		{
			if(itemStack.stackTagCompound == null)
			{
				itemStack.setTagCompound(new NBTTagCompound());
			}
	
			itemStack.stackTagCompound.setDouble("energyStored", Math.max(Math.min(amount, getMaxEnergy(itemStack)), 0));
		}
	}

	@Override
	public double getMaxEnergy(ItemStack itemStack)
	{
		if(BasicType.get(itemStack) == BasicType.INDUCTION_CELL)
		{
			return InductionCellTier.values()[getBaseTier(itemStack).ordinal()].maxEnergy;
		}
		
		return 0;
	}

	@Override
	public double getMaxTransfer(ItemStack itemStack)
	{
		return 0;
	}

	@Override
	public boolean canReceive(ItemStack itemStack)
	{
		return false;
	}

	@Override
	public boolean canSend(ItemStack itemStack)
	{
		return false;
	}
}
