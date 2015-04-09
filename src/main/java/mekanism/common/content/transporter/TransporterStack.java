package mekanism.common.content.transporter;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.transmitters.IBlockableConnection;
import mekanism.common.PacketHandler;
import mekanism.common.base.ILogisticalTransporter;
import mekanism.common.base.ITransporterTile;
import mekanism.common.content.transporter.TransporterPathfinder.Destination;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.util.TransporterUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public class TransporterStack
{
	public ItemStack itemStack;

	public int progress;

	public EnumColor color = null;

	public boolean initiatedPath = false;
	
	public ForgeDirection idleDir = ForgeDirection.UNKNOWN;

	public List<Coord4D> pathToTarget = new ArrayList<Coord4D>();

	public Coord4D originalLocation;
	public Coord4D homeLocation;

	public Coord4D clientNext;
	public Coord4D clientPrev;

	public Path pathType;

	public void write(ILogisticalTransporter transporter, ArrayList data)
	{
		if(color != null)
		{
			data.add(TransporterUtils.colors.indexOf(color));
		}
		else {
			data.add(-1);
		}

		data.add(progress);
		originalLocation.write(data);
		data.add(pathType.ordinal());

		if(pathToTarget.indexOf(transporter.coord()) > 0)
		{
			data.add(true);
			getNext(transporter).write(data);
		}
		else {
			data.add(false);
		}

		getPrev(transporter).write(data);

		data.add(itemStack);
	}

	public void read(ByteBuf dataStream)
	{
		int c = dataStream.readInt();

		if(c != -1)
		{
			color = TransporterUtils.colors.get(c);
		}
		else {
			color = null;
		}

		progress = dataStream.readInt();
		originalLocation = Coord4D.read(dataStream);
		pathType = Path.values()[dataStream.readInt()];

		if(dataStream.readBoolean())
		{
			clientNext = Coord4D.read(dataStream);
		}

		clientPrev = Coord4D.read(dataStream);

		itemStack = PacketHandler.readStack(dataStream);
	}

	public void write(NBTTagCompound nbtTags)
	{
		if(color != null)
		{
			nbtTags.setInteger("color", TransporterUtils.colors.indexOf(color));
		}

		nbtTags.setInteger("progress", progress);
		nbtTags.setTag("originalLocation", originalLocation.write(new NBTTagCompound()));
		nbtTags.setInteger("idleDir", idleDir.ordinal());

		if(homeLocation != null)
		{
			nbtTags.setTag("homeLocation", homeLocation.write(new NBTTagCompound()));
		}

		nbtTags.setInteger("pathType", pathType.ordinal());
		itemStack.writeToNBT(nbtTags);
	}

	public void read(NBTTagCompound nbtTags)
	{
		if(nbtTags.hasKey("color"))
		{
			color = TransporterUtils.colors.get(nbtTags.getInteger("color"));
		}

		progress = nbtTags.getInteger("progress");
		originalLocation = Coord4D.read(nbtTags.getCompoundTag("originalLocation"));
		idleDir = ForgeDirection.values()[nbtTags.getInteger("idleDir")];

		if(nbtTags.hasKey("homeLocation"))
		{
			homeLocation = Coord4D.read(nbtTags.getCompoundTag("homeLocation"));
		}

		pathType = Path.values()[nbtTags.getInteger("pathType")];
		itemStack = ItemStack.loadItemStackFromNBT(nbtTags);
	}

	public static TransporterStack readFromNBT(NBTTagCompound nbtTags)
	{
		TransporterStack stack = new TransporterStack();
		stack.read(nbtTags);

		return stack;
	}

	public static TransporterStack readFromPacket(ByteBuf dataStream)
	{
		TransporterStack stack = new TransporterStack();
		stack.read(dataStream);

		return stack;
	}

	public boolean hasPath()
	{
		return pathToTarget != null && pathToTarget.size() >= 2;
	}

	public ItemStack recalculatePath(ILogisticalTransporter transporter, int min)
	{
		Destination newPath = TransporterPathfinder.getNewBasePath(transporter, this, min);

		if(newPath == null)
		{
			return itemStack;
		}

		pathToTarget = newPath.path;
		pathType = Path.DEST;
		idleDir = ForgeDirection.UNKNOWN;
		initiatedPath = true;

		return newPath.rejected;
	}

	public ItemStack recalculateRRPath(TileEntityLogisticalSorter outputter, ILogisticalTransporter transporter, int min)
	{
		Destination newPath = TransporterPathfinder.getNewRRPath(transporter, this, outputter, min);

		if(newPath == null)
		{
			return itemStack;
		}

		pathToTarget = newPath.path;
		pathType = Path.DEST;
		idleDir = ForgeDirection.UNKNOWN;
		initiatedPath = true;

		return newPath.rejected;
	}

	public boolean calculateIdle(ILogisticalTransporter transporter)
	{
		List<Coord4D> newPath = TransporterPathfinder.getIdlePath(transporter, this);

		if(newPath == null)
		{
			return false;
		}
		
		if(pathType == Path.HOME)
		{
			idleDir = ForgeDirection.UNKNOWN;
		}

		pathToTarget = newPath;

		originalLocation = transporter.coord();
		initiatedPath = true;

		return true;
	}

	public boolean isFinal(ILogisticalTransporter transporter)
	{
		return pathToTarget.indexOf(transporter.coord()) == (pathType == Path.NONE ? 0 : 1);
	}

	public Coord4D getNext(ILogisticalTransporter transporter)
	{
		if(!transporter.world().isRemote)
		{
			int index = pathToTarget.indexOf(transporter.coord())-1;

			if(index < 0)
			{
				return null;
			}

			return pathToTarget.get(index);
		}
		else {
			return clientNext;
		}
	}

	public Coord4D getPrev(ILogisticalTransporter transporter)
	{
		if(!transporter.world().isRemote)
		{
			int index = pathToTarget.indexOf(transporter.coord())+1;

			if(index < pathToTarget.size())
			{
				return pathToTarget.get(index);
			}
			else {
				return originalLocation;
			}
		}
		else {
			return clientPrev;
		}
	}

	public int getSide(ILogisticalTransporter transporter)
	{
		if(progress < 50)
		{
			if(getPrev(transporter) != null)
			{
				return transporter.coord().sideDifference(getPrev(transporter)).ordinal();
			}
		}
		else if(progress == 50)
		{
			if(getNext(transporter) != null)
			{
				return getNext(transporter).sideDifference(transporter.coord()).ordinal();
			}
		}
		else if(progress > 50)
		{
			if(getNext(transporter) != null)
			{
				return getNext(transporter).sideDifference(transporter.coord()).ordinal();
			}
		}

		return 0;
	}

	public boolean canInsertToTransporter(TileEntity tileEntity, ForgeDirection from)
	{
		if(!(tileEntity instanceof ITransporterTile))
		{
			return false;
		}

		ILogisticalTransporter transporter = ((ITransporterTile)tileEntity).getTransmitter();

		if(!((ITransporterTile)tileEntity).canConnectMutual(from.getOpposite()))
		{
			return false;
		}

		return transporter.getColor() == color || transporter.getColor() == null;
	}

	public boolean canInsertToTransporter(ILogisticalTransporter transporter, ForgeDirection side)
	{
		if(!transporter.canConnectMutual(side))
		{
			return false;
		}

		return transporter.getColor() == color || transporter.getColor() == null;
	}

	public Coord4D getDest()
	{
		return pathToTarget.get(0);
	}

	public static enum Path
	{
		DEST, HOME, NONE
	}
}
