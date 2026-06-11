package mekanism.common.inventory;

import java.util.UUID;
import java.util.function.Supplier;
import mekanism.common.util.RegistryUtils;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;
import org.jetbrains.annotations.Range;

public interface ISlotClickHandler {

    void onClick(Supplier<@Nullable IScrollableSlot> slotProvider, @MouseButtonInfo.MouseButton int button, boolean hasShiftDown, ItemStack heldItem);

    interface IScrollableSlot {

        ItemResource itemType();

        UUID itemUUID();

        @Range(from = 0, to = Long.MAX_VALUE)
        long count();

        default String getDisplayName() {
            return itemType().getHoverName().getString();
        }

        String getModID();

        default Identifier getRegistryName() {
            return RegistryUtils.getName(itemType().typeHolder(), BuiltInRegistries.ITEM);
        }
    }
}