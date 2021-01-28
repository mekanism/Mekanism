package mekanism.api;

import javax.annotation.Nonnull;
import mekanism.api.tier.AlloyTier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

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
     * @param hand   - the hand this alloy was right-clicked with
     * @param stack  - the stack of alloy being right-clicked
     * @param tier   - the tier of the alloy
     */
    //TODO - 1.17: Remove the hand parameter as it is not needed
    void onAlloyInteraction(PlayerEntity player, Hand hand, ItemStack stack, @Nonnull AlloyTier tier);
}