package mekanism.common.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.security.ISecurityUtils;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.SelectedWindowData;
import mekanism.common.item.interfaces.IDroppableContents;
import mekanism.common.lib.inventory.TileTransitRequest;
import net.minecraft.core.Direction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.common.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class InventoryUtils {

    private InventoryUtils() {
    }

    /**
     * Helper to drop the contents of an inventory when it is destroyed if it is public or the cause of the destruction has access to the inventory.
     */
    public static void dropItemContents(ItemEntity entity, DamageSource source) {
        ItemStack stack = entity.getItem();
        if (!entity.level().isClientSide && !stack.isEmpty() && stack.getItem() instanceof IDroppableContents inventory && inventory.canContentsDrop(stack)) {
            boolean shouldDrop;
            if (source.getEntity() instanceof Player player) {
                //If the destroyer is a player use security utils to properly check for access
                shouldDrop = ISecurityUtils.INSTANCE.canAccess(player, stack);
            } else {
                // otherwise, just check against there being no known player
                shouldDrop = ISecurityUtils.INSTANCE.canAccess(null, stack, false);
            }
            if (shouldDrop) {
                for (IInventorySlot slot : inventory.getDroppedSlots(stack)) {
                    if (!slot.isEmpty()) {
                        //Note: While some implementations return a dummy slot, so we would be able to pass them directly without copying
                        // we have implementations that pass the actual backing slot, so we have to copy the stack just in case
                        dropStack(slot.getStack().copy(), slotStack -> entity.level().addFreshEntity(new ItemEntity(entity.level(), entity.getX(), entity.getY(), entity.getZ(), slotStack)));
                    }
                }
            }
        }
    }

    /**
     * Helper to drop a stack that may potentially be oversized.
     *
     * @param stack   Item Stack to drop, may be passed directly to the dropper.
     * @param dropper Called to drop the item.
     */
    public static void dropStack(ItemStack stack, Consumer<ItemStack> dropper) {
        int count = stack.getCount();
        int max = stack.getMaxStackSize();
        if (count > max) {
            //If we have more than a stack of the item (such as we are a bin) or some other thing that allows for compressing
            // stack counts, drop as many stacks as we need at their max size
            while (count > max) {
                dropper.accept(stack.copyWithCount(max));
                count -= max;
            }
            if (count > 0) {
                //If we have anything left to drop afterward, do so
                dropper.accept(stack.copyWithCount(count));
            }
        } else {
            //If we have a valid stack, we can just directly drop that instead without requiring any copies
            dropper.accept(stack);
        }
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
    public static IItemHandler assertItemHandler(String desc, BlockEntity tile, Direction side) {
        Optional<IItemHandler> capability = CapabilityUtils.getCapability(tile, Capabilities.ITEM_HANDLER, side).resolve();
        if (capability.isPresent()) {
            return capability.get();
        }
        Mekanism.logger.warn("'{}' was wrapped around a non-IItemHandler inventory. This should not happen!", desc, new Exception());
        if (tile == null) {
            Mekanism.logger.warn(" - null tile");
        } else {
            Mekanism.logger.warn(" - details: {} {}", tile, tile.getBlockPos());
        }
        return null;
    }

    public static boolean isItemHandler(BlockEntity tile, Direction side) {
        return CapabilityUtils.getCapability(tile, Capabilities.ITEM_HANDLER, side).isPresent();
    }

    public static TileTransitRequest getEjectItemMap(BlockEntity tile, Direction side, List<IInventorySlot> slots) {
        return getEjectItemMap(new TileTransitRequest(tile, side), slots);
    }

    @Contract("_, _ -> param1")
    public static <REQUEST extends TileTransitRequest> REQUEST getEjectItemMap(REQUEST request, List<IInventorySlot> slots) {
        // shuffle the order we look at our slots to avoid ejection patterns
        List<IInventorySlot> shuffled = new ArrayList<>(slots);
        Collections.shuffle(shuffled);
        for (IInventorySlot slot : shuffled) {
            //Note: We are using EXTERNAL as that is what we actually end up using when performing the extraction in the end
            ItemStack simulatedExtraction = slot.extractItem(slot.getCount(), Action.SIMULATE, AutomationType.EXTERNAL);
            if (!simulatedExtraction.isEmpty()) {
                request.addItem(simulatedExtraction, slots.indexOf(slot));
            }
        }
        return request;
    }

    /**
     * Helper to first try inserting ignoring empty slots, and then insert not ignoring empty slots
     *
     * @param slots          Slots to insert into
     * @param stack          Stack to insert (do not modify).
     * @param action         The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     * @param automationType The method that this slot is being interacted from.
     *
     * @return Remainder
     *
     * @see ItemHandlerHelper#insertItemStacked(IItemHandler, ItemStack, boolean)
     */
    public static ItemStack insertItem(List<? extends IInventorySlot> slots, @NotNull ItemStack stack, Action action, AutomationType automationType) {
        stack = insertItem(slots, stack, true, false, action, automationType);
        return insertItem(slots, stack, false, false, action, automationType);
    }

    /**
     * Helper to try inserting a stack into a list of inventory slots only inserting into either empty slots or inserting into non-empty slots.
     *
     * @param slots          Slots to insert into
     * @param stack          Stack to insert (do not modify).
     * @param ignoreEmpty    {@code true} to ignore/skip empty slots, {@code false} to ignore/skip non-empty slots.
     * @param checkAll       {@code true} to check all slots regardless of empty state. When this is {@code true}, {@code ignoreEmpty} is ignored.
     * @param action         The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     * @param automationType The method that this slot is being interacted from.
     *
     * @return Remainder
     *
     * @see mekanism.common.inventory.container.MekanismContainer#insertItem(List, ItemStack, boolean, boolean, SelectedWindowData, Action)
     */
    @NotNull
    public static ItemStack insertItem(List<? extends IInventorySlot> slots, @NotNull ItemStack stack, boolean ignoreEmpty, boolean checkAll, Action action,
          AutomationType automationType) {
        if (stack.isEmpty()) {
            //Skip doing anything if the stack is already empty.
            // Makes it easier to chain calls, rather than having to check if the stack is empty after our previous call
            return stack;
        }
        for (IInventorySlot slot : slots) {
            if (!checkAll && ignoreEmpty == slot.isEmpty()) {
                //Skip checking empty stacks if we want to ignore them, and skip non-empty stacks if we don't want ot ignore them
                continue;
            }
            stack = slot.insertItem(stack, action, automationType);
            if (stack.isEmpty()) {
                break;
            }
        }
        return stack;
    }
}
