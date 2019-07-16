package mekanism.common.integration.buildcraft;

import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.IMjPassiveProvider;
import buildcraft.api.mj.IMjReadable;
import buildcraft.api.mj.IMjReceiver;
import javax.annotation.Nonnull;
import mekanism.api.energy.IEnergizedItem;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.ItemCapabilityWrapper.ItemCapability;
import mekanism.common.integration.MekanismHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.Optional.Interface;
import net.minecraftforge.fml.common.Optional.InterfaceList;
import net.minecraftforge.fml.common.Optional.Method;

@InterfaceList({
      @Interface(iface = "buildcraft.api.mj.IMjPassiveProvider", modid = MekanismHooks.BUILDCRAFT_MOD_ID),
      @Interface(iface = "buildcraft.api.mj.IMjReceiver", modid = MekanismHooks.BUILDCRAFT_MOD_ID),
      @Interface(iface = "buildcraft.api.mj.IMjReadable", modid = MekanismHooks.BUILDCRAFT_MOD_ID)
})
public class MjItemWrapper extends ItemCapability implements IMjReadable, IMjPassiveProvider, IMjReceiver {

    //TODO: Figure out if this class is even needed. I am not sure BuildCraft supports items having "inventory power"

    @Override
    public boolean canProcess(Capability<?> capability) {
        return capability == Capabilities.MJ_READABLE_CAPABILITY || capability == Capabilities.MJ_PROVIDER_CAPABILITY && getItem().canReceive(getStack()) ||
               capability == Capabilities.MJ_RECEIVER_CAPABILITY && getItem().canSend(getStack());
    }

    public IEnergizedItem getItem() {
        return (IEnergizedItem) getStack().getItem();
    }

    @Override
    @Method(modid = MekanismHooks.BUILDCRAFT_MOD_ID)
    public long extractPower(long min, long max, boolean simulate) {
        if (getItem().canSend(getStack())) {
            long energyRemaining = getStored();
            if (energyRemaining < min) {
                return 0;
            }
            long toSend = Math.min(max, energyRemaining);
            if (!simulate) {
                getItem().setEnergy(getStack(), getItem().getEnergy(getStack()) - MjIntegration.fromMj(toSend));
            }
            return toSend;
        }
        return 0;
    }

    @Override
    @Method(modid = MekanismHooks.BUILDCRAFT_MOD_ID)
    public long getPowerRequested() {
        return MjIntegration.toMj(getItem().getMaxEnergy(getStack()) - getItem().getEnergy(getStack()));
    }

    @Override
    @Method(modid = MekanismHooks.BUILDCRAFT_MOD_ID)
    public long receivePower(long microJoules, boolean simulate) {
        if (getItem().canReceive(getStack())) {
            long energyNeeded = getCapacity() - getStored();
            long toReceive = Math.min(microJoules, energyNeeded);
            if (!simulate) {
                getItem().setEnergy(getStack(), getItem().getEnergy(getStack()) + MjIntegration.fromMj(toReceive));
            }
            return toReceive;
        }
        return 0;
    }

    @Override
    @Method(modid = MekanismHooks.BUILDCRAFT_MOD_ID)
    public boolean canConnect(@Nonnull IMjConnector other) {
        return false;
    }

    @Override
    @Method(modid = MekanismHooks.BUILDCRAFT_MOD_ID)
    public long getStored() {
        return MjIntegration.toMj(getItem().getEnergy(getStack()));
    }

    @Override
    @Method(modid = MekanismHooks.BUILDCRAFT_MOD_ID)
    public long getCapacity() {
        return MjIntegration.toMj(getItem().getMaxEnergy(getStack()));
    }
}