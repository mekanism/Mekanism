package mekanism.common.lib.transmitter.acceptor;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.common.content.network.transmitter.Transmitter;
import mekanism.common.integration.energy.EnergyCompatUtils;
import mekanism.common.integration.energy.IEnergyCompat;
import mekanism.common.integration.energy.StrictEnergyCompat;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.util.CapabilityUtils;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class EnergyAcceptorCache extends AcceptorCache<IStrictEnergyHandler> {

    public EnergyAcceptorCache(Transmitter<IStrictEnergyHandler, ?, ?> transmitter, TileEntityTransmitter transmitterTile) {
        super(transmitter, transmitterTile);
    }

    /**
     * @apiNote Only call this from the server side
     */
    public boolean hasStrictEnergyHandlerAndListen(@Nullable BlockEntity tile, Direction side) {
        if (tile != null && !tile.isRemoved() && tile.hasLevel()) {
            Direction opposite = side.getOpposite();
            for (IEnergyCompat energyCompat : EnergyCompatUtils.getCompats()) {
                if (energyCompat.isUsable()) {
                    LazyOptional<?> acceptor = CapabilityUtils.getCapability(tile, energyCompat.getCapability(), opposite);
                    if (acceptor.isPresent()) {
                        if (energyCompat instanceof StrictEnergyCompat) {
                            //Our lazy optional is already the proper type
                            updateCachedAcceptorAndListen(side, tile, (LazyOptional<IStrictEnergyHandler>) acceptor);
                        } else {
                            //Update the cache with the strict energy lazy optional as that is the one we interact with
                            LazyOptional<IStrictEnergyHandler> wrappedAcceptor = energyCompat.getLazyStrictEnergyHandler(tile, opposite);
                            //Note: The wrapped acceptor should always be present, but double check just in case
                            if (wrappedAcceptor.isPresent()) {
                                updateCachedAcceptorAndListen(side, tile, wrappedAcceptor, acceptor, false);
                            }
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }
}