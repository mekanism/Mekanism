package mekanism.common.transporter;

import java.util.ArrayList;
import java.util.List;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.common.ILogisticalTransporter;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.transporter.TransporterPathfinder.Destination;
import mekanism.common.util.TransporterUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import com.google.common.io.ByteArrayDataInput;

public class TransporterStack
{
	public ItemStack itemStack;

	public int progress;

	public EnumColor color = null;

	public boolean initiatedPath = false;

	public List<Coord4D> pathToTarget = new ArrayList<Coord4D>();

	public Coord4D originalLocation;
	public Coord4D homeLocation;

	public Coord4D clientNext;
	public Coord4D clientPrev;

	public Path pathType;

	public void write(ILogisticalTransporter tileEntity, ArrayList data)
	{
		if(color != null)
		{
			data.add(TransporterUtils.colors.indexOf(color));
		}
		else {
			data.add(-1);
		}

		data.add(progress);
		data.add(pathType.ordinal());

		if(pathToTarget.indexOf(Coord4D.get(tileEntity.getTile())) > 0)
		{
			data.add(true);
			getNext(tileEntity).write(data);
		}
		else {
			data.add(false);
		}

		getPrev(tileEntity).write(data);

		data.add(itemStack.itemID);
		data.add(itemStack.stackSize);
		data.add(itemStack.getItemDamage());
	}

	public void read(ByteArrayDataInput dataStream)
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
		pathType = Path.values()[dataStream.readInt()];

		if(dataStream.readBoolean())
		{
			clientNext = Coord4D.read(dataStream);
		}

		clientPrev = Coord4D.read(dataStream);

		itemStack = new ItemStack(dataStream.readInt(), dataStream.readInt(), dataStream.readInt());
	}

	public void write(NBTTagCompound nbtTags)
	{
		if(color != null)
		{
			nbtTags.setInteger("color", TransporterUtils.colors.indexOf(color));
		}

		nbtTags.setInteger("progress", progress);
		nbtTags.setTag("originalLocation", originalLocation.write(new NBTTagCompound()));

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

	public static TransporterStack readFromPacket(ByteArrayDataInput dataStream)
	{
		TransporterStack stack = new TransporterStack();
		stack.read(dataStream);

		return stack;
	}

	public boolean hasPath()
	{
		return pathToTarget != null && pathToTarget.size() >= 2;
	}

	public ItemStack recalculatePath(ILogisticalTransporter tileEntity, int min)
	{
		Destination newPath = TransporterPathfinder.getNewBasePath(tileEntity, this, min);

		if(newPath == null)
		{
			return itemStack;
		}

		pathToTarget = newPath.path;

		pathType = Path.DEST;
		initiatedPath = true;

		return newPath.rejected;
	}

	public ItemStack recalculateRRPath(TileEntityLogisticalSorter outputter, ILogisticalTransporter tileEntity, int min)
	{
		Destination newPath = TransporterPathfinder.getNewRRPath(tileEntity, this, outputter, min);

		if(newPath == null)
		{
			return itemStack;
		}

		pathToTarget = newPath.path;

		pathType = Path.DEST;
		initiatedPath = true;

		return newPath.rejected;
	}

	public boolean calculateIdle(ILogisticalTransporter tileEntity)
	{
		List<Coord4D> newPath = TransporterPathfinder.getIdlePath(tileEntity, this);

		if(newPath == null)
		{
			return false;
		}

		pathToTarget = newPath;

		originalLocation = Coord4D.get(tileEntity.getTile());
		initiatedPath = true;

		return true;
	}

	public boolean isFinal(ILogisticalTransporter tileEntity)
	{
		return pathToTarget.indexOf(Coord4D.get(tileEntity.getTile())) == (pathType == Path.NONE ? 0 : 1);
	}

	public Coord4D getNext(ILogisticalTransporter tileEntity)
	{
		if(!tileEntity.getTile().getWorldObj().isRemote)
		{
			int index = pathToTarget.indexOf(Coord4D.get(tileEntity.getTile()))-1;

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

	public Coord4D getPrev(ILogisticalTransporter tileEntity)
	{
		if(!tileEntity.getTile().getWorldObj().isRemote)
		{
			int index = pathToTarget.indexOf(Coord4D.get(tileEntity.getTile()))+1;

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

	public int getSide(ILogisticalTransporter tileEntity)
	{
		if(progress < 50)
		{
			if(getPrev(tileEntity) != null)
			{
				return Coord4D.get(tileEntity.getTile()).sideDifference(getPrev(tileEntity)).ordinal();
			}
		}
		else if(progress == 50)
		{
			if(getNext(tileEntity) != null)
			{
				return getNext(tileEntity).sideDifference(Coord4D.get(tileEntity.getTile())).ordinal();
			}
		}
		else if(progress > 50)
		{
			if(getNext(tileEntity) != null)
			{
				return getNext(tileEntity).sideDifference(Coord4D.get(tileEntity.getTile())).ordinal();
			}
		}

		return 0;
	}

	public boolean canInsertToTransporter(TileEntity tileEntity, ForgeDirection side)
	{
		if(!(tileEntity instanceof ILogisticalTransporter))
		{
			return false;
		}

		TileEntity from = Coord4D.get(tileEntity).getFromSide(side.getOpposite()).getTileEntity(tileEntity.getWorldObj());
		ILogisticalTransporter transporter = (ILogisticalTransporter)tileEntity;

		if(!transporter.canConnectMutual(side.getOpposite()))
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
