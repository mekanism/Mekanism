package mekanism.tools.common;

import javax.annotation.Nonnull;
import mekanism.common.MekanismItems;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.ItemStack;

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

    public static ItemStack getRepairStack(ToolMaterial material) {
        if (material == MekanismTools.toolOBSIDIAN || material == MekanismTools.toolOBSIDIAN2) {
            return new ItemStack(MekanismItems.Ingot, 1, 0);
        } else if (material == MekanismTools.toolLAZULI || material == MekanismTools.toolLAZULI2) {
            return new ItemStack(Items.DYE, 1, 4);
        } else if (material == MekanismTools.toolOSMIUM || material == MekanismTools.toolOSMIUM2) {
            return new ItemStack(MekanismItems.Ingot, 1, 1);
        } else if (material == MekanismTools.toolBRONZE || material == MekanismTools.toolBRONZE2) {
            return new ItemStack(MekanismItems.Ingot, 1, 2);
        } else if (material == MekanismTools.toolGLOWSTONE || material == MekanismTools.toolGLOWSTONE2) {
            return new ItemStack(MekanismItems.Ingot, 1, 3);
        } else if (material == MekanismTools.toolSTEEL || material == MekanismTools.toolSTEEL2) {
            return new ItemStack(MekanismItems.Ingot, 1, 4);
        }
        return material.getRepairItemStack();
    }

    public static ItemStack getRepairStack(ArmorMaterial material) {
        if (material == MekanismTools.armorOBSIDIAN) {
            return new ItemStack(MekanismItems.Ingot, 1, 0);
        } else if (material == MekanismTools.armorLAZULI) {
            return new ItemStack(Items.DYE, 1, 4);
        } else if (material == MekanismTools.armorOSMIUM) {
            return new ItemStack(MekanismItems.Ingot, 1, 1);
        } else if (material == MekanismTools.armorBRONZE) {
            return new ItemStack(MekanismItems.Ingot, 1, 2);
        } else if (material == MekanismTools.armorGLOWSTONE) {
            return new ItemStack(MekanismItems.Ingot, 1, 3);
        } else if (material == MekanismTools.armorSTEEL) {
            return new ItemStack(MekanismItems.Ingot, 1, 4);
        }
        return material.getRepairItemStack();
    }
}