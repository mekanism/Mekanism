package mekanism.common.item;

import mekanism.common.Tier.BaseTier;
import mekanism.common.base.IMetaItem;
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
        return BaseTier.values()[meta].getSimpleName() + "ControlCircuit";
    }

    @Override
    public int getVariants() {
        return BaseTier.values().length - 1;
    }

    @Override
    public void getSubItems(CreativeTabs tabs, NonNullList<ItemStack> itemList) {
        if (!isInCreativeTab(tabs)) {
            return;
        }
        for (BaseTier tier : BaseTier.values()) {
            if (tier.isObtainable()) {
                itemList.add(new ItemStack(this, 1, tier.ordinal()));
            }
        }
    }

    @Override
    public String getUnlocalizedName(ItemStack item) {
        return "item." + BaseTier.values()[item.getItemDamage()].getSimpleName() + "ControlCircuit";
    }
}
