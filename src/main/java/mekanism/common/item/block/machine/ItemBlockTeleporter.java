package mekanism.common.item.block.machine;

import java.util.function.Consumer;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.item.block.ItemBlockTooltip;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.frequency.FrequencyTypes;
import mekanism.common.lib.frequency.IFrequencyItem;
import mekanism.common.util.MekanismUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jetbrains.annotations.NotNull;

public class ItemBlockTeleporter extends ItemBlockTooltip<BlockTile<?, ?>> implements IFrequencyItem {

    public ItemBlockTeleporter(BlockTile<?, ?> block, Item.Properties properties) {
        super(block, true, properties);
    }

    @Override
    protected void addStats(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull TooltipDisplay tooltipDisplay, @NotNull Consumer<Component> tooltipAdder, @NotNull TooltipFlag flag) {
        MekanismUtils.addFrequencyItemTooltip(stack, context, tooltipDisplay, tooltipAdder, flag);
    }

    @Override
    public FrequencyType<?> getFrequencyType() {
        return FrequencyTypes.TELEPORTER;
    }
}