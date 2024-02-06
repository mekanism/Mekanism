package mekanism.common.item.block.machine;

import java.util.List;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.item.block.ItemBlockTooltip;
import mekanism.common.item.interfaces.IColoredItem;
import mekanism.common.item.interfaces.IItemSustainedInventory;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.frequency.IFrequencyItem;
import mekanism.common.util.MekanismUtils;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ItemBlockQIOComponent extends ItemBlockTooltip<BlockTile<?, ?>> implements IColoredItem, IFrequencyItem {

    public ItemBlockQIOComponent(BlockTile<?, ?> block) {
        super(block);
    }

    @Override
    protected void addStats(@NotNull ItemStack stack, Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        MekanismUtils.addFrequencyItemTooltip(stack, tooltip);
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (!level.isClientSide && level.getGameTime() % (5 * SharedConstants.TICKS_PER_SECOND) == 0) {
            syncColorWithFrequency(stack);
        }
    }

    @Override
    public FrequencyType<?> getFrequencyType() {
        return FrequencyType.QIO;
    }

    public static class ItemBlockQIOInventoryComponent extends ItemBlockQIOComponent implements IItemSustainedInventory {

        public ItemBlockQIOInventoryComponent(BlockTile<?, ?> block) {
            super(block);
        }
    }
}