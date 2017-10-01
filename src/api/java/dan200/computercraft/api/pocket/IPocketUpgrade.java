package dan200.computercraft.api.pocket;

import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.turtle.ITurtleUpgrade;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Additional peripherals for pocket computers.
 *
 * This is similar to {@link dan200.computercraft.api.turtle.ITurtleUpgrade}.
 */
public interface IPocketUpgrade
{

    /**
     * Gets a unique identifier representing this type of turtle upgrade. eg: "computercraft:wireless_modem" or
     * "my_mod:my_upgrade".
     *
     * You should use a unique resource domain to ensure this upgrade is uniquely identified. The upgrade will fail
     * registration if an already used ID is specified.
     *
     * @return The upgrade's id.
     * @see IPocketUpgrade#getUpgradeID()
     * @see ComputerCraftAPI#registerPocketUpgrade(IPocketUpgrade)
     */
    @Nonnull
    ResourceLocation getUpgradeID();

    /**
     * Return an unlocalised string to describe the type of pocket computer this upgrade provides.
     *
     * An example of a built-in adjectives is "Wireless" - this is converted to "Wireless Pocket Computer".
     *
     * @return The unlocalised adjective.
     * @see ITurtleUpgrade#getUnlocalisedAdjective()
     */
    @Nonnull
    String getUnlocalisedAdjective();

    /**
     * Return an item stack representing the type of item that a pocket computer must be crafted with to create a
     * pocket computer which holds this upgrade. This item stack is also used to determine the upgrade given by
     * {@code pocket.equip()}/{@code pocket.unequip()}.
     *
     * @return The item stack used for crafting. This can be {@link ItemStack#EMPTY} if crafting is disabled.
     */
    @Nonnull
    ItemStack getCraftingItem();

    /**
     * Creates a peripheral for the pocket computer.
     *
     * The peripheral created will be stored for the lifetime of the upgrade, will be passed an argument to
     * {@link #update(IPocketAccess, IPeripheral)} and will be attached, detached and have methods called in the same
     * manner as an ordinary peripheral.
     *
     * @param access The access object for the pocket item stack.
     * @return The newly created peripheral.
     * @see #update(IPocketAccess, IPeripheral)
     */
    @Nullable
    IPeripheral createPeripheral( @Nonnull IPocketAccess access );

    /**
     * Called when the pocket computer item stack updates.
     *
     * @param access     The access object for the pocket item stack.
     * @param peripheral The peripheral for this upgrade.
     * @see #createPeripheral(IPocketAccess)
     */
    default void update( @Nonnull IPocketAccess access, @Nullable IPeripheral peripheral )
    {
    }

    /**
     * Called when the pocket computer is right clicked.
     *
     * @param world      The world the computer is in.
     * @param access     The access object for the pocket item stack.
     * @param peripheral The peripheral for this upgrade.
     * @return {@code true} to stop the GUI from opening, otherwise false. You should always provide some code path
     * which returns {@code false}, such as requiring the player to be sneaking - otherwise they will be unable to
     * access the GUI.
     * @see #createPeripheral(IPocketAccess)
     */
    default boolean onRightClick( @Nonnull World world, @Nonnull IPocketAccess access, @Nullable IPeripheral peripheral )
    {
        return false;
    }
}
