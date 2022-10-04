package mekanism.api;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;

/**
 * Expose this as a capability on your TileEntity to allow if your block to be modified by a Configurator.
 *
 * @author aidancbrady
 */
@AutoRegisterCapability
public interface IConfigurable {

    /**
     * Called when a player shift-right clicks this block with a Configurator.
     *
     * @param player - the player who clicked the block
     *
     * @return action that was performed
     *
     * @apiNote Only called from the server
     */
    InteractionResult onSneakRightClick(Player player);

    /**
     * Called when a player right-clicks this block with a Configurator.
     *
     * @param player - the player who clicked the block
     *
     * @return action that was performed
     *
     * @apiNote Only called from the server
     */
    InteractionResult onRightClick(Player player);
}