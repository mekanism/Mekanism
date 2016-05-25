/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package buildcraft.api.statements;

public interface IActionInternal extends IStatement {

    void actionActivate(IStatementContainer source, IStatementParameter[] parameters);

    public interface IActionInternalSingle extends IActionInternal {
        /** @return True if this action should only be fired for the first tick of it being active. */
        boolean singleActionTick();
    }
}
