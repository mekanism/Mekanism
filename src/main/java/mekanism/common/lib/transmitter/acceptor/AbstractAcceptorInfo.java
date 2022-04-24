package mekanism.common.lib.transmitter.acceptor;

import net.minecraft.world.level.block.entity.BlockEntity;

public abstract class AbstractAcceptorInfo {

    private final BlockEntity tile;

    protected AbstractAcceptorInfo(BlockEntity tile) {
        this.tile = tile;
    }

    public BlockEntity getTile() {
        return tile;
    }
}