/*
 * This file is part of the public ComputerCraft API - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. This API may be redistributed unmodified and in full only.
 * For help using the API, and posting your mods, visit the forums at computercraft.info.
 */

package dan200.computercraft.api.turtle;

/**
 * An animation a turtle will play between executing commands.
 *
 * Each animation takes 8 ticks to complete unless otherwise specified.
 *
 * @see ITurtleAccess#playAnimation(TurtleAnimation)
 */
public enum TurtleAnimation
{
    /**
     * An animation which does nothing. This takes no time to complete.
     *
     * @see #Wait
     * @see #ShortWait
     */
    None,

    /**
     * Make the turtle move forward. Note that the animation starts from the block <em>behind</em> it, and
     * moves into this one.
     */
    MoveForward,

    /**
     * Make the turtle move backwards. Note that the animation starts from the block <em>in front</em> it, and
     * moves into this one.
     */
    MoveBack,

    /**
     * Make the turtle move backwards. Note that the animation starts from the block <em>above</em> it, and
     * moves into this one.
     */
    MoveUp,

    /**
     * Make the turtle move backwards. Note that the animation starts from the block <em>below</em> it, and
     * moves into this one.
     */
    MoveDown,

    /**
     * Turn the turtle to the left. Note that the animation starts with the turtle facing <em>right</em>, and
     * the turtle turns to face in the current direction.
     */
    TurnLeft,

    /**
     * Turn the turtle to the left. Note that the animation starts with the turtle facing <em>right</em>, and
     * the turtle turns to face in the current direction.
     */
    TurnRight,

    /**
     * Swing the tool on the left.
     */
    SwingLeftTool,

    /**
     * Swing the tool on the right.
     */
    SwingRightTool,

    /**
     * Wait until the animation has finished, performing no movement.
     *
     * @see #ShortWait
     * @see #None
     */
    Wait,

    /**
     * Wait until the animation has finished, performing no movement. This takes 4 ticks to complete.
     *
     * @see #Wait
     * @see #None
     */
    ShortWait,
}
