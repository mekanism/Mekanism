package mekanism.common.item.block;

import javax.annotation.Nonnull;
import mekanism.common.Mekanism;
import mekanism.common.item.IItemMekanism;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;

public class ItemBlockMekanism<BLOCK extends Block> extends BlockItem implements IItemMekanism {

    @Nonnull
    private final BLOCK block;

    public ItemBlockMekanism(@Nonnull BLOCK block) {
        this(block, new Item.Properties());
    }

    public ItemBlockMekanism(@Nonnull BLOCK block, Item.Properties properties) {
        super(block, properties.group(Mekanism.tabMekanism));
        this.block = block;
        //Ensure the name is lower case as with concatenating with values from enums it may not be
        setRegistryName(block.getRegistryName());
    }

    @Nonnull
    @Override
    public BLOCK getBlock() {
        return block;
    }
}