package mekanism.common.inventory.container.tile;

import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.tile.TileEntityEnrichmentChamber;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class EnrichmentChamberContainer extends MekanismTileContainer<TileEntityEnrichmentChamber> {

    public EnrichmentChamberContainer(int id, PlayerInventory inv, TileEntityEnrichmentChamber tile) {
        super(MekanismContainerTypes.ENRICHMENT_CHAMBER, id, inv, tile);
    }

    public EnrichmentChamberContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityEnrichmentChamber.class));
    }
}