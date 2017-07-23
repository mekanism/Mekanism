package buildcraft.api.transport.pipe;

import javax.annotation.Nonnull;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

import buildcraft.api.core.IStackFilter;
import buildcraft.api.transport.IInjectable;

public interface IFlowItems extends IInjectable {
    /** Attempts to extract items from the inventory connected to this pipe on the given side.
     * 
     * @param count The maximum number of items to extract
     * @param from The direction to extract from.
     * @param colour The colour that extracted items should be painted.
     * @param filter The filter to determine what can be extracted.
     * @return The number of items extracted. */
    int tryExtractItems(int count, EnumFacing from, EnumDyeColor colour, IStackFilter filter);

    /** Inserts an item directly into the centre of this pipe, going in the given direction. This should ONLY be called
     * from an instance of {@link PipeBehaviour}, as otherwise it can lead to problems. (For example crashing if a pipe
     * contains an item that the pipe behaviour doesn't expect).
     * 
     * @param stack ItemStack offered for addition. Do not manipulate this!
     * @param from Orientation the ItemStack should pretend to be coming from.
     * @param colour The colour of the item to be added to the pipe, or null for no colour.
     * @param speed The speed of the item to be added (in blocks per tick) or {@code <=0} if a default should be
     *            used. */
    void insertItemsForce(@Nonnull ItemStack stack, EnumFacing from, EnumDyeColor colour, double speed);
}
