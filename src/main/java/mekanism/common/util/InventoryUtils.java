package mekanism.common.util;

import java.util.Map;
import org.apache.commons.lang3.tuple.Pair;
import mekanism.api.EnumColor;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.content.transporter.InvStack;
import mekanism.common.content.transporter.TransitRequest;
import mekanism.common.content.transporter.TransitRequest.HashedItem;
import mekanism.common.content.transporter.TransitRequest.TransitResponse;
import mekanism.common.content.transporter.TransporterManager;
import mekanism.common.tile.TileEntityBin;
import mekanism.common.tile.TileEntityLogisticalSorter;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public final class InventoryUtils {

    public static final int[] EMPTY = new int[] {};

    public static int[] getIntRange(int start, int end) {
        int[] ret = new int[1 + end - start];

        for (int i = start; i <= end; i++) {
            ret[i - start] = i;
        }

        return ret;
    }

    public static IInventory checkChestInv(IInventory inv) {
        if (inv instanceof TileEntityChest) {
            TileEntityChest main = (TileEntityChest) inv;
            TileEntityChest adj = null;

            if (main.adjacentChestXNeg != null) {
                adj = main.adjacentChestXNeg;
            } else if (main.adjacentChestXPos != null) {
                adj = main.adjacentChestXPos;
            } else if (main.adjacentChestZNeg != null) {
                adj = main.adjacentChestZNeg;
            } else if (main.adjacentChestZPos != null) {
                adj = main.adjacentChestZPos;
            }

            if (adj != null) {
                return new InventoryLargeChest("", main, adj);
            }
        }

        return inv;
    }

    public static TransitResponse putStackInInventory(TileEntity tile, TransitRequest request, EnumFacing side,
            boolean force) {
        if (force && tile instanceof TileEntityLogisticalSorter) {
            return ((TileEntityLogisticalSorter) tile).sendHome(request.getSingleStack());
        }

        for (Map.Entry<HashedItem, Pair<Integer, Map<Integer, Integer>>> requestEntry : request.getItemMap().entrySet()) {
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
            } else if (tile instanceof ISidedInventory) {
                ISidedInventory sidedInventory = (ISidedInventory) tile;
                int[] slots = sidedInventory.getSlotsForFace(side.getOpposite());

                if (slots.length != 0) {
                    if (force && sidedInventory instanceof TileEntityBin && side == EnumFacing.UP) {
                        slots = sidedInventory.getSlotsForFace(EnumFacing.UP);
                    }

                    for (int get = 0; get <= slots.length - 1; get++) {
                        int slotID = slots[get];

                        if (!force) {
                            if (!sidedInventory.isItemValidForSlot(slotID, toInsert)
                                    || !sidedInventory.canInsertItem(slotID, toInsert, side.getOpposite())) {
                                continue;
                            }
                        }

                        ItemStack inSlot = sidedInventory.getStackInSlot(slotID);

                        if (inSlot.isEmpty()) {
                            if (toInsert.getCount() <= sidedInventory.getInventoryStackLimit()) {
                                sidedInventory.setInventorySlotContents(slotID, toInsert);
                                sidedInventory.markDirty();

                                return new TransitResponse(origInsert, requestEntry.getValue().getRight());
                            } else {
                                int rejects = toInsert.getCount() - sidedInventory.getInventoryStackLimit();

                                ItemStack toSet = toInsert.copy();
                                toSet.setCount(sidedInventory.getInventoryStackLimit());

                                ItemStack remains = toInsert.copy();
                                remains.setCount(rejects);

                                sidedInventory.setInventorySlotContents(slotID, toSet);
                                sidedInventory.markDirty();

                                toInsert = remains;
                            }
                        } else if (areItemsStackable(toInsert, inSlot) && inSlot.getCount() < Math
                                .min(inSlot.getMaxStackSize(), sidedInventory.getInventoryStackLimit())) {
                            int max = Math.min(inSlot.getMaxStackSize(), sidedInventory.getInventoryStackLimit());

                            if (inSlot.getCount() + toInsert.getCount() <= max) {
                                ItemStack toSet = toInsert.copy();
                                toSet.grow(inSlot.getCount());

                                sidedInventory.setInventorySlotContents(slotID, toSet);
                                sidedInventory.markDirty();

                                return new TransitResponse(origInsert, requestEntry.getValue().getRight());
                            } else {
                                int rejects = (inSlot.getCount() + toInsert.getCount()) - max;

                                ItemStack toSet = toInsert.copy();
                                toSet.setCount(max);

                                ItemStack remains = toInsert.copy();
                                remains.setCount(rejects);

                                sidedInventory.setInventorySlotContents(slotID, toSet);
                                sidedInventory.markDirty();

                                toInsert = remains;
                            }
                        }
                    }
                }
            } else if (tile instanceof IInventory) {
                IInventory inventory = checkChestInv((IInventory) tile);

                for (int i = 0; i <= inventory.getSizeInventory() - 1; i++) {
                    if (!force) {
                        if (!inventory.isItemValidForSlot(i, toInsert)) {
                            continue;
                        }
                    }

                    ItemStack inSlot = inventory.getStackInSlot(i);

                    if (inSlot.isEmpty()) {
                        if (toInsert.getCount() <= inventory.getInventoryStackLimit()) {
                            inventory.setInventorySlotContents(i, toInsert);
                            inventory.markDirty();

                            return new TransitResponse(origInsert, requestEntry.getValue().getRight());
                        } else {
                            int rejects = toInsert.getCount() - inventory.getInventoryStackLimit();

                            ItemStack toSet = toInsert.copy();
                            toSet.setCount(inventory.getInventoryStackLimit());

                            ItemStack remains = toInsert.copy();
                            remains.setCount(rejects);

                            inventory.setInventorySlotContents(i, toSet);
                            inventory.markDirty();

                            toInsert = remains;
                        }
                    } else if (areItemsStackable(toInsert, inSlot) && inSlot.getCount() < Math
                            .min(inSlot.getMaxStackSize(), inventory.getInventoryStackLimit())) {
                        int max = Math.min(inSlot.getMaxStackSize(), inventory.getInventoryStackLimit());

                        if (inSlot.getCount() + toInsert.getCount() <= max) {
                            ItemStack toSet = toInsert.copy();
                            toSet.grow(inSlot.getCount());

                            inventory.setInventorySlotContents(i, toSet);
                            inventory.markDirty();

                            return new TransitResponse(origInsert, requestEntry.getValue().getRight());
                        } else {
                            int rejects = (inSlot.getCount() + toInsert.getCount()) - max;

                            ItemStack toSet = toInsert.copy();
                            toSet.setCount(max);

                            ItemStack remains = toInsert.copy();
                            remains.setCount(rejects);

                            inventory.setInventorySlotContents(i, toSet);
                            inventory.markDirty();

                            toInsert = remains;
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
        } else if (tile instanceof ISidedInventory) {
            ISidedInventory sidedInventory = (ISidedInventory) tile;
            int[] slots = sidedInventory.getSlotsForFace(side.getOpposite());

            if (slots.length != 0) {
                for (int get = slots.length - 1; get >= 0; get--) {
                    int slotID = slots[get];

                    if (!sidedInventory.getStackInSlot(slotID).isEmpty()
                            && StackUtils.equalsWildcard(sidedInventory.getStackInSlot(slotID), type)) {
                        ItemStack stack = sidedInventory.getStackInSlot(slotID);
                        int current = !ret.getStack().isEmpty() ? ret.getStack().getCount() : 0;

                        if (current + stack.getCount() <= max) {
                            ItemStack copy = stack.copy();

                            if (sidedInventory.canExtractItem(slotID, copy, side.getOpposite())) {
                                ret.appendStack(slotID, copy);
                            }
                        } else {
                            ItemStack copy = stack.copy();

                            if (sidedInventory.canExtractItem(slotID, copy, side.getOpposite())) {
                                copy.setCount(max - current);
                                ret.appendStack(slotID, copy);
                            }
                        }

                        if (!ret.getStack().isEmpty() && ret.getStack().getCount() == max) {
                            return ret;
                        }
                    }
                }
            }
        } else if (tile instanceof IInventory) {
            IInventory inventory = checkChestInv((IInventory) tile);

            for (int i = inventory.getSizeInventory() - 1; i >= 0; i--) {
                if (!inventory.getStackInSlot(i).isEmpty()
                        && StackUtils.equalsWildcard(inventory.getStackInSlot(i), type)) {
                    ItemStack stack = inventory.getStackInSlot(i);
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
        } else if (tileEntity instanceof ISidedInventory) {
            ISidedInventory sidedInventory = (ISidedInventory) tileEntity;
            int[] slots = sidedInventory.getSlotsForFace(side.getOpposite());

            if (slots.length != 0) {
                if (force && sidedInventory instanceof TileEntityBin && side == EnumFacing.UP) {
                    slots = sidedInventory.getSlotsForFace(EnumFacing.UP);
                }

                for (int get = 0; get <= slots.length - 1; get++) {
                    int slotID = slots[get];

                    if (!force) {
                        if (!sidedInventory.isItemValidForSlot(slotID, itemStack)
                                || !sidedInventory.canInsertItem(slotID, itemStack, side.getOpposite())) {
                            continue;
                        }
                    }

                    ItemStack inSlot = sidedInventory.getStackInSlot(slotID);

                    if (inSlot.isEmpty()) {
                        if (itemStack.getCount() <= sidedInventory.getInventoryStackLimit()) {
                            return true;
                        } else {
                            int rejects = itemStack.getCount() - sidedInventory.getInventoryStackLimit();

                            if (rejects < itemStack.getCount()) {
                                return true;
                            }
                        }
                    } else if (areItemsStackable(itemStack, inSlot) && inSlot.getCount() < Math
                            .min(inSlot.getMaxStackSize(), sidedInventory.getInventoryStackLimit())) {
                        int max = Math.min(inSlot.getMaxStackSize(), sidedInventory.getInventoryStackLimit());

                        if (inSlot.getCount() + itemStack.getCount() <= max) {
                            return true;
                        } else {
                            int rejects = (inSlot.getCount() + itemStack.getCount()) - max;

                            if (rejects < itemStack.getCount()) {
                                return true;
                            }
                        }
                    }
                }
            }
        } else if (tileEntity instanceof IInventory) {
            IInventory inventory = checkChestInv((IInventory) tileEntity);

            for (int i = 0; i <= inventory.getSizeInventory() - 1; i++) {
                if (!force) {
                    if (!inventory.isItemValidForSlot(i, itemStack)) {
                        continue;
                    }
                }

                ItemStack inSlot = inventory.getStackInSlot(i);

                if (inSlot.isEmpty()) {
                    if (itemStack.getCount() <= inventory.getInventoryStackLimit()) {
                        return true;
                    } else {
                        int rejects = itemStack.getCount() - inventory.getInventoryStackLimit();

                        if (rejects < itemStack.getCount()) {
                            return true;
                        }
                    }
                } else if (areItemsStackable(itemStack, inSlot)
                        && inSlot.getCount() < Math.min(inSlot.getMaxStackSize(), inventory.getInventoryStackLimit())) {
                    int max = Math.min(inSlot.getMaxStackSize(), inventory.getInventoryStackLimit());

                    if (inSlot.getCount() + itemStack.getCount() <= max) {
                        return true;
                    } else {
                        int rejects = (inSlot.getCount() + itemStack.getCount()) - max;

                        if (rejects < itemStack.getCount()) {
                            return true;
                        }
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
