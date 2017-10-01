/*
 * This file is part of the public ComputerCraft API - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. This API may be redistributed unmodified and in full only.
 * For help using the API, and posting your mods, visit the forums at computercraft.info.
 */

package dan200.computercraft.api.turtle;

/**
 * An enum representing the different types of turtle that an {@link ITurtleUpgrade} implementation can add to a turtle.
 *
 * @see ITurtleUpgrade#getType()
 */
public enum TurtleUpgradeType
{
    /**
     * A tool is rendered as an item on the side of the turtle, and responds to the {@code turtle.dig()}
     * and {@code turtle.attack()} methods (Such as pickaxe or sword on Mining and Melee turtles).
     */
    Tool,

    /**
     * A peripheral adds a special peripheral which is attached to the side of the turtle,
     * and can be interacted with the {@code peripheral} API (Such as the modem on Wireless Turtles).
     */
    Peripheral,

    /**
     * An upgrade which provides both a tool and a peripheral. This can be used when you wish
     * your upgrade to also provide methods. For example, a pickaxe could provide methods
     * determining whether it can break the given block or not.
     */
    Both,;

    public boolean isTool()
    {
        return this == Tool || this == Both;
    }

    public boolean isPeripheral()
    {
        return this == Peripheral || this == Both;
    }
}
