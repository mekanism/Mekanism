package mekanism.common.item.block.plastic;

import javax.annotation.Nonnull;
import mekanism.common.block.plastic.BlockPlasticReinforced;
import mekanism.common.item.IItemRedirectedModel;
import mekanism.common.item.block.ItemBlockColoredName;

public class ItemBlockPlasticReinforced extends ItemBlockColoredName implements IItemRedirectedModel {

    public ItemBlockPlasticReinforced(BlockPlasticReinforced block) {
        super(block);
    }

    @Nonnull
    @Override
    public String getRedirectLocation() {
        return "reinforced_plastic_block";
    }
}