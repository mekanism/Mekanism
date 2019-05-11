package mekanism.common.item;

import java.util.Locale;
import javax.annotation.Nonnull;
import mekanism.common.base.IMetaItem;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class ItemIngot extends ItemMekanism implements IMetaItem {

    public static String[] en_USNames = {"Obsidian", "Osmium", "Bronze", "Glowstone", "Steel", "Copper", "Tin"};

    public ItemIngot() {
        super();
        setHasSubtypes(true);
    }

    @Override
    public String getTexture(int meta) {
        return en_USNames[meta] + "Ingot";
    }

    @Override
    public int getVariants() {
        return en_USNames.length;
    }

    @Override
    public void getSubItems(@Nonnull CreativeTabs tabs, @Nonnull NonNullList<ItemStack> itemList) {
        if (isInCreativeTab(tabs)) {
            for (int counter = 0; counter < en_USNames.length; counter++) {
                itemList.add(new ItemStack(this, 1, counter));
            }
        }
    }

    @Nonnull
    @Override
    public String getTranslationKey(ItemStack item) {
        return "item." + en_USNames[item.getItemDamage()].toLowerCase(Locale.ROOT) + "Ingot";
    }
}