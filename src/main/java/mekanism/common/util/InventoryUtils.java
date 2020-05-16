package mekanism.common.util;

import java.util.Optional;
import javax.annotation.Nullable;
import mekanism.common.Mekanism;
import mekanism.common.content.transporter.TransporterManager;
import mekanism.common.lib.inventory.TransitRequest;
import mekanism.common.lib.inventory.TransitRequest.ItemData;
import mekanism.common.lib.inventory.TransitRequest.TransitResponse;
import mekanism.common.tile.TileEntityLogisticalSorter;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public final class InventoryUtils {

    public static TransitResponse putStackInInventory(TileEntity tile, TransitRequest request, Direction side, boolean force) {
        if (force && tile instanceof TileEntityLogisticalSorter) {
            return ((TileEntityLogisticalSorter) tile).sendHome(request);
        }
        if (request.isEmpty()) {
            return request.getEmptyResponse();
        }
        Optional<IItemHandler> capability = MekanismUtils.toOptional(CapabilityUtils.getCapability(tile, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side.getOpposite()));
        if (capability.isPresent()) {
            IItemHandler inventory = capability.get();
            for (ItemData data : request.getItemData()) {
                ItemStack origInsert = StackUtils.size(data.getStack(), data.getTotalCount());
                ItemStack toInsert = origInsert.copy();
                for (int i = 0; i < inventory.getSlots(); i++) {
                    // Check validation
                    if (inventory.isItemValid(i, toInsert)) {
                        // Do insert
                        toInsert = inventory.insertItem(i, toInsert, false);
                        // If empty, end
                        if (toInsert.isEmpty()) {
                            return request.createResponse(origInsert, data);
                        }
                    }
                }
                if (TransporterManager.didEmit(origInsert, toInsert)) {
                    return request.createResponse(TransporterManager.getToUse(origInsert, toInsert), data);
                }
            }
        }
        return request.getEmptyResponse();
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
        Optional<IItemHandler> capability = MekanismUtils.toOptional(CapabilityUtils.getCapability(tile, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side));
        if (capability.isPresent()) {
            return capability.get();
        }
        Mekanism.logger.warn("'" + desc + "' was wrapped around a non-IItemHandler inventory. This should not happen!", new Exception());
        if (tile == null) {
            Mekanism.logger.warn(" - null tile");
        } else {
            Mekanism.logger.warn(" - details: " + tile + " " + tile.getPos());
        }
        return null;
    }

    public static boolean isItemHandler(TileEntity tile, Direction side) {
        return CapabilityUtils.getCapability(tile, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side).isPresent();
    }
}