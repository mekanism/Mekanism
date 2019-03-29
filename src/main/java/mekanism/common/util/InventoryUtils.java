package mekanism.common.util;

import java.util.Map;
import mekanism.api.EnumColor;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.content.transporter.InvStack;
import mekanism.common.content.transporter.TransitRequest;
import mekanism.common.content.transporter.TransitRequest.HashedItem;
import mekanism.common.content.transporter.TransitRequest.TransitResponse;
import mekanism.common.content.transporter.TransporterManager;
import mekanism.common.tile.TileEntityLogisticalSorter;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
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

    public static TransitResponse putStackInInventory(TileEntity tile, TransitRequest request, EnumFacing side,
          boolean force) {
        if (force && tile instanceof TileEntityLogisticalSorter) {
            return ((TileEntityLogisticalSorter) tile).sendHome(request.getSingleStack());
        }

        for (Map.Entry<HashedItem, Pair<Integer, Map<Integer, Integer>>> requestEntry : request.getItemMap()
              .entrySet()) {
            ItemStack origInsert = StackUtils.size(requestEntry.getKey().getStack(), requestEntry.getValue().getLeft());
            ItemStack toInsert = origInsert.copy();

            if (isItemHandler(tile, side.getOpposite())) {
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
            }

            if (TransporterManager.didEmit(origInsert, toInsert)) {
                return new TransitResponse(TransporterManager.getToUse(origInsert, toInsert),
                      requestEntry.getValue().getRight());
            }
        }

        return TransitResponse.EMPTY;
    }

    public static boolean areItemsStackable(ItemStack toInsert, ItemStack inSlot) {
        if (toInsert.isEmpty() || inSlot.isEmpty()) {
            return true;
        }

        return inSlot.isItemEqual(toInsert) && ItemStack.areItemStackTagsEqual(inSlot, toInsert);
    }

    public static InvStack takeDefinedItem(TileEntity tile, EnumFacing side, ItemStack type, int min, int max) {
        InvStack ret = new InvStack(tile, side.getOpposite());

        if (isItemHandler(tile, side.getOpposite())) {
            IItemHandler inventory = getItemHandler(tile, side.getOpposite());

            for (int i = inventory.getSlots() - 1; i >= 0; i--) {
                ItemStack stack = inventory.extractItem(i, max, true);

                if (!stack.isEmpty() && StackUtils.equalsWildcard(stack, type)) {
                    int current = !ret.getStack().isEmpty() ? ret.getStack().getCount() : 0;

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
        }

        if (!ret.getStack().isEmpty() && ret.getStack().getCount() >= min) {
            return ret;
        }

        return null;
    }

    public static boolean canInsert(TileEntity tileEntity, EnumColor color, ItemStack itemStack, EnumFacing side,
          boolean force) {
        if (force && tileEntity instanceof TileEntityLogisticalSorter) {
            return ((TileEntityLogisticalSorter) tileEntity).canSendHome(itemStack);
        }

        if (!force && tileEntity instanceof ISideConfiguration) {
            ISideConfiguration config = (ISideConfiguration) tileEntity;
            EnumFacing tileSide = config.getOrientation();
            EnumColor configColor =
                  config.getEjector().getInputColor(MekanismUtils.getBaseOrientation(side, tileSide).getOpposite());

            if (config.getEjector().hasStrictInput() && configColor != null && configColor != color) {
                return false;
            }
        }

        if (isItemHandler(tileEntity, side.getOpposite())) {
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
        }

        return false;
    }

    public static boolean isItemHandler(TileEntity tile, EnumFacing side) {
        return CapabilityUtils.hasCapability(tile, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
    }

    public static IItemHandler getItemHandler(TileEntity tile, EnumFacing side) {
        return CapabilityUtils.getCapability(tile, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
    }

    /* TODO From CCLib -- go back to that version when we're using dependencies again */
    public static boolean canStack(ItemStack stack1, ItemStack stack2) {
        return stack1.isEmpty() || stack2.isEmpty()
              || (stack1.getItem() == stack2.getItem()
              && (!stack2.getHasSubtypes() || stack2.getItemDamage() == stack1.getItemDamage())
              && ItemStack.areItemStackTagsEqual(stack2, stack1)) && stack1.isStackable();
    }
}
