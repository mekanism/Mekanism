package mekanism.common.item.block.plastic;

import javax.annotation.Nonnull;
import mekanism.common.block.plastic.BlockPlasticSlick;
import mekanism.common.item.IItemRedirectedModel;
import mekanism.common.item.block.ItemBlockColoredName;

public class ItemBlockPlasticSlick extends ItemBlockColoredName implements IItemRedirectedModel {

    public ItemBlockPlasticSlick(BlockPlasticSlick block) {
        super(block);
    }

    @Nonnull
    @Override
    public String getRedirectLocation() {
        return "slick_plastic_block";
    }
}