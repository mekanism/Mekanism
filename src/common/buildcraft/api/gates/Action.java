/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.api.gates;


public abstract class Action implements IAction {

	protected int id;

	public Action(int id) {
		this.id = id;
		ActionManager.actions[id] = this;
	}

	@Override
	public int getId() {
		return this.id;
	}
	
	@Override
	public abstract String getTexture();

	@Override
	public int getIndexInTexture() {
		return 0;
	}

	@Override
	public boolean hasParameter() {
		return false;
	}

	@Override
	public String getDescription() {
		return "";
	}
}
