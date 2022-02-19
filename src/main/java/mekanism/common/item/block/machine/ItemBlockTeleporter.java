package mekanism.common.item.block.machine;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

public class ItemBlockTeleporter extends ItemBlockMachine {

    public ItemBlockTeleporter(BlockTile<?, ?> block) {
        super(block);
    }

    @Override
    public void addStats(@Nonnull ItemStack stack, World world, @Nonnull List<ITextComponent> tooltip, boolean advanced) {
        MekanismUtils.addFrequencyToTileTooltip(stack, FrequencyType.TELEPORTER, tooltip);
    }
}