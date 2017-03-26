/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package buildcraft.api.statements;

/** Designates some sort of statement. Most of the time you should implement {@link ITriggerExternal},
 * {@link ITriggerInternal}, {@link IActionExternal} or {@link IActionInternal} though. */
public interface IStatement extends IGuiSlot {

    /** Return the maximum number of parameter this statement can have, 0 if none. */
    int maxParameters();

    /** Return the minimum number of parameter this statement can have, 0 if none. */
    int minParameters();

    /** Create parameters for the statement. */
    IStatementParameter createParameter(int index);

    /** This returns the statement after a left rotation. Used in particular in blueprints orientation. */
    IStatement rotateLeft();

    /** This returns a group of related statements. For example "redstone signal input" should probably return an array
     * of "RS_SIGNAL_ON" and "RS_SIGNAL_OFF". It is recommended to return an array containing this object. */
    IStatement[] getPossible();
}
