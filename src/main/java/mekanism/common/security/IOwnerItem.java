package mekanism.common.security;

import java.util.UUID;
import net.minecraft.item.ItemStack;

public interface IOwnerItem {

    UUID getOwnerUUID(ItemStack stack);

    void setOwnerUUID(ItemStack stack, UUID owner);

    boolean hasOwner(ItemStack stack);
}
