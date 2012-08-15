package net.uberkat.obsidian.common;

import net.minecraft.src.Block;
import net.minecraft.src.EnumToolMaterial;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Material;

public class ItemObsidianAxe extends ItemObsidianTool
{
    private static Block blocksEffectiveAgainst[];

    public ItemObsidianAxe(int par1, EnumToolMaterial par2EnumToolMaterial)
    {
        super(par1, 3, par2EnumToolMaterial, blocksEffectiveAgainst);
    }

    /**
     * Returns the strength of the stack against a given block. 1.0F base, (Quality+1)*2 if correct blocktype, 1.5F if
     * sword
     */
    public float getStrVsBlock(ItemStack par1ItemStack, Block par2Block)
    {
        if (par2Block != null && par2Block.blockMaterial == Material.wood)
        {
            return efficiencyOnProperMaterial;
        }
        else
        {
            return super.getStrVsBlock(par1ItemStack, par2Block);
        }
    }

    static
    {
        blocksEffectiveAgainst = (new Block[]
                {
                    Block.planks, Block.bookShelf, Block.wood, Block.chest, Block.woodSingleSlab, Block.woodDoubleSlab, Block.pumpkin, Block.pumpkinLantern
                });
    }
}
