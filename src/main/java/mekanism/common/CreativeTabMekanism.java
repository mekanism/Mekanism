package mekanism.common;

import javax.annotation.Nonnull;
import mekanism.common.registries.MekanismItems;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public class CreativeTabMekanism extends ItemGroup {

    public CreativeTabMekanism() {
        //TODO: I think this is lang string so rename it to a better format
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
        return MekanismLang.MEKANISM.getTranslationKey();
    }
}