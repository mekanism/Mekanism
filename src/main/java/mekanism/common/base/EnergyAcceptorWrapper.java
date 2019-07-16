package mekanism.common.base;

import buildcraft.api.mj.IMjReceiver;
import cofh.redstoneflux.api.IEnergyReceiver;
import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergyTile;
import java.util.function.Function;
import mekanism.api.Coord4D;
import mekanism.api.energy.IStrictEnergyAcceptor;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.buildcraft.MjIntegration;
import mekanism.common.integration.forgeenergy.ForgeEnergyIntegration;
import mekanism.common.integration.tesla.TeslaIntegration;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.MekanismUtils;
import net.darkhax.tesla.api.ITeslaConsumer;
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
        } else if (MekanismUtils.useTesla() && CapabilityUtils.hasCapability(tileEntity, Capabilities.TESLA_CONSUMER_CAPABILITY, side)) {
            wrapper = fromCapability(tileEntity, Capabilities.TESLA_CONSUMER_CAPABILITY, side, TeslaAcceptor::new);
        } else if (MekanismUtils.useMj() && CapabilityUtils.hasCapability(tileEntity, Capabilities.MJ_RECEIVER_CAPABILITY, side)) {
            wrapper = fromCapability(tileEntity, Capabilities.MJ_RECEIVER_CAPABILITY, side, MjAcceptor::new);
        } else if (MekanismUtils.useForge() && CapabilityUtils.hasCapability(tileEntity, CapabilityEnergy.ENERGY, side)) {
            wrapper = fromCapability(tileEntity, CapabilityEnergy.ENERGY, side, ForgeAcceptor::new);
        } else if (MekanismUtils.useRF() && tileEntity instanceof IEnergyReceiver) {
            wrapper = new RFAcceptor((IEnergyReceiver) tileEntity);
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
    private static <T> EnergyAcceptorWrapper fromCapability(TileEntity tileEntity, Capability<T> capability,
          EnumFacing side, Function<T, EnergyAcceptorWrapper> makeAcceptor) {
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

    public static class RFAcceptor extends EnergyAcceptorWrapper {

        private IEnergyReceiver acceptor;

        public RFAcceptor(IEnergyReceiver rfAcceptor) {
            acceptor = rfAcceptor;
        }

        @Override
        public double acceptEnergy(EnumFacing side, double amount, boolean simulate) {
            return fromRF(acceptor.receiveEnergy(side, toRF(amount), simulate));
        }

        @Override
        public boolean canReceiveEnergy(EnumFacing side) {
            return acceptor.canConnectEnergy(side);
        }

        @Override
        public boolean needsEnergy(EnumFacing side) {
            return acceptor.receiveEnergy(side, 1, true) > 0;
        }

        public int toRF(double joules) {
            return MekanismUtils.clampToInt(joules * MekanismConfig.current().general.TO_RF.val());
        }

        public double fromRF(int rf) {
            return rf * MekanismConfig.current().general.FROM_RF.val();
        }
    }

    public static class IC2Acceptor extends EnergyAcceptorWrapper {

        private IEnergySink acceptor;

        public IC2Acceptor(IEnergySink ic2Acceptor) {
            acceptor = ic2Acceptor;
        }

        @Override
        public double acceptEnergy(EnumFacing side, double amount, boolean simulate) {
            double toTransfer = Math.min(acceptor.getDemandedEnergy(), toEU(amount));
            if (simulate) {
                //IC2 has no built in way to simulate, so we have to calculate it ourselves
                return fromEU(toTransfer);
            }
            double rejects = acceptor.injectEnergy(side, toTransfer, 0);
            return fromEU(toTransfer - rejects);
        }

        @Override
        public boolean canReceiveEnergy(EnumFacing side) {
            return acceptor.acceptsEnergyFrom(null, side);
        }

        @Override
        public boolean needsEnergy(EnumFacing side) {
            return acceptor.getDemandedEnergy() > 0;
        }

        public double toEU(double joules) {
            return joules * MekanismConfig.current().general.TO_IC2.val();
        }

        public double fromEU(double eu) {
            return eu * MekanismConfig.current().general.FROM_IC2.val();
        }
    }

    public static class TeslaAcceptor extends EnergyAcceptorWrapper {

        private ITeslaConsumer acceptor;

        public TeslaAcceptor(ITeslaConsumer teslaConsumer) {
            acceptor = teslaConsumer;
        }

        @Override
        public double acceptEnergy(EnumFacing side, double amount, boolean simulate) {
            return TeslaIntegration.fromTesla(acceptor.givePower(TeslaIntegration.toTesla(amount), simulate));
        }

        @Override
        public boolean canReceiveEnergy(EnumFacing side) {
            return acceptor.givePower(1, true) > 0;
        }

        @Override
        public boolean needsEnergy(EnumFacing side) {
            return canReceiveEnergy(side);
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
            return acceptor.canReceive();
        }
    }

    public static class MjAcceptor extends EnergyAcceptorWrapper {

        private IMjReceiver receiver;

        public MjAcceptor(IMjReceiver receiver) {
            this.receiver = receiver;
        }

        @Override
        public boolean needsEnergy(EnumFacing side) {
            return receiver.getPowerRequested() > 0;
        }

        @Override
        public double acceptEnergy(EnumFacing side, double amount, boolean simulate) {
            return MjIntegration.fromMj(receiver.receivePower(MjIntegration.toMj(amount), simulate));
        }

        @Override
        public boolean canReceiveEnergy(EnumFacing side) {
            return receiver.canReceive();
        }
    }
}