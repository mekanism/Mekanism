/*
 * This file is part of the public ComputerCraft API - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. This API may be redistributed unmodified and in full only.
 * For help using the API, and posting your mods, visit the forums at computercraft.info.
 */

package dan200.computercraft.api.peripheral;

import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.filesystem.IMount;
import dan200.computercraft.api.filesystem.IWritableMount;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * The interface passed to peripherals by computers or turtles, providing methods
 * that they can call. This should not be implemented by your classes. Do not interact
 * with computers except via this interface.
 */
public interface IComputerAccess
{
    /**
     * Mount a mount onto the computer's file system in a read only mode.
     *
     * @param desiredLocation The location on the computer's file system where you would like the mount to be mounted.
     * @param mount           The mount object to mount on the computer.
     * @return The location on the computer's file system where you the mount mounted, or {@code null} if there was already a
     * file in the desired location. Store this value if you wish to unmount the mount later.
     * @throws RuntimeException If the peripheral has been detached.
     * @see ComputerCraftAPI#createSaveDirMount(World, String, long)
     * @see ComputerCraftAPI#createResourceMount(Class, String, String)
     * @see #mount(String, IMount, String)
     * @see #mountWritable(String, IWritableMount)
     * @see #unmount(String)
     * @see IMount
     */
    @Nullable
    String mount( @Nonnull String desiredLocation, @Nonnull IMount mount );

    /**
     * Mount a mount onto the computer's file system in a read only mode.
     *
     * @param desiredLocation The location on the computer's file system where you would like the mount to be mounted.
     * @param mount           The mount object to mount on the computer.
     * @param driveName       A custom name to give for this mount location, as returned by {@code fs.getDrive()}.
     * @return The location on the computer's file system where you the mount mounted, or {@code null} if there was already a
     * file in the desired location. Store this value if you wish to unmount the mount later.
     * @throws RuntimeException If the peripheral has been detached.
     * @see ComputerCraftAPI#createSaveDirMount(World, String, long)
     * @see ComputerCraftAPI#createResourceMount(Class, String, String)
     * @see #mount(String, IMount)
     * @see #mountWritable(String, IWritableMount)
     * @see #unmount(String)
     * @see IMount
     */
    @Nullable
    String mount( @Nonnull String desiredLocation, @Nonnull IMount mount, @Nonnull String driveName );

    /**
     * Mount a mount onto the computer's file system in a writable mode.
     *
     * @param desiredLocation The location on the computer's file system where you would like the mount to be mounted.
     * @param mount           The mount object to mount on the computer.
     * @return The location on the computer's file system where you the mount mounted, or null if there was already a
     * file in the desired location. Store this value if you wish to unmount the mount later.
     * @throws RuntimeException If the peripheral has been detached.
     * @see ComputerCraftAPI#createSaveDirMount(World, String, long)
     * @see ComputerCraftAPI#createResourceMount(Class, String, String)
     * @see #mount(String, IMount)
     * @see #unmount(String)
     * @see IMount
     */
    @Nullable
    String mountWritable( @Nonnull String desiredLocation, @Nonnull IWritableMount mount );

    /**
     * Mount a mount onto the computer's file system in a writable mode.
     *
     * @param desiredLocation The location on the computer's file system where you would like the mount to be mounted.
     * @param mount           The mount object to mount on the computer.
     * @param driveName       A custom name to give for this mount location, as returned by {@code fs.getDrive()}.
     * @return The location on the computer's file system where you the mount mounted, or null if there was already a
     * file in the desired location. Store this value if you wish to unmount the mount later.
     * @throws RuntimeException If the peripheral has been detached.
     * @see ComputerCraftAPI#createSaveDirMount(World, String, long)
     * @see ComputerCraftAPI#createResourceMount(Class, String, String)
     * @see #mount(String, IMount)
     * @see #unmount(String)
     * @see IMount
     */
    String mountWritable( @Nonnull String desiredLocation, @Nonnull IWritableMount mount, @Nonnull String driveName );

    /**
     * Unmounts a directory previously mounted onto the computers file system by {@link #mount(String, IMount)}
     * or {@link #mountWritable(String, IWritableMount)}.
     *
     * When a directory is unmounted, it will disappear from the computers file system, and the user will no longer be
     * able to access it. All directories mounted by a mount or mountWritable are automatically unmounted when the
     * peripheral is attached if they have not been explicitly unmounted.
     *
     * Note that you cannot unmount another peripheral's mounts.
     *
     * @param location The desired location in the computers file system of the directory to unmount.
     *                 This must be the location of a directory previously mounted by {@link #mount(String, IMount)} or
     *                 {@link #mountWritable(String, IWritableMount)}, as indicated by their return value.
     * @throws RuntimeException If the peripheral has been detached.
     * @throws RuntimeException If the mount does not exist, or was mounted by another peripheral.
     * @see #mount(String, IMount)
     * @see #mountWritable(String, IWritableMount)
     */
    void unmount( @Nullable String location );

    /**
     * Returns the numerical ID of this computer.
     *
     * This is the same number obtained by calling {@code os.getComputerID()} or running the "id" program from lua,
     * and is guaranteed unique. This number will be positive.
     *
     * @return The identifier.
     */
    int getID();

    /**
     * Causes an event to be raised on this computer, which the computer can respond to by calling
     * {@code os.pullEvent()}. This can be used to notify the computer when things happen in the world or to
     * this peripheral.
     *
     * @param event     A string identifying the type of event that has occurred, this will be
     *                  returned as the first value from {@code os.pullEvent()}. It is recommended that you
     *                  you choose a name that is unique, and recognisable as originating from your
     *                  peripheral. eg: If your peripheral type is "button", a suitable event would be
     *                  "button_pressed".
     * @param arguments In addition to a name, you may pass an array of extra arguments to the event, that will
     *                  be supplied as extra return values to os.pullEvent(). Objects in the array will be converted
     *                  to lua data types in the same fashion as the return values of IPeripheral.callMethod().
     *
     *                  You may supply {@code null} to indicate that no arguments are to be supplied.
     * @throws RuntimeException If the peripheral has been detached.
     * @see dan200.computercraft.api.peripheral.IPeripheral#callMethod
     */
    void queueEvent( @Nonnull String event, @Nullable Object[] arguments );

    /**
     * Get a string, unique to the computer, by which the computer refers to this peripheral.
     * For directly attached peripherals this will be "left","right","front","back",etc, but
     * for peripherals attached remotely it will be different. It is good practice to supply
     * this string when raising events to the computer, so that the computer knows from
     * which peripheral the event came.
     *
     * @return A string unique to the computer, but not globally.
     * @throws RuntimeException If the peripheral has been detached.
     */
    @Nonnull
    String getAttachmentName();
}
