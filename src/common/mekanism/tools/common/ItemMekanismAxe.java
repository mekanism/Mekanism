package mekanism.tools.common;

import net.minecraft.src.Block;
import net.minecraft.src.EnumToolMaterial;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Material;

public class ItemMekanismAxe extends ItemMekanismTool
{
    private static Block blocksEffectiveAgainst[];

    public ItemMekanismAxe(int id, EnumToolMaterial enumtoolmaterial)
    {
        super(id, 3, enumtoolmaterial, blocksEffectiveAgainst);
    }

    @Override
    public float getStrVsBlock(ItemStack itemstack, Block block)
    {
        if (block != null && block.blockMaterial == Material.wood)
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
                    Block.planks, Block.bookShelf, Block.wood, Block.chest, Block.woodSingleSlab, Block.woodDoubleSlab, Block.pumpkin, Block.pumpkinLantern
                });
    }
}
