package mekanism.common.inventory.container.tile;

import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.tile.TileEntityCombiner;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class CombinerContainer extends MekanismTileContainer<TileEntityCombiner> {

    public CombinerContainer(int id, PlayerInventory inv, TileEntityCombiner tile) {
        super(MekanismContainerTypes.COMBINER, id, inv, tile);
    }

    public CombinerContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityCombiner.class));
    }
}