package mekanism.common.tile.transmitter;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.api.IAlloyInteraction;
import mekanism.api.transmitters.DynamicNetwork;
import mekanism.api.transmitters.DynamicNetwork.NetworkClientRequest;
import mekanism.api.transmitters.IGridTransmitter;
import mekanism.api.transmitters.TransmitterNetworkRegistry;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.transmitters.TransmitterImpl;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;

public abstract class TileEntityTransmitter<A, N extends DynamicNetwork<A, N>> extends TileEntitySidedPipe implements
      IAlloyInteraction {

    public TransmitterImpl<A, N> transmitterDelegate;

    public boolean unloaded = true;

    public boolean dataRequest = false;

    private N lastClientNetwork = null;

    public TileEntityTransmitter() {
        transmitterDelegate = new TransmitterImpl<>(this);
    }

    public TransmitterImpl<A, N> getTransmitter() {
        return transmitterDelegate;
    }

    public abstract N createNewNetwork();

    public abstract N createNetworkByMerging(Collection<N> networks);

    @Override
    public void onWorldJoin() {
        if (!getWorld().isRemote) {
            TransmitterNetworkRegistry.registerOrphanTransmitter(getTransmitter());
        } else if (lastClientNetwork != null) {
            getTransmitter().setTransmitterNetwork(lastClientNetwork);
        }

        unloaded = false;
    }

    @Override
    public void update() {
        super.update();

        if (getWorld().isRemote) {
            if (!dataRequest) {
                dataRequest = true;
                MinecraftForge.EVENT_BUS.post(new NetworkClientRequest(getWorld().getTileEntity(getPos())));
            }
        }
    }

    @Override
    public void onChunkUnload() {
        if (!getWorld().isRemote) {
            getTransmitter().takeShare();
        }

        super.onChunkUnload();
    }

    @Override
    public void onWorldSeparate() {
        unloaded = true;

        if (!getWorld().isRemote) {
            TransmitterNetworkRegistry.invalidateTransmitter(getTransmitter());
        } else {
            lastClientNetwork = getTransmitter().getTransmitterNetwork();
            getTransmitter().setTransmitterNetwork(null);
        }
    }

    @Override
    public void markDirtyTransmitters() {
        super.markDirtyTransmitters();

        if (getTransmitter().hasTransmitterNetwork()) {
            TransmitterNetworkRegistry.invalidateTransmitter(getTransmitter());
        }
    }

    @Override
    public void markDirtyAcceptor(EnumFacing side) {
        super.markDirtyAcceptor(side);

        if (getTransmitter().hasTransmitterNetwork()) {
            getTransmitter().getTransmitterNetwork().acceptorChanged(getTransmitter(), side);
        }
    }

    public abstract A getCachedAcceptor(EnumFacing side);

    protected TileEntity getCachedTile(EnumFacing side) {
        ConnectionType type = connectionTypes[side.ordinal()];

        if (type == ConnectionType.PULL || type == ConnectionType.NONE) {
            return null;
        }

        return connectionMapContainsSide(currentAcceptorConnections, side) ? cachedAcceptors[side.ordinal()] : null;
    }

    @Override
    public void onAlloyInteraction(EntityPlayer player, EnumHand hand, ItemStack stack, int tierOrdinal) {
        if (getTransmitter().hasTransmitterNetwork()) {
            int upgraded = 0;
            Object[] array = ((LinkedHashSet) getTransmitter().getTransmitterNetwork().transmitters.clone()).toArray();

            Arrays.sort(array, (o1, o2) ->
            {
                if (o1 instanceof IGridTransmitter && o2 instanceof IGridTransmitter) {
                    Coord4D thisCoord = new Coord4D(getPos(), getWorld());

                    Coord4D o1Coord = ((IGridTransmitter) o1).coord();
                    Coord4D o2Coord = ((IGridTransmitter) o2).coord();

                    return Integer.compare(o1Coord.distanceTo(thisCoord), o2Coord.distanceTo(thisCoord));
                }

                return 0;
            });

            for (Object iter : array) {
                if (iter instanceof TransmitterImpl) {
                    TileEntityTransmitter t = ((TransmitterImpl) iter).containingTile;

                    if (t.upgrade(tierOrdinal)) {
                        upgraded++;

                        if (upgraded == 8) {
                            break;
                        }
                    }
                }
            }

            if (upgraded > 0) {
                if (!player.capabilities.isCreativeMode) {
                    stack.shrink(1);

                    if (stack.getCount() == 0) {
                        player.setHeldItem(hand, ItemStack.EMPTY);
                    }
                }
            }
        }
    }

    public boolean upgrade(int tierOrdinal) {
        return false;
    }

    public abstract int getCapacity();

    public abstract Object getBuffer();

    public abstract void takeShare();

    public abstract void updateShare();

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing side) {
        return capability == Capabilities.GRID_TRANSMITTER_CAPABILITY
              || capability == Capabilities.ALLOY_INTERACTION_CAPABILITY
              || super.hasCapability(capability, side);
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing side) {
        if (capability == Capabilities.GRID_TRANSMITTER_CAPABILITY) {
            return Capabilities.GRID_TRANSMITTER_CAPABILITY.cast(getTransmitter());
        } else if (capability == Capabilities.ALLOY_INTERACTION_CAPABILITY) {
            return Capabilities.ALLOY_INTERACTION_CAPABILITY.cast(this);
        }

        return super.getCapability(capability, side);
    }
}
