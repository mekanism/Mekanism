/*
 * This file is part of the public ComputerCraft API - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. This API may be redistributed unmodified and in full only.
 * For help using the API, and posting your mods, visit the forums at computercraft.info.
 */

package dan200.computercraft.api.lua;

import javax.annotation.Nullable;

/**
 * An exception representing an error in Lua, like that raised by the {@code error()} function.
 */
public class LuaException extends Exception
{
    private static final long serialVersionUID = -6136063076818512651L;
    private final int m_level;

    public LuaException()
    {
        this( "error", 1 );
    }

    public LuaException( @Nullable String message )
    {
        this( message, 1 );
    }

    public LuaException( @Nullable String message, int level )
    {
        super( message );
        m_level = level;
    }

    /**
     * The level this error is raised at. Level 1 is the function's caller, level 2 is that function's caller, and so
     * on.
     *
     * @return The level to raise the error at.
     */
    public int getLevel()
    {
        return m_level;
    }
}
