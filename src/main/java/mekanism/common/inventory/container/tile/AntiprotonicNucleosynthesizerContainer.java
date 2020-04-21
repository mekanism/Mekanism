package mekanism.common.inventory.container.tile;

import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.tile.TileEntityAntiprotonicNucleosynthesizer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class AntiprotonicNucleosynthesizerContainer extends MekanismTileContainer<TileEntityAntiprotonicNucleosynthesizer> {

    public AntiprotonicNucleosynthesizerContainer(int id, PlayerInventory inv, TileEntityAntiprotonicNucleosynthesizer tile) {
        super(MekanismContainerTypes.ANTIPROTONIC_NUCLEOSYNTHESIZER, id, inv, tile);
    }

    public AntiprotonicNucleosynthesizerContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityAntiprotonicNucleosynthesizer.class));
    }

    @Override
    protected int getInventoryYOffset() {
        return super.getInventoryYOffset() + 27;
    }

    @Override
    protected int getInventoryXOffset() {
        return super.getInventoryXOffset() + 10;
    }
}