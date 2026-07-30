package mekanism.common.item.block.machine;

import mekanism.common.block.prefab.BlockTile;
import mekanism.common.item.block.ItemBlockTooltip;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.frequency.FrequencyTypes;
import mekanism.common.lib.frequency.IFrequencyItem;
import mekanism.common.registries.MekanismDataComponents;
import net.minecraft.util.Unit;
import net.minecraft.world.item.Item;

public class ItemBlockTeleporter extends ItemBlockTooltip<BlockTile<?, ?>> implements IFrequencyItem {

    public ItemBlockTeleporter(BlockTile<?, ?> block, Item.Properties properties) {
        super(block, properties.component(MekanismDataComponents.DETAILS, Unit.INSTANCE));
    }

    @Override
    public FrequencyType<?> getFrequencyType() {
        return FrequencyTypes.TELEPORTER;
    }
}