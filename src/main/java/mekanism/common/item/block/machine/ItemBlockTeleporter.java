package mekanism.common.item.block.machine;

import java.util.List;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.item.block.ItemBlockTooltip;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.frequency.IFrequencyItem;
import mekanism.common.util.MekanismUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ItemBlockTeleporter extends ItemBlockTooltip<BlockTile<?, ?>> implements IFrequencyItem {

    public ItemBlockTeleporter(BlockTile<?, ?> block) {
        super(block);
    }

    @Override
    protected void addStats(@NotNull ItemStack stack, Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        MekanismUtils.addFrequencyItemTooltip(stack, tooltip);
    }

    @Override
    public FrequencyType<?> getFrequencyType() {
        return FrequencyType.TELEPORTER;
    }
}