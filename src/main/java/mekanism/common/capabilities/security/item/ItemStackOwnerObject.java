package mekanism.common.capabilities.security.item;

import java.util.UUID;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.NBTConstants;
import mekanism.api.security.IOwnerObject;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.CapabilityCache;
import mekanism.common.capabilities.ItemCapabilityWrapper.ItemCapability;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
import mekanism.common.lib.frequency.IFrequencyItem;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.text.OwnerDisplay;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;

/**
 * Helper class for implementing owners on items
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
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
    protected void addCapabilityResolvers(CapabilityCache capabilityCache) {
        capabilityCache.addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.OWNER_OBJECT, this));
    }
}