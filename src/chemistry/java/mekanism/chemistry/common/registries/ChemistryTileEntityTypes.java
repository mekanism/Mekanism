package mekanism.chemistry.common.registries;

import mekanism.chemistry.common.MekanismChemistry;
import mekanism.chemistry.common.tile.TileEntityAirCompressor;
import mekanism.chemistry.common.tile.multiblock.TileEntityFractionatingDistillerBlock;
import mekanism.chemistry.common.tile.multiblock.TileEntityFractionatingDistillerController;
import mekanism.chemistry.common.tile.multiblock.TileEntityFractionatingDistillerValve;
import mekanism.common.registration.impl.TileEntityTypeDeferredRegister;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;

public class ChemistryTileEntityTypes {

    public static final TileEntityTypeDeferredRegister TILE_ENTITY_TYPES = new TileEntityTypeDeferredRegister(MekanismChemistry.MODID);
    public static final TileEntityTypeRegistryObject<TileEntityAirCompressor> AIR_COMPRESSOR = TILE_ENTITY_TYPES.register(ChemistryBlocks.AIR_COMPRESSOR, TileEntityAirCompressor::new);
    public static final TileEntityTypeRegistryObject<TileEntityFractionatingDistillerBlock> FRACTIONATING_DISTILLER_BLOCK = TILE_ENTITY_TYPES.register(ChemistryBlocks.FRACTIONATING_DISTILLER_BLOCK, TileEntityFractionatingDistillerBlock::new);
    public static final TileEntityTypeRegistryObject<TileEntityFractionatingDistillerValve> FRACTIONATING_DISTILLER_VALVE = TILE_ENTITY_TYPES.register(ChemistryBlocks.FRACTIONATING_DISTILLER_VALVE, TileEntityFractionatingDistillerValve::new);
    public static final TileEntityTypeRegistryObject<TileEntityFractionatingDistillerController> FRACTIONATING_DISTILLER_CONTROLLER = TILE_ENTITY_TYPES.register(ChemistryBlocks.FRACTIONATING_DISTILLER_CONTROLLER, TileEntityFractionatingDistillerController::new);

    private ChemistryTileEntityTypes() {
    }
}
