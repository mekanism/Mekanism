/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package buildcraft.api.tiles;

/** This interface should be implemented by any Tile Entity which wishes to have non-redstone automation (for example,
 * BuildCraft Gates, but also other mods which implement it, e.g. OpenComputers). */
public interface IControllable {
    public enum Mode {
        Unknown,
        On,
        Off,
        Mode,
        Loop
    }

    /** Get the current control mode of the Tile Entity.
     * 
     * @return */
    Mode getControlMode();

    /** Set the mode of the Tile Entity.
     * 
     * @param mode */
    void setControlMode(Mode mode);

    /** Check if a given control mode is accepted. If you query IControllable tiles, you MUST check with
     * acceptsControlMode first.
     * 
     * @param mode
     * @return True if this control mode is accepted. */
    boolean acceptsControlMode(Mode mode);
}
