package mekanism.tools.item;

import java.util.HashSet;
import javax.annotation.Nonnull;
import mekanism.tools.common.ToolUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

public class ItemMekanismPaxel extends ItemMekanismTool {

    public ItemMekanismPaxel(ToolMaterial toolMaterial) {
        super(4, -2.4F, toolMaterial, new HashSet<>());
    }

    @Override
    public float getDestroySpeed(@Nonnull ItemStack stack, IBlockState blockState) {
        return blockState.getBlock() != Blocks.BEDROCK ? efficiency : 1.0F;
    }

    @Override
    public boolean canHarvestBlock(@Nonnull IBlockState state, ItemStack stack) {
        return ToolUtils.canShovelHarvest(state.getBlock()) || ToolUtils.canPickaxeHarvest(state, toolMaterial);
    }
}