package mekanism.common.item.block.plastic;

import javax.annotation.Nonnull;
import mekanism.common.block.plastic.BlockPlasticRoad;
import mekanism.common.item.IItemRedirectedModel;
import mekanism.common.item.block.ItemBlockColoredName;

public class ItemBlockPlasticRoad extends ItemBlockColoredName<BlockPlasticRoad> implements IItemRedirectedModel {

    public ItemBlockPlasticRoad(BlockPlasticRoad block) {
        super(block);
    }

    @Nonnull
    @Override
    public String getRedirectLocation() {
        return "plastic_road";
    }
}