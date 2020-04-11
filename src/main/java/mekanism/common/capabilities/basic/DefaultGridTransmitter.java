package mekanism.common.capabilities.basic;

import java.util.Collection;
import java.util.UUID;
import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.api.math.FloatingLong;
import mekanism.api.transmitters.DynamicNetwork;
import mekanism.api.transmitters.IGridTransmitter;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.capabilities.basic.DefaultStorageHelper.NullStorage;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.CapabilityManager;

/**
 * Created by ben on 03/05/16.
 */
public class DefaultGridTransmitter<A, N extends DynamicNetwork<A, N, BUFFER>, BUFFER> implements IGridTransmitter<A, N, BUFFER> {

    public static void register() {
        CapabilityManager.INSTANCE.register(IGridTransmitter.class, new NullStorage<>(), DefaultGridTransmitter::new);
    }

    @Override
    public boolean hasTransmitterNetwork() {
        return false;
    }

    @Override
    public N getTransmitterNetwork() {
        return null;
    }

    @Override
    public void setTransmitterNetwork(N network) {
    }

    @Override
    public int getTransmitterNetworkSize() {
        return 0;
    }

    @Override
    public int getTransmitterNetworkAcceptorSize() {
        return 0;
    }

    @Override
    public ITextComponent getTransmitterNetworkNeeded() {
        return null;
    }

    @Override
    public ITextComponent getTransmitterNetworkFlow() {
        return null;
    }

    @Override
    public ITextComponent getTransmitterNetworkBuffer() {
        return null;
    }

    @Override
    public long getTransmitterNetworkCapacity() {
        return 0;
    }

    @Nonnull
    @Override
    public FloatingLong getCapacityAsFloatingLong() {
        return FloatingLong.ZERO;
    }

    @Override
    public long getCapacity() {
        return 0;
    }

    @Override
    public World world() {
        return null;
    }

    @Override
    public Coord4D coord() {
        return null;
    }

    @Override
    public Coord4D getAdjacentConnectableTransmitterCoord(Direction side) {
        return null;
    }

    @Override
    public A getAcceptor(Direction side) {
        return null;
    }

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public boolean isOrphan() {
        return false;
    }

    @Override
    public void setOrphan(boolean orphaned) {
    }

    @Override
    public N createEmptyNetwork() {
        return null;
    }

    @Override
    public N createEmptyNetworkWithID(UUID networkID) {
        return null;
    }

    @Override
    public N mergeNetworks(Collection<N> toMerge) {
        return null;
    }

    @Override
    public N getExternalNetwork(Coord4D from) {
        return null;
    }

    @Override
    public void takeShare() {
    }

    @Override
    public BUFFER getBuffer() {
        return null;
    }

    @Override
    public void setRequestsUpdate() {
    }

    @Override
    public TransmissionType getTransmissionType() {
        return null;
    }

    @Override
    public boolean isCompatibleWith(IGridTransmitter<A, N, BUFFER> other) {
        return false;
    }
}