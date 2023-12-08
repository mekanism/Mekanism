package mekanism.common.inventory.container;

import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public interface ISecurityContainer {

    /**
     * @apiNote Only for use on the server, which means that it doesn't need to properly update on the client side if the stack changes
     */
    boolean canPlayerAccess(@NotNull Player player);
}