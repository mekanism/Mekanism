package mekanism.common.item.interfaces;

import java.util.Optional;
import mekanism.common.lib.frequency.IColorableFrequency;
import mekanism.common.registries.MekanismAttachmentTypes;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.attachment.IAttachmentHolder;

public interface IColoredItem {

    default void syncColorWithFrequency(ItemStack stack) {
        if (stack.getData(MekanismAttachmentTypes.FREQUENCY_AWARE).getFrequency() instanceof IColorableFrequency frequency) {
            stack.setData(MekanismAttachmentTypes.COLORABLE, Optional.of(frequency.getColor()));
        } else {
            stack.removeData(MekanismAttachmentTypes.COLORABLE);
        }
    }

    static boolean supports(IAttachmentHolder attachmentHolder) {
        return attachmentHolder instanceof ItemStack stack && stack.getItem() instanceof IColoredItem;
    }
}