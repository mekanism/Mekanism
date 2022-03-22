package mekanism.chemistry.common.registries;

import mekanism.chemistry.common.MekanismChemistry;
import mekanism.chemistry.common.tile.TileEntityAirCompressor;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.registration.impl.ContainerTypeDeferredRegister;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;

public class ChemistryContainerTypes {

    public static final ContainerTypeDeferredRegister CONTAINER_TYPES = new ContainerTypeDeferredRegister(MekanismChemistry.MODID);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityAirCompressor>> AIR_COMPRESSOR = CONTAINER_TYPES.register(ChemistryBlocks.AIR_COMPRESSOR, TileEntityAirCompressor.class);

    private ChemistryContainerTypes() {
    }
}
