package mekanism.common.inventory.container.tile;

import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.tile.TileEntityDigitalMiner;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class DigitalMinerConfigContainer extends EmptyTileContainer<TileEntityDigitalMiner> {

    public DigitalMinerConfigContainer(int id, PlayerInventory inv, TileEntityDigitalMiner tile) {
        super(MekanismContainerTypes.DIGITAL_MINER_CONFIG, id, inv, tile);
    }

    public DigitalMinerConfigContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityDigitalMiner.class));
    }

    @Override
    protected void addContainerTrackers() {
        if (tile != null) {
            tile.addConfigContainerTrackers(this);
        }
    }
}