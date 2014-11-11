package mekanism.common.util;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import mekanism.api.Coord4D;
import mekanism.api.energy.ICableOutputter;
import mekanism.api.energy.IStrictEnergyAcceptor;
import mekanism.api.transmitters.IGridTransmitter;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.Mekanism;
import mekanism.common.tile.TileEntityElectricBlock;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import cofh.api.energy.IEnergyConnection;
import cofh.api.energy.IEnergyProvider;
import cofh.api.energy.IEnergyReceiver;
import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;

public final class CableUtils
{
	private static Set<ForgeDirection> allSides = EnumSet.complementOf(EnumSet.of(ForgeDirection.UNKNOWN));

	/**
	 * Gets all the connected energy acceptors, whether IC2-based or BuildCraft-based, surrounding a specific tile entity.
	 * @param tileEntity - center tile entity
	 * @return TileEntity[] of connected acceptors
	 */
	public static TileEntity[] getConnectedEnergyAcceptors(TileEntity tileEntity)
	{
		TileEntity[] acceptors = new TileEntity[] {null, null, null, null, null, null};

		for(ForgeDirection orientation : ForgeDirection.VALID_DIRECTIONS)
		{
			TileEntity acceptor = Coord4D.get(tileEntity).getFromSide(orientation).getTileEntity(tileEntity.getWorldObj());

			if(isEnergyAcceptor(acceptor))
			{
				acceptors[orientation.ordinal()] = acceptor;
			}
		}

		return acceptors;
	}

	public static boolean isEnergyAcceptor(TileEntity tileEntity)
	{
		return (tileEntity instanceof IStrictEnergyAcceptor ||
				(MekanismUtils.useIC2() && tileEntity instanceof IEnergySink) ||
				(MekanismUtils.useRF() && tileEntity instanceof IEnergyReceiver));
	}

	/**
	 * Gets all the connected cables around a specific tile entity.
	 * @param tileEntity - center tile entity
	 * @return TileEntity[] of connected cables
	 */
	public static TileEntity[] getConnectedCables(TileEntity tileEntity)
	{
		TileEntity[] cables = new TileEntity[] {null, null, null, null, null, null};

		for(ForgeDirection orientation : ForgeDirection.VALID_DIRECTIONS)
		{
			TileEntity cable = Coord4D.get(tileEntity).getFromSide(orientation).getTileEntity(tileEntity.getWorldObj());

			if(isCable(cable))
			{
				cables[orientation.ordinal()] = cable;
			}
		}

		return cables;
	}

	public static boolean isCable(TileEntity tileEntity)
	{
		return TransmissionType.checkTransmissionType(tileEntity, TransmissionType.ENERGY);
	}

	/**
	 * Gets all the adjacent connections to a TileEntity.
	 * @param tileEntity - center TileEntity
	 * @return boolean[] of adjacent connections
	 */
	public static boolean[] getConnections(TileEntity tileEntity)
	{
		return getConnections(tileEntity, allSides);
	}

	/**
	 * Gets the adjacent connections to a TileEntity, from a subset of its sides.
	 * @param tileEntity - center TileEntity
	 * @param sides - set of sides to check
	 * @return boolean[] of adjacent connections
	 */
	public static boolean[] getConnections(TileEntity tileEntity, Set<ForgeDirection> sides)
	{
		boolean[] connectable = new boolean[] {false, false, false, false, false, false};
		Coord4D coord = Coord4D.get(tileEntity);

		for(ForgeDirection side : sides)
		{
			TileEntity tile = coord.getFromSide(side).getTileEntity(tileEntity.getWorldObj());

			connectable[side.ordinal()] |= isEnergyAcceptor(tile) && isConnectable(tileEntity, tile, side);
			connectable[side.ordinal()] |= isCable(tile);
			connectable[side.ordinal()] |= isOutputter(tile, side);
		}

		return connectable;
	}

	/**
	 * Gets all the connected cables around a specific tile entity.
	 * @param tileEntity - center tile entity
	 * @return TileEntity[] of connected cables
	 */
	public static TileEntity[] getConnectedOutputters(TileEntity tileEntity)
	{
		TileEntity[] outputters = new TileEntity[] {null, null, null, null, null, null};

		for(ForgeDirection orientation : ForgeDirection.VALID_DIRECTIONS)
		{
			TileEntity outputter = Coord4D.get(tileEntity).getFromSide(orientation).getTileEntity(tileEntity.getWorldObj());

			if(isOutputter(outputter, orientation))
			{
				outputters[orientation.ordinal()] = outputter;
			}
		}

		return outputters;
	}

	public static boolean isOutputter(TileEntity tileEntity, ForgeDirection side)
	{
		return (tileEntity instanceof ICableOutputter && ((ICableOutputter)tileEntity).canOutputTo(side.getOpposite())) ||
				(MekanismUtils.useIC2() && tileEntity instanceof IEnergySource && ((IEnergySource)tileEntity).emitsEnergyTo(null, side.getOpposite())) ||
				(MekanismUtils.useRF() && tileEntity instanceof IEnergyProvider && ((IEnergyConnection)tileEntity).canConnectEnergy(side.getOpposite()));
	}

