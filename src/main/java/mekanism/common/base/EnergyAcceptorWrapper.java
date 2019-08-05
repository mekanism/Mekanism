package mekanism.common.base;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergyTile;
import java.util.function.Function;
import mekanism.api.Coord4D;
import mekanism.api.energy.IStrictEnergyAcceptor;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.integration.forgeenergy.ForgeEnergyIntegration;
import mekanism.common.integration.ic2.IC2Integration;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class EnergyAcceptorWrapper implements IStrictEnergyAcceptor {

    private static final Logger LOGGER = LogManager.getLogger("Mekanism EnergyAcceptorWrapper");
    public Coord4D coord;

    public static EnergyAcceptorWrapper get(TileEntity tileEntity, EnumFacing side) {
        if (tileEntity == null || tileEntity.getWorld() == null) {
            return null;
        }
        EnergyAcceptorWrapper wrapper = null;
        if (CapabilityUtils.hasCapability(tileEntity, Capabilities.ENERGY_ACCEPTOR_CAPABILITY, side)) {
            wrapper = fromCapability(tileEntity, Capabilities.ENERGY_ACCEPTOR_CAPABILITY, side, MekanismAcceptor::new);
        } else if (MekanismUtils.useForge() && CapabilityUtils.hasCapability(tileEntity, CapabilityEnergy.ENERGY, side)) {
            wrapper = fromCapability(tileEntity, CapabilityEnergy.ENERGY, side, ForgeAcceptor::new);
        } else if (MekanismUtils.useIC2()) {
            IEnergyTile tile = EnergyNet.instance.getSubTile(tileEntity.getWorld(), tileEntity.getPos());
            if (tile instanceof IEnergySink) {
                wrapper = new IC2Acceptor((IEnergySink) tile);
            }
        }
        if (wrapper != null) {
            wrapper.coord = Coord4D.get(tileEntity);
        }
        return wrapper;
    }

    /**
     * Note: It is assumed that a check for hasCapability was already ran.
     */
    private static <T> EnergyAcceptorWrapper fromCapability(TileEntity tileEntity, Capability<T> capability, EnumFacing side, Function<T, EnergyAcceptorWrapper> makeAcceptor) {
        T acceptor = CapabilityUtils.getCapability(tileEntity, capability, side);
        if (acceptor != null) {
            return makeAcceptor.apply(acceptor);
        } else {
            LOGGER.error("Tile {} @ {} told us it had {} cap but returned null", tileEntity, tileEntity.getPos(), capability.getName());
        }
        return null;
    }

    public abstract boolean needsEnergy(EnumFacing side);

    public static class MekanismAcceptor extends EnergyAcceptorWrapper {

        private IStrictEnergyAcceptor acceptor;

        public MekanismAcceptor(IStrictEnergyAcceptor mekAcceptor) {
            acceptor = mekAcceptor;
        }

        @Override
        public double acceptEnergy(EnumFacing side, double amount, boolean simulate) {
            return acceptor.acceptEnergy(side, amount, simulate);
        }

        @Override
        public boolean canReceiveEnergy(EnumFacing side) {
            return acceptor.canReceiveEnergy(side);
        }

        @Override
        public boolean needsEnergy(EnumFacing side) {
            return acceptor.acceptEnergy(side, 1, true) > 0;
        }
    }

    public static class IC2Acceptor extends EnergyAcceptorWrapper {

        private IEnergySink acceptor;

        public IC2Acceptor(IEnergySink ic2Acceptor) {
            acceptor = ic2Acceptor;
        }

        @Override
        public double acceptEnergy(EnumFacing side, double amount, boolean simulate) {
            double toTransfer = Math.min(acceptor.getDemandedEnergy(), IC2Integration.toEU(amount));
            if (simulate) {
                //IC2 has no built in way to simulate, so we have to calculate it ourselves
                return IC2Integration.fromEU(toTransfer);
            }
            double rejects = acceptor.injectEnergy(side, toTransfer, 0);
            return IC2Integration.fromEU(toTransfer - rejects);
        }

        @Override
        public boolean canReceiveEnergy(EnumFacing side) {
            return acceptor.acceptsEnergyFrom(null, side);
        }

        @Override
        public boolean needsEnergy(EnumFacing side) {
            return acceptor.getDemandedEnergy() > 0;
        }
    }

    public static class ForgeAcceptor extends EnergyAcceptorWrapper {

        private IEnergyStorage acceptor;

        public ForgeAcceptor(IEnergyStorage forgeConsumer) {
            acceptor = forgeConsumer;
        }

        @Override
        public double acceptEnergy(EnumFacing side, double amount, boolean simulate) {
            return ForgeEnergyIntegration.fromForge(acceptor.receiveEnergy(ForgeEnergyIntegration.toForge(amount), simulate));
        }

        @Override
        public boolean canReceiveEnergy(EnumFacing side) {
            return acceptor.canReceive();
        }

        @Override
        public boolean needsEnergy(EnumFacing side) {
            return acceptor.receiveEnergy(1, true) > 0;
        }
    }
}