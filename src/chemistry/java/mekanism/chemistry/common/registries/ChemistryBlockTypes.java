package mekanism.chemistry.common.registries;

import java.util.EnumSet;
import mekanism.api.Upgrade;
import mekanism.chemistry.common.ChemistryLang;
import mekanism.chemistry.common.config.MekanismChemistryConfig;
import mekanism.chemistry.common.content.blocktype.ChemistryMachine;
import mekanism.chemistry.common.content.blocktype.ChemistryMachine.ChemistryMachineBuilder;
import mekanism.chemistry.common.tile.TileEntityAirCompressor;
import mekanism.chemistry.common.tile.multiblock.TileEntityFractionatingDistillerBlock;
import mekanism.chemistry.common.tile.multiblock.TileEntityFractionatingDistillerController;
import mekanism.chemistry.common.tile.multiblock.TileEntityFractionatingDistillerValve;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeStateFacing;
import mekanism.common.block.attribute.Attributes;
import mekanism.common.block.attribute.Attributes.AttributeCustomResistance;
import mekanism.common.block.attribute.Attributes.AttributeMobSpawn;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.content.blocktype.BlockTypeTile.BlockTileBuilder;

public class ChemistryBlockTypes {

    public static final ChemistryMachine<TileEntityAirCompressor> AIR_COMPRESSOR = ChemistryMachineBuilder
          .createChemistryMachine(() -> ChemistryTileEntityTypes.AIR_COMPRESSOR, ChemistryLang.DESCRIPTION_AIR_COMPRESSOR)
          .withGui(() -> ChemistryContainerTypes.AIR_COMPRESSOR)
          .withEnergyConfig(MekanismChemistryConfig.usageConfig.airCompressor, MekanismChemistryConfig.storageConfig.airCompressor)
          .withSupportedUpgrades(EnumSet.of(Upgrade.SPEED, Upgrade.ENERGY))
          .withComputerSupport("airCompressor")
          .replace(Attributes.ACTIVE)
          .build();
    public static final BlockTypeTile<TileEntityFractionatingDistillerBlock> FRACTIONATING_DISTILLER_BLOCK = BlockTileBuilder
          .createBlock(() -> ChemistryTileEntityTypes.FRACTIONATING_DISTILLER_BLOCK, ChemistryLang.DESCRIPTION_FRACTIONATING_DISTILLER_BLOCK)
          .with(new AttributeCustomResistance(9), Attributes.MULTIBLOCK, AttributeMobSpawn.WHEN_NOT_FORMED)
          .build();
    public static final BlockTypeTile<TileEntityFractionatingDistillerValve> FRACTIONATING_DISTILLER_VALVE = BlockTileBuilder
          .createBlock(() -> ChemistryTileEntityTypes.FRACTIONATING_DISTILLER_VALVE, ChemistryLang.DESCRIPTION_FRACTIONATING_DISTILLER_VALVE)
          .with(new AttributeCustomResistance(9), Attributes.MULTIBLOCK, Attributes.COMPARATOR, AttributeMobSpawn.WHEN_NOT_FORMED)
          .withComputerSupport("fractionatingDistillerValve")
          .build();
    public static final BlockTypeTile<TileEntityFractionatingDistillerController> FRACTIONATING_DISTILLER_CONTROLLER = BlockTileBuilder
          .createBlock(() -> ChemistryTileEntityTypes.FRACTIONATING_DISTILLER_CONTROLLER, ChemistryLang.DESCRIPTION_FRACTIONATING_DISTILLER_CONTROLLER)
          .withGui(() -> ChemistryContainerTypes.FRACTIONATING_DISTILLER_CONTROLLER, ChemistryLang.DISTILLER)
          .with(Attributes.INVENTORY, Attributes.ACTIVE, new AttributeStateFacing(), new AttributeCustomResistance(9), Attributes.MULTIBLOCK, AttributeMobSpawn.WHEN_NOT_FORMED)
          .withComputerSupport("fractionatingDistillerController")
          .build();

    private ChemistryBlockTypes() {
    }
}
