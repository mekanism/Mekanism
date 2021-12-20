package mekanism.common.lib.frequency;

import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.NBTConstants;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.lib.security.IOwnerItem;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.Constants.NBT;

public interface IFrequencyItem extends IOwnerItem {

    @Override
    default void setOwnerUUID(@Nonnull ItemStack stack, @Nullable UUID owner) {
        setFrequency(stack, null);
        IOwnerItem.super.setOwnerUUID(stack, owner);
    }

    @Nullable
    default FrequencyIdentity getFrequencyIdentity(ItemStack stack) {
        if (hasFrequency(stack)) {
            return FrequencyIdentity.load(getFrequencyType(), ItemDataUtils.getCompound(stack, NBTConstants.FREQUENCY));
        }
        return null;
    }

    default boolean hasFrequency(ItemStack stack) {
        return ItemDataUtils.hasData(stack, NBTConstants.FREQUENCY, NBT.TAG_COMPOUND);
    }

    @Nullable
    default Frequency getFrequency(ItemStack stack) {
        if (hasFrequency(stack)) {
            CompoundNBT frequencyCompound = ItemDataUtils.getCompound(stack, NBTConstants.FREQUENCY);
            FrequencyIdentity identity = FrequencyIdentity.load(getFrequencyType(), frequencyCompound);
            if (identity != null) {
                UUID owner;
                if (frequencyCompound.hasUUID(NBTConstants.OWNER_UUID)) {
                    //TODO - 1.18: Require the compound to actually have an owner uuid stored as well
                    // having a fallback to the tile's owner is mostly for properly loading legacy data
                    owner = frequencyCompound.getUUID(NBTConstants.OWNER_UUID);
                } else {
                    owner = getOwnerUUID(stack);
                    if (owner == null) {
                        return null;
                    }
                }
                return getFrequencyType().getManager(identity, owner).getFrequency(identity.getKey());
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