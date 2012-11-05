package net.uberkat.obsidian.common;

import net.minecraft.src.Block;
import net.minecraft.src.CreativeTabs;
import net.minecraft.src.EnumToolMaterial;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Material;

public class ItemObsidianPaxel extends ItemObsidianTool
{
    private static Block blocksEffectiveAgainst[];

    public ItemObsidianPaxel(int i, EnumToolMaterial enumtoolmaterial)
    {
        super(i, 3, enumtoolmaterial, blocksEffectiveAgainst);
    }

    public boolean canHarvestBlock(Block block)
    {
        if (block == Block.obsidian)
        {
            return toolMaterial.getHarvestLevel() == 3;
        }
        if (block == Block.blockDiamond || block == Block.oreDiamond)
        {
            return toolMaterial.getHarvestLevel() >= 2;
        }
        if (block == Block.blockGold || block == Block.oreGold)
        {
            return toolMaterial.getHarvestLevel() >= 2;
        }
        if (block == Block.blockSteel || block == Block.oreIron)
        {
            return toolMaterial.getHarvestLevel() >= 1;
        }
        if (block == Block.blockLapis || block == Block.oreLapis)
        {
            return toolMaterial.getHarvestLevel() >= 1;
        }
        if (block == Block.oreRedstone || block == Block.oreRedstoneGlowing)
        {
            return toolMaterial.getHarvestLevel() >= 2;
        }
        if (block.blockMaterial == Material.rock)
        {
            return true;
        }
        else
        {
            return block.blockMaterial == Material.iron;
        }
    }

    public float getStrVsBlock(ItemStack itemstack, Block block)
    {
        if (block != null && (block.blockMaterial == Material.iron || block.blockMaterial == Material.rock))
        {
            return efficiencyOnProperMaterial;
        }
        else
        {
            return super.getStrVsBlock(itemstack, block);
        }
    }

    static
    {
        blocksEffectiveAgainst = (new Block[]
                {
	                Block.cobblestone, Block.woodSingleSlab, Block.woodDoubleSlab, Block.stone, Block.sandStone, Block.cobblestoneMossy, Block.oreIron, Block.blockSteel, Block.oreCoal, Block.blockGold,
	                Block.oreGold, Block.oreDiamond, Block.blockDiamond, Block.ice, Block.netherrack, Block.oreLapis, Block.blockLapis, Block.oreRedstone, Block.oreRedstoneGlowing, Block.rail,
	                Block.railDetector, Block.railPowered, Block.leaves, Block.grass, Block.dirt, Block.sand,
	                Block.gravel, Block.snow, Block.blockSnow, Block.blockClay, Block.tilledField, Block.slowSand, Block.mycelium, Block.planks, Block.bookShelf, Block.wood,
	                Block.chest, Block.stoneDoubleSlab, Block.stoneSingleSlab, Block.pumpkin, Block.pumpkinLantern, Block.snow, Block.pistonBase, Block.pistonStickyBase, Block.workbench, 
	                Block.stoneOvenActive, Block.stoneOvenIdle, Block.music, Block.trapdoor, Block.silverfish, Block.stoneBrick, Block.glowStone
                });
    }
}
