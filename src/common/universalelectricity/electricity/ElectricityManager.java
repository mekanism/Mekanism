package universalelectricity.electricity;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.minecraft.src.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.core.Vector3;
import universalelectricity.implement.IConductor;
import universalelectricity.implement.IElectricityReceiver;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.TickType;

/**
 * This class is used to manage electricity
 * transferring and flow. It is also used to call
 * updates on UE tile entities.
 * 
 * @author Calclavia
 * 
 */
public class ElectricityManager
{
	/**
	 * ElectricityManager exists on both client
	 * and server side. Rely on the server side
	 * one as it is more accurate! Client side
	 * only simulates.
	 */
	public static ElectricityManager instance;

	private List<ElectricityTransferData> electricityTransferQueue = new ArrayList<ElectricityTransferData>();
	private List<ElectricityNetwork> electricityNetworks = new ArrayList<ElectricityNetwork>();

	public ElectricityManager()
	{
		System.out.println("Universal Electricity's Electricity Manager Initiated.");
	}

	/**
	 * Registers a the conductor into the UE
	 * electricity net.
	 * 
	 * @param conductor
	 *            - The IConductor tile entity.
	 */
	public void registerConductor(IConductor newConductor)
	{
		cleanUpConnections();
		this.electricityNetworks.add(new ElectricityNetwork(newConductor));
	}

	/**
	 * Merges two connection lines together into
	 * one.
	 * 
	 * @param networkA
	 *            - The network to be merged into.
	 *            This network will be kept.
	 * @param networkB
	 *            - The network to be merged. This
	 *            network will be deleted.
	 */
	public void mergeConnection(ElectricityNetwork networkA, ElectricityNetwork networkB)
	{
		if (networkA != networkB)
		{
			if (networkA != null && networkB != null)
			{
				networkA.conductors.addAll(networkB.conductors);
				networkA.setNetwork();
				this.electricityNetworks.remove(networkB);
				networkB = null;
			}
			else
			{
				System.err.println("Failed to merge Universal Electricity wire connections!");
			}
		}
	}

	/**
	 * Separate one connection line into two
	 * different ones between two conductors. This
	 * function does this by resetting all wires
	 * in the connection line and making them each
	 * reconnect.
	 * 
	 * @param conductorA
	 *            - existing conductor
	 * @param conductorB
	 *            - broken/invalid conductor
	 */
	public void splitConnection(IConductor conductorA, IConductor conductorB)
	{
		ElectricityNetwork connection = conductorA.getNetwork();

		if (connection != null)
		{
			connection.cleanUpArray();

			for (IConductor conductor : connection.conductors)
			{
				conductor.reset();
			}

			for (IConductor conductor : connection.conductors)
			{
				for (byte i = 0; i < 6; i++)
				{
					conductor.updateConnectionWithoutSplit(Vector3.getConnectorFromSide(conductor.getWorld(), new Vector3(((TileEntity) conductor).xCoord, ((TileEntity) conductor).yCoord, ((TileEntity) conductor).zCoord), ForgeDirection.getOrientation(i)), ForgeDirection.getOrientation(i));
				}
			}
		}
		else
		{
			FMLLog.severe("Conductor invalid network while splitting connection!");
		}
	}

	/**
	 * Clean up and remove all useless and invalid
	 * connections.
	 */
	public void cleanUpConnections()
	{
		try
		{
			for (int i = 0; i < this.electricityNetworks.size(); i++)
			{
				this.electricityNetworks.get(i).cleanUpArray();

				if (this.electricityNetworks.get(i).conductors.size() == 0)
				{
					this.electricityNetworks.remove(i);
				}
			}
		}
		catch (Exception e)
		{
			FMLLog.severe("Failed to clean up wire connections!");
		}
	}

	/**
	 * Produces electricity into a specific wire
	 * which will be distributed across the
	 * electricity network.
	 * 
	 * @param sender
	 *            The machine sending the
	 *            electricity.
	 * @param targetConductor
	 *            The conductor receiving the
	 *            electricity (or connected to the
	 *            machine).
	 * @param amps
	 *            The amount of amps this machine
	 *            is sending.
	 * @param voltage
	 *            The amount of volts this machine
	 *            is sending.
	 */
	public void produceElectricity(TileEntity sender, IConductor targetConductor, double amps, double voltage)
	{
		if (targetConductor != null && amps > 0 && voltage > 0)
		{
			// Find a path between this conductor
			// and all connected units and
			// try to send the electricity to them
			// directly
			ElectricityNetwork electricityNetwork = targetConductor.getNetwork();

			if (electricityNetwork != null)
			{
				List<IElectricityReceiver> allElectricUnitsInLine = electricityNetwork.getConnectedReceivers();
				double leftOverAmps = amps;

				for (IConductor conductor : electricityNetwork.conductors)
				{
					for (byte i = 0; i < conductor.getConnectedBlocks().length; i++)
					{
						TileEntity tileEntity = conductor.getConnectedBlocks()[i];

						if (tileEntity != null)
						{
							if (tileEntity instanceof IElectricityReceiver)
							{
								IElectricityReceiver receiver = (IElectricityReceiver) tileEntity;

								if (this.getActualWattRequest(receiver) > 0 && receiver.canReceiveFromSide(ForgeDirection.getOrientation(i).getOpposite()))
								{
									double transferAmps = Math.max(0, Math.min(leftOverAmps, Math.min(amps / allElectricUnitsInLine.size(), ElectricInfo.getAmps(this.getActualWattRequest(receiver), receiver.getVoltage()))));
									leftOverAmps -= transferAmps;

									// Calculate
									// electricity
									// loss
									double distance = Vector3.distance(Vector3.get(sender), Vector3.get((TileEntity) receiver));
									double ampsReceived = transferAmps - (transferAmps * transferAmps * targetConductor.getResistance() * distance) / voltage;
									double voltsReceived = voltage - (transferAmps * targetConductor.getResistance() * distance);

									this.electricityTransferQueue.add(new ElectricityTransferData(sender, receiver, electricityNetwork, ForgeDirection.getOrientation(i).getOpposite(), ampsReceived, voltsReceived));
								}
							}
						}
					}
				}
			}
			else
			{
				FMLLog.severe("Conductor not registered to a network!");
			}
		}
	}

