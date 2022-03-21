package mekanism.chemistry.common.registries;

import mekanism.chemistry.common.MekanismChemistry;
import mekanism.common.registration.impl.TileEntityTypeDeferredRegister;

public class ChemistryTileEntityTypes {

    private ChemistryTileEntityTypes() {
    }

    public static final TileEntityTypeDeferredRegister TILE_ENTITY_TYPES = new TileEntityTypeDeferredRegister(MekanismChemistry.MODID);
}
