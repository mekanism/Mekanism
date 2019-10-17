package mekanism.common.util;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.inventory.slot.IInventorySlot;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemHandlerHelper;

public final class StackUtils {

    //ignores count
    //TODO: Remove this method/replace it as there is no longer a wildcard
    public static boolean equalsWildcard(ItemStack wild, ItemStack check) {
        if (wild.isEmpty() && check.isEmpty()) {
            return true;
        }
        return wild.getItem() == check.getItem() && wild.getDamage() == check.getDamage();
    }

    //ignores count
    //TODO: Remove this method/replace it as there is no longer a wildcard
    public static boolean equalsWildcardWithNBT(ItemStack wild, ItemStack check) {
        boolean wildcard = equalsWildcard(wild, check);
        if (wild.isEmpty() || check.isEmpty()) {
            return wildcard;
        }
        return wildcard && (!wild.hasTag() ? !check.hasTag() : (wild.getTag() == check.getTag() || wild.getTag().equals(check.getTag())));
    }

    //assumes stacks same
    public static ItemStack subtract(ItemStack stack1, ItemStack stack2) {
        if (stack1.isEmpty()) {
            return ItemStack.EMPTY;
        } else if (stack2.isEmpty()) {
            return stack1;
        }
        return size(stack1, stack1.getCount() - stack2.getCount());
    }

    public static ItemStack size(ItemStack stack, int size) {
        if (size <= 0 || stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack ret = stack.copy();
        ret.setCount(size);
        return ret;
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

    //TODO: Figure out what used this or if this can be safely removed now
    public static int hashItemStack(ItemStack stack) {
        if (stack.isEmpty()) {
            return -1;
        }
        ResourceLocation registryName = stack.getItem().getRegistryName();
        return registryName == null ? 0 : registryName.hashCode();
    }

    /**
     * Get state for placement for a generic item, with our fake player
     *
     * @param stack  the item to place
     * @param world  which universe
     * @param pos    where
     * @param player our fake player, usually
     *
     * @return the result of {@link Block#getStateForPlacement(BlockItemUseContext)} float, int, net.minecraft.entity.LivingEntity, net.minecraft.util.Hand)}
     */
    @Nonnull
    public static BlockState getStateForPlacement(ItemStack stack, World world, BlockPos pos, PlayerEntity player) {
        Block blockFromItem = Block.getBlockFromItem(stack.getItem());
        //TODO: Fix this
        return blockFromItem.getDefaultState();//.getStateForPlacement(world, pos, Direction.UP, 0, 0, 0, stack.getMetadata(), player, Hand.MAIN_HAND);
    }
}