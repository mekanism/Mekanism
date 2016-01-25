package buildcraft.api.transport;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

public interface IInjectable {
    boolean canInjectItems(EnumFacing from);

    /** Offers an ItemStack for 3addition to the pipe. Will be rejected if the pipe doesn't accept items from that side.
     *
     * @param stack ItemStack offered for addition. Do not manipulate this!
     * @param doAdd If false no actual addition should take place. Implementors should simulate.
     * @param from Orientation the ItemStack is offered from.
     * @param color The color of the item to be added to the pipe, or null for no color.
     * @return Amount of items used from the passed stack. */
    int injectItem(ItemStack stack, boolean doAdd, EnumFacing from, EnumDyeColor color);
}
