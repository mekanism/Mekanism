package mekanism.common.capabilities.security.item;

import java.util.UUID;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.security.IOwnerObject;
import mekanism.common.lib.frequency.IFrequencyItem;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.text.OwnerDisplay;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Helper class for implementing owners on items
 */
@NothingNullByDefault
public class ItemStackOwnerObject implements IOwnerObject {

    protected final ItemStack stack;

    public ItemStackOwnerObject(ItemStack stack) {
        this.stack = stack;
    }

    @Nullable
    @Override
    public UUID getOwnerUUID() {
        return stack.isEmpty() ? null : ItemDataUtils.getUniqueID(stack, NBTConstants.OWNER_UUID);
    }

    @Nullable
    @Override
    public String getOwnerName() {
        UUID owner = getOwnerUUID();
        if (owner != null) {
            //Do our best effort to figure out what the owner's name is, but it is possible we won't be able to calculate one
            return OwnerDisplay.getOwnerName(MekanismUtils.tryGetClientPlayer(), owner, null);
        }
        return null;
    }

    @Override
    public void setOwnerUUID(@Nullable UUID owner) {
        if (!stack.isEmpty()) {
            if (stack.getItem() instanceof IFrequencyItem frequencyItem) {
                //If the item happens to be a frequency item reset the frequency when the owner changes
                frequencyItem.setFrequency(stack, null);
            }
            ItemDataUtils.setUUID(stack, NBTConstants.OWNER_UUID, owner);
        }
    }
}