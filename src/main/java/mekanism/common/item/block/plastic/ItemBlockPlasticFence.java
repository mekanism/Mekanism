package mekanism.common.item.block.plastic;

import javax.annotation.Nonnull;
import mekanism.common.block.plastic.BlockPlasticFence;
import mekanism.common.item.IItemRedirectedModel;
import mekanism.common.item.block.ItemBlockColoredName;

public class ItemBlockPlasticFence extends ItemBlockColoredName implements IItemRedirectedModel {

    public ItemBlockPlasticFence(BlockPlasticFence block) {
        super(block);
    }

    @Nonnull
    @Override
    public String getRedirectLocation() {
        return "plastic_fence";
    }
}