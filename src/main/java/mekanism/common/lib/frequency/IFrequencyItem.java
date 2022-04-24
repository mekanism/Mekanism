package mekanism.common.lib.frequency;

import javax.annotation.Nullable;
import mekanism.api.NBTConstants;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

public interface IFrequencyItem {

    @Nullable
    default FrequencyIdentity getFrequencyIdentity(ItemStack stack) {
        if (hasFrequency(stack)) {
            return FrequencyIdentity.load(getFrequencyType(), ItemDataUtils.getCompound(stack, NBTConstants.FREQUENCY));
        }
        return null;
    }

    default boolean hasFrequency(ItemStack stack) {
        return ItemDataUtils.hasData(stack, NBTConstants.FREQUENCY, Tag.TAG_COMPOUND);
    }

    @Nullable
    default Frequency getFrequency(ItemStack stack) {
        if (hasFrequency(stack)) {
            CompoundTag frequencyCompound = ItemDataUtils.getCompound(stack, NBTConstants.FREQUENCY);
            FrequencyIdentity identity = FrequencyIdentity.load(getFrequencyType(), frequencyCompound);
            if (identity != null && frequencyCompound.hasUUID(NBTConstants.OWNER_UUID)) {
                return getFrequencyType().getManager(identity, frequencyCompound.getUUID(NBTConstants.OWNER_UUID)).getFrequency(identity.key());
            }
        }
        return null;
    }

    default void setFrequency(ItemStack stack, Frequency frequency) {
        if (frequency == null) {
            ItemDataUtils.removeData(stack, NBTConstants.FREQUENCY);
        } else {
            ItemDataUtils.setCompound(stack, NBTConstants.FREQUENCY, frequency.serializeIdentityWithOwner());
        }
    }

    FrequencyType<?> getFrequencyType();
}