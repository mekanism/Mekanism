package mekanism.chemistry.common.registries;

import java.util.EnumSet;
import mekanism.api.Upgrade;
import mekanism.chemistry.common.ChemistryLang;
import mekanism.chemistry.common.config.MekanismChemistryConfig;
import mekanism.chemistry.common.content.blocktype.ChemistryMachine;
import mekanism.chemistry.common.content.blocktype.ChemistryMachine.ChemistryMachineBuilder;
import mekanism.chemistry.common.tile.TileEntityAirCompressor;
import mekanism.common.block.attribute.Attributes;

public class ChemistryBlockTypes {

    public static final ChemistryMachine<TileEntityAirCompressor> AIR_COMPRESSOR = ChemistryMachineBuilder
          .createChemistryMachine(() -> ChemistryTileEntityTypes.AIR_COMPRESSOR, ChemistryLang.DESCRIPTION_AIR_COMPRESSOR)
          .withGui(() -> ChemistryContainerTypes.AIR_COMPRESSOR)
          .withEnergyConfig(MekanismChemistryConfig.usageConfig.airCompressor, MekanismChemistryConfig.storageConfig.airCompressor)
          .withSupportedUpgrades(EnumSet.of(Upgrade.SPEED, Upgrade.ENERGY))
          .withComputerSupport("airCompressor")
          .replace(Attributes.ACTIVE)
          .build();

    private ChemistryBlockTypes() {
    }
}
