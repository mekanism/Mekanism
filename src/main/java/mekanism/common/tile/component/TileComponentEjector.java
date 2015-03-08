package mekanism.common.tile.component;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.SideData;
import mekanism.common.base.IEjector;
import mekanism.common.base.ILogisticalTransporter;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.base.ITileComponent;
import mekanism.common.content.transporter.TransporterManager;
import mekanism.common.tile.TileEntityContainerBlock;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.TransporterUtils;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public class TileComponentEjector implements ITileComponent, IEjector
{
	public TileEntityContainerBlock tileEntity;

	public boolean strictInput;

	public EnumColor outputColor;

	public EnumColor[] inputColors = new EnumColor[] {null, null, null, null, null, null};

	public int tickDelay = 0;

	public SideData sideData;

	public int[] trackers;

	public TileComponentEjector(TileEntityContainerBlock tile, SideData data)
	{
		tileEntity = tile;
		sideData = data;
		trackers = new int[sideData.availableSlots.length];

		tile.components.add(this);
	}

	private List<ForgeDirection> getTrackedOutputs(int index, List<ForgeDirection> dirs)
	{
		List<ForgeDirection> sides = new ArrayList<ForgeDirection>();

		for(int i = trackers[index]+1; i <= trackers[index]+6; i++)
		{
			for(ForgeDirection side : dirs)
			{
				if(ForgeDirection.getOrientation(i%6) == side)
				{
					sides.add(side);
				}
			}
		}

		return sides;
	}

	@Override
	public void tick()
	{
		if(tickDelay == 0)
		{
			onOutput();
		}
		else {
			tickDelay--;
		}
	}

	@Override
	public void onOutput()
	{
		if(!getEjecting(TransmissionType.ITEM) || tileEntity.getWorldObj().isRemote)
		{
			return;
		}

		List<ForgeDirection> outputSides = new ArrayList<ForgeDirection>();

		ISideConfiguration configurable = (ISideConfiguration)tileEntity;

		for(int i = 0; i < configurable.getConfig().getConfig(TransmissionType.ITEM).length; i++)
		{
			if(configurable.getConfig().getConfig(TransmissionType.ITEM)[i] == configurable.getConfig().getOutputs(TransmissionType.ITEM).indexOf(sideData))
			{
				outputSides.add(ForgeDirection.getOrientation(MekanismUtils.getBaseOrientation(i, tileEntity.facing)));
			}
		}

		for(int index = 0; index < sideData.availableSlots.length; index++)
		{
			int slotID = sideData.availableSlots[index];

			if(tileEntity.inventory[slotID] == null)
			{
				continue;
			}

			ItemStack stack = tileEntity.inventory[slotID];
			List<ForgeDirection> outputs = getTrackedOutputs(index, outputSides);

			for(ForgeDirection side : outputs)
			{
				TileEntity tile = Coord4D.get(tileEntity).getFromSide(side).getTileEntity(tileEntity.getWorldObj());
				ItemStack prev = stack.copy();

				if(tile instanceof IInventory && !(tile instanceof ILogisticalTransporter))
				{
					stack = InventoryUtils.putStackInInventory((IInventory)tile, stack, side.ordinal(), false);
				}
				else if(tile instanceof ILogisticalTransporter)
				{
					ItemStack rejects = TransporterUtils.insert(tileEntity, (ILogisticalTransporter)tile, stack, outputColor, true, 0);

					if(TransporterManager.didEmit(stack, rejects))
					{
						stack = rejects;
					}
				}

				if(stack == null || prev.stackSize != stack.stackSize)
				{
					trackers[index] = side.ordinal();
				}

				if(stack == null)
				{
					break;
				}
			}

			tileEntity.inventory[slotID] = stack;
			tileEntity.markDirty();
		}

		tickDelay = 20;
	}

	@Override
	public boolean hasStrictInput()
	{
		return strictInput;
	}

	@Override
	public void setStrictInput(boolean strict)
	{
		strictInput = strict;
		MekanismUtils.saveChunk(tileEntity);
	}

	@Override
	public void setOutputColor(EnumColor color)
	{
		outputColor = color;
		MekanismUtils.saveChunk(tileEntity);
	}

	@Override
	public EnumColor getOutputColor()
	{
		return outputColor;
	}

	@Override
	public void setInputColor(ForgeDirection side, EnumColor color)
	{
		inputColors[side.ordinal()] = color;
		MekanismUtils.saveChunk(tileEntity);
	}

	@Override
	public EnumColor getInputColor(ForgeDirection side)
	{
		return inputColors[side.ordinal()];
	}

	@Override
	public void read(NBTTagCompound nbtTags)
	{
		strictInput = nbtTags.getBoolean("strictInput");

		if(nbtTags.hasKey("ejectColor"))
		{
			outputColor = TransporterUtils.colors.get(nbtTags.getInteger("ejectColor"));
		}

		for(int i = 0; i < sideData.availableSlots.length; i++)
		{
			trackers[i] = nbtTags.getInteger("tracker" + i);
		}

		for(int i = 0; i < 6; i++)
		{
			if(nbtTags.hasKey("inputColors" + i))
			{
				int inC = nbtTags.getInteger("inputColors" + i);

				if(inC != -1)
				{
					inputColors[i] = TransporterUtils.colors.get(inC);
				}
				else {
					inputColors[i] = null;
				}
			}
		}
	}

	@Override
	public void read(ByteBuf dataStream)
	{
		strictInput = dataStream.readBoolean();

		int c = dataStream.readInt();

		if(c != -1)
		{
			outputColor = TransporterUtils.colors.get(c);
		}
		else {
			outputColor = null;
		}

		for(int i = 0; i < 6; i++)
		{
			int inC = dataStream.readInt();

			if(inC != -1)
			{
				inputColors[i] = TransporterUtils.colors.get(inC);
			}
			else {
				inputColors[i] = null;
			}
		}
	}

	@Override
	public void write(NBTTagCompound nbtTags)
	{
		nbtTags.setBoolean("strictInput", strictInput);

		if(outputColor != null)
		{
			nbtTags.setInteger("ejectColor", TransporterUtils.colors.indexOf(outputColor));
		}

		for(int i = 0; i < sideData.availableSlots.length; i++)
		{
			nbtTags.setInteger("tracker" + i, trackers[i]);
		}

		for(int i = 0; i < 6; i++)
		{
			if(inputColors[i] == null)
			{
				nbtTags.setInteger("inputColors" + i, -1);
			}
			else {
				nbtTags.setInteger("inputColors" + i, TransporterUtils.colors.indexOf(inputColors[i]));
			}
		}
	}

	@Override
	public void write(ArrayList data)
	{
		data.add(strictInput);

		if(outputColor != null)
		{
			data.add(TransporterUtils.colors.indexOf(outputColor));
		}
		else {
			data.add(-1);
		}

		for(int i = 0; i < 6; i++)
		{
			if(inputColors[i] == null)
			{
				data.add(-1);
			}
			else {
				data.add(TransporterUtils.colors.indexOf(inputColors[i]));
			}
		}
	}
	
	private boolean getEjecting(TransmissionType type)
	{
		return ((ISideConfiguration)tileEntity).getConfig().isEjecting(type);
	}
}
