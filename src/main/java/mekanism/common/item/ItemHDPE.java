package mekanism.common.item;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
        PlasticItem plasticItem = PlasticItem.get(meta);
        return plasticItem == null ? "Invalid" : plasticItem.getName();
    }

    @Override
    public int getVariants() {
        return PlasticItem.values().length;
    }

    @Override
    public void getSubItems(@Nonnull CreativeTabs tabs, @Nonnull NonNullList<ItemStack> itemList) {
        if (isInCreativeTab(tabs)) {
            for (int counter = 0; counter < getVariants(); counter++) {
                itemList.add(new ItemStack(this, 1, counter));
            }
        }
    }

    @Nonnull
    @Override
    public String getTranslationKey(ItemStack item) {
        PlasticItem plasticItem = PlasticItem.get(item.getItemDamage());
        return plasticItem == null ? "Invalid" : "item." + plasticItem.getName();
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

        @Nullable
        public static PlasticItem get(int index) {
            if (index < 0 || index >= values().length) {
                return null;
            }
            return values()[index];
        }

        public String getName() {
            return name;
        }
    }
}