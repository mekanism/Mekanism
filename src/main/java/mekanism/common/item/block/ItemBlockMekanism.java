package mekanism.common.item.block;

import java.util.List;
import mekanism.common.block.interfaces.IBlockOreDict;
import mekanism.common.item.IItemMekanism;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class ItemBlockMekanism extends ItemBlock implements IItemMekanism {

    public ItemBlockMekanism(Block block) {
        super(block);
        //Ensure the name is lower case as with concatenating with values from enums it may not be
        setRegistryName(block.getRegistryName());
    }

    @Override
    public void registerOreDict() {
        if (block instanceof IBlockOreDict) {
            List<String> oredictEntries = ((IBlockOreDict) block).getOredictEntries();
            for (String entry : oredictEntries) {
                OreDictionary.registerOre(entry, new ItemStack(this));
            }
        }
    }
}