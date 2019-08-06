package mekanism.common.item.block;

import mekanism.common.block.interfaces.IBlockOreDict;
import mekanism.common.item.IItemMekanism;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class ItemBlockMekanism extends BlockItem implements IItemMekanism {

    public ItemBlockMekanism(Block block) {
        super(block);
        //Ensure the name is lower case as with concatenating with values from enums it may not be
        setRegistryName(block.getRegistryName());
    }

    @Override
    public void registerOreDict() {
        if (block instanceof IBlockOreDict) {
            for (String entry : ((IBlockOreDict) block).getOredictEntries()) {
                OreDictionary.registerOre(entry, new ItemStack(this));
            }
        }
    }
}