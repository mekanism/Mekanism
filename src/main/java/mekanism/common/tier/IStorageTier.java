package mekanism.common.tier;

import java.util.function.Consumer;
import mekanism.api.text.EnumColor;
import mekanism.api.text.ILangEntry;
import mekanism.api.tier.BaseTier;
import mekanism.api.tier.ITier;
import mekanism.common.MekanismLang;
import mekanism.common.util.text.TextUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;

public interface IStorageTier extends ITier {

    long getCapacity();

    int getTransferRate();

    default boolean isCreative() {
        return getBaseTier() == BaseTier.CREATIVE;
    }

    default TextColor getTextColor() {
        return getBaseTier().getTextColor();
    }

    default void addCapacityTooltip(Consumer<Component> builder, ILangEntry capacityLang) {
        if (isCreative()) {
            builder.accept(MekanismLang.CAPACITY.translateColored(getTextColor(), EnumColor.GRAY, MekanismLang.INFINITE));
        } else {
            builder.accept(capacityLang.translateColored(getTextColor(), EnumColor.GRAY, TextUtils.format(getCapacity())));
        }
    }
}