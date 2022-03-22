package mekanism.chemistry.common.registries;

import java.util.function.Supplier;
import mekanism.chemistry.common.MekanismChemistry;
import mekanism.chemistry.common.content.blocktype.ChemistryMachine;
import mekanism.chemistry.common.tile.TileEntityAirCompressor;
import mekanism.common.block.interfaces.IHasDescription;
import mekanism.common.block.prefab.BlockTile.BlockTileModel;
import mekanism.common.item.block.ItemBlockTooltip;
import mekanism.common.item.block.machine.ItemBlockMachine;
import mekanism.common.registration.impl.BlockDeferredRegister;
import mekanism.common.registration.impl.BlockRegistryObject;
import net.minecraft.world.level.block.Block;

public class ChemistryBlocks {

    public static final BlockDeferredRegister BLOCKS = new BlockDeferredRegister(MekanismChemistry.MODID);
    public static final BlockRegistryObject<BlockTileModel<TileEntityAirCompressor, ChemistryMachine<TileEntityAirCompressor>>, ItemBlockMachine> AIR_COMPRESSOR = BLOCKS.register("air_compressor", () -> new BlockTileModel<>(ChemistryBlockTypes.AIR_COMPRESSOR), ItemBlockMachine::new);

    private ChemistryBlocks() {
    }

    private static <BLOCK extends Block & IHasDescription> BlockRegistryObject<BLOCK, ItemBlockTooltip<BLOCK>> registerTooltipBlock(String name, Supplier<BLOCK> blockCreator) {
        return BLOCKS.registerDefaultProperties(name, blockCreator, ItemBlockTooltip::new);
    }
}
