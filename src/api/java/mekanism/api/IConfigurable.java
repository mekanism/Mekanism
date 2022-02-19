package mekanism.api;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;

/**
 * Expose this as a capability on your TileEntity to allow if your block to be modified by a Configurator.
 *
 * @author aidancbrady
 */
public interface IConfigurable {//TODO - 1.18: Make the capability instance handle what side we interact with these from and remove the side from the methods

    /**
     * Called when a player shift-right clicks this block with a Configurator.
     *
     * @param player - the player who clicked the block
     * @param side   - the side the block was clicked on
     *
     * @return action that was performed
     *
     * @apiNote Only called from the server
     */
    ActionResultType onSneakRightClick(PlayerEntity player, Direction side);

    /**
     * Called when a player right-clicks this block with a Configurator.
     *
     * @param player - the player who clicked the block
     * @param side   - the side the block was clicked on
     *
     * @return action that was performed
     *
     * @apiNote Only called from the server
     */
    ActionResultType onRightClick(PlayerEntity player, Direction side);
}