	/**
	 * Whether or not a cable can connect to a specific acceptor.
	 * @param side - side to check
	 * @param tile - cable TileEntity
	 * @return whether or not the cable can connect to the specific side
	 */
	public static boolean canConnectToAcceptor(ForgeDirection side, TileEntity tile)
	{
		if(tile == null)
		{
			return false;
		}

		TileEntity tileEntity = Coord4D.get(tile).getFromSide(side).getTileEntity(tile.getWorldObj());

		return isConnectable(tile, tileEntity, side);
	}

	public static boolean isConnectable(TileEntity orig, TileEntity tileEntity, ForgeDirection side)
	{
		if(tileEntity instanceof IGridTransmitter)
		{
			return false;
		}

		if(tileEntity instanceof IStrictEnergyAcceptor)
		{
			if(((IStrictEnergyAcceptor)tileEntity).canReceiveEnergy(side.getOpposite()))
			{
				return true;
			}
		}
		else if(MekanismUtils.useIC2() && tileEntity instanceof IEnergyAcceptor)
		{
			if(((IEnergyAcceptor)tileEntity).acceptsEnergyFrom(orig, side.getOpposite()))
			{
				return true;
			}
		}
		else if(tileEntity instanceof ICableOutputter)
		{
			if(((ICableOutputter)tileEntity).canOutputTo(side.getOpposite()))
			{
				return true;
			}
		}
		else if(MekanismUtils.useRF() && tileEntity instanceof IEnergyConnection)
		{
			if(((IEnergyConnection)tileEntity).canConnectEnergy(side.getOpposite()))
			{
				return true;
			}
		}

		return false;
	}

	public static void emit(TileEntityElectricBlock emitter)
	{
		if(!emitter.getWorldObj().isRemote && MekanismUtils.canFunction(emitter))
		{
			double energyToSend = Math.min(emitter.getEnergy(), emitter.getMaxOutput());

			if(energyToSend > 0)
			{
				List<ForgeDirection> outputtingSides = new ArrayList<ForgeDirection>();
				boolean[] connectable = getConnections(emitter, emitter.getOutputtingSides());

				for(ForgeDirection side : emitter.getOutputtingSides())
				{
					if(connectable[side.ordinal()])
					{
						outputtingSides.add(side);
					}
				}

				if(outputtingSides.size() > 0)
				{
					double sent = 0;
					boolean tryAgain = false;
					int i = 0;

					do {
						double prev = sent;
						sent += emit_do(emitter, outputtingSides, energyToSend-sent, tryAgain);

						tryAgain = energyToSend-sent > 0 && sent-prev > 0 && i < 100;

						i++;
					} while(tryAgain);

					emitter.setEnergy(emitter.getEnergy() - sent);
				}
			}
		}
	}

	private static double emit_do(TileEntityElectricBlock emitter, List<ForgeDirection> outputtingSides, double totalToSend, boolean tryAgain)
	{
		double remains = totalToSend%outputtingSides.size();
		double splitSend = (totalToSend-remains)/outputtingSides.size();
		double sent = 0;

		List<ForgeDirection> toRemove = new ArrayList<ForgeDirection>();

		for(ForgeDirection side : outputtingSides)
		{
			TileEntity tileEntity = Coord4D.get(emitter).getFromSide(side).getTileEntity(emitter.getWorldObj());
			double toSend = splitSend+remains;
			remains = 0;

			double prev = sent;
			sent += emit_do_do(emitter, tileEntity, side, toSend, tryAgain);

			if(sent-prev == 0)
			{
				toRemove.add(side);
			}
		}

		for(ForgeDirection side : toRemove)
		{
			outputtingSides.remove(side);
		}

		return sent;
	}

	private static double emit_do_do(TileEntityElectricBlock from, TileEntity tileEntity, ForgeDirection side, double currentSending, boolean tryAgain)
	{
		double sent = 0;

		if(tileEntity instanceof IStrictEnergyAcceptor)
		{
			IStrictEnergyAcceptor acceptor = (IStrictEnergyAcceptor)tileEntity;

			if(acceptor.canReceiveEnergy(side.getOpposite()))
			{
				sent += acceptor.transferEnergyToAcceptor(side.getOpposite(), currentSending);
			}
		}
		else if(MekanismUtils.useRF() && tileEntity instanceof IEnergyReceiver)
		{
			IEnergyReceiver handler = (IEnergyReceiver)tileEntity;

			if(handler.canConnectEnergy(side.getOpposite()))
			{
				int used = handler.receiveEnergy(side.getOpposite(), (int)Math.round(currentSending*Mekanism.TO_TE), false);
				sent += used*Mekanism.FROM_TE;
			}
		}
		else if(MekanismUtils.useIC2() && tileEntity instanceof IEnergySink)
		{
			if(((IEnergySink)tileEntity).acceptsEnergyFrom(from, side.getOpposite()))
			{
				double toSend = Math.min(currentSending, EnergyNet.instance.getPowerFromTier(((IEnergySink)tileEntity).getSinkTier())*Mekanism.FROM_IC2);
				toSend = Math.min(toSend, ((IEnergySink)tileEntity).getDemandedEnergy()*Mekanism.FROM_IC2);
				sent += (toSend - (((IEnergySink)tileEntity).injectEnergy(side.getOpposite(), toSend*Mekanism.TO_IC2, 0)*Mekanism.FROM_IC2));
			}
		}

		return sent;
	}
}
