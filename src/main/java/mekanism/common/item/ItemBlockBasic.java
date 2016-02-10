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
import mekanism.common.Tier.InductionCellTier;
import mekanism.common.Tier.InductionProviderTier;
import mekanism.common.block.states.BlockStateBasic.BasicBlockType;
import mekanism.common.inventory.InventoryBin;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.tile.TileEntityBin;
import mekanism.common.tile.TileEntityInductionCell;
import mekanism.common.tile.TileEntityInductionProvider;
import mekanism.common.tile.TileEntityMultiblock;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
 * 0:14: Solar Evaporation Controller
 * 0:15: Solar Evaporation Valve
 * 1:0: Solar Evaporation Block
 * 1:1: Induction Casing
 * 1:2: Induction Port
 * 1:3: Induction Cell
 * 1:4: Induction Provider
 * 1:5: Superheating Element
 * 1:6: Boiler Casing
 * 1:7: Boiler Valve
 * @author AidanBrady
 *
 */
public class ItemBlockBasic extends ItemBlock implements IEnergizedItem
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
		if(Block.getBlockFromItem(this) == MekanismBlocks.BasicBlock && stack.getItemDamage() == 6)
		{
			return new InventoryBin(stack).getItemCount() == 0 ? super.getItemStackLimit(stack) : 1;
		}
		
		return super.getItemStackLimit(stack);
    }
	
	public ItemStack getUnchargedCell(InductionCellTier tier)
	{
		ItemStack stack = new ItemStack(MekanismBlocks.BasicBlock2, 1, 3);
		setTier(stack, tier.getBaseTier());
		return stack;
	}
	
	public ItemStack getUnchargedProvider(InductionProviderTier tier)
	{
		ItemStack stack = new ItemStack(MekanismBlocks.BasicBlock2, 1, 4);
		setTier(stack, tier.getBaseTier());
		return stack;
	}
	
	public BaseTier getTier(ItemStack itemstack)
	{
		if(itemstack.getTagCompound() == null)
		{
			return BaseTier.BASIC;
		}

		return BaseTier.values()[itemstack.getTagCompound().getInteger("tier")];
	}

	public void setTier(ItemStack itemstack, BaseTier tier)
	{
		if(itemstack.getTagCompound() == null)
		{
			itemstack.setTagCompound(new NBTTagCompound());
		}

		itemstack.getTagCompound().setInteger("tier", tier.ordinal());
	}

	@Override
	public int getMetadata(int i)
	{
		return i;
	}

	@Override
	public ModelResourceLocation getModel(ItemStack stack, EntityPlayer player, int useRemaining)
	{
		return null; //TODO: IDONTKNOWHOWTODOTHIS
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List list, boolean flag)
	{
		BasicBlockType type = BasicBlockType.get(itemstack);
		
		if(type.hasDescription)
		{
			if(!MekKeyHandler.getIsKeyPressed(MekanismKeyHandler.sneakKey))
			{
				if(type == BasicBlockType.BIN)
				{
					InventoryBin inv = new InventoryBin(itemstack);
		
					if(inv.getItemCount() > 0)
					{
						list.add(EnumColor.BRIGHT_GREEN + inv.getItemType().getDisplayName());
						list.add(EnumColor.INDIGO + LangUtils.localize("tooltip.itemAmount") + ": " + EnumColor.GREY + inv.getItemCount());
					}
					else {
						list.add(EnumColor.DARK_RED + LangUtils.localize("gui.empty"));
					}
				}
				else if(type == BasicBlockType.INDUCTION_CELL)
				{
					InductionCellTier tier = InductionCellTier.values()[getTier(itemstack).ordinal()];
					
					list.add(tier.getBaseTier().getColor() + LangUtils.localize("tooltip.capacity") + ": " + EnumColor.GREY + MekanismUtils.getEnergyDisplay(tier.maxEnergy));
				}
				else if(type == BasicBlockType.INDUCTION_PROVIDER)
				{
					InductionProviderTier tier = InductionProviderTier.values()[getTier(itemstack).ordinal()];
					
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
		return BasicBlockType.get(stack) == BasicBlockType.BIN && stack.getTagCompound() != null && stack.getTagCompound().hasKey("newCount");
	}

/*
	@Override
	public boolean doesContainerItemLeaveCraftingGrid(ItemStack stack)
	{
		if(BasicType.get(stack) != BasicType.BIN)
		{
			return true;
		}

		if(stack.getTagCompound() == null || !stack.getTagCompound().hasKey("newCount"))
		{
			return true;
		}

		return false;
	}
*/

	@Override
	public ItemStack getContainerItem(ItemStack stack)
	{
		if(BasicBlockType.get(stack) == BasicBlockType.BIN)
		{
			if(stack.getTagCompound() == null || !stack.getTagCompound().hasKey("newCount"))
			{
				return null;
			}

            ItemStack ret = stack.copy();
            ret.getTagCompound().setInteger("itemCount", stack.getTagCompound().getInteger("newCount"));

            return ret;
		}

		return null;
	}

	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState metadata)
	{
		boolean place = super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, metadata);

		if(place)
		{
			BasicBlockType type = BasicBlockType.get(stack);

			if(type == BasicBlockType.BIN && stack.getTagCompound() != null)
			{
				TileEntityBin tileEntity = (TileEntityBin)world.getTileEntity(pos);
				InventoryBin inv = new InventoryBin(stack);

				if(inv.getItemType() != null)
				{
					tileEntity.setItemType(inv.getItemType());
				}

				tileEntity.setItemCount(inv.getItemCount());
			}
			else if(type == BasicBlockType.INDUCTION_CELL)
			{
				TileEntityInductionCell tileEntity = (TileEntityInductionCell)world.getTileEntity(pos);
				tileEntity.tier = InductionCellTier.values()[getTier(stack).ordinal()];

				if(!world.isRemote)
				{
					Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(tileEntity), tileEntity.getNetworkedData(new ArrayList<Object>())), new Range4D(Coord4D.get(tileEntity)));
				}
			}
			else if(type == BasicBlockType.INDUCTION_PROVIDER)
			{
				TileEntityInductionProvider tileEntity = (TileEntityInductionProvider)world.getTileEntity(pos);
				tileEntity.tier = InductionProviderTier.values()[getTier(stack).ordinal()];

				if(!world.isRemote)
				{
					Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(tileEntity), tileEntity.getNetworkedData(new ArrayList<Object>())), new Range4D(Coord4D.get(tileEntity)));
				}
			}

			TileEntity tileEntity = world.getTileEntity(pos);

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
		if(BasicBlockType.get(itemstack) != null)
		{
			return getUnlocalizedName() + "." + BasicBlockType.get(itemstack).name;
		}

		return "null";
	}
	
	@Override
	public String getItemStackDisplayName(ItemStack itemstack)
	{
		BasicBlockType type = BasicBlockType.get(itemstack);
		
		if(type == BasicBlockType.INDUCTION_CELL || type == BasicBlockType.INDUCTION_PROVIDER)
		{
			return getTier(itemstack).getLocalizedName() + " " + super.getItemStackDisplayName(itemstack);
		}
		
		return super.getItemStackDisplayName(itemstack);
	}
	
	@Override
	public double getEnergy(ItemStack itemStack)
	{
		if(BasicBlockType.get(itemStack) == BasicBlockType.INDUCTION_CELL)
		{
			if(itemStack.getTagCompound() == null)
			{
				return 0;
			}
	
			return itemStack.getTagCompound().getDouble("energyStored");
		}
		
		return 0;
	}

	@Override
	public void setEnergy(ItemStack itemStack, double amount)
	{
		if(BasicBlockType.get(itemStack) == BasicBlockType.INDUCTION_CELL)
		{
			if(itemStack.getTagCompound() == null)
			{
				itemStack.setTagCompound(new NBTTagCompound());
			}
	
			itemStack.getTagCompound().setDouble("energyStored", Math.max(Math.min(amount, getMaxEnergy(itemStack)), 0));
		}
	}

	@Override
	public double getMaxEnergy(ItemStack itemStack)
	{
		if(BasicBlockType.get(itemStack) == BasicBlockType.INDUCTION_CELL)
		{
			return InductionCellTier.values()[getTier(itemStack).ordinal()].maxEnergy;
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

	@Override
	public boolean isMetadataSpecific(ItemStack itemStack) 
	{
		return true;
	}
}
