/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.robots;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class AIRobot {
	public EntityRobotBase robot;

	private AIRobot delegateAI;
	private AIRobot parentAI;

	public AIRobot(EntityRobotBase iRobot) {
		robot = iRobot;
	}

	public void start() {

	}

	public void preempt(AIRobot ai) {

	}

	public void update() {
		// Update should always handle terminate. Some AI are not using update
		// at all, their code being in start() and end(). In these case,
		// calling update is a malfunction, the ai should be terminated.
		terminate();
	}

	public void end() {

	}

	public void delegateAIEnded(AIRobot ai) {

	}

	public void delegateAIAborted(AIRobot ai) {

	}

	public void writeSelfToNBT(NBTTagCompound nbt) {

	}

	public void loadSelfFromNBT(NBTTagCompound nbt) {

	}

	public boolean success() {
		return true;
	}

	public int getEnergyCost() {
		return 1;
	}

	public boolean canLoadFromNBT() {
		return false;
	}

	/**
	 * Tries to receive items in parameters, return items that are left after
	 * the operation.
	 */
	public ItemStack receiveItem(ItemStack stack) {
		return stack;
	}

	public final void terminate() {
		abortDelegateAI();
		end();

		if (parentAI != null) {
			parentAI.delegateAI = null;
			parentAI.delegateAIEnded(this);
		}
	}

	public final void abort() {
		abortDelegateAI();

		try {
			end();

			if (parentAI != null) {
				parentAI.delegateAI = null;
				parentAI.delegateAIAborted(this);
			}
		} catch (Throwable e) {
			e.printStackTrace();
			delegateAI = null;

			if (parentAI != null) {
				parentAI.delegateAI = null;
			}
		}
	}

	public final void cycle() {
		try {
			preempt(delegateAI);

			if (delegateAI != null) {
				delegateAI.cycle();
			} else {
				robot.getBattery().extractEnergy(getEnergyCost(), false);
				update();
			}
		} catch (Throwable e) {
			e.printStackTrace();
			abort();
		}
	}

	public final void startDelegateAI(AIRobot ai) {
		abortDelegateAI();
		delegateAI = ai;
		ai.parentAI = this;
		delegateAI.start();
	}

	public final void abortDelegateAI() {
		if (delegateAI != null) {
			delegateAI.abort();
		}
	}

	public final AIRobot getActiveAI() {
		if (delegateAI != null) {
			return delegateAI.getActiveAI();
		} else {
			return this;
		}
	}

	public final AIRobot getDelegateAI() {
		return delegateAI;
	}

	public final void writeToNBT(NBTTagCompound nbt) {
		nbt.setString("class", getClass().getCanonicalName());

		NBTTagCompound data = new NBTTagCompound();
		writeSelfToNBT(data);
		nbt.setTag("data", data);

		if (delegateAI != null && delegateAI.canLoadFromNBT()) {
			NBTTagCompound sub = new NBTTagCompound();

			delegateAI.writeToNBT(sub);
			nbt.setTag("delegateAI", sub);
		}
	}

	public final void loadFromNBT(NBTTagCompound nbt) {
		loadSelfFromNBT(nbt.getCompoundTag("data"));

		if (nbt.hasKey("delegateAI")) {
			NBTTagCompound sub = nbt.getCompoundTag("delegateAI");

			try {
				delegateAI = (AIRobot) Class.forName(sub.getString("class")).getConstructor(EntityRobotBase.class)
						.newInstance(robot);

				if (delegateAI.canLoadFromNBT()) {
					delegateAI.parentAI = this;
					delegateAI.loadFromNBT(sub);
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}

	public static AIRobot loadAI(NBTTagCompound nbt, EntityRobotBase robot) {
		AIRobot ai = null;

		try {
			ai = (AIRobot) Class.forName(nbt.getString("class")).getConstructor(EntityRobotBase.class)
					.newInstance(robot);
			ai.loadFromNBT(nbt);
		} catch (Throwable e) {
			e.printStackTrace();
		}

		return ai;
	}
}
