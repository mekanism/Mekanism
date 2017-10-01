/*
 * This file is part of the public ComputerCraft API - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. This API may be redistributed unmodified and in full only.
 * For help using the API, and posting your mods, visit the forums at computercraft.info.
 */

package dan200.computercraft.api.lua;

import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An interface for representing custom objects returned by {@link IPeripheral#callMethod(IComputerAccess, ILuaContext, int, Object[])}
 * calls.
 *
 * Return objects implementing this interface to expose objects with methods to lua.
 */
public interface ILuaObject
{
    /**
     * Get the names of the methods that this object implements. This works the same as {@link IPeripheral#getMethodNames()}.
     * See that method for detailed documentation.
     *
     * @return The method names this object provides.
     * @see IPeripheral#getMethodNames()
     */
    @Nonnull
    String[] getMethodNames();

    /**
     * Called when a user calls one of the methods that this object implements. This works the same as
     * {@link IPeripheral#callMethod(IComputerAccess, ILuaContext, int, Object[])}}. See that method for detailed
     * documentation.
     *
     * @param context   The context of the currently running lua thread. This can be used to wait for events
     *                  or otherwise yield.
     * @param method    An integer identifying which of the methods from getMethodNames() the computercraft
     *                  wishes to call. The integer indicates the index into the getMethodNames() table
     *                  that corresponds to the string passed into peripheral.call()
     * @param arguments The arguments for this method. See {@link IPeripheral#callMethod(IComputerAccess, ILuaContext, int, Object[])}
     *                  the possible values and conversion rules.
     * @return An array of objects, representing the values you wish to return to the Lua program.
     * See {@link IPeripheral#callMethod(IComputerAccess, ILuaContext, int, Object[])} for the valid values and
     * conversion rules.
     * @throws LuaException         If the task could not be queued, or if the task threw an exception.
     * @throws InterruptedException If the user shuts down or reboots the computer the coroutine is suspended,
     *                              InterruptedException will be thrown. This exception must not be caught or
     *                              intercepted, or the computer will leak memory and end up in a broken state.w
     * @see IPeripheral#callMethod(IComputerAccess, ILuaContext, int, Object[])
     */
    @Nullable
    Object[] callMethod( @Nonnull ILuaContext context, int method, @Nonnull Object[] arguments ) throws LuaException, InterruptedException;
}
