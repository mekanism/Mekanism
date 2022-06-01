package mekanism.common.item.block.machine;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.item.block.ItemBlockTooltip;
import mekanism.common.item.interfaces.IItemSustainedInventory;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.util.MekanismUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class ItemBlockQIOComponent extends ItemBlockTooltip<BlockTile<?, ?>> {

    public ItemBlockQIOComponent(BlockTile<?, ?> block) {
        super(block);
    }

    @Override
    protected void addStats(@Nonnull ItemStack stack, Level world, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag) {
        MekanismUtils.addFrequencyToTileTooltip(stack, FrequencyType.QIO, tooltip);
    }

    public static class ItemBlockQIOInventoryComponent extends ItemBlockQIOComponent implements IItemSustainedInventory {

        public ItemBlockQIOInventoryComponent(BlockTile<?, ?> block) {
            super(block);
        }
    }
}