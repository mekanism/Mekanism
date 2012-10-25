package railcraft.common.api.core.items;

import net.minecraft.src.BlockRail;
import net.minecraft.src.Item;
import net.minecraft.src.ItemBlock;
import net.minecraft.src.ItemFood;
import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntityFurnace;
import net.minecraftforge.common.MinecartRegistry;

/**
 * This interface is used with several of the functions in IItemTransfer
 * to provide a convenient means of dealing with entire classes of items without
 * having to specify each item individually.
 * @author CovertJaguar <railcraft.wikispaces.com>
 */
public enum EnumItemType
{

    FUEL, RAIL, MINECART, BALLAST, FOOD;

    public static boolean isItemType(ItemStack stack, EnumItemType filter)
    {
        return filter.isItemType(stack);
    }

    public boolean isItemType(ItemStack stack)
    {
        if(stack == null) {
            return false;
        }
        switch (this) {
            case FUEL:
                return TileEntityFurnace.isItemFuel(stack);
            case RAIL:
                return stack.getItem() instanceof ITrackItem || (stack.getItem() instanceof ItemBlock && BlockRail.isRailBlock(stack.itemID));
            case MINECART:
                return MinecartRegistry.getCartClassForItem(stack) != null || stack.getItem() instanceof IMinecartItem;
            case BALLAST:
                return BallastRegistry.isItemBallast(stack);
            case FOOD:
                return stack.getItem() instanceof ItemFood || stack.itemID == Item.wheat.shiftedIndex;
            default:
                return false;
        }
    }
}
