package mekanism.common.item.interfaces;

import mekanism.api.text.EnumColor;
import mekanism.common.lib.frequency.IColorableFrequency;
import mekanism.common.registries.MekanismAttachmentTypes;
import net.minecraft.world.item.ItemStack;

public interface IColoredItem {

    default void syncColorWithFrequency(ItemStack stack) {
        EnumColor frequencyColor = stack.getData(MekanismAttachmentTypes.FREQUENCY_AWARE).getFrequency() instanceof IColorableFrequency frequency ? frequency.getColor() : null;
        stack.getData(MekanismAttachmentTypes.COLORABLE).setColor(frequencyColor);
    }
}