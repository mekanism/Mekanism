package mekanism.common.item;

import javax.annotation.Nonnull;
import mekanism.common.Mekanism;
import mekanism.common.base.IMetaItem;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class ItemHDPE extends ItemMekanism implements IMetaItem {

    public ItemHDPE() {
        super();
        setHasSubtypes(true);
        setCreativeTab(Mekanism.tabMekanism);
    }

    @Override
    public String getTexture(int meta) {
        return PlasticItem.values()[meta].getName();
    }

    @Override
    public int getVariants() {
        return PlasticItem.values().length;
    }

    @Override
    public void getSubItems(@Nonnull CreativeTabs tabs, @Nonnull NonNullList<ItemStack> itemList) {
        if (!isInCreativeTab(tabs)) {
            return;
        }
        for (int counter = 0; counter < PlasticItem.values().length; counter++) {
            itemList.add(new ItemStack(this, 1, counter));
        }
    }

    @Nonnull
    @Override
    public String getTranslationKey(ItemStack item) {
        return "item." + PlasticItem.values()[item.getItemDamage()].getName();
    }

    public enum PlasticItem {
        PELLET("HDPEPellet"),
        ROD("HDPERod"),
        SHEET("HDPESheet"),
        STICK("PlaStick");

        private String name;

        PlasticItem(String itemName) {
            name = itemName;
        }

        public String getName() {
            return name;
        }
    }
}
