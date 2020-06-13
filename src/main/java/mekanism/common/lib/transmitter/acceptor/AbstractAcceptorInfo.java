package mekanism.common.lib.transmitter.acceptor;

import net.minecraft.tileentity.TileEntity;

public abstract class AbstractAcceptorInfo {

    private final TileEntity tile;

    protected AbstractAcceptorInfo(TileEntity tile) {
        this.tile = tile;
    }

    public TileEntity getTile() {
        return tile;
    }
}