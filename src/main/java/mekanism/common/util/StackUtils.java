package mekanism.common.util;

import java.util.ArrayList;
import java.util.Collections;
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
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class StackUtils {

    private StackUtils() {
    }

    public static ItemStack size(ItemStack stack, int size) {
        if (size <= 0 || stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return ItemHandlerHelper.copyStackWithSize(stack, size);
    }

    public static List<ItemStack> merge(@NotNull List<IInventorySlot> orig, @NotNull List<IInventorySlot> toAdd) {
        List<ItemStack> rejects = new ArrayList<>();
        merge(orig, toAdd, rejects);
        return rejects;
    }

    public static void merge(@NotNull List<IInventorySlot> orig, @NotNull List<IInventorySlot> toAdd, List<ItemStack> rejects) {
        if (orig.size() != toAdd.size()) {
            throw new IllegalArgumentException("Mismatched slot count");
        }
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
                        origSlot.setStack(size(toAddStack, max));
                        //Add any remainder to the rejects (if this is zero this will no-op
                        addStack(rejects, size(toAddStack, toAddStack.getCount() - max));
                    }
                } else if (ItemHandlerHelper.canItemStacksStack(origSlot.getStack(), toAddStack)) {
                    int added = origSlot.growStack(toAddStack.getCount(), Action.EXECUTE);
                    //Add any remainder to the rejects (if this is zero this will no-op
                    addStack(rejects, size(toAddStack, toAddStack.getCount() - added));
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
                if (needed > 0 && ItemHandlerHelper.canItemStacksStack(existingStack, stack)) {
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
                        stacks.add(size(stack, excess));
                    }
                    // and then add as many max size stacks as needed
                    ItemStack maxSize = size(stack, max);
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

    //Assumes that the stacks are already
    private static List<ItemStack> flatten(List<ItemStack> stacks) {
        if (stacks.isEmpty()) {
            return Collections.emptyList();
        }
        List<ItemStack> compacted = new ArrayList<>();
        for (ItemStack stack : stacks) {
            if (!stack.isEmpty()) {
                int count = stack.getCount();
                int max = stack.getMaxStackSize();
                //TODO: Fix comments
                if (count > max) {
                    //If we have more than a stack of the item (such as we are a bin) or some other thing that allows for compressing
                    // stack counts, drop as many stacks as we need at their max size
                    while (count > max) {
                        compacted.add(size(stack, max));
                        count -= max;
                    }
                    if (count > 0) {
                        //If we have anything left to drop afterward, do so
                        compacted.add(size(stack, count));
                    }
                } else {
                    //If we have a valid stack, we can just directly drop that instead without requiring any copies
                    // as while IInventorySlot#getStack says to not mutate the stack, our slot is a dummy slot
                    compacted.add(stack);
                }
            }
        }

        for (int i = 0; i < stacks.size(); i++) {
            ItemStack s = stacks.get(i);
            if (!s.isEmpty()) {
                for (int j = i + 1; j < stacks.size(); j++) {
                    ItemStack s1 = stacks.get(j);
                    if (ItemHandlerHelper.canItemStacksStack(s, s1)) {
                        s.grow(s1.getCount());
                        stacks.set(j, ItemStack.EMPTY);
                    }
                }
            }
        }

        return compacted;
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