package universalelectricity.core.electricity;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.core.block.IConnector;
import universalelectricity.core.block.INetworkProvider;
import universalelectricity.core.vector.Vector3;
import universalelectricity.core.vector.VectorHelper;

/**
 * A helper class that provides additional useful functions to interact with the ElectricityNetwork
 * 
 * @author Calclavia
 * 
 */
public class ElectricityNetworkHelper
{

	/**
	 * Invalidates a TileEntity from the electrical network, thereby removing it from all
	 * electricity network that are adjacent to it.
	 */
	public static void invalidate(TileEntity tileEntity)
	{
		for (int i = 0; i < 6; i++)
		{
			ForgeDirection direction = ForgeDirection.getOrientation(i);
			TileEntity checkTile = VectorHelper.getConnectorFromSide(tileEntity.worldObj, new Vector3(tileEntity), direction);

			if (checkTile instanceof INetworkProvider)
			{
				IElectricityNetwork network = ((INetworkProvider) checkTile).getNetwork();

				if (network != null)
				{
					network.stopRequesting(tileEntity);
					network.stopProducing(tileEntity);
				}
			}
		}
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

	public static ElectricityPack produceFromMultipleSides(TileEntity tileEntity, ElectricityPack electricityPack)
	{
		return ElectricityNetworkHelper.produceFromMultipleSides(tileEntity, getDirections(tileEntity), electricityPack);
	}

	/**
	 * Produces electricity from all specified sides. Use this as a simple helper function.
	 * 
	 * @param tileEntity- The TileEntity consuming the electricity.
	 * @param approachDirection - The sides in which you can connect to.
	 * @param producePack - The amount of electricity to be produced.
	 * @return What remained in the electricity pack.
	 */
	public static ElectricityPack produceFromMultipleSides(TileEntity tileEntity, EnumSet<ForgeDirection> approachingDirection, ElectricityPack producingPack)
	{
		ElectricityPack remainingElectricity = producingPack.clone();

		if (tileEntity != null && approachingDirection != null)
		{
			final List<IElectricityNetwork> connectedNetworks = ElectricityNetworkHelper.getNetworksFromMultipleSides(tileEntity, approachingDirection);

			if (connectedNetworks.size() > 0)
			{
				/**
				 * Requests an even amount of electricity from all sides.
				 */
				double wattsPerSide = (producingPack.getWatts() / connectedNetworks.size());
				double voltage = producingPack.voltage;

				for (IElectricityNetwork network : connectedNetworks)
				{
					if (wattsPerSide > 0 && producingPack.getWatts() > 0)
					{
						double amperes = wattsPerSide / voltage;
						network.startProducing(tileEntity, amperes, voltage);
						remainingElectricity.amperes -= amperes;
					}
					else
					{
						network.stopProducing(tileEntity);
					}
				}
			}
		}

		return remainingElectricity;
	}

	public static ElectricityPack consumeFromMultipleSides(TileEntity tileEntity, ElectricityPack electricityPack)
	{
		return ElectricityNetworkHelper.consumeFromMultipleSides(tileEntity, getDirections(tileEntity), electricityPack);
	}

	/**
	 * Requests and attempts to consume electricity from all specified sides. Use this as a simple
	 * helper function.
	 * 
	 * @param tileEntity- The TileEntity consuming the electricity.
	 * @param approachDirection - The sides in which you can connect.
	 * @param requestPack - The amount of electricity to be requested.
	 * @return The consumed ElectricityPack.
	 */
	public static ElectricityPack consumeFromMultipleSides(TileEntity tileEntity, EnumSet<ForgeDirection> approachingDirection, ElectricityPack requestPack)
	{
		ElectricityPack consumedPack = new ElectricityPack();

		if (tileEntity != null && approachingDirection != null)
		{
			final List<IElectricityNetwork> connectedNetworks = ElectricityNetworkHelper.getNetworksFromMultipleSides(tileEntity, approachingDirection);

			if (connectedNetworks.size() > 0)
			{
				/**
				 * Requests an even amount of electricity from all sides.
				 */
				double wattsPerSide = (requestPack.getWatts() / connectedNetworks.size());
				double voltage = requestPack.voltage;

				for (IElectricityNetwork network : connectedNetworks)
				{
					if (wattsPerSide > 0 && requestPack.getWatts() > 0)
					{
						network.startRequesting(tileEntity, wattsPerSide / voltage, voltage);
						ElectricityPack receivedPack = network.consumeElectricity(tileEntity);
						consumedPack.amperes += receivedPack.amperes;
						consumedPack.voltage = Math.max(consumedPack.voltage, receivedPack.voltage);
					}
					else
					{
						network.stopRequesting(tileEntity);
					}
				}
			}
		}

		return consumedPack;
	}

	/**
	 * @param tileEntity - The TileEntity's sides.
	 * @param approachingDirection - The directions that can be connected.
	 * @return A list of networks from all specified sides. There will be no repeated
	 * ElectricityNetworks and it will never return null.
	 */
	public static List<IElectricityNetwork> getNetworksFromMultipleSides(TileEntity tileEntity, EnumSet<ForgeDirection> approachingDirection)
	{
		final List<IElectricityNetwork> connectedNetworks = new ArrayList<IElectricityNetwork>();

		for (int i = 0; i < 6; i++)
		{
			ForgeDirection direction = ForgeDirection.getOrientation(i);

			if (approachingDirection.contains(direction))
			{
				Vector3 position = new Vector3(tileEntity);
				position.modifyPositionFromSide(direction);
				TileEntity outputConductor = position.getTileEntity(tileEntity.worldObj);
				IElectricityNetwork electricityNetwork = ElectricityNetworkHelper.getNetworkFromTileEntity(outputConductor, direction);

				if (electricityNetwork != null && !connectedNetworks.contains(connectedNetworks))
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
