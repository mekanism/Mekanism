package mekanism.common.lib.transmitter;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.common.integration.energy.EnergyCompatUtils;
import mekanism.common.integration.energy.IEnergyCompat;
import mekanism.common.integration.energy.StrictEnergyCompat;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.EmitUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullConsumer;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AcceptorCache<ACCEPTOR> {

    private final Map<Direction, NonNullConsumer<LazyOptional<ACCEPTOR>>> cachedListeners = new EnumMap<>(Direction.class);
    private final Map<Direction, AcceptorInfo<ACCEPTOR>> cachedAcceptors = new EnumMap<>(Direction.class);
    private final TileEntityTransmitter<ACCEPTOR, ?, ?> transmitter;
    //TODO: Move this to being private?
    public byte currentAcceptorConnections = 0x00;

    public AcceptorCache(TileEntityTransmitter<ACCEPTOR, ?, ?> transmitter) {
        this.transmitter = transmitter;
    }

    public void clear() {
        cachedListeners.clear();
        cachedAcceptors.clear();
    }

    public void invalidateCachedAcceptor(Direction side) {
        if (cachedAcceptors.containsKey(side)) {
            //Fully remove the cached acceptor, both the tile, and the value
            cachedAcceptors.remove(side);
            transmitter.markDirtyAcceptor(side);
            //Note: as our listeners don't care about the actual implementation of the tile on a given side
            // we don't bother invalidating and getting rid of the old listener when we remove the cached
            // acceptor as we don't want to accidentally flood the acceptor with listeners if something
            // breaks on our end.
        }
    }

    private void updateCachedAcceptorAndListen(Direction side, TileEntity acceptorTile, LazyOptional<ACCEPTOR> acceptor, boolean addListener) {
        boolean dirtyAcceptor = false;
        if (cachedAcceptors.containsKey(side)) {
            AcceptorInfo<ACCEPTOR> acceptorInfo = cachedAcceptors.get(side);
            if (acceptorTile != acceptorInfo.tile) {
                //The tile changed, fully invalidate it
                cachedAcceptors.put(side, new AcceptorInfo<>(acceptorTile, acceptor));
                dirtyAcceptor = true;
            } else if (acceptor != acceptorInfo.acceptor) {
                //The acceptor is different, make sure we update it
                acceptorInfo.updateAcceptor(acceptor);
                dirtyAcceptor = true;
            }
        } else {
            cachedAcceptors.put(side, new AcceptorInfo<>(acceptorTile, acceptor));
            dirtyAcceptor = true;
        }
        if (dirtyAcceptor) {
            transmitter.markDirtyAcceptor(side);
            if (addListener) {
                //If the capability is present and we want to add the listener, add a listener so that once it gets invalidated
                // we recheck that side assuming that the world and position is still loaded and our tile has not been removed
                acceptor.addListener(getRefreshListener(side));
            }
        }
    }

    /**
     * @implNote Gets the acceptor from the cached tile rather than the cached acceptor
     */
    public LazyOptional<ACCEPTOR> getCachedAcceptor(Capability<ACCEPTOR> capability, Direction side) {
        //TODO: Should this query the cached acceptor instead?
        return CapabilityUtils.getCapability(getCachedAcceptorTile(side), capability, side.getOpposite());
    }

    @Nullable
    public TileEntity getCachedAcceptorTile(Direction side) {
        //TODO: Do we need to also invalidate if the connection map doesn't contain it
        if (TileEntityTransmitter.connectionMapContainsSide(currentAcceptorConnections, side)) {
            if (cachedAcceptors.containsKey(side)) {
                return cachedAcceptors.get(side).tile;
            }
        }
        return null;
    }

    /**
     * @apiNote Only call this from the server side
     */
    public boolean hasStrictEnergyHandlerAndListen(@Nullable TileEntity tile, Direction side) {
        if (tile != null && !tile.isRemoved() && tile.hasWorld()) {
            Direction opposite = side.getOpposite();
            for (IEnergyCompat energyCompat : EnergyCompatUtils.getCompats()) {
                if (energyCompat.isUsable()) {
                    //Note: Capability should not be null due to us validating the compat is usable
                    LazyOptional<?> acceptor = CapabilityUtils.getCapability(tile, energyCompat.getCapability(), opposite);
                    if (acceptor.isPresent()) {
                        if (energyCompat instanceof StrictEnergyCompat) {
                            //Our lazy optional is already the proper type
                            updateCachedAcceptorAndListen(side, tile, (LazyOptional<ACCEPTOR>) acceptor, true);
                        } else {
                            //Update the cache with the strict energy lazy optional as that is the one we interact with
                            LazyOptional<IStrictEnergyHandler> wrappedAcceptor = energyCompat.getLazyStrictEnergyHandler(tile, opposite);
                            //Note: The wrapped acceptor should always be present, but double check just in case
                            if (wrappedAcceptor.isPresent()) {
                                //If the capability is present add a listener to the actual lazy optional so that once it gets invalidated we recheck that side
                                CapabilityUtils.addListener(acceptor, getRefreshListener(side));
                                //TODO: The acceptor will be different due to how it maps them, we should compare the acceptor
                                // with the acceptor info for the "true" lazy optional instead of the wrapped one
                                updateCachedAcceptorAndListen(side, tile, (LazyOptional<ACCEPTOR>) wrappedAcceptor, false);
                            }
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * @apiNote Only call this from the server side
     */
    public boolean isAcceptorAndListen(@Nullable TileEntity tile, Direction side, Capability<ACCEPTOR> capability) {
        LazyOptional<ACCEPTOR> acceptor = CapabilityUtils.getCapability(tile, capability, side.getOpposite());
        if (acceptor.isPresent()) {
            //Update the cached acceptor and if it changed, add a listener to it to listen for invalidation
            updateCachedAcceptorAndListen(side, tile, acceptor, true);
            return true;
        }
        return false;
    }

    /**
     * Similar to {@link EmitUtils#forEachSide(World, BlockPos, Iterable, BiConsumer)} except queries our cached acceptors.
     *
     * @implNote Grabs the acceptors from cache
     */
    public List<ACCEPTOR> getConnectedAcceptors(Set<Direction> sides) {
        List<ACCEPTOR> acceptors = new ArrayList<>(sides.size());
        for (Direction side : sides) {
            getConnectedAcceptor(side).ifPresent(acceptors::add);
        }
        return acceptors;
    }

    /**
     * @implNote Grabs the acceptors from cache
     */
    public LazyOptional<ACCEPTOR> getConnectedAcceptor(Direction side) {
        if (cachedAcceptors.containsKey(side)) {
            AcceptorInfo<ACCEPTOR> acceptorInfo = cachedAcceptors.get(side);
            if (!acceptorInfo.tile.isRemoved()) {
                return acceptorInfo.acceptor;
            }
            //TODO: If the tile has been removed should we force an invalidation/recheck?
        }
        return LazyOptional.empty();
    }

    /**
     * Gets the listener that will refresh connections on a given side.
     */
    private NonNullConsumer<LazyOptional<ACCEPTOR>> getRefreshListener(Direction side) {
        return cachedListeners.computeIfAbsent(side, this::getUncachedRefreshListener);
    }

    private NonNullConsumer<LazyOptional<ACCEPTOR>> getUncachedRefreshListener(Direction side) {
        return ignored -> {
            //Check to make sure the transmitter is still valid and that the position we are going to check is actually still loaded
            if (!transmitter.isRemoved() && transmitter.hasWorld() && transmitter.getWorld().isBlockPresent(transmitter.getPos().offset(side))) {
                //If it is, then refresh the connection
                transmitter.refreshConnections(side);
            }
        };
    }

    private static class AcceptorInfo<ACCEPTOR> {

        private final TileEntity tile;
        private LazyOptional<ACCEPTOR> acceptor;

        private AcceptorInfo(TileEntity tile, LazyOptional<ACCEPTOR> acceptor) {
            this.tile = tile;
            this.acceptor = acceptor;
        }

        private void updateAcceptor(LazyOptional<ACCEPTOR> acceptor) {
            this.acceptor = acceptor;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (o instanceof AcceptorInfo) {
                AcceptorInfo<?> other = (AcceptorInfo<?>) o;
                return tile.equals(other.tile) && acceptor.equals(other.acceptor);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(tile, acceptor);
        }
    }
}