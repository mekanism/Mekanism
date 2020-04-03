package mekanism.common.capabilities;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

public class ItemCapabilityWrapper implements ICapabilityProvider {

    protected final ItemStack itemStack;
    private List<ItemCapability> capabilities = new ArrayList<>();

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
        if (!itemStack.isEmpty()) {
            //Only provide capabilities if we are not empty
            for (ItemCapability cap : capabilities) {
                if (cap.canProcess(capability)) {
                    //Make sure that we load any data the cap needs from the stack, as it doesn't have any NBT set when it is initially initialized
                    // This also allows us to update to any direct changes on the NBT of the stack that someone may have made
                    cap.load();
                    return cap.getMatchingCapability(capability);
                }
            }
        }
        return LazyOptional.empty();
    }

    public static abstract class ItemCapability {

        private ItemCapabilityWrapper wrapper;

        public abstract boolean canProcess(Capability<?> capability);

        protected void init() {
        }

        protected void load() {
        }

        public ItemStack getStack() {
            return wrapper.itemStack;
        }

        /**
         * Note: it is expected that canProcess is called before this
         */
        public <T> LazyOptional<T> getMatchingCapability(@Nonnull Capability<T> capability) {
            return LazyOptional.of(() -> this).cast();
        }
    }
}