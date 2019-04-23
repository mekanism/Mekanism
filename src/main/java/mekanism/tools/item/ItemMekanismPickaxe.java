package mekanism.tools.item;

import com.google.common.collect.Sets;
import java.util.Set;
import javax.annotation.Nonnull;
import mekanism.tools.common.ToolUtils;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

public class ItemMekanismPickaxe extends ItemMekanismTool {

    private static final Set<Block> EFFECTIVE_ON = Sets
          .newHashSet(Blocks.ACTIVATOR_RAIL, Blocks.COAL_ORE, Blocks.COBBLESTONE, Blocks.DETECTOR_RAIL,
                Blocks.DIAMOND_BLOCK, Blocks.DIAMOND_ORE, Blocks.DOUBLE_STONE_SLAB, Blocks.GOLDEN_RAIL,
                Blocks.GOLD_BLOCK, Blocks.GOLD_ORE, Blocks.ICE, Blocks.IRON_BLOCK, Blocks.IRON_ORE, Blocks.LAPIS_BLOCK,
                Blocks.LAPIS_ORE, Blocks.LIT_REDSTONE_ORE, Blocks.MOSSY_COBBLESTONE, Blocks.NETHERRACK,
                Blocks.PACKED_ICE, Blocks.RAIL, Blocks.REDSTONE_ORE, Blocks.SANDSTONE, Blocks.RED_SANDSTONE,
                Blocks.STONE, Blocks.STONE_SLAB, Blocks.STONE_BUTTON, Blocks.STONE_PRESSURE_PLATE);

    public ItemMekanismPickaxe(ToolMaterial toolMaterial) {
        super(1, -2.8F, toolMaterial, EFFECTIVE_ON);
    }

    @Override
    public boolean canHarvestBlock(@Nonnull IBlockState state, ItemStack stack) {
        return ToolUtils.canPickaxeHarvest(state, toolMaterial);
    }

    @Override
    public float getDestroySpeed(@Nonnull ItemStack itemstack, IBlockState blockState) {
        if (blockState != null && (blockState.getMaterial() == Material.IRON
              || blockState.getMaterial() == Material.ANVIL || blockState.getMaterial() == Material.ROCK)) {
            return efficiency;
        } else {
            return super.getDestroySpeed(itemstack, blockState);
        }
    }
}