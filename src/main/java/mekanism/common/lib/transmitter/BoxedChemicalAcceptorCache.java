package mekanism.common.lib.transmitter;

import javax.annotation.Nullable;
import mekanism.common.capabilities.chemical.BoxedChemicalHandler;
import mekanism.common.content.network.transmitter.BoxedPressurizedTube;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;

public class BoxedChemicalAcceptorCache extends AcceptorCache<BoxedChemicalHandler> {

    public BoxedChemicalAcceptorCache(BoxedPressurizedTube transmitter, TileEntityTransmitter transmitterTile) {
        super(transmitter, transmitterTile);
    }

    public boolean isChemicalAcceptorAndListen(@Nullable TileEntity tile, Direction side) {
        //TODO: IMPLEMENT ME
        return false;
    }
}