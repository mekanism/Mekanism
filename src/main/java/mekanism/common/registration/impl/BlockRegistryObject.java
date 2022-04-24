package mekanism.common.registration.impl;

import javax.annotation.Nonnull;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.registration.DoubleWrappedRegistryObject;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;

public class BlockRegistryObject<BLOCK extends Block, ITEM extends Item> extends DoubleWrappedRegistryObject<BLOCK, ITEM> implements IBlockProvider {

    public BlockRegistryObject(RegistryObject<BLOCK> blockRegistryObject, RegistryObject<ITEM> itemRegistryObject) {
        super(blockRegistryObject, itemRegistryObject);
    }

    @Nonnull
    @Override
    public BLOCK getBlock() {
        return getPrimary();
    }

    @Nonnull
    @Override
    public ITEM asItem() {
        return getSecondary();
    }
}