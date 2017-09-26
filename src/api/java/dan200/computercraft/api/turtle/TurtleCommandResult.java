/*
 * This file is part of the public ComputerCraft API - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. This API may be redistributed unmodified and in full only.
 * For help using the API, and posting your mods, visit the forums at computercraft.info.
 */

package dan200.computercraft.api.turtle;

import net.minecraft.util.EnumFacing;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Used to indicate the result of executing a turtle command.
 *
 * @see ITurtleCommand#execute(ITurtleAccess)
 * @see ITurtleUpgrade#useTool(ITurtleAccess, TurtleSide, TurtleVerb, EnumFacing)
 */
public final class TurtleCommandResult
{
    private static final TurtleCommandResult s_success = new TurtleCommandResult( true, null, null );
    private static final TurtleCommandResult s_emptyFailure = new TurtleCommandResult( false, null, null );

    /**
     * Create a successful command result with no result.
     *
     * @return A successful command result with no values.
     */
    @Nonnull
    public static TurtleCommandResult success()
    {
        return success( null );
    }

    /**
     * Create a successful command result with the given result values.
     *
     * @param results The results of executing this command.
     * @return A successful command result with the given values.
     */
    @Nonnull
    public static TurtleCommandResult success( @Nullable Object[] results )
    {
        if( results == null || results.length == 0 )
        {
            return s_success;
        }
        else
        {
            return new TurtleCommandResult( true, null, results );
        }
    }

    /**
     * Create a failed command result with no error message.
     *
     * @return A failed command result with no message.
     */
    @Nonnull
    public static TurtleCommandResult failure()
    {
        return failure( null );
    }

    /**
     * Create a failed command result with an error message.
     *
     * @param errorMessage The error message to provide.
     * @return A failed command result with a message.
     */
    @Nonnull
    public static TurtleCommandResult failure( @Nullable String errorMessage )
    {
        if( errorMessage == null )
        {
            return s_emptyFailure;
        }
        else
        {
            return new TurtleCommandResult( false, errorMessage, null );
        }
    }

    private final boolean m_success;
    private final String m_errorMessage;
    private final Object[] m_results;

    private TurtleCommandResult( boolean success, String errorMessage, Object[] results )
    {
        m_success = success;
        m_errorMessage = errorMessage;
        m_results = results;
    }

    /**
     * Determine whether the command executed successfully.
     *
     * @return If the command was successful.
     */
    public boolean isSuccess()
    {
        return m_success;
    }

    /**
     * Get the error message of this command result.
     *
     * @return The command's error message, or {@code null} if it was a success.
     */
    @Nullable
    public String getErrorMessage()
    {
        return m_errorMessage;
    }

    /**
     * Get the resulting values of this command result.
     *
     * @return The command's result, or {@code null} if it was a failure.
     */
    @Nullable
    public Object[] getResults()
    {
        return m_results;
    }
}
