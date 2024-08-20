package mekanism.common.inventory;

import java.util.UUID;
import java.util.function.Supplier;
import mekanism.common.annotations.GLFWMouseButtons;
import mekanism.common.lib.inventory.HashedItem;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.RegistryUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface ISlotClickHandler {

    void onClick(Supplier<@Nullable IScrollableSlot> slotProvider, @GLFWMouseButtons int button, boolean hasShiftDown, ItemStack heldItem);

    interface IScrollableSlot {

        HashedItem item();

        UUID itemUUID();

        long count();

        default String getDisplayName() {
            return item().getInternalStack().getHoverName().getString();
        }

        default String getModID() {
            return MekanismUtils.getModId(item().getInternalStack());
        }

        default ResourceLocation getRegistryName() {
            return RegistryUtils.getName(item().getInternalStack().getItem());
        }
    }
}