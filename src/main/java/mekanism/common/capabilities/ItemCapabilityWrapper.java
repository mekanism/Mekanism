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
        for (ItemCapability c : caps) {
            c.wrapper = this;
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
                    return LazyOptional.of(() -> cap).cast();
                }
            }
        }
        return LazyOptional.empty();
    }

    public static abstract class ItemCapability {

        private ItemCapabilityWrapper wrapper;

        public abstract boolean canProcess(Capability<?> capability);

        public ItemStack getStack() {
            return wrapper.itemStack;
        }
    }
}