package mekanism.common.util;

import java.util.Map;
import java.util.Map.Entry;
import mekanism.api.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.content.transporter.HashedItem;
import mekanism.common.content.transporter.InvStack;
import mekanism.common.content.transporter.TransitRequest;
import mekanism.common.content.transporter.TransitRequest.TransitResponse;
import mekanism.common.content.transporter.TransporterManager;
import mekanism.common.tile.TileEntityLogisticalSorter;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.apache.commons.lang3.tuple.Pair;

public final class InventoryUtils {

    public static final int[] EMPTY = new int[]{};

    public static int[] getIntRange(int start, int end) {
        int[] ret = new int[1 + end - start];
        for (int i = start; i <= end; i++) {
            ret[i - start] = i;
        }
        return ret;
    }

    public static TransitResponse putStackInInventory(TileEntity tile, TransitRequest request, Direction side, boolean force) {
        if (force && tile instanceof TileEntityLogisticalSorter) {
            return ((TileEntityLogisticalSorter) tile).sendHome(request.getSingleStack());
        }
        for (Entry<HashedItem, Pair<Integer, Map<Integer, Integer>>> requestEntry : request.getItemMap().entrySet()) {
            ItemStack origInsert = StackUtils.size(requestEntry.getKey().getStack(), requestEntry.getValue().getLeft());
            ItemStack toInsert = origInsert.copy();
            if (!isItemHandler(tile, side.getOpposite())) {
                return TransitResponse.EMPTY;
            }
            IItemHandler inventory = getItemHandler(tile, side.getOpposite());
            for (int i = 0; i < inventory.getSlots(); i++) {
                // Check validation
                if (inventory.isItemValid(i, toInsert)) {
                    // Do insert
                    toInsert = inventory.insertItem(i, toInsert, false);

                    // If empty, end
                    if (toInsert.isEmpty()) {
                        return new TransitResponse(origInsert, requestEntry.getValue().getRight());
                    }
                }
            }
            if (TransporterManager.didEmit(origInsert, toInsert)) {
                return new TransitResponse(TransporterManager.getToUse(origInsert, toInsert), requestEntry.getValue().getRight());
            }
        }
        return TransitResponse.EMPTY;
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

    public static InvStack takeDefinedItem(TileEntity tile, Direction side, ItemStack type, int min, int max) {
        InvStack ret = new InvStack(tile, side.getOpposite());
        if (!isItemHandler(tile, side.getOpposite())) {
            return null;
        }

        IItemHandler inventory = getItemHandler(tile, side.getOpposite());
        for (int i = inventory.getSlots() - 1; i >= 0; i--) {
            ItemStack stack = inventory.extractItem(i, max, true);
            if (!stack.isEmpty() && StackUtils.equalsWildcardWithNBT(stack, type)) {
                int current = ret.getStack().getCount();
                if (current + stack.getCount() <= max) {
                    ret.appendStack(i, stack.copy());
                } else {
                    ItemStack copy = stack.copy();
                    copy.setCount(max - current);
                    ret.appendStack(i, copy);
                }
                if (!ret.getStack().isEmpty() && ret.getStack().getCount() == max) {
                    return ret;
                }
            }
        }
        if (!ret.getStack().isEmpty() && ret.getStack().getCount() >= min) {
            return ret;
        }
        return null;
    }

    public static boolean canInsert(TileEntity tileEntity, EnumColor color, ItemStack itemStack, Direction side, boolean force) {
        if (force && tileEntity instanceof TileEntityLogisticalSorter) {
            return ((TileEntityLogisticalSorter) tileEntity).canSendHome(itemStack);
        }
        if (!force && tileEntity instanceof ISideConfiguration) {
            ISideConfiguration config = (ISideConfiguration) tileEntity;
            if (config.getEjector().hasStrictInput()) {
                Direction tileSide = config.getOrientation();
                EnumColor configColor = config.getEjector().getInputColor(MekanismUtils.getBaseOrientation(side, tileSide).getOpposite());
                if (configColor != null && configColor != color) {
                    return false;
                }
            }
        }
        if (!isItemHandler(tileEntity, side.getOpposite())) {
            return false;
        }

        IItemHandler inventory = getItemHandler(tileEntity, side.getOpposite());
        for (int i = 0; i < inventory.getSlots(); i++) {
            // Check validation
            if (inventory.isItemValid(i, itemStack)) {
                // Simulate insert
                ItemStack rejects = inventory.insertItem(i, itemStack, true);
                if (TransporterManager.didEmit(itemStack, rejects)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean assertItemHandler(String desc, TileEntity tileEntity, Direction side) {
        if (!isItemHandler(tileEntity, side)) {
            Mekanism.logger.warn("'" + desc + "' was wrapped around a non-IItemHandler inventory. This should not happen!", new Exception());
            if (tileEntity == null) {
                Mekanism.logger.warn(" - null tile");
            } else {
                Mekanism.logger.warn(" - details: " + tileEntity + " " + tileEntity.getPos());
            }
            return false;
        }
        return true;
    }

    public static boolean isItemHandler(TileEntity tile, Direction side) {
        //TODO: Remove this?? given sometimes it may make more sense to just get and keep value
        return CapabilityUtils.getCapabilityHelper(tile, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side).isPresent();
    }

    public static IItemHandler getItemHandler(TileEntity tile, Direction side) {
        //TODO: Do this better
        return CapabilityUtils.getCapabilityHelper(tile, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side).getValue();
    }
}