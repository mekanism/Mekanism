package mekanism.common.inventory.container;

import net.minecraft.world.entity.player.Player;

public interface ISecurityContainer {

    /// @apiNote Only for use on the server, which means that it doesn't need to properly update on the client side if the stack changes
    boolean canPlayerAccess(Player player);
}