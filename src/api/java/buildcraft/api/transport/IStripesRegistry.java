package buildcraft.api.transport;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.api.core.EnumHandlerPriority;

public interface IStripesRegistry {
    /** Adds a handler with a {@link EnumHandlerPriority} of {@linkplain EnumHandlerPriority#NORMAL} */
    default void addHandler(IStripesHandlerItem handler) {
        addHandler(handler, EnumHandlerPriority.NORMAL);
    }

    void addHandler(IStripesHandlerItem handler, EnumHandlerPriority priority);

    /** Adds a handler with a {@link EnumHandlerPriority} of {@linkplain EnumHandlerPriority#NORMAL NORMAL} */
    default void addHandler(IStripesHandlerBlock handler) {
        addHandler(handler, EnumHandlerPriority.NORMAL);
    }

    void addHandler(IStripesHandlerBlock handler, EnumHandlerPriority priority);

    /** @param pos The position of the stripes pipe.
     * @return True if a handler handled the itemstack, false otherwise (and so nothing has been done) */
    boolean handleItem(World world,
                       BlockPos pos,
                       EnumFacing direction,
                       ItemStack stack,
                       EntityPlayer player,
                       IStripesActivator activator);

    /** @return True if a handler broke a block, false otherwise (and so nothing has been done) */
    boolean handleBlock(World world,
                        BlockPos pos,
                        EnumFacing direction,
                        EntityPlayer player,
                        IStripesActivator activator);
}
