package mekanism.tools.common;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item.ToolMaterial;

import javax.annotation.Nonnull;

public class ToolUtils {
    public static boolean canPickaxeHarvest(@Nonnull IBlockState state, ToolMaterial toolMaterial) {
        Block block = state.getBlock();

        if (block == Blocks.OBSIDIAN) {
            return toolMaterial.getHarvestLevel() == 3;
        }

        if (block == Blocks.DIAMOND_BLOCK || block == Blocks.DIAMOND_ORE) {
            return toolMaterial.getHarvestLevel() >= 2;
        }

        if (block == Blocks.GOLD_BLOCK || block == Blocks.GOLD_ORE) {
            return toolMaterial.getHarvestLevel() >= 2;
        }

        if (block == Blocks.IRON_BLOCK || block == Blocks.IRON_ORE) {
            return toolMaterial.getHarvestLevel() >= 1;
        }

        if (block == Blocks.LAPIS_BLOCK || block == Blocks.LAPIS_ORE) {
            return toolMaterial.getHarvestLevel() >= 1;
        }

        if (block == Blocks.REDSTONE_ORE || block == Blocks.LIT_REDSTONE_ORE) {
            return toolMaterial.getHarvestLevel() >= 2;
        }

        if (block == Blocks.ANVIL) {
            return toolMaterial.getHarvestLevel() >= 0;
        }

        if (state.getMaterial() == Material.ROCK) {
            return true;
        }

        return state.getMaterial() == Material.IRON;
    }

    public static boolean canShovelHarvest(Block block) {
        return block == Blocks.SNOW_LAYER || block == Blocks.SNOW;
    }
}