package mekanism.common;

import mekanism.common.registries.MekanismItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CreativeTabMekanism extends CreativeModeTab {

    public CreativeTabMekanism() {
        super(Mekanism.MODID);
    }

    @NotNull
    @Override
    public ItemStack makeIcon() {
        return MekanismItems.ATOMIC_ALLOY.getItemStack();
    }

    @NotNull
    @Override
    public Component getDisplayName() {
        //Overwrite the lang key to match the one representing Mekanism
        return MekanismLang.MEKANISM.translate();
    }
}