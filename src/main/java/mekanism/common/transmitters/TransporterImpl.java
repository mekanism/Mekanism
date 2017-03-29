package mekanism.common.transmitters;

import java.util.HashSet;
import java.util.Set;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.Range4D;
import mekanism.api.util.CapabilityUtils;
import mekanism.common.HashList;
import mekanism.common.InventoryNetwork;
import mekanism.common.Mekanism;
import mekanism.common.base.ILogisticalTransporter;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.content.transporter.TransporterManager;
import mekanism.common.content.transporter.TransporterStack;
import mekanism.common.content.transporter.TransporterStack.Path;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.tile.transmitter.TileEntityLogisticalTransporter;
import mekanism.common.tile.transmitter.TileEntitySidedPipe.ConnectionType;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.TransporterUtils;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

public class TransporterImpl extends TransmitterImpl<IInventory, InventoryNetwork> implements ILogisticalTransporter
{
	public HashList<TransporterStack> transit = new HashList<>();

	public EnumColor color;

	public Set<TransporterStack> needsSync = new HashSet<>();

	public TransporterImpl(TileEntityLogisticalTransporter multiPart)
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
					stack.progress = Math.min(100, stack.progress+getTileEntity().tier.speed);
				}
			}
		}
		else {
			if(getTransmitterNetwork() == null)
			{
				return;
			}
			
			Set<TransporterStack> remove = new HashSet<TransporterStack>();

			getTileEntity().pullItems();

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

				stack.progress += getTileEntity().tier.speed;

				if(stack.progress > 100)
				{
					Coord4D prevSet = null;

					if(stack.hasPath())
					{
						int currentIndex = stack.getPath().indexOf(coord());

						if(currentIndex == 0) //Necessary for transition reasons, not sure why
						{
							remove.add(stack);
							continue;
						}

						Coord4D next = stack.getPath().get(currentIndex-1);

						if(!stack.isFinal(this))
						{
							if(next != null && stack.canInsertToTransporter(stack.getNext(this).getTileEntity(world()), stack.getSide(this)))
							{
								ILogisticalTransporter nextTile = CapabilityUtils.getCapability(next.getTileEntity(world()), Capabilities.LOGISTICAL_TRANSPORTER_CAPABILITY, null);
								nextTile.entityEntering(stack, stack.progress%100);
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

						if(!stack.canInsertToTransporter(next, stack.getSide(this)))
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
				Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(coord(), getTileEntity().getSyncPacket(stack, true)), new Range4D(coord()));
				transit.remove(stack);
				MekanismUtils.saveChunk(getTileEntity());
			}

			for(TransporterStack stack : needsSync)
			{
				if(transit.contains(stack))
				{
					Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(coord(), getTileEntity().getSyncPacket(stack, false)), new Range4D(coord()));
				}
			}

			needsSync.clear();
		}
	}

	private boolean checkSideForInsert(TransporterStack stack)
	{
		EnumFacing side = stack.getSide(this);

		return getTileEntity().getConnectionType(side) == ConnectionType.NORMAL || getTileEntity().getConnectionType(side) == ConnectionType.PUSH;
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
		EnumFacing from = coord().sideDifference(original).getOpposite();

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
				Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(coord(), getTileEntity().getSyncPacket(stack, false)), new Range4D(coord()));
				MekanismUtils.saveChunk(getTileEntity());
			}

			return rejected;
		}

		return itemStack;
	}

	@Override
	public ItemStack insertRR(TileEntityLogisticalSorter outputter, ItemStack itemStack, EnumColor color, boolean doEmit, int min)
	{
		EnumFacing from = coord().sideDifference(Coord4D.get(outputter)).getOpposite();

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
				Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(coord(), getTileEntity().getSyncPacket(stack, false)), new Range4D(coord()));
				MekanismUtils.saveChunk(getTileEntity());
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
		Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(coord(), getTileEntity().getSyncPacket(stack, false)), new Range4D(coord()));
		MekanismUtils.saveChunk(getTileEntity());
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
	public boolean canEmitTo(TileEntity tileEntity, EnumFacing side)
	{
		if(!getTileEntity().canConnect(side))
		{
			return false;
		}

		return getTileEntity().getConnectionType(side) == ConnectionType.NORMAL || getTileEntity().getConnectionType(side) == ConnectionType.PUSH;
	}

	@Override
	public boolean canReceiveFrom(TileEntity tileEntity, EnumFacing side)
	{
		if(!getTileEntity().canConnect(side))
		{
			return false;
		}

		return getTileEntity().getConnectionType(side) == ConnectionType.NORMAL;
	}

	@Override
	public double getCost()
	{
		return getTileEntity().getCost();
	}

	@Override
	public boolean canConnectMutual(EnumFacing side)
	{
		return getTileEntity().canConnectMutual(side);
	}

	@Override
	public boolean canConnect(EnumFacing side)
	{
		return getTileEntity().canConnect(side);
	}

	@Override
	public TileEntityLogisticalTransporter getTileEntity()
	{
		return (TileEntityLogisticalTransporter)containingTile;
	}
}
