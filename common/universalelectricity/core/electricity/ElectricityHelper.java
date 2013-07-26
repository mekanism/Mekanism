package universalelectricity.core.electricity;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.event.ForgeSubscribe;
import universalelectricity.core.block.IConnector;
import universalelectricity.core.block.INetworkProvider;
import universalelectricity.core.electricity.ElectricalEvent.ElectricityProduceEvent;
import universalelectricity.core.grid.IElectricityNetwork;
import universalelectricity.core.vector.Vector3;
import universalelectricity.core.vector.VectorHelper;

/**
 * A helper class that provides additional useful functions to interact with the ElectricityNetwork
 * 
 * @author Calclavia
 * 
 */
public class ElectricityHelper
{
	@ForgeSubscribe
	public void onProduce(ElectricityProduceEvent evt)
	{
		// Needs work with shuffling to be able to evenly distribute to all
		// connected networks
		// instead of just defaulting to one of them.
		Vector3 position = new Vector3((TileEntity) evt.tileEntity);
		HashMap<IElectricityNetwork, ForgeDirection> networks = new HashMap<IElectricityNetwork, ForgeDirection>();

		for (ForgeDirection direction : getDirections((TileEntity) evt.tileEntity))
		{
			IElectricityNetwork network = getNetworkFromTileEntity(VectorHelper.getTileEntityFromSide(evt.world, position, direction), direction.getOpposite());

			if (network != null)
			{
				networks.put(network, direction);
				ElectricityPack provided = evt.tileEntity.provideElectricity(direction, network.getRequest((TileEntity) evt.tileEntity), true);

				if (provided != null && provided.getWatts() > 0)
				{
					network.produce(provided, (TileEntity) evt.tileEntity);
				}
			}
		}

		/*
		 * ElectricityPack request = this.getNetwork().getRequest(this);
		 * 
		 * if (request.getWatts() > 0) { List<ElectricityPack> providedPacks = new
		 * ArrayList<ElectricityPack>(); List<TileEntity> tileEntities = new
		 * ArrayList<TileEntity>();
		 * 
		 * for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) { Vector3 position = new
		 * Vector3(this).modifyPositionFromSide(direction); TileEntity tileEntity =
		 * position.getTileEntity(this.worldObj);
		 * 
		 * if (tileEntity instanceof IElectrical) { tileEntities.add(tileEntity); IElectrical
		 * electrical = (IElectrical) tileEntity;
		 * 
		 * if (electrical.canConnect(direction.getOpposite())) { if
		 * (electrical.getProvide(direction.getOpposite()) > 0) { providedPacks.add
		 * (electrical.provideElectricity(direction.getOpposite(), request, true)); } } } }
		 * 
		 * ElectricityPack mergedPack = ElectricityPack.merge(providedPacks);
		 * 
		 * if (mergedPack.getWatts() > 0) { this.getNetwork().produce(mergedPack,
		 * tileEntities.toArray(new TileEntity[0])); } }
		 */
	}

	public static EnumSet<ForgeDirection> getDirections(TileEntity tileEntity)
	{
		EnumSet<ForgeDirection> possibleSides = EnumSet.noneOf(ForgeDirection.class);

		if (tileEntity instanceof IConnector)
		{
			for (int i = 0; i < 6; i++)
			{
				ForgeDirection direction = ForgeDirection.getOrientation(i);
				if (((IConnector) tileEntity).canConnect(direction))
				{
					possibleSides.add(direction);
				}
			}
		}

		return possibleSides;
	}

	@Deprecated
	public static ElectricityPack produceFromMultipleSides(TileEntity tileEntity, ElectricityPack electricityPack)
	{
		return ElectricityHelper.produceFromMultipleSides(tileEntity, getDirections(tileEntity), electricityPack);
	}

	/**
	 * Produces electricity from all specified sides. Use this as a simple helper function.
	 * 
	 * @param tileEntity - The TileEntity consuming the electricity.
	 * @param approachDirection - The sides in which you can connect to.
	 * @param producePack - The amount of electricity to be produced.
	 * @return What remained in the electricity pack.
	 */
	@Deprecated
	public static ElectricityPack produceFromMultipleSides(TileEntity tileEntity, EnumSet<ForgeDirection> approachingDirection, ElectricityPack producingPack)
	{
		ElectricityPack remainingElectricity = producingPack.clone();

		if (tileEntity != null && approachingDirection != null)
		{
			final Set<IElectricityNetwork> connectedNetworks = ElectricityHelper.getNetworksFromMultipleSides(tileEntity, approachingDirection);

			if (connectedNetworks.size() > 0)
			{
				/**
				 * Requests an even amount of electricity from all sides.
				 */
				float wattsPerSide = (producingPack.getWatts() / connectedNetworks.size());
				float voltage = producingPack.voltage;

				for (IElectricityNetwork network : connectedNetworks)
				{
					if (wattsPerSide > 0 && producingPack.getWatts() > 0)
					{
						float amperes = Math.min(wattsPerSide / voltage, network.getRequest(tileEntity).getWatts() / voltage);

						if (amperes > 0)
						{
							network.produce(new ElectricityPack(amperes, voltage));
							remainingElectricity.amperes -= amperes;
						}
					}
				}
			}
		}

		return remainingElectricity;
	}

	/**
	 * @param tileEntity - The TileEntity's sides.
	 * @param approachingDirection - The directions that can be connected.
	 * @return A list of networks from all specified sides. There will be no repeated
	 * ElectricityNetworks and it will never return null.
	 */
	public static Set<IElectricityNetwork> getNetworksFromMultipleSides(TileEntity tileEntity, EnumSet<ForgeDirection> approachingDirection)
	{
		final Set<IElectricityNetwork> connectedNetworks = new HashSet<IElectricityNetwork>();

		for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
		{
			if (approachingDirection.contains(side))
			{
				Vector3 position = new Vector3(tileEntity);
				position.modifyPositionFromSide(side);

				TileEntity outputConductor = position.getTileEntity(tileEntity.worldObj);
				IElectricityNetwork electricityNetwork = ElectricityHelper.getNetworkFromTileEntity(outputConductor, side);

				if (electricityNetwork != null)
				{
					connectedNetworks.add(electricityNetwork);
				}
			}
		}

		return connectedNetworks;
	}

	/**
	 * Tries to find the electricity network based in a tile entity and checks to see if it is a
	 * conductor. All machines should use this function to search for a connecting conductor around
	 * it.
	 * 
	 * @param conductor - The TileEntity conductor
	 * @param approachDirection - The direction you are approaching this wire from.
	 * @return The ElectricityNetwork or null if not found.
	 */
	public static IElectricityNetwork getNetworkFromTileEntity(TileEntity tileEntity, ForgeDirection approachDirection)
	{
		if (tileEntity != null)
		{
			if (tileEntity instanceof INetworkProvider)
			{
				if (tileEntity instanceof IConnector)
				{
					if (((IConnector) tileEntity).canConnect(approachDirection.getOpposite()))
					{
						return ((INetworkProvider) tileEntity).getNetwork();
					}
				}
				else
				{
					return ((INetworkProvider) tileEntity).getNetwork();
				}
			}
		}

		return null;
	}
}
