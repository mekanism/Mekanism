/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.statements;

public class StatementSlot {
	public IStatement statement;
	public IStatementParameter[] parameters;

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof StatementSlot)) {
			return false;
		}
		StatementSlot s = (StatementSlot) o;
		if (s.statement != statement || parameters.length != s.parameters.length) {
			return false;
		}
		for (int i = 0; i < parameters.length; i++) {
			IStatementParameter p1 = parameters[i];
			IStatementParameter p2 = s.parameters[i];
			if (p1 == null && p2 != null) {
				return false;
			}
			if (!(p1.equals(p2))) {
				return false;
			}
		}
		return true;
	}
}
