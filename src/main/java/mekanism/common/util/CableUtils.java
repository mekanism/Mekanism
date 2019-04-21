package mekanism.common.util;

import cofh.redstoneflux.api.IEnergyConnection;
import cofh.redstoneflux.api.IEnergyProvider;
import cofh.redstoneflux.api.IEnergyReceiver;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import mekanism.api.Coord4D;
import mekanism.api.energy.IStrictEnergyOutputter;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.base.EnergyAcceptorTarget;
import mekanism.common.base.EnergyAcceptorWrapper;
import mekanism.common.base.IEnergyWrapper;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.integration.ic2.IC2Integration;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.energy.CapabilityEnergy;

public final class CableUtils {

    public static boolean isCable(TileEntity tileEntity) {
        if (CapabilityUtils.hasCapability(tileEntity, Capabilities.GRID_TRANSMITTER_CAPABILITY, null)) {
            return TransmissionType.checkTransmissionType(
                  CapabilityUtils.getCapability(tileEntity, Capabilities.GRID_TRANSMITTER_CAPABILITY, null),
                  TransmissionType.ENERGY);
        }

        return false;
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

    /**
     * @param availableAcceptors The EnergyAcceptorWrapper targets to send energy fairly to.
     * @param totalAcceptors The total number of acceptors. Note: this number is bigger than availableAcceptors.size if
     * any targets have more than one acceptor.
     * @param energyToSend The amount of energy to attempt to send
     * @return The amount that actually got sent
     */
    public static double sendToAcceptors(Set<EnergyAcceptorTarget> availableAcceptors, int totalAcceptors,
          double energyToSend) {
        if (availableAcceptors.isEmpty() || totalAcceptors == 0) {
            return 0;
        }
        double sent = 0;
        double energyToSplit = energyToSend;
        int toSplitAmong = totalAcceptors;
        double amountPer = energyToSplit / toSplitAmong;
        boolean amountPerChanged = false;

        //Simulate addition
        for (EnergyAcceptorTarget target : availableAcceptors) {
            Map<EnumFacing, EnergyAcceptorWrapper> wrappers = target.getWrappers();
            for (Entry<EnumFacing, EnergyAcceptorWrapper> entry : wrappers.entrySet()) {
                EnumFacing side = entry.getKey();
                double amountNeeded = entry.getValue().acceptEnergy(side, energyToSend, true);
                boolean canGive = amountNeeded <= amountPer;
                //Add the amount
                target.addAmount(side, amountNeeded, canGive);
                if (canGive) {
                    //If we are giving it, then lower the amount we are checking/splitting
                    energyToSplit -= amountNeeded;
                    toSplitAmong--;
                    //Only recalculate it if it is not willing to accept/doesn't want the
                    // full per side split
                    if (amountNeeded != amountPer && toSplitAmong != 0) {
                        amountPer = energyToSplit / toSplitAmong;
                        amountPerChanged = true;
                    }
                }
            }
        }

        //Only run this if we changed the amountPer from when we first ran things
        while (amountPerChanged) {
            amountPerChanged = false;
            double amountPerLast = amountPer;
            for (EnergyAcceptorTarget target : availableAcceptors) {
                if (target.noneNeeded()) {
                    continue;
                }
                //Use an iterator rather than a copy of the keyset of the needed submap
                // This allows for us to remove it once we find it without  having to
                // start looping again or make a large number of copies of the set
                Iterator<Entry<EnumFacing, Double>> iterator = target.getNeededIterator();
                while (iterator.hasNext()) {
                    Entry<EnumFacing, Double> needInfo = iterator.next();
                    Double amountNeeded = needInfo.getValue();
                    if (amountNeeded <= amountPer) {
                        target.addGiven(needInfo.getKey(), amountNeeded);
                        //Remove it as it no longer valid
                        iterator.remove();
                        //Adjust the energy split
                        energyToSplit -= amountNeeded;
                        toSplitAmong--;
                        //Only recalculate it if it is not willing to accept/doesn't want the
                        // full per side split
                        if (amountNeeded != amountPer && toSplitAmong != 0) {
                            amountPer = energyToSplit / toSplitAmong;
                            if (!amountPerChanged && amountPer != amountPerLast) {
                                //We changed our amount so set it back to true so that we know we need
                                // to loop over things again
                                amountPerChanged = true;
                                //Continue checking things in case we happen to be
                                // getting things in a bad order so that we don't recheck
                                // the same values many times
                            }
                        }
                    }
                }
            }
        }

        //Give them all the energy we calculated they deserve/want
        for (EnergyAcceptorTarget target : availableAcceptors) {
            sent += target.sendGivenWithDefault(amountPer);
        }

        return sent;
    }

    public static void emit(IEnergyWrapper emitter) {
        TileEntity tileEntity = (TileEntity) emitter;
        if (!tileEntity.getWorld().isRemote && MekanismUtils.canFunction(tileEntity)) {
            double energyToSend = Math.min(emitter.getEnergy(), emitter.getMaxOutput());

            if (energyToSend > 0) {
                Coord4D coord = Coord4D.get(tileEntity);
                //Fake that we have one target given we know that no sides will overlap
                // This allows us to have slightly better performance
                EnergyAcceptorTarget target = new EnergyAcceptorTarget();
                int totalAcceptors = 0;
                for (EnumFacing side : EnumFacing.values()) {
                    if (emitter.sideIsOutput(side)) {
                        TileEntity tile = coord.offset(side).getTileEntity(tileEntity.getWorld());
                        //If it can accept energy or it is a cable
                        if (isValidAcceptorOnSide(tileEntity, tile, side) || isCable(tile)) {
                            //Get the opposite side as the current side is relative to us
                            EnumFacing opposite = side.getOpposite();
                            EnergyAcceptorWrapper acceptor = EnergyAcceptorWrapper.get(tile, opposite);
                            if (acceptor != null && acceptor.canReceiveEnergy(opposite) && acceptor
                                  .needsEnergy(opposite)) {
                                target.addSide(opposite, acceptor);
                                totalAcceptors++;
                            }
                        }
                    }
                }
                if (target.hasAcceptors()) {
                    Set<EnergyAcceptorTarget> targets = new HashSet<>();
                    targets.add(target);
                    double sent = sendToAcceptors(targets, totalAcceptors, energyToSend);
                    emitter.setEnergy(emitter.getEnergy() - sent);
                }
            }
        }
    }
}