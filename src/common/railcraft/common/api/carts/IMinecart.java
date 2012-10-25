package railcraft.common.api.carts;

import net.minecraft.src.EntityMinecart;
import net.minecraft.src.ItemStack;

/**
 * Some helper functions to make interacting with carts simpler.
 *
 * This interface is implemented by CartBase.
 *
 * @author CovertJaguar <railcraft.wikispaces.com>
 * @see CartBase
 */
public interface IMinecart
{

    /**
     * Returns true if the Minecart matches the item provided.
     * Generally just stack.isItemEqual(cart.getCartItem()),
     * but some carts may need more control (the Tank Cart for example).
     *
     * @param stack the Filter
     * @param cart the Cart
     * @return true if the item matches the cart
     */
    public boolean doesCartMatchFilter(ItemStack stack, EntityMinecart cart);

    /**
     * Unlike EntityMinecart.getMaxSpeedRail(),
     * this function is independent of the actual max speed of the cart.
     *
     * It should represent the max possible speed at this point in time
     * before any modifiers due to linked carts, etc are applied.
     *
     * This is really only used for Train speed calculations.
     * Which ever cart in the train returns the lowest value here will be the max speed of the entire train.
     *
     * @return
     */
    public float getCartMaxSpeed();

    /**
     * Sets the max speed of a train.
     *
     * This should be used to limit the return value for EntityMinecart.getMaxSpeedRail().
     *
     * @param speed
     * @see CartBase
     */
    public void setTrainSpeed(float speed);

}
