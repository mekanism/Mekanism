package mekanism.common;

import net.minecraft.src.Block;
import net.minecraft.src.CreativeTabs;
import net.minecraft.src.EnumToolMaterial;

public class ItemMekanismSpade extends ItemMekanismTool
{
    private static Block blocksEffectiveAgainst[];

    public ItemMekanismSpade(int par1, EnumToolMaterial par2EnumToolMaterial)
    {
        super(par1, 1, par2EnumToolMaterial, blocksEffectiveAgainst);
    }

    @Override
    public boolean canHarvestBlock(Block par1Block)
    {
        if (par1Block == Block.snow)
        {
            return true;
        }

        return par1Block == Block.blockSnow;
    }

    static
    {
        blocksEffectiveAgainst = (new Block[]
                {
                    Block.grass, Block.dirt, Block.sand, Block.gravel, Block.snow, Block.blockSnow, Block.blockClay, Block.tilledField, Block.slowSand, Block.mycelium
                });
    }
}
