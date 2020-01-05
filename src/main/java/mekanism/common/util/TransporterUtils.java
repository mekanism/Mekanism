package mekanism.common.util;

import java.util.List;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.transmitters.ITransmitterTile;
import mekanism.api.util.ListUtils;
import mekanism.common.base.ILogisticalTransporter;
import mekanism.common.base.ITransporterTile;
import mekanism.common.content.transporter.TransporterManager;
import mekanism.common.content.transporter.TransporterStack;
import mekanism.common.tile.TileEntityLogisticalSorter;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public final class TransporterUtils
{
	public static List<EnumColor> colors = ListUtils.asList(EnumColor.DARK_BLUE, EnumColor.DARK_GREEN, EnumColor.DARK_AQUA, EnumColor.DARK_RED, EnumColor.PURPLE,
			EnumColor.INDIGO, EnumColor.BRIGHT_GREEN, EnumColor.AQUA, EnumColor.RED, EnumColor.PINK, EnumColor.YELLOW, EnumColor.BLACK);

	public static boolean isValidAcceptorOnSide(TileEntity tile, ForgeDirection side)
	{
		if(tile instanceof ITransmitterTile || !(tile instanceof IInventory))
		{
			return false;
		}

		IInventory inventory = (IInventory)tile;

		if(inventory.getSizeInventory() > 0)
		{
			if(!(inventory instanceof ISidedInventory))
			{
				return true;
			}

			int[] slots = ((ISidedInventory)inventory).getAccessibleSlotsFromSide(side.getOpposite().ordinal());

			return (slots != null && slots.length > 0);
		}
		
		return false;
	}

	/**
	 * Gets all the inventories around a tile entity.
	 * @param transporter - center tile entity
	 * @return array of IInventories
	 */
	public static IInventory[] getConnectedInventories(ILogisticalTransporter transporter)
	{
		IInventory[] inventories = new IInventory[] {null, null, null, null, null, null};

		for(ForgeDirection orientation : ForgeDirection.VALID_DIRECTIONS)
		{
			TileEntity inventory = transporter.coord().getFromSide(orientation).getTileEntity(transporter.world());

			if(inventory instanceof IInventory && !(inventory instanceof ITransmitterTile))
			{
				inventories[orientation.ordinal()] = (IInventory)inventory;
			}
		}

		return inventories;
	}

	public static ItemStack insert(TileEntity outputter, ILogisticalTransporter transporter, ItemStack itemStack, EnumColor color, boolean doEmit, int min)
	{
		return transporter.insert(Coord4D.get(outputter), itemStack.copy(), color, doEmit, min);
	}

	public static ItemStack insertRR(TileEntityLogisticalSorter outputter, ILogisticalTransporter transporter, ItemStack itemStack, EnumColor color, boolean doEmit, int min)
	{
		return transporter.insertRR(outputter, itemStack.copy(), color, doEmit, min);
	}

	public static EnumColor increment(EnumColor color)
	{
		if(color == null)
		{
			return colors.get(0);
		}
		else if(colors.indexOf(color) == colors.size()-1)
		{
			return null;
		}

		return colors.get(colors.indexOf(color)+1);
	}

	public static EnumColor decrement(EnumColor color)
	{
		if(color == null)
		{
			return colors.get(colors.size()-1);
		}
		else if(colors.indexOf(color) == 0)
		{
			return null;
		}

		return colors.get(colors.indexOf(color)-1);
	}

	public static void drop(ILogisticalTransporter tileEntity, TransporterStack stack)
	{
		float[] pos;

		if(stack.hasPath())
		{
			pos = TransporterUtils.getStackPosition(tileEntity, stack, 0);
		}
		else {
			pos = new float[] {0, 0, 0};
		}

		TransporterManager.remove(stack);

		EntityItem entityItem = new EntityItem(tileEntity.world(), tileEntity.coord().xCoord + pos[0], tileEntity.coord().yCoord + pos[1], tileEntity.coord().zCoord + pos[2], stack.itemStack);

		entityItem.motionX = 0;
		entityItem.motionY = 0;
		entityItem.motionZ = 0;

		tileEntity.world().spawnEntityInWorld(entityItem);
	}

	public static float[] getStackPosition(ILogisticalTransporter tileEntity, TransporterStack stack, float partial)
	{
		Coord4D offset = new Coord4D(0, 0, 0, tileEntity.world().provider.dimensionId).step(ForgeDirection.getOrientation(stack.getSide(tileEntity)));
		float progress = (((float)stack.progress + partial) / 100F) - 0.5F;

		float itemFix = 0;

		if(!(stack.itemStack.getItem() instanceof ItemBlock))
		{
			itemFix = 0.1F;
		}

		return new float[] {0.5F + offset.xCoord*progress, 0.5F + offset.yCoord*progress - itemFix, 0.5F + offset.zCoord*progress};
	}

	public static void incrementColor(ILogisticalTransporter tileEntity)
	{
		if(tileEntity.getColor() == null)
		{
			tileEntity.setColor(colors.get(0));
			return;
		}
		else if(colors.indexOf(tileEntity.getColor()) == colors.size()-1)
		{
			tileEntity.setColor(null);
			return;
		}

		int index = colors.indexOf(tileEntity.getColor());
		tileEntity.setColor(colors.get(index+1));
	}
}