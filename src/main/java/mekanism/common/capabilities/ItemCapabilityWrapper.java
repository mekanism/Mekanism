package mekanism.common.capabilities;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

public class ItemCapabilityWrapper implements ICapabilityProvider {

    protected final ItemStack itemStack;
    private final List<ItemCapability> capabilities = new ArrayList<>();

    public ItemCapabilityWrapper(ItemStack stack, ItemCapability... caps) {
        itemStack = stack;
        add(caps);
    }

    public void add(ItemCapability... caps) {
        for (ItemCapability c : caps) {
            c.wrapper = this;
            c.init();
            capabilities.add(c);
        }
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (!itemStack.isEmpty() && capability.isRegistered()) {
            //Only provide capabilities if we are not empty and the capability is registered
            // as if it isn't registered we can just short circuit and not look up the capability
            for (ItemCapability cap : capabilities) {
                if (cap.capabilityCache.isCapabilityDisabled(capability, null)) {
                    //Note: Currently no item capabilities have toggleable capabilities, but check anyway to properly support our API
                    return LazyOptional.empty();
                } else if (cap.capabilityCache.canResolve(capability)) {
                    //Make sure that we load any data the cap needs from the stack, as it doesn't have any NBT set when it is initially initialized
                    // This also allows us to update to any direct changes on the NBT of the stack that someone may have made
                    //TODO: Potentially move the loading to the capability initializing spot, as NBT shouldn't be randomly changing anyways
                    // and then that may allow us to better cache the capabilities
                    cap.load();
                    return cap.capabilityCache.getCapabilityUnchecked(capability, null);
                }
            }
        }
        return LazyOptional.empty();
    }

    public abstract static class ItemCapability {

        private final CapabilityCache capabilityCache = new CapabilityCache();
        private ItemCapabilityWrapper wrapper;

        protected abstract void addCapabilityResolvers(CapabilityCache capabilityCache);

        protected void init() {
            addCapabilityResolvers(capabilityCache);
        }

        protected void load() {
        }

        public ItemStack getStack() {
            return wrapper.itemStack;
        }
    }
}