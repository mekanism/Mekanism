package mekanism.induction.common.wire;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.core.block.IConductor;
import universalelectricity.core.block.IConnector;
import universalelectricity.core.block.INetworkProvider;
import universalelectricity.core.electricity.NetworkLoader;
import universalelectricity.core.grid.IElectricityNetwork;
import universalelectricity.core.vector.Vector3;
import universalelectricity.core.vector.VectorHelper;
import codechicken.multipart.TileMultipart;

public abstract class PartConductor extends PartAdvanced implements IConductor
{
	private IElectricityNetwork network;

	public TileEntity[] adjacentConnections = null;
	public byte currentWireConnections = 0x00;
	public byte currentAcceptorConnections = 0x00;

	public byte getAllCurrentConnections()
	{
		return (byte) (currentWireConnections | currentAcceptorConnections);
	}

	public static boolean connectionMapContainsSide(byte connections, ForgeDirection side)
	{
		byte tester = (byte) (1 << side.ordinal());
		return ((connections & tester) > 0);
	}

	@Override
	public void bind(TileMultipart t)
	{
		if (tile() != null && network != null)
		{
			this.getNetwork().getConductors().remove(tile());
			super.bind(t);
			this.getNetwork().getConductors().add((IConductor) tile());
		}
		else
		{
			super.bind(t);
		}
	}

	@Override
	public void preRemove()
	{
		if (!this.world().isRemote && this.tile() instanceof IConductor)
		{
			this.getNetwork().split((IConductor) this.tile());
		}

		super.preRemove();
	}

	@Override
	public boolean doesTick()
	{
		return false;
	}

	@Override
	public IElectricityNetwork getNetwork()
	{
		if (this.network == null && this.tile() instanceof IConductor)
		{
			this.setNetwork(NetworkLoader.getNewNetwork((IConductor) this.tile()));
		}

		return this.network;
	}

	public boolean canConnectBothSides(TileEntity tile, ForgeDirection side)
	{
		boolean notPrevented = !connectionPrevented(tile, side);

		if (tile instanceof IConnector)
		{
			notPrevented &= ((IConnector) tile).canConnect(side.getOpposite());
		}
		return notPrevented;
	}

	@Override
	public void setNetwork(IElectricityNetwork network)
	{
		this.network = network;
	}

	/**
	 * Override if there are ways of preventing a connection
	 * 
	 * @param tile The TileEntity on the given side
	 * @param side The side we're checking
	 * @return Whether we're preventing connections on given side or to given tileEntity
	 */
	public boolean connectionPrevented(TileEntity tile, ForgeDirection side)
	{
		return false;
	}

	public byte getPossibleWireConnections()
	{
		byte connections = 0x00;

		for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
		{
			TileEntity tileEntity = VectorHelper.getTileEntityFromSide(this.world(), new Vector3(tile()), side);
			if (tileEntity instanceof INetworkProvider && this.canConnectBothSides(tileEntity, side))
				connections |= 1 << side.ordinal();
		}
		return connections;
	}

	public byte getPossibleAcceptorConnections()
	{
		byte connections = 0x00;

		for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
		{
			TileEntity tileEntity = VectorHelper.getTileEntityFromSide(this.world(), new Vector3(tile()), side);
			if (this.isValidAcceptor(tileEntity) && this.canConnectBothSides(tileEntity, side))
				connections |= 1 << side.ordinal();
		}
		return connections;
	}

	/**
	 * Override if there are different kinds of acceptor possible
	 */
	public boolean isValidAcceptor(TileEntity tile)
	{
		return tile instanceof IConnector;
	}

	@Override
	public void refresh()
	{
		if (!this.world().isRemote)
		{
			this.adjacentConnections = null;
			byte possibleWireConnections = getPossibleWireConnections();
			byte possibleAcceptorConnections = getPossibleAcceptorConnections();

			if (possibleWireConnections != currentWireConnections)
			{
				byte or = (byte) (possibleWireConnections | currentWireConnections);
				if (or != possibleWireConnections) // Connections have been removed
				{
					this.getNetwork().split((IConductor) tile());
					this.setNetwork(null);
				}

				for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
				{
					if (connectionMapContainsSide(possibleWireConnections, side))
					{
						TileEntity tileEntity = VectorHelper.getConnectorFromSide(this.world(), new Vector3(tile()), side);

						if (tileEntity instanceof INetworkProvider)
						{
							this.getNetwork().merge(((INetworkProvider) tileEntity).getNetwork());
						}
					}
				}

				this.currentWireConnections = possibleWireConnections;
			}

			this.currentAcceptorConnections = possibleAcceptorConnections;
			
			this.getNetwork().refresh();
			this.sendDescUpdate();
		}
		
		this.tile().markRender();
	}

	/**
	 * Should include connections that are in the current connection maps even if those connections
	 * aren't allowed any more. This is so that networks split correctly.
	 */
	@Override
	public TileEntity[] getAdjacentConnections()
	{
		if (this.adjacentConnections == null)
		{
			this.adjacentConnections = new TileEntity[6];

			for (byte i = 0; i < 6; i++)
			{
				ForgeDirection side = ForgeDirection.getOrientation(i);
				TileEntity tileEntity = VectorHelper.getTileEntityFromSide(this.world(), new Vector3(tile()), side);

				if (isCurrentlyConnected(side))
				{
					adjacentConnections[i] = tileEntity;
				}
			}
		}
		return this.adjacentConnections;
	}

	public boolean isCurrentlyConnected(ForgeDirection side)
	{
		return connectionMapContainsSide(this.getAllCurrentConnections(), side);
	}

	/**
	 * Shouldn't need to be overridden. Override connectionPrevented instead
	 */
	@Override
	public boolean canConnect(ForgeDirection direction)
	{
		Vector3 connectPos = new Vector3(tile()).modifyPositionFromSide(direction);
		TileEntity connectTile = connectPos.getTileEntity(this.world());
		return !connectionPrevented(connectTile, direction);
	}

	@Override
	public void onAdded()
	{
		super.onAdded();
		this.refresh();
	}

	@Override
	public void onChunkLoad()
	{
		super.onChunkLoad();
		this.refresh();
	}

	@Override
	public void onNeighborChanged()
	{
		super.onNeighborChanged();
		this.refresh();
	}
}