	/**
	 * Gets the actual watt request of an electric
	 * receiver accounting all current electricity
	 * packets qued up for it.
	 * 
	 * @return - The amount of watts requested.
	 */
	public double getActualWattRequest(IElectricityReceiver receiver)
	{
		double wattsRequest = receiver.wattRequest();

		try
		{
			for (int i = 0; i < electricityTransferQueue.size(); i++)
			{
				if (electricityTransferQueue.get(i) != null)
				{
					if (electricityTransferQueue.get(i).isValid())
					{
						if (electricityTransferQueue.get(i).receiver == receiver)
						{
							wattsRequest -= electricityTransferQueue.get(i).amps * electricityTransferQueue.get(i).voltage;
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			FMLLog.severe("Failed to get watt request!");
		}

		return Math.max(Math.min(wattsRequest, receiver.wattRequest()), 0);
	}

	/**
	 * Checks if the current connection line needs
	 * electricity
	 * 
	 * @return - The amount of joules this
	 *         connection line needs
	 */
	public double getElectricityRequired(ElectricityNetwork network)
	{
		double need = 0;

		if (network != null)
		{
			for (IConductor conductor : network.conductors)
			{
				for (byte i = 0; i < conductor.getConnectedBlocks().length; i++)
				{
					TileEntity tileEntity = conductor.getConnectedBlocks()[i];

					if (tileEntity != null)
					{
						if (tileEntity instanceof IElectricityReceiver)
						{
							IElectricityReceiver electricUnit = (IElectricityReceiver) tileEntity;

							if (electricUnit.canReceiveFromSide(ForgeDirection.getOrientation(i).getOpposite()))
							{
								need += electricUnit.wattRequest();
							}
						}
					}
				}
			}
		}

		return need;
	}

	public double getActualElectricityRequired(ElectricityNetwork network)
	{
		double need = 0;

		if (network != null)
		{
			for (IConductor conductor : network.conductors)
			{
				for (byte i = 0; i < conductor.getConnectedBlocks().length; i++)
				{
					TileEntity tileEntity = conductor.getConnectedBlocks()[i];

					if (tileEntity != null)
					{
						if (tileEntity instanceof IElectricityReceiver)
						{
							IElectricityReceiver electricUnit = (IElectricityReceiver) tileEntity;

							if (electricUnit.canReceiveFromSide(ForgeDirection.getOrientation(i).getOpposite()))
							{
								need += this.getActualWattRequest(electricUnit);
							}
						}
					}
				}
			}
		}

		return need;
	}

	/**
	 * This function is called to refresh all
	 * conductors in the world.
	 */
	public void refreshConductors()
	{
		try
		{
			Iterator it = electricityNetworks.iterator();

			while (it.hasNext())
			{
				((ElectricityNetwork) it.next()).refreshConductors();
			}
		}
		catch (Exception e)
		{
			FMLLog.fine("Failed to refresh conductors.");
		}
	}

	public void onTick(EnumSet<TickType> type, Object... tickData)
	{
		if (type.contains(TickType.WORLD) && !type.contains(TickType.WORLDLOAD))
		{
			if (ElectricityManagerTicker.inGameTicks % 40 == 0)
			{
				this.refreshConductors();
			}

			try
			{
				HashMap conductorAmpData = new HashMap<ElectricityNetwork, Double>();

				for (int i = 0; i < electricityTransferQueue.size(); i++)
				{
					if (electricityTransferQueue.get(i) != null)
					{
						if (electricityTransferQueue.get(i).isValid())
						{
							double amps = electricityTransferQueue.get(i).amps;

							if (conductorAmpData.containsKey(electricityTransferQueue.get(i).network))
							{
								amps += (Double) conductorAmpData.get(electricityTransferQueue.get(i).network);
							}

							conductorAmpData.put(electricityTransferQueue.get(i).network, amps);
							electricityTransferQueue.get(i).receiver.onReceive(electricityTransferQueue.get(i).sender, electricityTransferQueue.get(i).amps, electricityTransferQueue.get(i).voltage, electricityTransferQueue.get(i).side);
						}
					}

					electricityTransferQueue.remove(i);
				}

				Iterator it = conductorAmpData.entrySet().iterator();

				while (it.hasNext())
				{
					Map.Entry pairs = (Map.Entry) it.next();

					if (pairs.getKey() != null && pairs.getValue() != null)
					{
						if (pairs.getKey() instanceof ElectricityNetwork && pairs.getValue() instanceof Double)
						{
							if (((Double) pairs.getValue()) > ((ElectricityNetwork) pairs.getKey()).getLowestAmpConductor())
							{
								((ElectricityNetwork) pairs.getKey()).onOverCharge();
							}
						}
					}

					it.remove();
				}
			}
			catch (Exception e)
			{
				System.err.println("Failed to transfer electricity to receivers.");
				e.printStackTrace();
			}
		}

		if (ElectricityManagerTicker.inGameTicks == 0)
		{
			this.refreshConductors();
		}
	}
}