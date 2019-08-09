package mekanism.common.base;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergyTile;
import java.util.function.Function;
import java.util.function.Supplier;
import mekanism.api.Coord4D;
import mekanism.api.energy.IStrictEnergyAcceptor;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.integration.forgeenergy.ForgeEnergyIntegration;
import mekanism.common.integration.ic2.IC2Integration;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

public abstract class EnergyAcceptorWrapper implements IStrictEnergyAcceptor {

    private static final Supplier<EnergyAcceptorWrapper> IC2_WRAPPER = () -> {
        if (MekanismUtils.useIC2()) {
            IEnergyTile tile = EnergyNet.instance.getSubTile(tileEntity.getWorld(), tileEntity.getPos());
            if (tile instanceof IEnergySink) {
                return new IC2Acceptor((IEnergySink) tile);
            }
        }
        return null;
    };

    public Coord4D coord;

    public static EnergyAcceptorWrapper get(TileEntity tileEntity, Direction side) {
        if (tileEntity == null || tileEntity.getWorld() == null) {
            return null;
        }
        EnergyAcceptorWrapper wrapper = CapabilityUtils.getCapabilityHelper(tileEntity, Capabilities.ENERGY_ACCEPTOR_CAPABILITY, side).getIfPresentElseDo(
              MekanismAcceptor::new,
              () -> {
                  if (MekanismUtils.useForge()) {
                      return CapabilityUtils.getCapabilityHelper(tileEntity, CapabilityEnergy.ENERGY, side).getIfPresentElseDo(
                            ForgeAcceptor::new,
                            IC2_WRAPPER
                      );
                  }
                  return IC2_WRAPPER.get();
              }
        );
        if (wrapper != null) {
            wrapper.coord = Coord4D.get(tileEntity);
        }
        return wrapper;
    }

    /**
     * Note: It is assumed that a check for hasCapability was already ran.
     */
    private static <T> EnergyAcceptorWrapper fromCapability(TileEntity tileEntity, Capability<T> capability, Direction side, Function<T, EnergyAcceptorWrapper> makeAcceptor) {
        return CapabilityUtils.getCapabilityHelper(tileEntity, capability, side).getIfPresent(makeAcceptor);
    }

    public abstract boolean needsEnergy(Direction side);

    public static class MekanismAcceptor extends EnergyAcceptorWrapper {

        private IStrictEnergyAcceptor acceptor;

        public MekanismAcceptor(IStrictEnergyAcceptor mekAcceptor) {
            acceptor = mekAcceptor;
        }

        @Override
        public double acceptEnergy(Direction side, double amount, boolean simulate) {
            return acceptor.acceptEnergy(side, amount, simulate);
        }

        @Override
        public boolean canReceiveEnergy(Direction side) {
            return acceptor.canReceiveEnergy(side);
        }

        @Override
        public boolean needsEnergy(Direction side) {
            return acceptor.acceptEnergy(side, 1, true) > 0;
        }
    }

    public static class IC2Acceptor extends EnergyAcceptorWrapper {

        private IEnergySink acceptor;

        public IC2Acceptor(IEnergySink ic2Acceptor) {
            acceptor = ic2Acceptor;
        }

        @Override
        public double acceptEnergy(Direction side, double amount, boolean simulate) {
            double toTransfer = Math.min(acceptor.getDemandedEnergy(), IC2Integration.toEU(amount));
            if (simulate) {
                //IC2 has no built in way to simulate, so we have to calculate it ourselves
                return IC2Integration.fromEU(toTransfer);
            }
            double rejects = acceptor.injectEnergy(side, toTransfer, 0);
            return IC2Integration.fromEU(toTransfer - rejects);
        }

        @Override
        public boolean canReceiveEnergy(Direction side) {
            return acceptor.acceptsEnergyFrom(null, side);
        }

        @Override
        public boolean needsEnergy(Direction side) {
            return acceptor.getDemandedEnergy() > 0;
        }
    }

    public static class ForgeAcceptor extends EnergyAcceptorWrapper {

        private IEnergyStorage acceptor;

        public ForgeAcceptor(IEnergyStorage forgeConsumer) {
            acceptor = forgeConsumer;
        }

        @Override
        public double acceptEnergy(Direction side, double amount, boolean simulate) {
            return ForgeEnergyIntegration.fromForge(acceptor.receiveEnergy(ForgeEnergyIntegration.toForge(amount), simulate));
        }

        @Override
        public boolean canReceiveEnergy(Direction side) {
            return acceptor.canReceive();
        }

        @Override
        public boolean needsEnergy(Direction side) {
            return acceptor.receiveEnergy(1, true) > 0;
        }
    }
}