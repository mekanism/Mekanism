package mekanism.common.util;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import mekanism.common.capabilities.Capabilities;
import mekanism.api.Coord4D;
import mekanism.api.MekanismConfig.general;
import mekanism.api.energy.IStrictEnergyAcceptor;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.base.IEnergyWrapper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import cofh.api.energy.IEnergyConnection;
import cofh.api.energy.IEnergyProvider;
import cofh.api.energy.IEnergyReceiver;

public final class CableUtils
{
	public static boolean isEnergyAcceptor(TileEntity tileEntity)
	{
		return tileEntity != null && (MekanismUtils.hasCapability(tileEntity, Capabilities.ENERGY_ACCEPTOR_CAPABILITY, null) ||
				(MekanismUtils.useIC2() && tileEntity instanceof IEnergySink) ||
				(MekanismUtils.useRF() && tileEntity instanceof IEnergyReceiver));
	}

	public static boolean isCable(TileEntity tileEntity)
	{
		if(tileEntity != null && MekanismUtils.hasCapability(tileEntity, Capabilities.GRID_TRANSMITTER_CAPABILITY, null))
		{
			return TransmissionType.checkTransmissionType(MekanismUtils.getCapability(tileEntity, Capabilities.GRID_TRANSMITTER_CAPABILITY, null), TransmissionType.ENERGY);
		}
		return false;
	}

	/**
	 * Gets the adjacent connections to a TileEntity, from a subset of its sides.
	 * @param tileEntity - center TileEntity
	 * @param sides - set of sides to check
	 * @return boolean[] of adjacent connections
	 */
	public static boolean[] getConnections(TileEntity tileEntity, Set<EnumFacing> sides)
	{
		boolean[] connectable = new boolean[] {false, false, false, false, false, false};
		Coord4D coord = Coord4D.get(tileEntity);

		for(EnumFacing side : sides)
		{
			TileEntity tile = coord.offset(side).getTileEntity(tileEntity.getWorld());

			connectable[side.ordinal()] = isValidAcceptorOnSide(tileEntity, tile, side);
			connectable[side.ordinal()] |= isCable(tile);
		}

		return connectable;
	}

	/**
	 * Gets the adjacent connections to a TileEntity, from a subset of its sides.
	 * @param cableEntity - TileEntity that's trying to connect
	 * @param side - side to check
	 * @return boolean whether the acceptor is valid
	 */
	public static boolean isValidAcceptorOnSide(TileEntity cableEntity, TileEntity tile, EnumFacing side)
	{
		if(tile == null || isCable(tile))
		{
			return false;
		}

		if(isEnergyAcceptor(tile) && isConnectable(cableEntity, tile, side))
		{
			return true;
		}
		
		return isOutputter(tile, side) || (MekanismUtils.useRF() && tile instanceof IEnergyConnection && ((IEnergyConnection)tile).canConnectEnergy(side.getOpposite()));
	}

	/**
	 * Gets all the connected cables around a specific tile entity.
	 * @param tileEntity - center tile entity
	 * @return TileEntity[] of connected cables
	 */
	public static TileEntity[] getConnectedOutputters(TileEntity tileEntity)
	{
		return getConnectedOutputters(tileEntity.getPos(), tileEntity.getWorld());
	}

	public static TileEntity[] getConnectedOutputters(BlockPos pos, World world)
	{
		TileEntity[] outputters = new TileEntity[] {null, null, null, null, null, null};

		for(EnumFacing orientation : EnumFacing.VALUES)
		{
			TileEntity outputter = world.getTileEntity(pos.offset(orientation));

			if(isOutputter(outputter, orientation))
			{
				outputters[orientation.ordinal()] = outputter;
			}
		}

		return outputters;
	}

	public static boolean isOutputter(TileEntity tileEntity, EnumFacing side)
	{
		return tileEntity != null && (
				(MekanismUtils.hasCapability(tileEntity, Capabilities.CABLE_OUTPUTTER_CAPABILITY, side.getOpposite()) && MekanismUtils.getCapability(tileEntity, Capabilities.CABLE_OUTPUTTER_CAPABILITY, side.getOpposite()).canOutputTo(side.getOpposite())) ||
				(MekanismUtils.useIC2() && tileEntity instanceof IEnergySource && ((IEnergySource)tileEntity).emitsEnergyTo(null, side.getOpposite())) ||
				(MekanismUtils.useRF() && tileEntity instanceof IEnergyProvider && ((IEnergyConnection)tileEntity).canConnectEnergy(side.getOpposite()))
		);
	}

