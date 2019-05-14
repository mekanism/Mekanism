package mekanism.common.item;

import java.util.Locale;
import javax.annotation.Nonnull;
import mekanism.common.Resource;
import mekanism.common.base.IMetaItem;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class ItemDust extends ItemMekanism implements IMetaItem {

    public ItemDust() {
        super();
        setHasSubtypes(true);
    }

    @Override
    public String getTexture(int meta) {
        Resource resource = Resource.get(meta);
        return resource != null ? resource.getName() + "Dust" : "Invalid";
    }

    @Override
    public int getVariants() {
        return Resource.values().length;
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
        Resource resource = Resource.get(item.getItemDamage());
        if (resource != null) {
            return "item." + resource.getName().toLowerCase(Locale.ROOT) + "Dust";
        }
        return "mekanism.invalid.dust";
    }
}