/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package buildcraft.api.robots;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import buildcraft.api.mj.MjAPI;

public class AIRobot {
    public EntityRobotBase robot;

    private AIRobot delegateAI;
    private AIRobot parentAI;

    private boolean success;

    public AIRobot(EntityRobotBase iRobot) {
        robot = iRobot;
        success = true;
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

    /** This gets called when a delegate AI ends work naturally.
     * 
     * @param ai The delegate AI which ended work. */
    public void delegateAIEnded(AIRobot ai) {

    }

    /** This gets called when a delegate AI is forcibly aborted.
     * 
     * @param ai The delegate AI which was aborted. */
    public void delegateAIAborted(AIRobot ai) {

    }

    public void writeSelfToNBT(NBTTagCompound nbt) {

    }

    public void loadSelfFromNBT(NBTTagCompound nbt) {

    }

    public boolean success() {
        return success;
    }

    protected void setSuccess(boolean iSuccess) {
        success = iSuccess;
    }

    public long getPowerCost() {
        return MjAPI.MJ / 10;
    }

    public boolean canLoadFromNBT() {
        return false;
    }

    /** Tries to receive items in parameters, return items that are left after the operation. */
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
                robot.getBattery().extractPower(1, getPowerCost());
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
        nbt.setString("aiName", RobotManager.getAIRobotName(getClass()));

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
                Class<?> aiRobotClass;
                if (sub.hasKey("class")) {
                    // Migration support for 6.4.x
                    aiRobotClass = RobotManager.getAIRobotByLegacyClassName(sub.getString("class"));
                } else {
                    aiRobotClass = RobotManager.getAIRobotByName(sub.getString("aiName"));
                }
                if (aiRobotClass != null) {
                    delegateAI = (AIRobot) aiRobotClass.getConstructor(EntityRobotBase.class).newInstance(robot);
                    delegateAI.parentAI = this;

                    if (delegateAI.canLoadFromNBT()) {
                        delegateAI.loadFromNBT(sub);
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    public static AIRobot loadAI(NBTTagCompound nbt, EntityRobotBase robot) {
        AIRobot ai = null;

        try {
            Class<?> aiRobotClass;
            if (nbt.hasKey("class")) {
                // Migration support for 6.4.x
                aiRobotClass = RobotManager.getAIRobotByLegacyClassName(nbt.getString("class"));
            } else {
                aiRobotClass = RobotManager.getAIRobotByName(nbt.getString("aiName"));
            }
            if (aiRobotClass != null) {
                ai = (AIRobot) aiRobotClass.getConstructor(EntityRobotBase.class).newInstance(robot);
                ai.loadFromNBT(nbt);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return ai;
    }
}
