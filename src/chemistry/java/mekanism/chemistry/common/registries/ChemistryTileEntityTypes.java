package mekanism.chemistry.common.registries;

import mekanism.chemistry.common.MekanismChemistry;
import mekanism.chemistry.common.tile.TileEntityAirCompressor;
import mekanism.common.registration.impl.TileEntityTypeDeferredRegister;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;

public class ChemistryTileEntityTypes {

    public static final TileEntityTypeDeferredRegister TILE_ENTITY_TYPES = new TileEntityTypeDeferredRegister(MekanismChemistry.MODID);
    public static final TileEntityTypeRegistryObject<TileEntityAirCompressor> AIR_COMPRESSOR = TILE_ENTITY_TYPES.register(ChemistryBlocks.AIR_COMPRESSOR, TileEntityAirCompressor::new);

    private ChemistryTileEntityTypes() {
    }
}
