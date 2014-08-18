package mekanism.common;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.transmitters.DynamicNetwork;
import mekanism.api.transmitters.IGridTransmitter;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.transporter.ILogisticalTransporter;
import mekanism.common.transporter.TransporterManager;
import mekanism.common.util.TransporterUtils;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.common.FMLCommonHandler;

public class InventoryNetwork extends DynamicNetwork<IInventory, InventoryNetwork>
{
	public InventoryNetwork(IGridTransmitter<InventoryNetwork>... varPipes)
	{
		transmitters.addAll(Arrays.asList(varPipes));
		register();
	}

	public InventoryNetwork(Collection<IGridTransmitter<InventoryNetwork>> collection)
	{
		transmitters.addAll(collection);
		register();
	}

	public InventoryNetwork(Set<InventoryNetwork> networks)
	{
		for(InventoryNetwork net : networks)
		{
			if(net != null)
			{
				addAllTransmitters(net.transmitters);
				net.deregister();
			}
		}

		register();
	}
	
	public Map<Coord4D, ItemStack> calculateAcceptors(ItemStack stack, EnumColor color)
	{
		Map<Coord4D, ItemStack> toReturn = new HashMap<Coord4D, ItemStack>();
		
		for(Coord4D coord : ((Map<Coord4D, IInventory>)possibleAcceptors.clone()).keySet())
		{
			EnumSet<ForgeDirection> sides = acceptorDirections.get(coord);
			IInventory acceptor = (IInventory)coord.getTileEntity(getWorld());
			
			if(sides == null || sides.isEmpty())
			{
				continue;
			}
			
			for(ForgeDirection side : sides)
			{
				ItemStack returned = TransporterManager.getPredictedInsert((TileEntity)acceptor, color, stack, side.ordinal());
				
				if(TransporterManager.didEmit(stack, returned))
				{
					toReturn.put(coord, returned);
					break;
				}
			}
		}
		
		return toReturn;
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();

		if(FMLCommonHandler.instance().getEffectiveSide().isServer())
		{
			//Future!
		}
	}

	@Override
	public synchronized Set<IInventory> getAcceptors(Object... data)
	{
		Set<IInventory> toReturn = new HashSet<IInventory>();
		
		if(FMLCommonHandler.instance().getEffectiveSide().isClient())
		{
			return toReturn;
		}
		
		return toReturn;
	}

	@Override
	public synchronized void refresh()
	{
		Set<IGridTransmitter<InventoryNetwork>> iterPipes = (Set<IGridTransmitter<InventoryNetwork>>)transmitters.clone();
		Iterator it = iterPipes.iterator();
		boolean networkChanged = false;

		while(it.hasNext())
		{
			IGridTransmitter<InventoryNetwork> conductor = (IGridTransmitter<InventoryNetwork>)it.next();

			if(conductor == null || conductor.getTile().isInvalid())
			{
				it.remove();
				networkChanged = true;
				transmitters.remove(conductor);
			}
			else {
				conductor.setTransmitterNetwork(this);
			}
		}

		if(networkChanged) 
		{
			updateCapacity();
		}
	}
	
	@Override
	public synchronized void refresh(IGridTransmitter<InventoryNetwork> transmitter)
	{
		IInventory[] acceptors = TransporterUtils.getConnectedInventories((ILogisticalTransporter)transmitter.getTile());
		
		clearAround(transmitter);

		for(IInventory acceptor : acceptors)
		{
			ForgeDirection side = ForgeDirection.getOrientation(Arrays.asList(acceptors).indexOf(acceptor));

			if(side != null && acceptor != null && !(acceptor instanceof IGridTransmitter) && transmitter.canConnectToAcceptor(side, true))
			{
				possibleAcceptors.put(Coord4D.get((TileEntity)acceptor), acceptor);
				addSide(Coord4D.get((TileEntity)acceptor), ForgeDirection.getOrientation(Arrays.asList(acceptors).indexOf(acceptor)));
			}
		}
	}

	@Override
	public String toString()
	{
		return "[FluidNetwork] " + transmitters.size() + " transmitters, " + possibleAcceptors.size() + " acceptors.";
	}

	@Override
	protected InventoryNetwork create(Collection<IGridTransmitter<InventoryNetwork>> collection)
	{
		return new InventoryNetwork(collection);
	}

	@Override
	public TransmissionType getTransmissionType()
	{
		return TransmissionType.ITEM;
	}

	@Override
	public String getNeededInfo()
	{
		return null;
	}

	@Override
	public String getStoredInfo()
	{
		return null;
	}

	@Override
	public String getFlowInfo()
	{
		return null;
	}
}
