package mekanism.common.lib.frequency;

import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.NBTConstants;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.lib.security.IOwnerItem;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.Constants.NBT;

public interface IFrequencyItem extends IOwnerItem {

    @Override
    default void setOwnerUUID(@Nonnull ItemStack stack, @Nullable UUID owner) {
        setFrequency(stack, null);
        IOwnerItem.super.setOwnerUUID(stack, owner);
    }

    default FrequencyIdentity getFrequency(ItemStack stack) {
        if (ItemDataUtils.hasData(stack, NBTConstants.FREQUENCY, NBT.TAG_COMPOUND)) {
            return FrequencyIdentity.load(FrequencyType.TELEPORTER, ItemDataUtils.getCompound(stack, NBTConstants.FREQUENCY));
        }
        return null;
    }

    default void setFrequency(ItemStack stack, Frequency frequency) {
        if (frequency == null) {
            ItemDataUtils.removeData(stack, NBTConstants.FREQUENCY);
        } else {
            ItemDataUtils.setCompound(stack, NBTConstants.FREQUENCY, frequency.serializeIdentity());
        }
    }
}
