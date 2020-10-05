package mekanism.common.util;

import java.util.Optional;
import javax.annotation.Nullable;
import mekanism.common.Mekanism;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public final class InventoryUtils {

    private InventoryUtils() {
    }

    /**
     * Like {@link ItemHandlerHelper#canItemStacksStack(ItemStack, ItemStack)} but empty stacks mean equal (either param). Thiakil: not sure why.
     *
     * @param toInsert stack a
     * @param inSlot   stack b
     *
     * @return true if they are compatible
     */
    public static boolean areItemsStackable(ItemStack toInsert, ItemStack inSlot) {
        if (toInsert.isEmpty() || inSlot.isEmpty()) {
            return true;
        }
        return ItemHandlerHelper.canItemStacksStack(inSlot, toInsert);
    }

    @Nullable
    public static IItemHandler assertItemHandler(String desc, TileEntity tile, Direction side) {
        Optional<IItemHandler> capability = CapabilityUtils.getCapability(tile, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side).resolve();
        if (capability.isPresent()) {
            return capability.get();
        }
        Mekanism.logger.warn("'{}' was wrapped around a non-IItemHandler inventory. This should not happen!", desc, new Exception());
        if (tile == null) {
            Mekanism.logger.warn(" - null tile");
        } else {
            Mekanism.logger.warn(" - details: {} {}", tile, tile.getPos());
        }
        return null;
    }

    public static boolean isItemHandler(TileEntity tile, Direction side) {
        return CapabilityUtils.getCapability(tile, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side).isPresent();
    }
}