package mekanism.common;

import javax.annotation.Nonnull;
import mekanism.common.registries.MekanismItems;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public class CreativeTabMekanism extends ItemGroup {

    public CreativeTabMekanism() {
        super(Mekanism.MODID);
    }

    @Nonnull
    @Override
    public ItemStack createIcon() {
        return MekanismItems.ATOMIC_ALLOY.getItemStack();
    }

    @Nonnull
    @Override
    public String getTranslationKey() {
        //Overwrite the lang key to match the one representing Mekanism
        return MekanismLang.MEKANISM.getTranslationKey();
    }
}