/*
 * This file is part of the public ComputerCraft API - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. This API may be redistributed unmodified and in full only.
 * For help using the API, and posting your mods, visit the forums at computercraft.info.
 */

package dan200.computercraft.api.turtle;

import dan200.computercraft.api.lua.ILuaContext;

import javax.annotation.Nonnull;

/**
 * An interface for objects executing custom turtle commands, used with {@link ITurtleAccess#executeCommand(ILuaContext, ITurtleCommand)}.
 *
 * @see ITurtleAccess#executeCommand(ILuaContext, ITurtleCommand)
 */
@FunctionalInterface
public interface ITurtleCommand
{
    /**
     * Will be called by the turtle on the main thread when it is time to execute the custom command.
     *
     * The handler should either perform the work of the command, and return success, or return
     * failure with an error message to indicate the command cannot be executed at this time.
     *
     * @param turtle Access to the turtle for whom the command was issued.
     * @return A result, indicating whether this action succeeded or not.
     * @see ITurtleAccess#executeCommand(ILuaContext, ITurtleCommand)
     * @see TurtleCommandResult#success()
     * @see TurtleCommandResult#failure(String)
     * @see TurtleCommandResult
     */
    @Nonnull
    TurtleCommandResult execute( @Nonnull ITurtleAccess turtle );
}
