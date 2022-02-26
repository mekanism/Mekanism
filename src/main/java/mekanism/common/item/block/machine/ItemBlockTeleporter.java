package mekanism.common.item.block.machine;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.util.MekanismUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class ItemBlockTeleporter extends ItemBlockMachine {

    public ItemBlockTeleporter(BlockTile<?, ?> block) {
        super(block);
    }

    @Override
    protected void addStats(@Nonnull ItemStack stack, Level world, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag) {
        MekanismUtils.addFrequencyToTileTooltip(stack, FrequencyType.TELEPORTER, tooltip);
    }
}