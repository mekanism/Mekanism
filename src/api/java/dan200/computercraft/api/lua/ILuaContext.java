/*
 * This file is part of the public ComputerCraft API - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. This API may be redistributed unmodified and in full only.
 * For help using the API, and posting your mods, visit the forums at computercraft.info.
 */

package dan200.computercraft.api.lua;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An interface passed to peripherals and {@link ILuaObject}s by computers or turtles, providing methods
 * that allow the peripheral call to wait for events before returning, just like in lua. This is very useful if you need
 * to signal work to be performed on the main thread, and don't want to return until the work has been completed.
 */
public interface ILuaContext
{
    /**
     * Wait for an event to occur on the computer, suspending the thread until it arises. This method is exactly
     * equivalent to {@code os.pullEvent()} in lua.
     *
     * @param filter A specific event to wait for, or null to wait for any event.
     * @return An object array containing the name of the event that occurred, and any event parameters.
     * @throws LuaException         If the user presses CTRL+T to terminate the current program while pullEvent() is
     *                              waiting for an event, a "Terminated" exception will be thrown here.
     *
     *                              Do not attempt to catch this exception. You should use {@link #pullEventRaw(String)}
     *                              should you wish to disable termination.
     * @throws InterruptedException If the user shuts down or reboots the computer while pullEvent() is waiting for an
     *                              event, InterruptedException will be thrown. This exception must not be caught or
     *                              intercepted, or the computer will leak memory and end up in a broken state.
     */
    @Nonnull
    Object[] pullEvent( @Nullable String filter ) throws LuaException, InterruptedException;

    /**
     * The same as {@link #pullEvent(String)}, except "terminated" events are ignored. Only use this if you want to
     * prevent program termination, which is not recommended. This method is exactly equivalent to
     * {@code os.pullEventRaw()} in lua.
     *
     * @param filter A specific event to wait for, or null to wait for any event.
     * @return An object array containing the name of the event that occurred, and any event parameters.
     * @throws InterruptedException If the user shuts down or reboots the computer while pullEventRaw() is waiting for
     *                              an event, InterruptedException will be thrown. This exception must not be caught or
     *                              intercepted, or the computer will leak memory and end up in a broken state.
     * @see #pullEvent(String)
     */
    @Nonnull
    Object[] pullEventRaw( @Nullable String filter ) throws InterruptedException;

    /**
     * Yield the current coroutine with some arguments until it is resumed. This method is exactly equivalent to
     * {@code coroutine.yield()} in lua. Use {@code pullEvent()} if you wish to wait for events.
     *
     * @param arguments An object array containing the arguments to pass to coroutine.yield()
     * @return An object array containing the return values from coroutine.yield()
     * @throws InterruptedException If the user shuts down or reboots the computer the coroutine is suspended,
     *                              InterruptedException will be thrown. This exception must not be caught or
     *                              intercepted, or the computer will leak memory and end up in a broken state.
     * @see #pullEvent(String)
     */
    @Nonnull
    Object[] yield( @Nullable Object[] arguments ) throws InterruptedException;

    /**
     * Queue a task to be executed on the main server thread at the beginning of next tick, waiting for it to complete.
     * This should be used when you need to interact with the world in a thread-safe manner.
     *
     * Note that the return values of your task are handled as events, meaning more complex objects such as maps or
     * {@link ILuaObject} will not preserve their identities.
     *
     * @param task The task to execute on the main thread.
     * @return The objects returned by {@code task}.
     * @throws LuaException         If the task could not be queued, or if the task threw an exception.
     * @throws InterruptedException If the user shuts down or reboots the computer the coroutine is suspended,
     *                              InterruptedException will be thrown. This exception must not be caught or
     *                              intercepted, or the computer will leak memory and end up in a broken state.
     */
    @Nullable
    Object[] executeMainThreadTask( @Nonnull ILuaTask task ) throws LuaException, InterruptedException;

    /**
     * Queue a task to be executed on the main server thread at the beginning of next tick, but do not wait for it to
     * complete. This should be used when you need to interact with the world in a thread-safe manner but do not care
     * about the result or you wish to run asynchronously.
     *
     * When the task has finished, it will enqueue a {@code task_completed} event, which takes the task id, a success
     * value and the return values, or an error message if it failed. If you need to wait on this event, it may be
     * better to use {@link #executeMainThreadTask(ILuaTask)}.
     *
     * @param task The task to execute on the main thread.
     * @return The "id" of the task. This will be the first argument to the {@code task_completed} event.
     * @throws LuaException If the task could not be queued.
     */
    long issueMainThreadTask( @Nonnull ILuaTask task ) throws LuaException;
}
