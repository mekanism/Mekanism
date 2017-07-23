package buildcraft.api.transport;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IStripesRegistry {

    /** Adds a handler with a {@link HandlerPriority} of {@linkplain HandlerPriority#NORMAL NORMAL} */
    void addHandler(IStripesHandlerItem handler);

    void addHandler(IStripesHandlerItem handler, HandlerPriority priority);

    /** Adds a handler with a {@link HandlerPriority} of {@linkplain HandlerPriority#NORMAL NORMAL} */
    void addHandler(IStripesHandlerBlock handler);

    void addHandler(IStripesHandlerBlock handler, HandlerPriority priority);

    /** @param pos The position of the stripes pipe.
     * @return True if a handler handled the itemstack, false otherwise (and so nothing has been done) */
    boolean handleItem(World world, BlockPos pos, EnumFacing direction, ItemStack stack, EntityPlayer player, IStripesActivator activator);

    /** @return True if a handler broke a block, false otherwise (and so nothing has been done) */
    boolean handleBlock(World world, BlockPos pos, EnumFacing direction, EntityPlayer player, IStripesActivator activator);

    // TODO: Ensure that this method really will work better than an integer for priorities.
    public enum HandlerPriority {
        HIGHEST,
        HIGH,
        NORMAL,
        LOW,
        LOWEST
    }
}
