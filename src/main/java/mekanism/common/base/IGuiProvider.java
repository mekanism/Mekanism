package mekanism.common.base;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IGuiProvider {

    /**
     * Get the container for a GUI. Common.
     *
     * @param ID     - gui ID
     * @param player - player that opened the GUI
     * @param world  - world the GUI was opened in
     * @param pos    - gui's position
     *
     * @return the Container of the GUI
     */
    Container getServerGui(int ID, PlayerEntity player, World world, BlockPos pos);

    /**
     * Get the actual interface for a GUI. Client-only.
     *
     * @param ID     - gui ID
     * @param player - player that opened the GUI
     * @param world  - world the GUI was opened in
     * @param pos    - gui's position
     *
     * @return the GuiScreen of the GUI
     */
    Object getClientGui(int ID, PlayerEntity player, World world, BlockPos pos);
}