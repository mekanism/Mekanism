package mekanism.common.inventory.container.tile;

import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.tile.TileEntityDigitalMiner;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class DigitalMinerContainer extends MekanismTileContainer<TileEntityDigitalMiner> {

    public DigitalMinerContainer(int id, PlayerInventory inv, TileEntityDigitalMiner tile) {
        super(MekanismContainerTypes.DIGITAL_MINER, id, inv, tile);
    }

    public DigitalMinerContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityDigitalMiner.class));
    }

    @Override
    protected int getInventoryYOffset() {
        return 148;
    }
}