	public static boolean isConnectable(TileEntity orig, TileEntity tileEntity, EnumFacing side)
	{
		if(MekanismUtils.hasCapability(tileEntity, Capabilities.GRID_TRANSMITTER_CAPABILITY, side.getOpposite()))
		{
			return false;
		}

		if(MekanismUtils.hasCapability(tileEntity, Capabilities.ENERGY_ACCEPTOR_CAPABILITY, side.getOpposite()))
		{
			if(MekanismUtils.getCapability(tileEntity, Capabilities.ENERGY_ACCEPTOR_CAPABILITY, side.getOpposite()).canReceiveEnergy(side.getOpposite()))
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
		else if(MekanismUtils.hasCapability(tileEntity, Capabilities.CABLE_OUTPUTTER_CAPABILITY, side.getOpposite()))
		{
			if(MekanismUtils.getCapability(tileEntity, Capabilities.CABLE_OUTPUTTER_CAPABILITY, side.getOpposite()).canOutputTo(side.getOpposite()))
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

	public static void emit(IEnergyWrapper emitter)
	{
		if(!((TileEntity)emitter).getWorld().isRemote && MekanismUtils.canFunction((TileEntity)emitter))
		{
			double energyToSend = Math.min(emitter.getEnergy(), emitter.getMaxOutput());

			if(energyToSend > 0)
			{
				List<EnumFacing> outputtingSides = new ArrayList<EnumFacing>();
				boolean[] connectable = getConnections((TileEntity)emitter, emitter.getOutputtingSides());

				for(EnumFacing side : emitter.getOutputtingSides())
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

	private static double emit_do(IEnergyWrapper emitter, List<EnumFacing> outputtingSides, double totalToSend, boolean tryAgain)
	{
		double remains = totalToSend%outputtingSides.size();
		double splitSend = (totalToSend-remains)/outputtingSides.size();
		double sent = 0;

		List<EnumFacing> toRemove = new ArrayList<EnumFacing>();

		for(EnumFacing side : outputtingSides)
		{
			TileEntity tileEntity = Coord4D.get((TileEntity)emitter).offset(side).getTileEntity(((TileEntity)emitter).getWorld());
			double toSend = splitSend+remains;
			remains = 0;

			double prev = sent;
			sent += emit_do_do(emitter, tileEntity, side, toSend, tryAgain);

			if(sent-prev == 0)
			{
				toRemove.add(side);
			}
		}

		for(EnumFacing side : toRemove)
		{
			outputtingSides.remove(side);
		}

		return sent;
	}

	private static double emit_do_do(IEnergyWrapper from, TileEntity tileEntity, EnumFacing side, double currentSending, boolean tryAgain)
	{
		double sent = 0;

		if(MekanismUtils.hasCapability(tileEntity, Capabilities.ENERGY_ACCEPTOR_CAPABILITY, side.getOpposite()))
		{
			IStrictEnergyAcceptor acceptor = MekanismUtils.getCapability(tileEntity, Capabilities.ENERGY_ACCEPTOR_CAPABILITY, side.getOpposite());

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
				int used = handler.receiveEnergy(side.getOpposite(), (int)Math.round(currentSending*general.TO_TE), false);
				sent += used*general.FROM_TE;
			}
		}
		else if(MekanismUtils.useIC2() && tileEntity instanceof IEnergySink)
		{
			if(((IEnergySink)tileEntity).acceptsEnergyFrom((TileEntity)from, side.getOpposite()))
			{
				double toSend = Math.min(currentSending, EnergyNet.instance.getPowerFromTier(((IEnergySink)tileEntity).getSinkTier())*general.FROM_IC2);
				toSend = Math.min(toSend, ((IEnergySink)tileEntity).getDemandedEnergy()*general.FROM_IC2);
				sent += (toSend - (((IEnergySink)tileEntity).injectEnergy(side.getOpposite(), toSend*general.TO_IC2, 0)*general.FROM_IC2));
			}
		}

		return sent;
	}
}
