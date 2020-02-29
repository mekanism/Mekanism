package mekanism.common.block.machine;

import mekanism.common.registries.MekanismMachines;
import mekanism.common.tile.TileEntityEnrichmentChamber;

public class BlockEnrichmentChamber extends BlockMachine<TileEntityEnrichmentChamber> {

    public BlockEnrichmentChamber() {
        super(MekanismMachines.ENRICHMENT_CHAMBER);
    }
}