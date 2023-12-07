package mekanism.common.lib.transmitter.acceptor;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.capabilities.chemical.BoxedChemicalHandler;
import mekanism.common.content.network.transmitter.BoxedPressurizedTube;
import mekanism.common.lib.transmitter.acceptor.BoxedChemicalAcceptorCache.BoxedChemicalAcceptorInfo;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

//TODO - V11: Improve this so it only invalidates the types needed instead of doing all chemical types at once
@NothingNullByDefault
public class BoxedChemicalAcceptorCache extends AbstractAcceptorCache<BoxedChemicalHandler, BoxedChemicalAcceptorInfo> {

    public BoxedChemicalAcceptorCache(BoxedPressurizedTube transmitter, TileEntityTransmitter transmitterTile) {
        super(transmitter, transmitterTile);
    }

    public boolean isChemicalAcceptorAndListen(ServerLevel level, BlockPos pos, Direction side) {
        //TODO: Validate this and recheck the comment
        //Update the cached acceptor and if it changed, add a listener to it to listen for invalidation
        AcceptorInfo<BoxedChemicalHandler> cache = cachedAcceptors.computeIfAbsent(side, s ->
              new BoxedChemicalAcceptorInfo(new BoxedChemicalHandler(level, pos, s.getOpposite(), getRefreshListener(s))));
        //TODO: Make sure we are removing it from cachedAcceptors when it becomes invalidated or the next call to this will error
        // Is this even correct??? The cache only actually gets removed if isValid is false
        return cache.acceptor() != null;
    }

    //TODO - 1.20: Make this be the boxed chemical handler maybe?
    public static final class BoxedChemicalAcceptorInfo implements AcceptorInfo<BoxedChemicalHandler> {

        private final BoxedChemicalHandler acceptor;

        BoxedChemicalAcceptorInfo(BoxedChemicalHandler acceptor) {
            this.acceptor = acceptor;
        }

        @Nullable
        @Override
        public BoxedChemicalHandler acceptor() {
            return acceptor.hasAnyAcceptors() ? acceptor : null;
        }
    }
}