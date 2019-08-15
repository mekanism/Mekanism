package mekanism.common.util;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.oredict.OreDictionary;

public final class StackUtils {

    //ignores count
    public static boolean equalsWildcard(ItemStack wild, ItemStack check) {
        if (wild.isEmpty() && check.isEmpty()) {
            return true;
        }
        return wild.getItem() == check.getItem() && (wild.getDamage() == OreDictionary.WILDCARD_VALUE || check.getDamage() == OreDictionary.WILDCARD_VALUE ||
                                                     wild.getDamage() == check.getDamage());
    }

    //ignores count
    public static boolean equalsWildcardWithNBT(ItemStack wild, ItemStack check) {
        boolean wildcard = equalsWildcard(wild, check);
        if (wild.isEmpty() || check.isEmpty()) {
            return wildcard;
        }
        return wildcard && (!wild.hasTag() ? !check.hasTag() : (wild.getTag() == check.getTag() ||
                                                                                wild.getTag().equals(check.getTag())));
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

    public static List<ItemStack> getMergeRejects(NonNullList<ItemStack> orig, NonNullList<ItemStack> toAdd) {
        List<ItemStack> ret = new ArrayList<>();
        for (int i = 0; i < toAdd.size(); i++) {
            if (!toAdd.get(i).isEmpty()) {
                ItemStack reject = getMergeReject(orig.get(i), toAdd.get(i));
                if (!reject.isEmpty()) {
                    ret.add(reject);
                }
            }
        }
        return ret;
    }

    public static void merge(NonNullList<ItemStack> orig, NonNullList<ItemStack> toAdd) {
        for (int i = 0; i < toAdd.size(); i++) {
            if (!toAdd.get(i).isEmpty()) {
                orig.set(i, merge(orig.get(i), toAdd.get(i)));
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

    public static int hashItemStack(ItemStack stack) {
        if (stack.isEmpty()) {
            return -1;
        }
        ResourceLocation registryName = stack.getItem().getRegistryName();
        int nameHash = registryName == null ? 0 : registryName.hashCode();
        return nameHash << 8 | stack.getMetadata();
    }

    /**
     * Get state for placement for a generic item, with our fake player
     *
     * @param stack  the item to place
     * @param world  which universe
     * @param pos    where
     * @param player our fake player, usually
     *
     * @return the result of {@link Block#getStateForPlacement(BlockState, Direction, BlockState, IWorld, BlockPos, BlockPos, Hand)}
     * float, int, net.minecraft.entity.LivingEntity, net.minecraft.util.Hand)}
     */
    @Nonnull
    public static BlockState getStateForPlacement(ItemStack stack, World world, BlockPos pos, PlayerEntity player) {
        Block blockFromItem = Block.getBlockFromItem(stack.getItem());
        return blockFromItem.getStateForPlacement(world, pos, Direction.UP, 0, 0, 0, stack.getMetadata(), player, Hand.MAIN_HAND);
    }
}