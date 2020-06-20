package mekanism.common.lib.transmitter.acceptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.chemical.infuse.IInfusionHandler;
import mekanism.api.chemical.pigment.IPigmentHandler;
import mekanism.api.chemical.slurry.ISlurryHandler;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.chemical.BoxedChemicalHandler;
import mekanism.common.content.network.transmitter.BoxedPressurizedTube;
import mekanism.common.lib.transmitter.acceptor.BoxedChemicalAcceptorCache.BoxedChemicalAcceptorInfo;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.util.CapabilityUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.util.LazyOptional;

//TODO - V11: Improve this so it only invalidates the types needed instead of doing all chemical types at once
@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BoxedChemicalAcceptorCache extends AbstractAcceptorCache<BoxedChemicalHandler, BoxedChemicalAcceptorInfo> {

    public BoxedChemicalAcceptorCache(BoxedPressurizedTube transmitter, TileEntityTransmitter transmitterTile) {
        super(transmitter, transmitterTile);
    }

    private void updateCachedAcceptorAndListen(Direction side, TileEntity acceptorTile, BoxedChemicalHandler acceptor) {
        boolean dirtyAcceptor = false;
        if (cachedAcceptors.containsKey(side)) {
            BoxedChemicalAcceptorInfo acceptorInfo = cachedAcceptors.get(side);
            if (acceptorTile != acceptorInfo.getTile()) {
                //The tile changed, fully invalidate it
                cachedAcceptors.put(side, new BoxedChemicalAcceptorInfo(acceptorTile, acceptor));
                dirtyAcceptor = true;
            } else if (!acceptor.sameHandlers(acceptorInfo.boxedHandler)) {
                //The source acceptor is different, make sure we update it and the actual acceptor
                // This allows us to make sure we only mark the acceptor as dirty if it actually changed
                // Use case: Wrapped energy acceptors
                acceptorInfo.updateAcceptor(acceptor);
                dirtyAcceptor = true;
            }
        } else {
            cachedAcceptors.put(side, new BoxedChemicalAcceptorInfo(acceptorTile, acceptor));
            dirtyAcceptor = true;
        }
        if (dirtyAcceptor) {
            transmitter.markDirtyAcceptor(side);
            //If the capability is present and we want to add the listener, add a listener to all the types so that once it gets invalidated
            // we recheck that side assuming that the world and position is still loaded and our tile has not been removed
            acceptor.addRefreshListeners(getRefreshListener(side));
        }
    }

    public boolean isChemicalAcceptorAndListen(@Nullable TileEntity tile, Direction side) {
        //TODO: Improve this to make it easier to add more chemical types
        Direction opposite = side.getOpposite();
        LazyOptional<IGasHandler> gasAcceptor = CapabilityUtils.getCapability(tile, Capabilities.GAS_HANDLER_CAPABILITY, opposite);
        LazyOptional<IInfusionHandler> infusionAcceptor = CapabilityUtils.getCapability(tile, Capabilities.INFUSION_HANDLER_CAPABILITY, opposite);
        LazyOptional<IPigmentHandler> pigmentAcceptor = CapabilityUtils.getCapability(tile, Capabilities.PIGMENT_HANDLER_CAPABILITY, opposite);
        LazyOptional<ISlurryHandler> slurryAcceptor = CapabilityUtils.getCapability(tile, Capabilities.SLURRY_HANDLER_CAPABILITY, opposite);
        if (gasAcceptor.isPresent() || infusionAcceptor.isPresent() || pigmentAcceptor.isPresent() || slurryAcceptor.isPresent()) {
            BoxedChemicalHandler chemicalHandler = new BoxedChemicalHandler();
            if (gasAcceptor.isPresent()) {
                chemicalHandler.addGasHandler(gasAcceptor);
            }
            if (infusionAcceptor.isPresent()) {
                chemicalHandler.addInfusionHandler(infusionAcceptor);
            }
            if (pigmentAcceptor.isPresent()) {
                chemicalHandler.addPigmentHandler(pigmentAcceptor);
            }
            if (slurryAcceptor.isPresent()) {
                chemicalHandler.addSlurryHandler(slurryAcceptor);
            }
            //Update the cached acceptor and if it changed, add a listener to it to listen for invalidation
            updateCachedAcceptorAndListen(side, tile, chemicalHandler);
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @implNote We override this method to skip wrapping it in a lazy optional before resolving them
     */
    @Override
    public List<BoxedChemicalHandler> getConnectedAcceptors(Set<Direction> sides) {
        List<BoxedChemicalHandler> acceptors = new ArrayList<>(sides.size());
        for (Direction side : sides) {
            if (cachedAcceptors.containsKey(side)) {
                BoxedChemicalAcceptorInfo acceptorInfo = cachedAcceptors.get(side);
                if (!acceptorInfo.getTile().isRemoved()) {
                    acceptors.add(acceptorInfo.boxedHandler);
                }
            }
        }
        return acceptors;
    }

    @Override
    public LazyOptional<BoxedChemicalHandler> getConnectedAcceptor(Direction side) {
        if (cachedAcceptors.containsKey(side)) {
            BoxedChemicalAcceptorInfo acceptorInfo = cachedAcceptors.get(side);
            if (!acceptorInfo.getTile().isRemoved()) {
                return acceptorInfo.getAsLazy();
            }
        }
        return LazyOptional.empty();
    }

    public static class BoxedChemicalAcceptorInfo extends AbstractAcceptorInfo {

        private BoxedChemicalHandler boxedHandler;
        @Nullable
        private LazyOptional<BoxedChemicalHandler> asLazy;

        private BoxedChemicalAcceptorInfo(TileEntity tile, BoxedChemicalHandler boxedHandler) {
            super(tile);
            this.boxedHandler = boxedHandler;
        }

        public void updateAcceptor(BoxedChemicalHandler acceptor) {
            boxedHandler = acceptor;
            asLazy = null;
        }

        private LazyOptional<BoxedChemicalHandler> getAsLazy() {
            if (asLazy == null) {
                //Lazily calculate the lazy optional value of the boxed chemical handler
                asLazy = LazyOptional.of(() -> boxedHandler);
            }
            return asLazy;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            BoxedChemicalAcceptorInfo other = (BoxedChemicalAcceptorInfo) o;
            return boxedHandler.equals(other.boxedHandler);
        }

        @Override
        public int hashCode() {
            return boxedHandler.hashCode();
        }
    }
}