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
    public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing) {
        return getCapability(capability, facing) != null;
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing) {
        if (capability == MjAPI.CAP_CONNECTOR) {
            return MjAPI.CAP_CONNECTOR.cast(connector);
        }
        if (capability == MjAPI.CAP_RECEIVER) {
            return MjAPI.CAP_RECEIVER.cast(receiver);
        }
        if (capability == MjAPI.CAP_REDSTONE_RECEIVER) {
            return MjAPI.CAP_REDSTONE_RECEIVER.cast(rsReceiver);
        }
        if (capability == MjAPI.CAP_READABLE) {
            return MjAPI.CAP_READABLE.cast(readable);
        }
        if (capability == MjAPI.CAP_PASSIVE_PROVIDER) {
            return MjAPI.CAP_PASSIVE_PROVIDER.cast(provider);
        }
        return null;
    }
}
