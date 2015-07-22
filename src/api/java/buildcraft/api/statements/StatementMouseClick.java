/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License.
 * Please check the contents of the license, which should be located
 * as "LICENSE.API" in the BuildCraft source code distribution.
 */
package buildcraft.api.statements;

public final class StatementMouseClick {
	private int button;
	private boolean shift;
	
	public StatementMouseClick(int button, boolean shift) {
		this.button = button;
		this.shift = shift;
	}
	
	public boolean isShift() {
		return shift;
	}
	
	public int getButton() {
		return button;
	}
}
