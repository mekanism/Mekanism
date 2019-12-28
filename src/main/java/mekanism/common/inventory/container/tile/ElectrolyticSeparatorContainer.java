package mekanism.common.inventory.container.tile;

import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.tile.TileEntityElectrolyticSeparator;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class ElectrolyticSeparatorContainer extends MekanismTileContainer<TileEntityElectrolyticSeparator> {

    public ElectrolyticSeparatorContainer(int id, PlayerInventory inv, TileEntityElectrolyticSeparator tile) {
        super(MekanismContainerTypes.ELECTROLYTIC_SEPARATOR, id, inv, tile);
    }

    public ElectrolyticSeparatorContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityElectrolyticSeparator.class));
    }
}