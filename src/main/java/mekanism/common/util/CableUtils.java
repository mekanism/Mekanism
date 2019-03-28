package mekanism.common.util;

import cofh.redstoneflux.api.IEnergyConnection;
import cofh.redstoneflux.api.IEnergyProvider;
import cofh.redstoneflux.api.IEnergyReceiver;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import mekanism.api.Coord4D;
import mekanism.api.energy.IStrictEnergyAcceptor;
import mekanism.api.energy.IStrictEnergyOutputter;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.base.IEnergyWrapper;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.ic2.IC2Integration;
import net.darkhax.tesla.api.ITeslaConsumer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

public final class CableUtils {

    public static boolean isCable(TileEntity tileEntity) {
        if (tileEntity != null && CapabilityUtils
              .hasCapability(tileEntity, Capabilities.GRID_TRANSMITTER_CAPABILITY, null)) {
            return TransmissionType.checkTransmissionType(
                  CapabilityUtils.getCapability(tileEntity, Capabilities.GRID_TRANSMITTER_CAPABILITY, null),
                  TransmissionType.ENERGY);
        }

        return false;
    }

    /**
     * Gets the adjacent connections to a TileEntity, from a subset of its sides.
     *
     * @param tileEntity - center TileEntity
     * @param sideFunction - set of sides to check
     * @return boolean[] of adjacent connections
     */
    public static boolean[] getConnections(TileEntity tileEntity, Function<EnumFacing, Boolean> sideFunction) {
        boolean[] connectable = new boolean[]{false, false, false, false, false, false};
        Coord4D coord = Coord4D.get(tileEntity);

        for (EnumFacing side : EnumFacing.values()) {
            if (sideFunction.apply(side)) {
                TileEntity tile = coord.offset(side).getTileEntity(tileEntity.getWorld());

                connectable[side.ordinal()] = isValidAcceptorOnSide(tileEntity, tile, side);
                connectable[side.ordinal()] |= isCable(tile);
            }
        }

        return connectable;
    }

    /**
     * Gets the adjacent connections to a TileEntity, from a subset of its sides.
     *
     * @param cableEntity - TileEntity that's trying to connect
     * @param side - side to check
     * @return boolean whether the acceptor is valid
     */
    public static boolean isValidAcceptorOnSide(TileEntity cableEntity, TileEntity tile, EnumFacing side) {
        if (tile == null || isCable(tile)) {
            return false;
        }

        return isAcceptor(cableEntity, tile, side) || isOutputter(tile, side) ||
              (MekanismUtils.useRF() && tile instanceof IEnergyConnection && ((IEnergyConnection) tile)
                    .canConnectEnergy(side.getOpposite())) ||
              (MekanismUtils.useForge() && CapabilityUtils
                    .hasCapability(tile, CapabilityEnergy.ENERGY, side.getOpposite()));
    }

    /**
     * Gets all the connected cables around a specific tile entity.
     *
     * @param tileEntity - center tile entity
     * @return TileEntity[] of connected cables
     */
    public static TileEntity[] getConnectedOutputters(TileEntity tileEntity) {
        return getConnectedOutputters(tileEntity.getPos(), tileEntity.getWorld());
    }

    public static TileEntity[] getConnectedOutputters(BlockPos pos, World world) {
        TileEntity[] outputters = new TileEntity[]{null, null, null, null, null, null};

        for (EnumFacing orientation : EnumFacing.VALUES) {
            TileEntity outputter = world.getTileEntity(pos.offset(orientation));

            if (isOutputter(outputter, orientation)) {
                outputters[orientation.ordinal()] = outputter;
            }
        }

        return outputters;
    }

    public static boolean isOutputter(TileEntity tileEntity, EnumFacing side) {
        if (tileEntity == null) {
            return false;
        }

        if (CapabilityUtils.hasCapability(tileEntity, Capabilities.ENERGY_OUTPUTTER_CAPABILITY, side.getOpposite())) {
            IStrictEnergyOutputter outputter = CapabilityUtils
                  .getCapability(tileEntity, Capabilities.ENERGY_OUTPUTTER_CAPABILITY, side.getOpposite());

            if (outputter != null && outputter.canOutputEnergy(side.getOpposite())) {
                return true;
            }
        }

        if (MekanismUtils.useTesla() && CapabilityUtils
              .hasCapability(tileEntity, Capabilities.TESLA_PRODUCER_CAPABILITY, side.getOpposite())) {
            return true;
        }

        if (MekanismUtils.useForge() && CapabilityUtils
              .hasCapability(tileEntity, CapabilityEnergy.ENERGY, side.getOpposite())) {
            return CapabilityUtils.getCapability(tileEntity, CapabilityEnergy.ENERGY, side.getOpposite()).canExtract();
        }

        if (MekanismUtils.useRF() && tileEntity instanceof IEnergyProvider && ((IEnergyConnection) tileEntity)
              .canConnectEnergy(side.getOpposite())) {
            return true;
        }

        return MekanismUtils.useIC2() && IC2Integration.isOutputter(tileEntity, side);

    }

