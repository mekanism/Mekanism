package mekanism.common.util;

import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;

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
import buildcraft.api.power.IPowerEmitter;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import buildcraft.api.power.PowerHandler.Type;
import cofh.api.energy.IEnergyHandler;

public final class CableUtils
{
	private static Set<ForgeDirection> allSides;

	static
	{
		allSides = EnumSet.allOf(ForgeDirection.class);
		allSides.remove(ForgeDirection.UNKNOWN);
	}

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
				tileEntity instanceof IEnergySink ||
				(tileEntity instanceof IPowerReceptor && !(tileEntity instanceof IGridTransmitter) && MekanismUtils.useBuildCraft()) ||
				tileEntity instanceof IEnergyHandler);
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

			if(isOutputter(tileEntity, orientation))
			{
				outputters[orientation.ordinal()] = outputter;
			}
		}

		return outputters;
	}

	public static boolean isOutputter(TileEntity tileEntity, ForgeDirection side)
	{
		return (tileEntity instanceof ICableOutputter && ((ICableOutputter)tileEntity).canOutputTo(side.getOpposite())) ||
				(tileEntity instanceof IEnergySource && ((IEnergySource)tileEntity).emitsEnergyTo(tileEntity, side.getOpposite())) ||
				(tileEntity instanceof IEnergyHandler && ((IEnergyHandler)tileEntity).canConnectEnergy(side.getOpposite())) ||
				(tileEntity instanceof IPowerEmitter && ((IPowerEmitter)tileEntity).canEmitPowerFrom(side.getOpposite()));
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
		else if(tileEntity instanceof IEnergyAcceptor)
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
		else if(tileEntity instanceof IEnergyHandler)
		{
			if(((IEnergyHandler)tileEntity).canConnectEnergy(side.getOpposite()))
			{
				return true;
			}
		}
		else if(tileEntity instanceof IPowerReceptor && MekanismUtils.useBuildCraft())
		{
			if(((IPowerReceptor)tileEntity).getPowerReceiver(side.getOpposite()) != null)
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
			double sendingEnergy = Math.min(emitter.getEnergy(), emitter.getMaxOutput());

			if(sendingEnergy > 0)
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

					boolean cont = false;

					do {
						cont = false;
						double prev = sent;
						sent += emit_do(emitter, outputtingSides, sendingEnergy-sent);

						if(sendingEnergy-sent > 0 && sent-prev > 0)
						{
							cont = true;
						}
					} while(cont);

					emitter.setEnergy(emitter.getEnergy() - sent);
				}
			}
		}
	}

	private static double emit_do(TileEntityElectricBlock emitter, List<ForgeDirection> outputtingSides, double totalToSend)
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
			sent += emit_do_do(emitter, tileEntity, side, toSend);

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

	private static double emit_do_do(TileEntityElectricBlock from, TileEntity tileEntity, ForgeDirection side, double sendingEnergy)
	{
		double sent = 0;

		if(tileEntity instanceof IStrictEnergyAcceptor)
		{
			IStrictEnergyAcceptor acceptor = (IStrictEnergyAcceptor)tileEntity;

			if(acceptor.canReceiveEnergy(side.getOpposite()))
			{
				double prev = sent;
				sent += acceptor.transferEnergyToAcceptor(side.getOpposite(), sendingEnergy);
			}
		}
		else if(tileEntity instanceof IEnergyHandler)
		{
			IEnergyHandler handler = (IEnergyHandler)tileEntity;

			if(handler.canConnectEnergy(side.getOpposite()))
			{
				int used = handler.receiveEnergy(side.getOpposite(), (int)Math.round(sendingEnergy*Mekanism.TO_TE), false);
				sent += used*Mekanism.FROM_TE;
			}
		}
		else if(tileEntity instanceof IEnergySink)
		{
			if(((IEnergySink)tileEntity).acceptsEnergyFrom(from, side.getOpposite()))
			{
				double toSend = Math.min(sendingEnergy, Math.min(((IEnergySink)tileEntity).getMaxSafeInput(), ((IEnergySink)tileEntity).demandedEnergyUnits())*Mekanism.FROM_IC2);
				double rejects = ((IEnergySink)tileEntity).injectEnergyUnits(side.getOpposite(), toSend*Mekanism.TO_IC2)*Mekanism.FROM_IC2;
				sent += (toSend - rejects);
			}
		}
		else if(tileEntity instanceof IPowerReceptor && MekanismUtils.useBuildCraft())
		{
			PowerReceiver receiver = ((IPowerReceptor)tileEntity).getPowerReceiver(side.getOpposite());

			if(receiver != null)
			{
				double transferEnergy = Math.min(sendingEnergy, receiver.powerRequest()*Mekanism.FROM_BC);
				double used = receiver.receiveEnergy(Type.STORAGE, (float)(transferEnergy*Mekanism.TO_BC), side.getOpposite());
				sent += used*Mekanism.FROM_BC;
			}
		}

		return sent;
	}
}
