package mekanism.common.util;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.inventory.IInventorySlot;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.items.ItemHandlerHelper;

public final class StackUtils {

    private StackUtils() {
    }

    public static ItemStack size(ItemStack stack, int size) {
        if (size <= 0 || stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return ItemHandlerHelper.copyStackWithSize(stack, size);
    }

    public static List<ItemStack> getMergeRejects(@Nonnull List<IInventorySlot> orig, @Nonnull List<IInventorySlot> toAdd) {
        List<ItemStack> ret = new ArrayList<>();
        for (int i = 0; i < toAdd.size(); i++) {
            IInventorySlot toAddSlot = toAdd.get(i);
            if (!toAddSlot.isEmpty()) {
                ItemStack reject = getMergeReject(orig.get(i).getStack(), toAddSlot.getStack());
                if (!reject.isEmpty()) {
                    ret.add(reject);
                }
            }
        }
        return ret;
    }

    public static void merge(@Nonnull List<IInventorySlot> orig, @Nonnull List<IInventorySlot> toAdd) {
        for (int i = 0; i < toAdd.size(); i++) {
            IInventorySlot toAddSlot = toAdd.get(i);
            if (!toAddSlot.isEmpty()) {
                IInventorySlot origSlot = orig.get(i);
                origSlot.setStack(merge(origSlot.getStack(), toAddSlot.getStack()));
            }
        }
    }

    private static ItemStack merge(ItemStack orig, ItemStack toAdd) {
        if (orig.isEmpty()) {
            return toAdd;
        }
        if (toAdd.isEmpty() || !ItemHandlerHelper.canItemStacksStack(orig, toAdd)) {
            return orig;
        }
        return size(orig, Math.min(orig.getMaxStackSize(), orig.getCount() + toAdd.getCount()));
    }

    private static ItemStack getMergeReject(ItemStack orig, ItemStack toAdd) {
        if (orig.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (toAdd.isEmpty() || !ItemHandlerHelper.canItemStacksStack(orig, toAdd)) {
            return orig;
        }
        int newSize = orig.getCount() + toAdd.getCount();
        if (newSize > orig.getMaxStackSize()) {
            return size(orig, newSize - orig.getMaxStackSize());
        }
        return size(orig, newSize);
    }

    /**
     * Get state for placement for a generic item, with our fake player
     *
     * @param stack  the item to place
     * @param pos    where
     * @param player our fake player, usually
     *
     * @return the result of {@link Block#getStateForPlacement(BlockItemUseContext)}, or null if it cannot be placed in that location
     */
    @Nullable
    public static BlockState getStateForPlacement(ItemStack stack, BlockPos pos, PlayerEntity player) {
        return Block.getBlockFromItem(stack.getItem()).getStateForPlacement(new BlockItemUseContext(new ItemUseContext(player, Hand.MAIN_HAND,
              new BlockRayTraceResult(Vector3d.ZERO, Direction.UP, pos, false))));
    }
}