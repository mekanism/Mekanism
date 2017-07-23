package buildcraft.api.transport;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IStripesHandlerBlock {

    /** @param world
     * @param pos
     * @param direction
     * @param player
     * @param activator
     * @return True if this broke a block, false otherwise (note that this handler MUST NOT return false if it has
     *         changed the world in any way) */
    boolean handle(World world, BlockPos pos, EnumFacing direction, EntityPlayer player, IStripesActivator activator);
}
