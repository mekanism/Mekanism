package universalelectricity.compatibility;

import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergySink;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import universalelectricity.core.block.IConductor;
import universalelectricity.core.block.IElectrical;
import universalelectricity.core.block.INetworkConnection;
import universalelectricity.core.block.INetworkProvider;
import universalelectricity.core.electricity.ElectricalEvent.ElectricityProductionEvent;
import universalelectricity.core.electricity.ElectricalEvent.ElectricityRequestEvent;
import universalelectricity.core.electricity.ElectricityPack;
import universalelectricity.core.grid.ElectricityNetwork;
import universalelectricity.core.grid.IElectricityNetwork;
import universalelectricity.core.path.Pathfinder;
import universalelectricity.core.path.PathfinderChecker;
import universalelectricity.core.vector.Vector3;
import universalelectricity.core.vector.VectorHelper;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import buildcraft.api.power.PowerHandler.Type;
import cofh.api.energy.IEnergyHandler;
import cpw.mods.fml.common.FMLLog;

/**
 * A universal network that words with multiple energy systems.
 * 
 * @author micdoodle8, Calclavia, Aidancbrady
 * 
 */
public class UniversalNetwork extends ElectricityNetwork
{
	@Override
	public float produce(ElectricityPack electricity, TileEntity... ignoreTiles)
	{
		ElectricityProductionEvent evt = new ElectricityProductionEvent(this, electricity, ignoreTiles);
		MinecraftForge.EVENT_BUS.post(evt);

		float totalEnergy = electricity.getWatts();
		float networkResistance = getTotalResistance();
		float proportionWasted = getTotalResistance() / (getTotalResistance() + acceptorResistance);
		float energyWasted = totalEnergy * proportionWasted;
		float totalUsableEnergy = totalEnergy - energyWasted;
		float remainingUsableEnergy = totalUsableEnergy;
		float voltage = electricity.voltage;

		if (!evt.isCanceled())
		{
			Set<TileEntity> avaliableEnergyTiles = this.getAcceptors();

			if (!avaliableEnergyTiles.isEmpty())
			{
				final float totalEnergyRequest = this.getRequest(ignoreTiles).getWatts();

				if (totalEnergyRequest > 0)
				{
					boolean markRefresh = false;

					for (TileEntity tileEntity : avaliableEnergyTiles)
					{
						if (tileEntity != null && !tileEntity.isInvalid())
						{
							if (remainingUsableEnergy > 0 && !Arrays.asList(ignoreTiles).contains(tileEntity))
							{
								if (tileEntity instanceof IElectrical)
								{
									IElectrical electricalTile = (IElectrical) tileEntity;

									for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS)
									{
										if (electricalTile.canConnect(direction) && this.getConductors().contains(VectorHelper.getConnectorFromSide(tileEntity.worldObj, new Vector3(tileEntity), direction)))
										{
											float energyToSend = totalUsableEnergy * (Math.min(electricalTile.getRequest(direction), totalEnergyRequest) / totalEnergyRequest);

											if (energyToSend > 0)
											{
												ElectricityPack electricityToSend = ElectricityPack.getFromWatts(energyToSend, voltage);
												remainingUsableEnergy -= electricalTile.receiveElectricity(direction, electricityToSend, true);
											}
										}
									}
								}
								else if (Compatibility.isIndustrialCraft2Loaded() && tileEntity instanceof IEnergySink)
								{
									IEnergySink electricalTile = (IEnergySink) tileEntity;

									for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS)
									{
										TileEntity conductor = VectorHelper.getConnectorFromSide(tileEntity.worldObj, new Vector3(tileEntity), direction);

										if (this.getConductors().contains(conductor) && electricalTile.acceptsEnergyFrom(conductor, direction))
										{
											float energyToSend = (float) Math.min(totalUsableEnergy * ((electricalTile.demandedEnergyUnits() * Compatibility.IC2_RATIO) / totalEnergyRequest), electricalTile.getMaxSafeInput() * Compatibility.IC2_RATIO);

											if (energyToSend > 0)
											{
												remainingUsableEnergy -= electricalTile.injectEnergyUnits(direction, energyToSend * Compatibility.TO_IC2_RATIO) * Compatibility.IC2_RATIO;
											}
										}
									}
								}
								else if (Compatibility.isBuildcraftLoaded() && tileEntity instanceof IPowerReceptor)
								{
									IPowerReceptor electricalTile = (IPowerReceptor) tileEntity;

									for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS)
									{
										TileEntity conductor = VectorHelper.getConnectorFromSide(tileEntity.worldObj, new Vector3(tileEntity), direction);
										PowerReceiver receiver = electricalTile.getPowerReceiver(direction);

										if (receiver != null)
										{
											if (this.getConductors().contains(conductor))
											{
												float energyToSend = totalUsableEnergy * ((receiver.powerRequest() * Compatibility.BC3_RATIO) / totalEnergyRequest);

												if (energyToSend > 0)
												{
													remainingUsableEnergy -= receiver.receiveEnergy(Type.PIPE, energyToSend * Compatibility.TO_BC_RATIO, direction) * Compatibility.BC3_RATIO;
												}
											}
										}
									}
								}
								else if (Compatibility.isThermalExpansionLoaded() && tileEntity instanceof IEnergyHandler)
								{
									IEnergyHandler receiver = (IEnergyHandler) tileEntity;

									for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS)
									{
										TileEntity conductor = VectorHelper.getConnectorFromSide(tileEntity.worldObj, new Vector3(tileEntity), direction);

										if (receiver.canInterface(direction))
										{
											if (this.getConductors().contains(conductor))
											{
												float energyToSend = totalUsableEnergy * ((receiver.receiveEnergy(direction, (int) (remainingUsableEnergy * Compatibility.TO_TE_RATIO), true) * Compatibility.TE_RATIO) / totalEnergyRequest);

												if (energyToSend > 0)
												{
													remainingUsableEnergy -= receiver.receiveEnergy(direction, (int) (energyToSend * Compatibility.TO_TE_RATIO), false) * Compatibility.TE_RATIO;
												}
											}
										}
									}
								}
							}
						}
						else
						{
							markRefresh = true;
						}
					}

