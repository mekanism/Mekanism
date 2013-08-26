package mekanism.tools.item;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.EnumToolMaterial;
import net.minecraft.item.ItemStack;

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
