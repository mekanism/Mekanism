/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
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
