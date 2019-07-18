package mekanism.common.item;

import java.util.Locale;
import mekanism.common.Mekanism;
import mekanism.common.block.IBlockMekanism;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;

public class ItemBlockMekanism<BLOCK extends Block & IBlockMekanism> extends ItemBlock implements IItemMekanism {

    public ItemBlockMekanism(BLOCK block) {
        super(block);
    }

    public ItemBlockMekanism(BLOCK block, String name) {
        super(block);
        //Ensure the name is lower case as with concatenating with values from enums it may not be
        name = name.toLowerCase(Locale.ROOT);
        setCreativeTab(Mekanism.tabMekanism);
        setTranslationKey(name);
        setRegistryName(new ResourceLocation(Mekanism.MODID, name));
    }
}