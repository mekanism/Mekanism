package buildcraft.api.mj;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.util.EnumFacing;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

/** Provides a quick way to return all types of a single {@link IMjConnector} for all the different capabilities. */
public class MjCapabilityHelper implements ICapabilityProvider {

    @Nonnull
    private final IMjConnector connector;

    @Nullable
    private final IMjReceiver receiver;

    @Nullable
    private final IMjRedstoneReceiver rsReceiver;

    @Nullable
    private final IMjReadable readable;

    @Nullable
    private final IMjPassiveProvider provider;

    public MjCapabilityHelper(@Nonnull IMjConnector mj) {
        this.connector = mj;
        this.receiver = mj instanceof IMjReceiver ? (IMjReceiver) mj : null;
        this.rsReceiver = mj instanceof IMjRedstoneReceiver ? (IMjRedstoneReceiver) mj : null;
        this.readable = mj instanceof IMjReadable ? (IMjReadable) mj : null;
        this.provider = mj instanceof IMjPassiveProvider ? (IMjPassiveProvider) mj : null;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == MjAPI.CAP_CONNECTOR) return true;
        if (capability == MjAPI.CAP_RECEIVER) return receiver != null;
        if (capability == MjAPI.CAP_REDSTONE_RECEIVER) return rsReceiver != null;
        if (capability == MjAPI.CAP_READABLE) return readable != null;
        if (capability == MjAPI.CAP_PASSIVE_PROVIDER) return provider != null;
        return false;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == MjAPI.CAP_CONNECTOR) return (T) connector;
        if (capability == MjAPI.CAP_RECEIVER) return (T) receiver;
        if (capability == MjAPI.CAP_REDSTONE_RECEIVER) return (T) rsReceiver;
        if (capability == MjAPI.CAP_READABLE) return (T) readable;
        if (capability == MjAPI.CAP_PASSIVE_PROVIDER) return (T) provider;
        return null;
    }

}
