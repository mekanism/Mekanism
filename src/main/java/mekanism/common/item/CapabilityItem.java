package mekanism.common.item;

import java.util.ArrayList;
import java.util.List;
import mekanism.common.capabilities.ItemCapabilityWrapper;
import mekanism.common.capabilities.ItemCapabilityWrapper.ItemCapability;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public class CapabilityItem extends Item {

    protected CapabilityItem(Item.Properties properties) {
        super(properties);
    }

    protected void gatherCapabilities(List<ItemCapability> capabilities, ItemStack stack, CompoundTag nbt) {
    }

    @Override
    public final ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
        List<ItemCapability> capabilities = new ArrayList<>();
        gatherCapabilities(capabilities, stack, nbt);
        if (capabilities.isEmpty()) {
            return super.initCapabilities(stack, nbt);
        }
        return new ItemCapabilityWrapper(stack, capabilities.toArray(ItemCapability[]::new));
    }
}