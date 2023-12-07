package mekanism.common.lib.transmitter.acceptor;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.capabilities.chemical.BoxedChemicalHandler;
import mekanism.common.lib.transmitter.acceptor.BoxedChemicalAcceptorCache.BoxedChemicalAcceptorInfo;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class BoxedChemicalAcceptorCache extends AbstractAcceptorCache<BoxedChemicalHandler, BoxedChemicalAcceptorInfo> {

    public BoxedChemicalAcceptorCache(TileEntityTransmitter transmitterTile) {
        super(transmitterTile);
    }

    @Override
    protected BoxedChemicalAcceptorInfo initializeCache(ServerLevel level, BlockPos pos, Direction opposite, RefreshListener refreshListener) {
        return new BoxedChemicalAcceptorInfo(new BoxedChemicalHandler(level, pos, opposite, refreshListener));
    }

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