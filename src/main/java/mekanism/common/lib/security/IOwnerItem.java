package mekanism.common.lib.security;

import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.NBTConstants;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.item.ItemStack;

//TODO - V11: Convert this into being a capability and accessible from the API
public interface IOwnerItem {

    @Nullable
    default UUID getOwnerUUID(ItemStack stack) {
        if (ItemDataUtils.hasUUID(stack, NBTConstants.OWNER_UUID)) {
            return ItemDataUtils.getUniqueID(stack, NBTConstants.OWNER_UUID);
        }
        return null;
    }

    default void setOwnerUUID(@Nonnull ItemStack stack, @Nullable UUID owner) {
        if (owner == null) {
            ItemDataUtils.removeData(stack, NBTConstants.OWNER_UUID);
        } else {
            ItemDataUtils.setUUID(stack, NBTConstants.OWNER_UUID, owner);
        }
    }
}