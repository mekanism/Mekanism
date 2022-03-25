package mekanism.chemistry.common.registries;

import java.util.function.Supplier;
import mekanism.chemistry.common.MekanismChemistry;
import mekanism.chemistry.common.content.blocktype.ChemistryMachine;
import mekanism.chemistry.common.tile.TileEntityAirCompressor;
import mekanism.chemistry.common.tile.multiblock.TileEntityFractionatingDistillerBlock;
import mekanism.chemistry.common.tile.multiblock.TileEntityFractionatingDistillerController;
import mekanism.chemistry.common.tile.multiblock.TileEntityFractionatingDistillerValve;
import mekanism.common.block.interfaces.IHasDescription;
import mekanism.common.block.prefab.BlockBasicMultiblock;
import mekanism.common.block.prefab.BlockTile.BlockTileModel;
import mekanism.common.item.block.ItemBlockTooltip;
import mekanism.common.item.block.machine.ItemBlockMachine;
import mekanism.common.registration.impl.BlockDeferredRegister;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.registries.MekanismBlockTypes;
import mekanism.common.tile.multiblock.TileEntityThermalEvaporationBlock;
import mekanism.common.tile.multiblock.TileEntityThermalEvaporationController;
import mekanism.common.tile.multiblock.TileEntityThermalEvaporationValve;
import net.minecraft.world.level.block.Block;

public class ChemistryBlocks {

    public static final BlockDeferredRegister BLOCKS = new BlockDeferredRegister(MekanismChemistry.MODID);
    public static final BlockRegistryObject<BlockTileModel<TileEntityAirCompressor, ChemistryMachine<TileEntityAirCompressor>>, ItemBlockMachine> AIR_COMPRESSOR = BLOCKS.register("air_compressor", () -> new BlockTileModel<>(ChemistryBlockTypes.AIR_COMPRESSOR), ItemBlockMachine::new);
    public static final BlockRegistryObject<BlockBasicMultiblock<TileEntityFractionatingDistillerController>, ItemBlockTooltip<BlockBasicMultiblock<TileEntityFractionatingDistillerController>>> FRACTIONATING_DISTILLER_CONTROLLER = registerBlock("fractionating_distiller_controller", () -> new BlockBasicMultiblock<>(ChemistryBlockTypes.FRACTIONATING_DISTILLER_CONTROLLER));
    public static final BlockRegistryObject<BlockBasicMultiblock<TileEntityFractionatingDistillerValve>, ItemBlockTooltip<BlockBasicMultiblock<TileEntityFractionatingDistillerValve>>> FRACTIONATING_DISTILLER_VALVE = registerBlock("fractionating_distiller_valve", () -> new BlockBasicMultiblock<>(ChemistryBlockTypes.FRACTIONATING_DISTILLER_VALVE));
    public static final BlockRegistryObject<BlockBasicMultiblock<TileEntityFractionatingDistillerBlock>, ItemBlockTooltip<BlockBasicMultiblock<TileEntityFractionatingDistillerBlock>>> FRACTIONATING_DISTILLER_BLOCK = registerBlock("fractionating_distiller_block", () -> new BlockBasicMultiblock<>(ChemistryBlockTypes.FRACTIONATING_DISTILLER_BLOCK));

    private ChemistryBlocks() {
    }

    private static <BLOCK extends Block & IHasDescription> BlockRegistryObject<BLOCK, ItemBlockTooltip<BLOCK>> registerBlock(String name, Supplier<BLOCK> blockCreator) {
        return BLOCKS.registerDefaultProperties(name, blockCreator, ItemBlockTooltip::new);
    }
}
