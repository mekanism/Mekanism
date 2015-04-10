package mekanism.common.multipart;

import java.util.Collection;

import mekanism.api.Coord4D;
import mekanism.api.transmitters.DynamicNetwork;
import mekanism.api.transmitters.IGridTransmitter;
import mekanism.api.transmitters.ITransmitterTile;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.transmitters.Transmitter;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class MultipartTransmitter<A, N extends DynamicNetwork<A,N>> extends Transmitter<A, N>
{
	public PartTransmitter<A, N> containingPart;

	public MultipartTransmitter(PartTransmitter<A, N> multiPart)
	{
		setPart(multiPart);
	}

	@Override
	public int getCapacity()
	{
		return getPart().getCapacity();
	}

	@Override
	public World world()
	{
		return getPart().world();
	}

	@Override
	public Coord4D coord()
	{
		return new Coord4D(getPart().x(), getPart().y(), getPart().z(), getPart().world().provider.dimensionId);
	}

	@Override
	public Coord4D getAdjacentConnectableTransmitterCoord(ForgeDirection side)
	{
		Coord4D sideCoord = coord().getFromSide(side);

		TileEntity potentialTransmitterTile = sideCoord.getTileEntity(world());

		if(!containingPart.canConnectMutual(side))
		{
			return null;
		}

		if(potentialTransmitterTile instanceof ITransmitterTile)
		{
			IGridTransmitter transmitter = ((ITransmitterTile)potentialTransmitterTile).getTransmitter();

			if(TransmissionType.checkTransmissionType(transmitter, getTransmissionType()))
			{
				return sideCoord;
			}
		}
		
		return null;
	}

	@Override
	public A getAcceptor(ForgeDirection side)
	{
		return getPart().getCachedAcceptor(side);
	}

	@Override
	public boolean isValid()
	{
		return !(getPart().tile() == null || getPart().tile().isInvalid()) && coord().exists(world());
	}

	@Override
	public N createEmptyNetwork()
	{
		return getPart().createNewNetwork();
	}

	@Override
	public N getExternalNetwork(Coord4D from)
	{
		TileEntity tile = from.getTileEntity(world());
		
		if(tile instanceof ITransmitterTile)
		{
			IGridTransmitter transmitter = ((ITransmitterTile)tile).getTransmitter();
			
			if(TransmissionType.checkTransmissionType(transmitter, getTransmissionType()));
			{
				return ((IGridTransmitter<A, N>)transmitter).getTransmitterNetwork();
			}
		}
		
		return null;
	}

	@Override
	public void takeShare()
	{
		containingPart.takeShare();
	}

	@Override
	public Object getBuffer()
	{
		return getPart().getBuffer();
	}

	@Override
	public N mergeNetworks(Collection<N> toMerge)
	{
		return getPart().createNetworkByMerging(toMerge);
	}

	@Override
	public TransmissionType getTransmissionType()
	{
		return getPart().getTransmissionType();
	}

	public PartTransmitter<A, N> getPart()
	{
		return containingPart;
	}

	public void setPart(PartTransmitter<A, N> containingPart)
	{
		this.containingPart = containingPart;
	}
}
