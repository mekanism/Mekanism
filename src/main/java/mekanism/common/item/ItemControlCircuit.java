package mekanism.common.item;

import javax.annotation.Nonnull;
import mekanism.common.base.IMetaItem;
import mekanism.common.tier.BaseTier;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class ItemControlCircuit extends ItemMekanism implements IMetaItem {

    public ItemControlCircuit() {
        super();
        setHasSubtypes(true);
    }

    @Override
    public String getTexture(int meta) {
        return BaseTier.get(meta).getSimpleName() + "ControlCircuit";
    }

    @Override
    public int getVariants() {
        return BaseTier.values().length - 1;
    }

    @Override
    public void getSubItems(@Nonnull CreativeTabs tabs, @Nonnull NonNullList<ItemStack> itemList) {
        if (isInCreativeTab(tabs)) {
            for (BaseTier tier : BaseTier.values()) {
                if (tier.isObtainable()) {
                    itemList.add(new ItemStack(this, 1, tier.ordinal()));
                }
            }
        }
    }

    @Nonnull
    @Override
    public String getTranslationKey(ItemStack item) {
        return "item." + BaseTier.get(item.getItemDamage()).getSimpleName() + "ControlCircuit";
    }
}