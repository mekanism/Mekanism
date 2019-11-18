package mekanism.common.registration.impl;

import java.util.function.Function;
import java.util.function.Supplier;
import mekanism.common.registration.DoubleDeferredRegister;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

public class BlockDeferredRegister extends DoubleDeferredRegister<Block, Item> {

    public BlockDeferredRegister(String modid) {
        super(modid, ForgeRegistries.BLOCKS, ForgeRegistries.ITEMS);
    }

    //TODO: Create a helper wrapper that just takes a block properties??
    public <BLOCK extends Block> BlockRegistryObject<BLOCK, BlockItem> register(String name, Supplier<? extends BLOCK> blockSupplier) {
        return register(name, blockSupplier, block -> new BlockItem(block, ItemDeferredRegister.getMekBaseProperties()));
    }

    public <BLOCK extends Block, ITEM extends BlockItem> BlockRegistryObject<BLOCK, ITEM> register(String name, Supplier<? extends BLOCK> blockSupplier,
          Function<BLOCK, ITEM> itemCreator) {
        //TODO: Make sure it sets the creative tab
        return register(name, blockSupplier, itemCreator, BlockRegistryObject::new);
    }
}