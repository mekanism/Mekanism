package mekanism.common.multipart;

import java.util.HashSet;
import java.util.Set;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.Range4D;
import mekanism.common.HashList;
import mekanism.common.Mekanism;
import mekanism.common.base.ILogisticalTransporter;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.content.transporter.TransitRequest;
import mekanism.common.content.transporter.TransitRequest.TransitResponse;
import mekanism.common.content.transporter.TransporterManager;
import mekanism.common.content.transporter.TransporterStack;
import mekanism.common.content.transporter.TransporterStack.Path;
import mekanism.common.multipart.PartSidedPipe.ConnectionType;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.transmitters.grid.InventoryNetwork;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.TransporterUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

public class MultipartTransporter extends MultipartTransmitter<TileEntity, InventoryNetwork> implements ILogisticalTransporter
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
			if(getTransmitterNetwork() == null)
			{
				return;
			}
			
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
								TileEntity tile = next.getTileEntity(world());
								
								if(next != null && tile != null)
								{
									needsSync.add(stack);
									TransitResponse response = InventoryUtils.putStackInInventory(tile, TransitRequest.getFromTransport(stack), stack.getSide(this), stack.pathType == Path.HOME);

									if(response.getRejected(stack.itemStack) == null)
									{
										TransporterManager.remove(stack);
										remove.add(stack);
										continue;
									}
									else {
										needsSync.add(stack);
										stack.itemStack = response.getRejected(stack.itemStack);

										prevSet = next;
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
				Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(coord(), getPart().getSyncPacket(stack, true)), new Range4D(coord()));
				transit.remove(stack);
				MekanismUtils.saveChunk(getPart());
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
		EnumFacing side = stack.getSide(this);

		return getPart().getConnectionType(side) == ConnectionType.NORMAL || getPart().getConnectionType(side) == ConnectionType.PUSH;
	}

	private boolean recalculate(TransporterStack stack, Coord4D from)
	{
		needsSync.add(stack);

		if(stack.pathType != Path.NONE)
		{
			TransitResponse ret = stack.recalculatePath(TransitRequest.getFromTransport(stack), this, 0);
			
			if(ret.isEmpty())
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
	public TransitResponse insert(Coord4D original, TransitRequest request, EnumColor color, boolean doEmit, int min)
	{
		return insert_do(original, request, color, doEmit, min, false);
	}

	private TransitResponse insert_do(Coord4D original, TransitRequest request, EnumColor color, boolean doEmit, int min, boolean force)
	{
		EnumFacing from = coord().sideDifference(original).getOpposite();

		TransporterStack stack = new TransporterStack();
		stack.originalLocation = original;
		stack.homeLocation = original;
		stack.color = color;

		if((force && !canReceiveFrom(original.getTileEntity(world()), from)) || !stack.canInsertToTransporter(this, from))
		{
			return TransitResponse.EMPTY;
		}

		TransitResponse response = stack.recalculatePath(request, this, min);

		if(!response.isEmpty())
		{
			stack.itemStack = response.stack;

			if(doEmit)
			{
				transit.add(stack);
				Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(coord(), getPart().getSyncPacket(stack, false)), new Range4D(coord()));
				MekanismUtils.saveChunk(getPart());
			}

			return response;
		}

		return TransitResponse.EMPTY;
	}

	@Override
	public TransitResponse insertRR(TileEntityLogisticalSorter outputter, TransitRequest request, EnumColor color, boolean doEmit, int min)
	{
		EnumFacing from = coord().sideDifference(Coord4D.get(outputter)).getOpposite();

		TransporterStack stack = new TransporterStack();
		stack.originalLocation = Coord4D.get(outputter);
		stack.homeLocation = Coord4D.get(outputter);
		stack.color = color;

		if(!canReceiveFrom(outputter, from) || !stack.canInsertToTransporter(this, from))
		{
			return TransitResponse.EMPTY;
		}

		TransitResponse response = stack.recalculateRRPath(request, outputter, this, min);

		if(!response.isEmpty())
		{
			stack.itemStack = response.stack;

			if(doEmit)
			{
				transit.add(stack);
				Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(coord(), getPart().getSyncPacket(stack, false)), new Range4D(coord()));
				MekanismUtils.saveChunk(getPart());
			}
			
			return response;
		}

		return TransitResponse.EMPTY;
	}

	@Override
	public void entityEntering(TransporterStack stack, int progress)
	{
		stack.progress = progress;
		transit.add(stack);
		Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(coord(), getPart().getSyncPacket(stack, false)), new Range4D(coord()));
		MekanismUtils.saveChunk(getPart());
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
		if(!getPart().canConnect(side))
		{
			return false;
		}

		return getPart().getConnectionType(side) == ConnectionType.NORMAL || getPart().getConnectionType(side) == ConnectionType.PUSH;
	}

	@Override
	public boolean canReceiveFrom(TileEntity tileEntity, EnumFacing side)
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
	public boolean canConnectMutual(EnumFacing side)
	{
		return getPart().canConnectMutual(side);
	}

	@Override
	public boolean canConnect(EnumFacing side)
	{
		return getPart().canConnect(side);
	}

	@Override
	public PartLogisticalTransporter getPart()
	{
		return (PartLogisticalTransporter)containingPart;
	}
}
