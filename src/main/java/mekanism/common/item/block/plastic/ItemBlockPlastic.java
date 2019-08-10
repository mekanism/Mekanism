package mekanism.common.item.block.plastic;

import javax.annotation.Nonnull;
import mekanism.common.block.plastic.BlockPlastic;
import mekanism.common.item.IItemRedirectedModel;
import mekanism.common.item.block.ItemBlockColoredName;

public class ItemBlockPlastic extends ItemBlockColoredName<BlockPlastic> implements IItemRedirectedModel {

    public ItemBlockPlastic(BlockPlastic block) {
        super(block);
    }

    @Nonnull
    @Override
    public String getRedirectLocation() {
        return "plastic_block";
    }
}