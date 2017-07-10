/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package buildcraft.api.mj;

/** This interface should be defined by any Tile which wants to receive power from BuildCraft lasers.
 *
 * The respective Block MUST implement ILaserTargetBlock! */
public interface ILaserTarget {

    /** Returns The amount of power this target currently needs.
     *
     * @return The amount of power required, or 0 if no power is required. */
    long getRequiredLaserPower();

    /** Transfers power from the laser to the target.
     *
     * @param microJoules The number of micro Minecraft Joules to accept
     * @return The excess power. If the input is less than or equal to {@link #getRequiredLaserPower()} then this will return 0. */
    long receiveLaserPower(long microJoules);

    /** Return true if the Tile Entity object is no longer a valid target. For example, if its been invalidated.
     *
     * @return true if no longer a valid target object */
    boolean isInvalidTarget();
}
