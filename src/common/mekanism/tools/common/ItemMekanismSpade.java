package mekanism.tools.common;

import net.minecraft.src.Block;
import net.minecraft.src.CreativeTabs;
import net.minecraft.src.EnumToolMaterial;

public class ItemMekanismSpade extends ItemMekanismTool
{
    private static Block blocksEffectiveAgainst[];

    public ItemMekanismSpade(int id, EnumToolMaterial enumtoolmaterial)
    {
        super(id, 1, enumtoolmaterial, blocksEffectiveAgainst);
    }

    @Override
    public boolean canHarvestBlock(Block block)
    {
        if (block == Block.snow)
        {
            return true;
        }

        return block == Block.blockSnow;
    }

    static
    {
        blocksEffectiveAgainst = (new Block[]
                {
                    Block.grass, Block.dirt, Block.sand, Block.gravel, Block.snow, Block.blockSnow, Block.blockClay, Block.tilledField, Block.slowSand, Block.mycelium
                });
    }
}
