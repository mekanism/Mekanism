package mekanism.tools.item;

import net.minecraft.block.Block;
import net.minecraft.item.EnumToolMaterial;

public class ItemMekanismShovel extends ItemMekanismTool
{
    private static Block blocksEffectiveAgainst[];

    public ItemMekanismShovel(int id, EnumToolMaterial enumtoolmaterial)
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
