package railcraft.common.api.core.items;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.src.Block;
import net.minecraft.src.ItemBlock;
import net.minecraft.src.ItemStack;

/**
 * Register an item here to designate it as a possible
 * ballast that can be used in the Bore.
 *
 * It is expected that ballast is affected by gravity.
 *
 * @author CovertJaguar <railcraft.wikispaces.com>
 */
public abstract class BallastRegistry
{

    private static Set<ItemWrapper> ballastRegistry = new HashSet<ItemWrapper>();

    private static class ItemWrapper
    {

        public int itemID;
        public int itemDamage;
        public ItemStack stack;

        public ItemWrapper(ItemStack stack)
        {
            itemID = stack.itemID;
            itemDamage = stack.getItemDamage();
            this.stack = stack;
        }

        @Override
        public boolean equals(Object obj)
        {
            if(obj == null) {
                return false;
            }
            if(getClass() != obj.getClass()) {
                return false;
            }
            final ItemWrapper other = (ItemWrapper)obj;
            if(this.itemID != other.itemID) {
                return false;
            }
            if(this.itemDamage != other.itemDamage) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode()
        {
            int hash = 3;
            hash = 47 * hash + this.itemID;
            hash = 47 * hash + this.itemDamage;
            return hash;
        }
    }

    static {
        registerBallast(new ItemStack(Block.gravel));
    }

    public static void registerBallast(ItemStack ballast)
    {
        if(ballast.getItem() instanceof ItemBlock) {
            ballastRegistry.add(new ItemWrapper(ballast));
        } else {
            throw new RuntimeException("Attempted to register an invalid ballast, must be an ItemBlock item.");
        }
    }

    public static boolean isItemBallast(ItemStack ballast)
    {
        return ballastRegistry.contains(new ItemWrapper(ballast));
    }

    public static List<ItemStack> getRegisteredBallasts()
    {
        List<ItemStack> list = new ArrayList<ItemStack>();
        for(ItemWrapper item : ballastRegistry) {
            list.add(item.stack);
        }
        return list;
    }
}
