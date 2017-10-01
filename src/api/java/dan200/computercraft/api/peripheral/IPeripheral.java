/*
 * This file is part of the public ComputerCraft API - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. This API may be redistributed unmodified and in full only.
 * For help using the API, and posting your mods, visit the forums at computercraft.info.
 */

package dan200.computercraft.api.peripheral;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * The interface that defines a peripheral. See {@link IPeripheralProvider} for how to associate blocks with peripherals.
 */
public interface IPeripheral
{
    /**
     * Should return a string that uniquely identifies this type of peripheral.
     * This can be queried from lua by calling {@code peripheral.getType()}
     *
     * @return A string identifying the type of peripheral.
     */
    @Nonnull
    String getType();

    /**
     * Should return an array of strings that identify the methods that this
     * peripheral exposes to Lua. This will be called once before each attachment,
     * and should not change when called multiple times.
     *
     * @return An array of strings representing method names.
     * @see #callMethod
     */
    @Nonnull
    String[] getMethodNames();

    /**
     * This is called when a lua program on an attached computer calls {@code peripheral.call()} with
     * one of the methods exposed by {@link #getMethodNames()}.
     *
     * Be aware that this will be called from the ComputerCraft Lua thread, and must be thread-safe
     * when interacting with Minecraft objects.
     *
     * @param computer  The interface to the computer that is making the call. Remember that multiple
     *                  computers can be attached to a peripheral at once.
     * @param context   The context of the currently running lua thread. This can be used to wait for events
     *                  or otherwise yield.
     * @param method    An integer identifying which of the methods from getMethodNames() the computercraft
     *                  wishes to call. The integer indicates the index into the getMethodNames() table
     *                  that corresponds to the string passed into peripheral.call()
     * @param arguments An array of objects, representing the arguments passed into {@code peripheral.call()}.<br>
     *                  Lua values of type "string" will be represented by Object type String.<br>
     *                  Lua values of type "number" will be represented by Object type Double.<br>
     *                  Lua values of type "boolean" will be represented by Object type Boolean.<br>
     *                  Lua values of type "table" will be represented by Object type Map.<br>
     *                  Lua values of any other type will be represented by a null object.<br>
     *                  This array will be empty if no arguments are passed.
     * @return An array of objects, representing values you wish to return to the lua program. Integers, Doubles, Floats,
     * Strings, Booleans, Maps and ILuaObject and null be converted to their corresponding lua type. All other types
     * will be converted to nil.
     *
     * You may return null to indicate no values should be returned.
     * @throws LuaException         If you throw any exception from this function, a lua error will be raised with the
     *                              same message as your exception. Use this to throw appropriate errors if the wrong
     *                              arguments are supplied to your method.
     * @throws InterruptedException If the user shuts down or reboots the computer the coroutine is suspended,
     *                              InterruptedException will be thrown. This exception must not be caught or
     *                              intercepted, or the computer will leak memory and end up in a broken state.
     * @see #getMethodNames
     */
    @Nullable
    Object[] callMethod( @Nonnull IComputerAccess computer, @Nonnull ILuaContext context, int method, @Nonnull Object[] arguments ) throws LuaException, InterruptedException;

    /**
     * Is called when canAttachToSide has returned true, and a computer is attaching to the peripheral.
     *
     * This will occur when a peripheral is placed next to an active computer, when a computer is turned on next to a
     * peripheral, or when a turtle travels into a square next to a peripheral.
     *
     * Between calls to attach() and detach(), the attached computer can make method calls on the peripheral using
     * {@code peripheral.call()}. This method can be used to keep track of which computers are attached to the
     * peripheral, or to take action when attachment occurs.
     *
     * Be aware that this will be called from the ComputerCraft Lua thread, and must be thread-safe
     * when interacting with Minecraft objects.
     *
     * @param computer The interface to the computer that is being attached. Remember that multiple
     *                 computers can be attached to a peripheral at once.
     * @see #detach
     */
    default void attach( @Nonnull IComputerAccess computer )
    {
    }

    /**
     * Is called when a computer is detaching from the peripheral.
     *
     * This will occur when a computer shuts down, when the peripheral is removed while attached to computers,
     * or when a turtle moves away from a square attached to a peripheral. This method can be used to keep track of
     * which computers are attached to the peripheral, or to take action when detachment
     * occurs.
     *
     * Be aware that this will be called from the ComputerCraft Lua thread, and must be thread-safe
     * when interacting with Minecraft objects.
     *
     * @param computer The interface to the computer that is being detached. Remember that multiple
     *                 computers can be attached to a peripheral at once.
     * @see #detach
     */
    default void detach( @Nonnull IComputerAccess computer )
    {
    }

    /**
     * Determine whether this peripheral is equivalent to another one.
     *
     * The minimal example should at least check whether they are the same object. However, you may wish to check if
     * they point to the same block or tile entity.
     *
     * @param other The peripheral to compare against. This may be {@code null}.
     * @return Whether these peripherals are equivalent.
     */
    boolean equals( @Nullable IPeripheral other );
}
