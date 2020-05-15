package mekanism.common.util;

import java.util.Map.Entry;
import java.util.Optional;
import javax.annotation.Nullable;
import mekanism.api.RelativeSide;
import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.content.transporter.Finder;
import mekanism.common.content.transporter.HashedItem;
import mekanism.common.content.transporter.InvStack;
import mekanism.common.content.transporter.TransitRequest;
import mekanism.common.content.transporter.TransitRequest.SlotData;
import mekanism.common.content.transporter.TransitRequest.TransitResponse;
import mekanism.common.content.transporter.TransporterManager;
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
            return ((TileEntityLogisticalSorter) tile).sendHome(request.getSingleStack());
        }
        if (request.getItemMap().isEmpty()) {
            return request.getEmptyResponse();
        }
        Optional<IItemHandler> capability = MekanismUtils.toOptional(CapabilityUtils.getCapability(tile, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side.getOpposite()));
        if (capability.isPresent()) {
            IItemHandler inventory = capability.get();
            for (Entry<HashedItem, SlotData> requestEntry : request.getItemMap().entrySet()) {
                ItemStack origInsert = StackUtils.size(requestEntry.getKey().getStack(), requestEntry.getValue().getTotalCount());
                ItemStack toInsert = origInsert.copy();
                for (int i = 0; i < inventory.getSlots(); i++) {
                    // Check validation
                    if (inventory.isItemValid(i, toInsert)) {
                        // Do insert
                        toInsert = inventory.insertItem(i, toInsert, false);
                        // If empty, end
                        if (toInsert.isEmpty()) {
                            return request.createResponse(origInsert, requestEntry.getValue());
                        }
                    }
                }
                if (TransporterManager.didEmit(origInsert, toInsert)) {
                    return request.createResponse(TransporterManager.getToUse(origInsert, toInsert), requestEntry.getValue());
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

    public static InvStack takeTopStack(TileEntity tile, Direction side, Finder id, int amount) {
        Optional<IItemHandler> capability = MekanismUtils.toOptional(CapabilityUtils.getCapability(tile, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side.getOpposite()));
        if (capability.isPresent()) {
            IItemHandler inventory = capability.get();
            InvStack ret = new InvStack(tile, side.getOpposite());
            for (int slot = inventory.getSlots() - 1; slot >= 0; slot--) {
                ItemStack stack = inventory.extractItem(slot, amount, true);
                if (!stack.isEmpty() && id.modifies(stack) && (ret.getStack().isEmpty() || ItemHandlerHelper.canItemStacksStack(stack, ret.getStack()))) {
                    ret.appendStack(slot, StackUtils.size(stack, Math.min(stack.getCount(), amount - ret.getCount())));
                    if (ret.getCount() == amount) {
                        return ret;
                    }
                }
            }
            return ret;
        }
        return null;
    }

    public static InvStack takeDefinedItem(TileEntity tile, Direction side, ItemStack type, int min, int max) {
        Optional<IItemHandler> capability = MekanismUtils.toOptional(CapabilityUtils.getCapability(tile, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side.getOpposite()));
        if (capability.isPresent()) {
            InvStack ret = new InvStack(tile, side.getOpposite());
            IItemHandler inventory = capability.get();
            for (int i = inventory.getSlots() - 1; i >= 0; i--) {
                ItemStack stack = inventory.extractItem(i, max, true);
                if (!stack.isEmpty() && ItemHandlerHelper.canItemStacksStack(stack, type)) {
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
        }
        return null;
    }

    public static boolean canInsert(TileEntity tile, EnumColor color, ItemStack itemStack, Direction side, boolean force) {
        if (force && tile instanceof TileEntityLogisticalSorter) {
            return ((TileEntityLogisticalSorter) tile).canSendHome(itemStack);
        }
        if (!force && tile instanceof ISideConfiguration) {
            ISideConfiguration config = (ISideConfiguration) tile;
            if (config.getEjector().hasStrictInput()) {
                Direction tileSide = config.getOrientation();
                EnumColor configColor = config.getEjector().getInputColor(RelativeSide.fromDirections(tileSide, side.getOpposite()));
                if (configColor != null && configColor != color) {
                    return false;
                }
            }
        }
        Optional<IItemHandler> capability = MekanismUtils.toOptional(CapabilityUtils.getCapability(tile, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side.getOpposite()));
        if (capability.isPresent()) {
            IItemHandler inventory = capability.get();
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