package mekanism.api;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;

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
     * @param side   - the side the block was clicked on
     *
     * @return action that was performed
     *
     * @apiNote Only called from the server
     */
    ActionResultType onSneakRightClick(PlayerEntity player, Direction side);

    /**
     * Called when a player right clicks this block with a Configurator.
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