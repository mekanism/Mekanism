package mekanism.common.item.block.plastic;

import javax.annotation.Nonnull;
import mekanism.common.block.plastic.BlockPlasticGlow;
import mekanism.common.item.IItemRedirectedModel;
import mekanism.common.item.block.ItemBlockColoredName;

public class ItemBlockPlasticGlow extends ItemBlockColoredName<BlockPlasticGlow> implements IItemRedirectedModel {

    public ItemBlockPlasticGlow(BlockPlasticGlow block) {
        super(block);
    }

    @Nonnull
    @Override
    public String getRedirectLocation() {
        return "plastic_glow_block";
    }
}