    public static boolean isAcceptor(TileEntity orig, TileEntity tileEntity, EnumFacing side) {
        if (CapabilityUtils.hasCapability(tileEntity, Capabilities.GRID_TRANSMITTER_CAPABILITY, side.getOpposite())) {
            return false;
        }

        if (CapabilityUtils.hasCapability(tileEntity, Capabilities.ENERGY_ACCEPTOR_CAPABILITY, side.getOpposite())) {
            return CapabilityUtils
                  .getCapability(tileEntity, Capabilities.ENERGY_ACCEPTOR_CAPABILITY, side.getOpposite())
                  .canReceiveEnergy(side.getOpposite());
        } else if (MekanismUtils.useTesla() && CapabilityUtils
              .hasCapability(tileEntity, Capabilities.TESLA_CONSUMER_CAPABILITY, side.getOpposite())) {
            return true;
        } else if (MekanismUtils.useForge() && CapabilityUtils
              .hasCapability(tileEntity, CapabilityEnergy.ENERGY, side.getOpposite())) {
            return CapabilityUtils.getCapability(tileEntity, CapabilityEnergy.ENERGY, side.getOpposite()).canReceive();
        } else if (MekanismUtils.useIC2() && IC2Integration.isAcceptor(orig, tileEntity, side)) {
            return true;
        } else if (MekanismUtils.useRF() && tileEntity instanceof IEnergyReceiver) {
            return ((IEnergyReceiver) tileEntity).canConnectEnergy(side.getOpposite());
        }

        return false;
    }

    public static void emit(IEnergyWrapper emitter) {
        if (!((TileEntity) emitter).getWorld().isRemote && MekanismUtils.canFunction((TileEntity) emitter)) {
            double energyToSend = Math.min(emitter.getEnergy(), emitter.getMaxOutput());

            if (energyToSend > 0) {
                List<EnumFacing> outputtingSides = new LinkedList<>();
                boolean[] connectable = getConnections((TileEntity) emitter, emitter::sideIsOutput);

                for (EnumFacing side : EnumFacing.values()) {
                    if (connectable[side.ordinal()]) {
                        outputtingSides.add(side);
                    }
                }

                if (!outputtingSides.isEmpty()) {
                    double sent = 0;
                    boolean tryAgain = false;
                    int i = 0;

                    do {
                        double prev = sent;
                        sent += emit_do(emitter, outputtingSides, energyToSend - sent, tryAgain);

                        tryAgain = energyToSend - sent > 0 && sent - prev > 0 && i < 100;

                        i++;
                    } while (tryAgain);

                    emitter.setEnergy(emitter.getEnergy() - sent);
                }
            }
        }
    }

    private static double emit_do(IEnergyWrapper emitter, List<EnumFacing> outputtingSides, double totalToSend,
          boolean tryAgain) {
        double remains = totalToSend % outputtingSides.size();
        double splitSend = (totalToSend - remains) / outputtingSides.size();
        double sent = 0;

        for (Iterator<EnumFacing> it = outputtingSides.iterator(); it.hasNext(); ) {
            EnumFacing side = it.next();

            TileEntity tileEntity = Coord4D.get((TileEntity) emitter).offset(side)
                  .getTileEntity(((TileEntity) emitter).getWorld());
            double toSend = splitSend + remains;
            remains = 0;

            double prev = sent;
            sent += emit_do_do(emitter, tileEntity, side, toSend, tryAgain);

            if (sent - prev == 0) {
                it.remove();
            }
        }

        return sent;
    }

    private static double emit_do_do(IEnergyWrapper from, TileEntity tileEntity, EnumFacing side, double currentSending,
          boolean tryAgain) {
        double sent = 0;

        if (CapabilityUtils.hasCapability(tileEntity, Capabilities.ENERGY_ACCEPTOR_CAPABILITY, side.getOpposite())) {
            IStrictEnergyAcceptor acceptor = CapabilityUtils
                  .getCapability(tileEntity, Capabilities.ENERGY_ACCEPTOR_CAPABILITY, side.getOpposite());

            if (acceptor.canReceiveEnergy(side.getOpposite())) {
                sent += acceptor.acceptEnergy(side.getOpposite(), currentSending, false);
            }
        } else if (MekanismUtils.useTesla() && CapabilityUtils
              .hasCapability(tileEntity, Capabilities.TESLA_CONSUMER_CAPABILITY, side.getOpposite())) {
            ITeslaConsumer consumer = CapabilityUtils
                  .getCapability(tileEntity, Capabilities.TESLA_CONSUMER_CAPABILITY, side.getOpposite());
            sent += consumer
                  .givePower(Math.round(currentSending * MekanismConfig.current().general.TO_TESLA.val()), false)
                  * MekanismConfig.current().general.FROM_TESLA.val();
        } else if (MekanismUtils.useForge() && CapabilityUtils
              .hasCapability(tileEntity, CapabilityEnergy.ENERGY, side.getOpposite())) {
            IEnergyStorage storage = CapabilityUtils
                  .getCapability(tileEntity, CapabilityEnergy.ENERGY, side.getOpposite());
            sent += storage.receiveEnergy((int) Math
                        .round(Math.min(Integer.MAX_VALUE, currentSending * MekanismConfig.current().general.TO_FORGE.val())),
                  false) * MekanismConfig.current().general.FROM_FORGE.val();
        } else if (MekanismUtils.useRF() && tileEntity instanceof IEnergyReceiver) {
            IEnergyReceiver handler = (IEnergyReceiver) tileEntity;

            if (handler.canConnectEnergy(side.getOpposite())) {
                int toSend = Math.min((int) Math.round(currentSending * MekanismConfig.current().general.TO_RF.val()),
                      Integer.MAX_VALUE);
                int used = handler.receiveEnergy(side.getOpposite(), toSend, false);
                sent += used * MekanismConfig.current().general.FROM_RF.val();
            }
        } else if (MekanismUtils.useIC2()) {
            sent += IC2Integration.emitEnergy(from, tileEntity, side, currentSending);
        }

        return sent;
    }
}
