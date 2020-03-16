package mekanism.common.util;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Optional;
import java.util.Set;
import mekanism.api.energy.IStrictEnergyAcceptor;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.energy.IStrictEnergyOutputter;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.base.EnergyAcceptorWrapper;
import mekanism.common.base.target.EnergyAcceptorTarget;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.tile.TileEntityInductionPort;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

public final class CableUtils {

    public static boolean isCable(TileEntity tile) {
        return CapabilityUtils.getCapability(tile, Capabilities.GRID_TRANSMITTER_CAPABILITY, null).filter(transmitter ->
              TransmissionType.checkTransmissionType(transmitter, TransmissionType.ENERGY)).isPresent();
    }

    /**
     * Gets the adjacent connections to a TileEntity, from a subset of its sides.
     *
     * @param cableEntity - TileEntity that's trying to connect
     * @param side        - side to check
     *
     * @return boolean whether the acceptor is valid
     */
    public static boolean isValidAcceptorOnSide(TileEntity cableEntity, TileEntity tile, Direction side) {
        if (tile == null || isCable(tile)) {
            return false;
        }
        return isAcceptor(cableEntity, tile, side) || isOutputter(cableEntity, tile, side) ||
               (MekanismUtils.useForge() && CapabilityUtils.getCapability(tile, CapabilityEnergy.ENERGY, side.getOpposite()).isPresent());
    }

    /**
     * Gets all the connected cables around a specific tile entity.
     *
     * @param tile - center tile entity
     *
     * @return TileEntity[] of connected cables
     */
    public static TileEntity[] getConnectedOutputters(TileEntity tile) {
        return getConnectedOutputters(tile, tile.getPos(), tile.getWorld());
    }

    public static TileEntity[] getConnectedOutputters(BlockPos pos, World world) {
        return getConnectedOutputters(MekanismUtils.getTileEntity(world, pos), pos, world);
    }

    public static TileEntity[] getConnectedOutputters(TileEntity source, BlockPos pos, World world) {
        TileEntity[] outputters = new TileEntity[]{null, null, null, null, null, null};
        for (Direction orientation : EnumUtils.DIRECTIONS) {
            final TileEntity outputter = MekanismUtils.getTileEntity(world, pos.offset(orientation));
            if (isOutputter(source, outputter, orientation)) {
                outputters[orientation.ordinal()] = outputter;
            }
        }
        return outputters;
    }

    public static boolean isOutputter(TileEntity source, TileEntity tile, Direction side) {
        if (tile == null) {
            return false;
        }

        Direction opposite = side.getOpposite();
        Optional<IStrictEnergyOutputter> energyOutputter = MekanismUtils.toOptional(CapabilityUtils.getCapability(tile, Capabilities.ENERGY_OUTPUTTER_CAPABILITY, opposite));
        if (energyOutputter.isPresent() && energyOutputter.get().canOutputEnergy(opposite)) {
            return true;
        }
        if (MekanismUtils.useForge()) {
            Optional<IEnergyStorage> forgeEnergy = MekanismUtils.toOptional(CapabilityUtils.getCapability(tile, CapabilityEnergy.ENERGY, opposite));
            if (forgeEnergy.isPresent() && forgeEnergy.get().canExtract()) {
                return true;
            }
        }
        return false;
    }

    public static boolean isAcceptor(TileEntity source, TileEntity tile, Direction side) {
        Direction opposite = side.getOpposite();
        if (CapabilityUtils.getCapability(tile, Capabilities.GRID_TRANSMITTER_CAPABILITY, opposite).isPresent()) {
            return false;
        }
        Optional<IStrictEnergyAcceptor> energyAcceptor = MekanismUtils.toOptional(CapabilityUtils.getCapability(tile, Capabilities.ENERGY_ACCEPTOR_CAPABILITY, opposite));
        if (energyAcceptor.isPresent() && energyAcceptor.get().canReceiveEnergy(opposite)) {
            return true;
        }
        if (MekanismUtils.useForge()) {
            Optional<IEnergyStorage> forgeEnergy = MekanismUtils.toOptional(CapabilityUtils.getCapability(tile, CapabilityEnergy.ENERGY, opposite));
            if (forgeEnergy.isPresent() && forgeEnergy.get().canReceive()) {
                return true;
            }
        }
        return false;
    }

    public static void emit(IStrictEnergyHandler emitter) {
        TileEntity tileEntity = (TileEntity) emitter;
        if (!tileEntity.getWorld().isRemote() && MekanismUtils.canFunction(tileEntity)) {
            double energyToSend = Math.min(emitter.getEnergy(), emitter.getMaxOutput());
            if (energyToSend > 0) {
                BlockPos pos = tileEntity.getPos();
                //Fake that we have one target given we know that no sides will overlap
                // This allows us to have slightly better performance
                EnergyAcceptorTarget target = new EnergyAcceptorTarget();
                for (Direction side : EnumUtils.DIRECTIONS) {
                    if (emitter.canOutputEnergy(side)) {
                        TileEntity tile = MekanismUtils.getTileEntity(tileEntity.getWorld(), pos.offset(side));
                        //If it can accept energy or it is a cable
                        if (tile != null && (isValidAcceptorOnSide(tileEntity, tile, side) || isCable(tile))) {
                            //Get the opposite side as the current side is relative to us
                            Direction opposite = side.getOpposite();
                            EnergyAcceptorWrapper acceptor = EnergyAcceptorWrapper.get(tile, opposite);
                            if (acceptor != null && acceptor.canReceiveEnergy(opposite) && acceptor.needsEnergy(opposite)) {
                                target.addHandler(opposite, acceptor);
                            }
                        }
                    }
                }
                int curHandlers = target.getHandlers().size();
                if (curHandlers > 0) {
                    Set<EnergyAcceptorTarget> targets = new ObjectOpenHashSet<>();
                    targets.add(target);
                    double sent = EmitUtils.sendToAcceptors(targets, curHandlers, energyToSend);
                    if (emitter instanceof TileEntityInductionPort) {
                        //Streamline sideless removal method for induction port.
                        ((TileEntityInductionPort) emitter).removeEnergy(sent, false);
                    } else {
                        emitter.setEnergy(emitter.getEnergy() - sent);
                    }
                }
            }
        }
    }
}