package mekanism.common.multipart;

import java.util.HashSet;
import java.util.Set;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.Range4D;
import mekanism.common.HashList;
import mekanism.common.InventoryNetwork;
import mekanism.common.Mekanism;
import mekanism.common.base.ILogisticalTransporter;
import mekanism.common.base.ITransporterTile;
import mekanism.common.content.transporter.TransporterManager;
import mekanism.common.content.transporter.TransporterStack;
import mekanism.common.content.transporter.TransporterStack.Path;
import mekanism.common.multipart.PartSidedPipe.ConnectionType;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.TransporterUtils;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public class MultipartTransporter extends MultipartTransmitter<IInventory, InventoryNetwork> implements ILogisticalTransporter
{
	public HashList<TransporterStack> transit = new HashList<>();

	public EnumColor color;

	public Set<TransporterStack> needsSync = new HashSet<>();

	public MultipartTransporter(PartLogisticalTransporter multiPart)
	{
		super(multiPart);
	}

	public void update()
	{
		if(world().isRemote)
		{
			for(TransporterStack stack : transit)
			{
				if(stack != null)
				{
					stack.progress = Math.min(100, stack.progress+getPart().tier.speed);
				}
			}
		}
		else {
			Set<TransporterStack> remove = new HashSet<TransporterStack>();

			getPart().pullItems();

			for(TransporterStack stack : transit)
			{
				if(!stack.initiatedPath)
				{
					if(stack.itemStack == null || !recalculate(stack, null))
					{
						remove.add(stack);
						continue;
					}
				}

				stack.progress += getPart().tier.speed;

				if(stack.progress > 100)
				{
					Coord4D prevSet = null;

					if(stack.hasPath())
					{
						int currentIndex = stack.pathToTarget.indexOf(coord());

						if(currentIndex == 0) //Necessary for transition reasons, not sure why
						{
							remove.add(stack);
							continue;
						}

						Coord4D next = stack.pathToTarget.get(currentIndex-1);

						if(!stack.isFinal(this))
						{
							if(next != null && stack.canInsertToTransporter(stack.getNext(this).getTileEntity(world()), ForgeDirection.getOrientation(stack.getSide(this))))
							{
								ITransporterTile nextTile = (ITransporterTile)next.getTileEntity(world());
								nextTile.getTransmitter().entityEntering(stack, stack.progress%100);
								remove.add(stack);

								continue;
							}
							else if(next != null)
							{
								prevSet = next;
							}
						}
						else {
							if(stack.pathType != Path.NONE)
							{
								if(next != null && next.getTileEntity(world()) instanceof IInventory)
								{
									needsSync.add(stack);
									IInventory inventory = (IInventory)next.getTileEntity(world());

									if(inventory != null)
									{
										ItemStack rejected = InventoryUtils.putStackInInventory(inventory, stack.itemStack, stack.getSide(this), stack.pathType == Path.HOME);

										if(rejected == null)
										{
											TransporterManager.remove(stack);
											remove.add(stack);
											continue;
										}
										else {
											needsSync.add(stack);
											stack.itemStack = rejected;

											prevSet = next;
										}
									}
								}
							}
						}
					}

					if(!recalculate(stack, prevSet))
					{
						remove.add(stack);
						continue;
					}
					else {
						if(prevSet != null)
						{
							stack.progress = 0;
						}
						else {
							stack.progress = 50;
						}
					}
				}
				else if(stack.progress == 50)
				{
					if(stack.isFinal(this))
					{
						if(stack.pathType == Path.DEST && (!checkSideForInsert(stack) || !InventoryUtils.canInsert(stack.getDest().getTileEntity(world()), stack.color, stack.itemStack, stack.getSide(this), false)))
						{
							if(!recalculate(stack, null))
							{
								remove.add(stack);
								continue;
							}
						}
						else if(stack.pathType == Path.HOME && (!checkSideForInsert(stack) || !InventoryUtils.canInsert(stack.getDest().getTileEntity(world()), stack.color, stack.itemStack, stack.getSide(this), true)))
						{
							if(!recalculate(stack, null))
							{
								remove.add(stack);
								continue;
							}
						}
						else if(stack.pathType == Path.NONE)
						{
							if(!recalculate(stack, null))
							{
								remove.add(stack);
								continue;
							}
						}
					}
					else {
						TileEntity next = stack.getNext(this).getTileEntity(world());
						boolean recalculate = false;

						if(!stack.canInsertToTransporter(next, ForgeDirection.getOrientation(stack.getSide(this))))
						{
							recalculate = true;
						}

						if(recalculate)
						{
							if(!recalculate(stack, null))
							{
								remove.add(stack);
								continue;
							}
						}
					}
				}
			}

			for(TransporterStack stack : remove)
			{
				Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(coord(), getPart().getSyncPacket(stack, true)), new Range4D(coord()));
				transit.remove(stack);
				MekanismUtils.saveChunk(getPart().tile());
			}

			for(TransporterStack stack : needsSync)
			{
				if(transit.contains(stack))
				{
					Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(coord(), getPart().getSyncPacket(stack, false)), new Range4D(coord()));
				}
			}

			needsSync.clear();
		}
	}

	private boolean checkSideForInsert(TransporterStack stack)
	{
		ForgeDirection side = ForgeDirection.getOrientation(stack.getSide(this));

		return getPart().getConnectionType(side) == ConnectionType.NORMAL || getPart().getConnectionType(side) == ConnectionType.PUSH;
	}

	private boolean recalculate(TransporterStack stack, Coord4D from)
	{
		needsSync.add(stack);

		if(stack.pathType != Path.NONE)
		{
			if(!TransporterManager.didEmit(stack.itemStack, stack.recalculatePath(this, 0)))
			{
				if(!stack.calculateIdle(this))
				{
					TransporterUtils.drop(this, stack);
					return false;
				}
			}
		}
		else {
			if(!stack.calculateIdle(this))
			{
				TransporterUtils.drop(this, stack);
				return false;
			}
		}

		if(from != null)
		{
			stack.originalLocation = from;
		}

		return true;
	}

	@Override
	public ItemStack insert(Coord4D original, ItemStack itemStack, EnumColor color, boolean doEmit, int min)
	{
		return insert_do(original, itemStack, color, doEmit, min, false);
	}

	private ItemStack insert_do(Coord4D original, ItemStack itemStack, EnumColor color, boolean doEmit, int min, boolean force)
	{
		ForgeDirection from = coord().sideDifference(original).getOpposite();

		TransporterStack stack = new TransporterStack();
		stack.itemStack = itemStack;
		stack.originalLocation = original;
		stack.homeLocation = original;
		stack.color = color;

		if((force && !canReceiveFrom(original.getTileEntity(world()), from)) || !stack.canInsertToTransporter(this, from))
		{
			return itemStack;
		}

		ItemStack rejected = stack.recalculatePath(this, min);

		if(TransporterManager.didEmit(stack.itemStack, rejected))
		{
			stack.itemStack = TransporterManager.getToUse(stack.itemStack, rejected);

			if(doEmit)
			{
				transit.add(stack);
				TransporterManager.add(stack);
				Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(coord(), getPart().getSyncPacket(stack, false)), new Range4D(coord()));
				MekanismUtils.saveChunk(getPart().tile());
			}

			return rejected;
		}

		return itemStack;
	}

	@Override
	public ItemStack insertRR(TileEntityLogisticalSorter outputter, ItemStack itemStack, EnumColor color, boolean doEmit, int min)
	{
		ForgeDirection from = coord().sideDifference(Coord4D.get(outputter)).getOpposite();

		TransporterStack stack = new TransporterStack();
		stack.itemStack = itemStack;
		stack.originalLocation = Coord4D.get(outputter);
		stack.homeLocation = Coord4D.get(outputter);
		stack.color = color;

		if(!canReceiveFrom(outputter, from) || !stack.canInsertToTransporter(this, from))
		{
			return itemStack;
		}

		ItemStack rejected = stack.recalculateRRPath(outputter, this, min);

		if(TransporterManager.didEmit(stack.itemStack, rejected))
		{
			stack.itemStack = TransporterManager.getToUse(stack.itemStack, rejected);

			if(doEmit)
			{
				transit.add(stack);
				TransporterManager.add(stack);
				Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(coord(), getPart().getSyncPacket(stack, false)), new Range4D(coord()));
				MekanismUtils.saveChunk(getPart().tile());
			}

			return rejected;
		}

		return itemStack;
	}

	@Override
	public void entityEntering(TransporterStack stack, int progress)
	{
		stack.progress = progress;
		transit.add(stack);
		Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(coord(), getPart().getSyncPacket(stack, false)), new Range4D(coord()));
		MekanismUtils.saveChunk(getPart().tile());
	}

	@Override
	public EnumColor getColor()
	{
		return color;
	}

	@Override
	public void setColor(EnumColor c)
	{
		color = c;
	}

	@Override
	public boolean canEmitTo(TileEntity tileEntity, ForgeDirection side)
	{
		if(!getPart().canConnect(side))
		{
			return false;
		}

		return getPart().getConnectionType(side) == ConnectionType.NORMAL || getPart().getConnectionType(side) == ConnectionType.PUSH;
	}

	@Override
	public boolean canReceiveFrom(TileEntity tileEntity, ForgeDirection side)
	{
		if(!getPart().canConnect(side))
		{
			return false;
		}

		return getPart().getConnectionType(side) == ConnectionType.NORMAL;
	}

	@Override
	public double getCost()
	{
		return getPart().getCost();
	}

	@Override
	public boolean canConnectMutual(ForgeDirection side)
	{
		return getPart().canConnectMutual(side);
	}

	@Override
	public boolean canConnect(ForgeDirection side)
	{
		return getPart().canConnect(side);
	}

	@Override
	public PartLogisticalTransporter getPart()
	{
		return (PartLogisticalTransporter)containingPart;
	}
}