					if (markRefresh)
					{
						this.refresh();
					}
				}
			}
		}

		return remainingUsableEnergy;
	}

	@Override
	public ElectricityPack getRequest(TileEntity... ignoreTiles)
	{
		List<ElectricityPack> requests = new ArrayList<ElectricityPack>();

		Iterator<TileEntity> it = new HashSet(this.getAcceptors()).iterator();

		while (it.hasNext())
		{
			TileEntity tileEntity = it.next();

			if (Arrays.asList(ignoreTiles).contains(tileEntity))
			{
				continue;
			}

			if (tileEntity != null && !tileEntity.isInvalid())
			{
				if (tileEntity.worldObj.getBlockTileEntity(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord) == tileEntity)
				{
					if (tileEntity instanceof IElectrical)
					{
						for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS)
						{
							if (((IElectrical) tileEntity).canConnect(direction) && this.getConductors().contains(VectorHelper.getConnectorFromSide(tileEntity.worldObj, new Vector3(tileEntity), direction)))
							{
								requests.add(ElectricityPack.getFromWatts(((IElectrical) tileEntity).getRequest(direction), ((IElectrical) tileEntity).getVoltage()));
							}
						}
						continue;
					}

					if (Compatibility.isIndustrialCraft2Loaded() && tileEntity instanceof IEnergySink)
					{
						for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS)
						{
							if (((IEnergySink) tileEntity).acceptsEnergyFrom(VectorHelper.getTileEntityFromSide(tileEntity.worldObj, new Vector3(tileEntity), direction), direction) && this.getConductors().contains(VectorHelper.getConnectorFromSide(tileEntity.worldObj, new Vector3(tileEntity), direction)))
							{
								ElectricityPack pack = ElectricityPack.getFromWatts((float) (((IEnergySink) tileEntity).demandedEnergyUnits() * Compatibility.IC2_RATIO), 1);

								if (pack.getWatts() > 0)
								{
									requests.add(pack);
								}
							}
						}

						continue;
					}

					if (Compatibility.isBuildcraftLoaded() && tileEntity instanceof IPowerReceptor)
					{
						for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS)
						{
							if (((IPowerReceptor) tileEntity).getPowerReceiver(direction) != null)
							{
								ElectricityPack pack = ElectricityPack.getFromWatts(((IPowerReceptor) tileEntity).getPowerReceiver(direction).powerRequest() * Compatibility.BC3_RATIO, 1);

								if (pack.getWatts() > 0)
								{
									requests.add(pack);
									break;
								}
							}
						}

						continue;
					}

					if (Compatibility.isThermalExpansionLoaded() && tileEntity instanceof IEnergyHandler)
					{
						IEnergyHandler receiver = (IEnergyHandler) tileEntity;

						for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS)
						{
							TileEntity conductor = VectorHelper.getConnectorFromSide(tileEntity.worldObj, new Vector3(tileEntity), direction);

							if (receiver.canInterface(direction))
							{
								ElectricityPack pack = ElectricityPack.getFromWatts(receiver.receiveEnergy(direction, (int) Integer.MAX_VALUE, true) * Compatibility.TE_RATIO, 1);

								if (pack.getWatts() > 0)
								{
									requests.add(pack);
									break;
								}
							}
						}

						continue;
					}
				}
			}
		}

		ElectricityPack mergedPack = ElectricityPack.merge(requests);
		ElectricityRequestEvent evt = new ElectricityRequestEvent(this, mergedPack, ignoreTiles);
		MinecraftForge.EVENT_BUS.post(evt);
		return mergedPack;
	}

	@Override
	public void refresh()
	{
		this.electricalTiles.clear();

		try
		{
			Iterator<IConductor> it = this.getConductors().iterator();

			while (it.hasNext())
			{
				IConductor conductor = it.next();

				if (conductor == null)
				{
					it.remove();
				}
				else if (((TileEntity) conductor).isInvalid())
				{
					it.remove();
				}
				else
				{
					conductor.setNetwork(this);
				}

				for (int i = 0; i < conductor.getAdjacentConnections().length; i++)
				{
					TileEntity acceptor = conductor.getAdjacentConnections()[i];

					if (!(acceptor instanceof IConductor))
					{
						if (acceptor instanceof IElectrical)
						{
							ArrayList<ForgeDirection> possibleDirections = null;

							if (this.electricalTiles.containsKey(acceptor))
							{
								possibleDirections = this.electricalTiles.get(acceptor);
							}
							else
							{
								possibleDirections = new ArrayList<ForgeDirection>();
							}

							if (((IElectrical) acceptor).canConnect(ForgeDirection.getOrientation(i)) && this.getConductors().contains(VectorHelper.getConnectorFromSide(acceptor.worldObj, new Vector3(acceptor), ForgeDirection.getOrientation(i))))
							{
								possibleDirections.add(ForgeDirection.getOrientation(i));
							}

							this.electricalTiles.put(acceptor, possibleDirections);
							continue;
						}

						if (Compatibility.isIndustrialCraft2Loaded() && acceptor instanceof IEnergyAcceptor)
						{
							ArrayList<ForgeDirection> possibleDirections = null;

							if (this.electricalTiles.containsKey(acceptor))
							{
								possibleDirections = this.electricalTiles.get(acceptor);
							}
							else
							{
								possibleDirections = new ArrayList<ForgeDirection>();
							}

							if (((IEnergyAcceptor) acceptor).acceptsEnergyFrom(VectorHelper.getTileEntityFromSide(acceptor.worldObj, new Vector3(acceptor), ForgeDirection.getOrientation(i)), ForgeDirection.getOrientation(i)) && this.getConductors().contains(VectorHelper.getConnectorFromSide(acceptor.worldObj, new Vector3(acceptor), ForgeDirection.getOrientation(i))))
							{
								possibleDirections.add(ForgeDirection.getOrientation(i));
							}

							this.electricalTiles.put(acceptor, possibleDirections);
							continue;
						}

						if (Compatibility.isBuildcraftLoaded() && acceptor instanceof IPowerReceptor)
						{
							ArrayList<ForgeDirection> possibleDirections = null;

							if (this.electricalTiles.containsKey(acceptor))
							{
								possibleDirections = this.electricalTiles.get(acceptor);
							}
							else
							{
								possibleDirections = new ArrayList<ForgeDirection>();
							}

							if (this.getConductors().contains(VectorHelper.getConnectorFromSide(acceptor.worldObj, new Vector3(acceptor), ForgeDirection.getOrientation(i))))
							{
								possibleDirections.add(ForgeDirection.getOrientation(i));
							}

							this.electricalTiles.put(acceptor, possibleDirections);
							continue;
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			FMLLog.severe("Universal Electricity: Failed to refresh conductor.");
			e.printStackTrace();
		}
	}

	@Override
	public void merge(IElectricityNetwork network)
	{
		if (network != null && network != this)
		{
			UniversalNetwork newNetwork = new UniversalNetwork();
			newNetwork.getConductors().addAll(this.getConductors());
			newNetwork.getConductors().addAll(network.getConductors());
			newNetwork.refresh();
		}
	}

	@Override
	public void split(IConductor splitPoint)
	{
		if (splitPoint instanceof TileEntity)
		{
			this.getConductors().remove(splitPoint);

			/**
			 * Loop through the connected blocks and attempt to see if there are connections between
			 * the two points elsewhere.
			 */
			TileEntity[] connectedBlocks = splitPoint.getAdjacentConnections();

			for (int i = 0; i < connectedBlocks.length; i++)
			{
				TileEntity connectedBlockA = connectedBlocks[i];

				if (connectedBlockA instanceof INetworkConnection)
				{
					for (int ii = 0; ii < connectedBlocks.length; ii++)
					{
						final TileEntity connectedBlockB = connectedBlocks[ii];

						if (connectedBlockA != connectedBlockB && connectedBlockB instanceof INetworkConnection)
						{
							Pathfinder finder = new PathfinderChecker(((TileEntity) splitPoint).worldObj, (INetworkConnection) connectedBlockB, splitPoint);
							finder.init(new Vector3(connectedBlockA));

							if (finder.results.size() > 0)
							{
								/**
								 * The connections A and B are still intact elsewhere. Set all
								 * references of wire connection into one network.
								 */

								for (Vector3 node : finder.closedSet)
								{
									TileEntity nodeTile = node.getTileEntity(((TileEntity) splitPoint).worldObj);

									if (nodeTile instanceof INetworkProvider)
									{
										if (nodeTile != splitPoint)
										{
											((INetworkProvider) nodeTile).setNetwork(this);
										}
									}
								}
							}
							else
							{
								/**
								 * The connections A and B are not connected anymore. Give both of
								 * them a new network.
								 */
								IElectricityNetwork newNetwork = new UniversalNetwork();

								for (Vector3 node : finder.closedSet)
								{
									TileEntity nodeTile = node.getTileEntity(((TileEntity) splitPoint).worldObj);

									if (nodeTile instanceof INetworkProvider)
									{
										if (nodeTile != splitPoint)
										{
											newNetwork.getConductors().add((IConductor) nodeTile);
										}
									}
								}

								newNetwork.refresh();
							}
						}
					}
				}
			}
		}
	}

	@Override
	public String toString()
	{
		return "UniversalNetwork[" + this.hashCode() + "|Wires:" + this.getConductors().size() + "|Acceptors:" + this.electricalTiles.size() + "]";
	}
}
