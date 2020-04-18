package mekanism.defense.common.registries;

import java.util.function.Supplier;
import mekanism.common.block.interfaces.IHasDescription;
import mekanism.common.item.block.ItemBlockTooltip;
import mekanism.common.registration.impl.BlockDeferredRegister;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.defense.common.MekanismDefense;
import net.minecraft.block.Block;

public class DefenseBlocks {

    public static final BlockDeferredRegister BLOCKS = new BlockDeferredRegister(MekanismDefense.MODID);

    private static <BLOCK extends Block & IHasDescription> BlockRegistryObject<BLOCK, ItemBlockTooltip<BLOCK>> registerTooltipBlock(String name, Supplier<BLOCK> blockCreator) {
        return BLOCKS.registerDefaultProperties(name, blockCreator, ItemBlockTooltip::new);
    }
}