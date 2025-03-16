package mekanism.api;

import mekanism.api.tier.IAlloyTier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Implement this class in your TileEntity if it can interact with Mekanism alloys.
 *
 * @author aidancbrady
 */
public interface IAlloyInteraction {

    /**
     * Called when a player right-clicks this block with an alloy.
     *
     * @param player - the player right-clicking the block
     * @param stack  - the stack of alloy being right-clicked
     * @param tier   - the tier of the alloy
     */
    void onAlloyInteraction(Player player, ItemStack stack, @NotNull IAlloyTier tier);
}