package mekanism.common.capabilities.security.item;

import java.util.UUID;
import java.util.function.Consumer;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.security.IOwnerObject;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.ItemCapabilityWrapper.ItemCapability;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
import mekanism.common.capabilities.resolver.ICapabilityResolver;
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
public class ItemStackOwnerObject extends ItemCapability implements IOwnerObject {

    @Nullable
    @Override
    public UUID getOwnerUUID() {
        ItemStack stack = getStack();
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
        ItemStack stack = getStack();
        if (!stack.isEmpty()) {
            if (stack.getItem() instanceof IFrequencyItem frequencyItem) {
                //If the item happens to be a frequency item reset the frequency when the owner changes
                frequencyItem.setFrequency(stack, null);
            }
            ItemDataUtils.setUUID(stack, NBTConstants.OWNER_UUID, owner);
        }
    }

    @Override
    protected void gatherCapabilityResolvers(Consumer<ICapabilityResolver> consumer) {
        consumer.accept(BasicCapabilityResolver.constant(Capabilities.OWNER_OBJECT, this));
    }
}