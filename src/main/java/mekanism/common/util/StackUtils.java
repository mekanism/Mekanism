package mekanism.common.util;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.Action;
import mekanism.api.inventory.IInventorySlot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class StackUtils {

    private StackUtils() {
    }

    //TODO: Evaluate moving remainder of uses to copyWithCount. This method mainly is just useful for better handling when size is <= 0
    public static ItemStack size(ItemStack stack, int size) {
        return size <= 0 ? ItemStack.EMPTY : stack.copyWithCount(size);
    }

    public static List<ItemStack> merge(@NotNull List<IInventorySlot> orig, @NotNull List<IInventorySlot> toAdd) {
        List<ItemStack> rejects = new ArrayList<>();
        merge(orig, toAdd, rejects);
        return rejects;
    }

    public static void merge(@NotNull List<IInventorySlot> orig, @NotNull List<IInventorySlot> toAdd, List<ItemStack> rejects) {
        StorageUtils.validateSizeMatches(orig, toAdd, "slot");
        for (int i = 0; i < toAdd.size(); i++) {
            IInventorySlot toAddSlot = toAdd.get(i);
            if (!toAddSlot.isEmpty()) {
                IInventorySlot origSlot = orig.get(i);
                ItemStack toAddStack = toAddSlot.getStack();
                if (origSlot.isEmpty()) {
                    int max = origSlot.getLimit(toAddStack);
                    if (toAddStack.getCount() <= max) {
                        origSlot.setStack(toAddStack);
                    } else {
                        origSlot.setStack(toAddStack.copyWithCount(max));
                        //Add any remainder to the rejects (if this is zero this will no-op
                        addStack(rejects, toAddStack.copyWithCount(toAddStack.getCount() - max));
                    }
                } else if (ItemStack.isSameItemSameComponents(origSlot.getStack(), toAddStack)) {
                    int added = origSlot.growStack(toAddStack.getCount(), Action.EXECUTE);
                    //Add any remainder to the rejects (if this is zero this will no-op
                    addStack(rejects, toAddStack.copyWithCount(toAddStack.getCount() - added));
                } else {
                    addStack(rejects, toAddStack.copy());
                }
            }
        }
    }

    /**
     * @implNote Assumes the passed in stack and stacks in the list can be safely modified.
     */
    private static void addStack(List<ItemStack> stacks, ItemStack stack) {
        if (!stack.isEmpty()) {
            for (ItemStack existingStack : stacks) {
                int needed = existingStack.getMaxStackSize() - existingStack.getCount();
                if (needed > 0 && ItemStack.isSameItemSameComponents(existingStack, stack)) {
                    //This stack needs some items and can stack with the one we are adding
                    int toAdd = Math.min(needed, stack.getCount());
                    //Add the amount we can
                    existingStack.grow(toAdd);
                    stack.shrink(toAdd);
                    //And break out of checking as even if we have any remaining it won't be able to stack with later things
                    // as they will have a different type or this existing stack would have already been full
                    break;
                }
            }
            if (!stack.isEmpty()) {
                //If we have any we weren't able to add to existing stacks, we need to go ahead and add it as new stacks
                // making sure to split it if it is an oversized stack
                int count = stack.getCount();
                int max = stack.getMaxStackSize();
                if (count > max) {
                    //If we have more than a stack of the item stack counts,
                    int excess = count % max;
                    int stacksToAdd = count / max;
                    if (excess > 0) {
                        // start by adding any excess that won't go into full stack sizes (so that we have a lower index
                        // and have less to iterate when adding more of the same type)
                        stacks.add(stack.copyWithCount(excess));
                    }
                    // and then add as many max size stacks as needed
                    ItemStack maxSize = stack.copyWithCount(max);
                    stacks.add(maxSize);
                    for (int i = 1; i < stacksToAdd; i++) {
                        stacks.add(maxSize.copy());
                    }
                } else {
                    //Valid stack, just add it directly
                    stacks.add(stack);
                }
            }
        }
    }

    /**
     * Get state for placement for a generic item, with our fake player
     *
     * @param stack  the item to place
     * @param pos    where
     * @param player our fake player, usually
     *
     * @return the result of {@link Block#getStateForPlacement(BlockPlaceContext)}, or null if it cannot be placed in that location
     */
    @Nullable
    public static BlockState getStateForPlacement(ItemStack stack, BlockPos pos, Player player) {
        return Block.byItem(stack.getItem()).getStateForPlacement(new BlockPlaceContext(new UseOnContext(player, InteractionHand.MAIN_HAND,
              new BlockHitResult(Vec3.ZERO, Direction.UP, pos, false))));
    }
}