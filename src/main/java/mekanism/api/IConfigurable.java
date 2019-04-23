package mekanism.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;

/**
 * Implement this in your TileEntity class if your block can be modified by a Configurator.
 *
 * @author aidancbrady
 */
public interface IConfigurable {

    /**
     * Called when a player shift-right clicks this block with a Configurator.
     *
     * @param player - the player who clicked the block
     * @param side - the side the block was clicked on
     * @return action that was performed
     */
    EnumActionResult onSneakRightClick(EntityPlayer player, EnumFacing side);

    /**
     * Called when a player right clicks this block with a Configurator.
     *
     * @param player - the player who clicked the block
     * @param side - the side the block was clicked on
     * @return action that was performed
     */
    EnumActionResult onRightClick(EntityPlayer player, EnumFacing side);
}
