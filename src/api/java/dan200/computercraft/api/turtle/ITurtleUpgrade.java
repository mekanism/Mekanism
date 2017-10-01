/*
 * This file is part of the public ComputerCraft API - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. This API may be redistributed unmodified and in full only.
 * For help using the API, and posting your mods, visit the forums at computercraft.info.
 */

package dan200.computercraft.api.turtle;

import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;


/**
 * The primary interface for defining an update for Turtles. A turtle update
 * can either be a new tool, or a new peripheral.
 *
 * @see ComputerCraftAPI#registerTurtleUpgrade(ITurtleUpgrade)
 */
public interface ITurtleUpgrade
{
    /**
     * Gets a unique identifier representing this type of turtle upgrade. eg: "computercraft:wireless_modem" or "my_mod:my_upgrade".
     * You should use a unique resource domain to ensure this upgrade is uniquely identified.
     * The turtle will fail registration if an already used ID is specified.
     *
     * @return The unique ID for this upgrade.
     * @see ComputerCraftAPI#registerTurtleUpgrade(ITurtleUpgrade)
     */
    @Nonnull
    ResourceLocation getUpgradeID();

    /**
     * Gets a numerical identifier representing this type of turtle upgrade,
     * for backwards compatibility with pre-1.76 worlds. If your upgrade was
     * not released for older ComputerCraft versions, you can return -1 here.
     * The turtle will fail registration if an already used positive ID is specified.
     *
     * @return The legacy ID, or -1 if is needed.
     * @see ComputerCraftAPI#registerTurtleUpgrade(ITurtleUpgrade)
     */
    int getLegacyUpgradeID();

    /**
     * Return an unlocalised string to describe this type of turtle in turtle item names.
     *
     * Examples of built-in adjectives are "Wireless", "Mining" and "Crafty".
     *
     * @return The localisation key for this upgrade's adjective.
     */
    @Nonnull
    String getUnlocalisedAdjective();

    /**
     * Return whether this turtle adds a tool or a peripheral to the turtle.
     *
     * @return The type of upgrade this is.
     * @see TurtleUpgradeType for the differences between them.
     */
    @Nonnull
    TurtleUpgradeType getType();

    /**
     * Return an item stack representing the type of item that a turtle must be crafted
     * with to create a turtle which holds this upgrade. This item stack is also used
     * to determine the upgrade given by {@code turtle.equip()}
     *
     * @return The item stack to craft with, or {@link ItemStack#EMPTY} if it cannot be crafted.
     */
    @Nonnull
    ItemStack getCraftingItem();

    /**
     * Will only be called for peripheral upgrades. Creates a peripheral for a turtle being placed using this upgrade.
     *
     * The peripheral created will be stored for the lifetime of the upgrade and will be passed as an argument to
     * {@link #update(ITurtleAccess, TurtleSide)}. It will be attached, detached and have methods called in the same
     * manner as a Computer peripheral.
     *
     * @param turtle Access to the turtle that the peripheral is being created for.
     * @param side   Which side of the turtle (left or right) that the upgrade resides on.
     * @return The newly created peripheral. You may return {@code null} if this upgrade is a Tool
     * and this method is not expected to be called.
     */
    @Nullable
    IPeripheral createPeripheral( @Nonnull ITurtleAccess turtle, @Nonnull TurtleSide side );

    /**
     * Will only be called for Tool turtle. Called when turtle.dig() or turtle.attack() is called
     * by the turtle, and the tool is required to do some work.
     *
     * @param turtle    Access to the turtle that the tool resides on.
     * @param side      Which side of the turtle (left or right) the tool resides on.
     * @param verb      Which action (dig or attack) the turtle is being called on to perform.
     * @param direction Which world direction the action should be performed in, relative to the turtles
     *                  position. This will either be up, down, or the direction the turtle is facing, depending on
     *                  whether dig, digUp or digDown was called.
     * @return Whether the turtle was able to perform the action, and hence whether the {@code turtle.dig()}
     * or {@code turtle.attack()} lua method should return true. If true is returned, the tool will perform
     * a swinging animation. You may return {@code null} if this turtle is a Peripheral  and this method is not expected
     * to be called.
     */
    @Nonnull
    TurtleCommandResult useTool( @Nonnull ITurtleAccess turtle, @Nonnull TurtleSide side, @Nonnull TurtleVerb verb, @Nonnull EnumFacing direction );

    /**
     * Called to obtain the model to be used when rendering a turtle peripheral.
     *
     * This can be obtained from {@link net.minecraft.client.renderer.ItemModelMesher#getItemModel(ItemStack)},
     * {@link net.minecraft.client.renderer.block.model.ModelManager#getModel(ModelResourceLocation)} or any other
     * source.
     *
     * @param turtle Access to the turtle that the upgrade resides on. This will be null when getting item models!
     * @param side   Which side of the turtle (left or right) the upgrade resides on.
     * @return The model that you wish to be used to render your upgrade, and a transformation to apply to it. Returning
     * a transformation of {@code null} has the same effect as the identify matrix.
     */
    @SideOnly(Side.CLIENT)
    @Nonnull
    Pair<IBakedModel, Matrix4f> getModel( @Nullable ITurtleAccess turtle, @Nonnull TurtleSide side );

    /**
     * Called once per tick for each turtle which has the upgrade equipped.
     *
     * @param turtle Access to the turtle that the upgrade resides on.
     * @param side   Which side of the turtle (left or right) the upgrade resides on.
     */
    default void update( @Nonnull ITurtleAccess turtle, @Nonnull TurtleSide side )
    {
    }
}
