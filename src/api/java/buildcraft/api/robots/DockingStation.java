/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.api.robots;

import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.fluids.IFluidHandler;

import buildcraft.api.core.BCLog;
import buildcraft.api.core.EnumPipePart;
import buildcraft.api.statements.StatementSlot;
import buildcraft.api.transport.IInjectable;

public abstract class DockingStation {
    public EnumFacing side;
    public World world;

    private long robotTakingId = EntityRobotBase.NULL_ROBOT_ID;
    private EntityRobotBase robotTaking;

    private boolean linkIsMain = false;

    private BlockPos pos;

    public DockingStation(BlockPos iIndex, EnumFacing iSide) {
        pos = iIndex;
        side = iSide;
    }

    public DockingStation() {}

    public boolean isMainStation() {
        return linkIsMain;
    }

    public BlockPos getPos() {
        return pos;
    }

    public EnumFacing side() {
        return side;
    }

    public EntityRobotBase robotTaking() {
        if (robotTakingId == EntityRobotBase.NULL_ROBOT_ID) {
            return null;
        } else if (robotTaking == null) {
            robotTaking = RobotManager.registryProvider.getRegistry(world).getLoadedRobot(robotTakingId);
        }

        return robotTaking;
    }

    public void invalidateRobotTakingEntity() {
        robotTaking = null;
    }

    public long linkedId() {
        return robotTakingId;
    }

    public boolean takeAsMain(EntityRobotBase robot) {
        if (robotTakingId == EntityRobotBase.NULL_ROBOT_ID) {
            IRobotRegistry registry = RobotManager.registryProvider.getRegistry(world);
            linkIsMain = true;
            robotTaking = robot;
            robotTakingId = robot.getRobotId();
            registry.registryMarkDirty();
            robot.setMainStation(this);
            registry.take(this, robot.getRobotId());

            return true;
        } else {
            return robotTakingId == robot.getRobotId();
        }
    }

    public boolean take(EntityRobotBase robot) {
        if (robotTaking == null) {
            IRobotRegistry registry = RobotManager.registryProvider.getRegistry(world);
            linkIsMain = false;
            robotTaking = robot;
            robotTakingId = robot.getRobotId();
            registry.registryMarkDirty();
            registry.take(this, robot.getRobotId());

            return true;
        } else {
            return robot.getRobotId() == robotTakingId;
        }
    }

    public void release(EntityRobotBase robot) {
        if (robotTaking == robot && !linkIsMain) {
            IRobotRegistry registry = RobotManager.registryProvider.getRegistry(world);
            unsafeRelease(robot);
            registry.registryMarkDirty();
            registry.release(this, robot.getRobotId());
        }
    }

    /** Same a release but doesn't clear the registry (presumably called from the registry). */
    public void unsafeRelease(EntityRobotBase robot) {
        if (robotTaking == robot) {
            linkIsMain = false;
            robotTaking = null;
            robotTakingId = EntityRobotBase.NULL_ROBOT_ID;
        }
    }

    public void writeToNBT(NBTTagCompound nbt) {
        nbt.setIntArray("pos", new int[] { getPos().getX(), getPos().getY(), getPos().getZ() });
        nbt.setByte("side", (byte) side.ordinal());
        nbt.setBoolean("isMain", linkIsMain);
        nbt.setLong("robotId", robotTakingId);
    }

    public void readFromNBT(NBTTagCompound nbt) {
        if (nbt.hasKey("index")) {
            // For compatibility with older versions of minecraft and buildcraft
            NBTTagCompound indexNBT = nbt.getCompoundTag("index");
            int x = indexNBT.getInteger("i");
            int y = indexNBT.getInteger("j");
            int z = indexNBT.getInteger("k");
            pos = new BlockPos(x, y, z);
        } else {
            int[] array = nbt.getIntArray("pos");
            if (array.length == 3) {
                pos = new BlockPos(array[0], array[1], array[2]);
            } else if (array.length != 0) {
                BCLog.logger.warn("Found an integer array that wwas not the right length! (" + array + ")");
            } else {
                BCLog.logger.warn("Did not find any integer positions! This is a bug!");
            }
        }
        side = EnumFacing.values()[nbt.getByte("side")];
        linkIsMain = nbt.getBoolean("isMain");
        robotTakingId = nbt.getLong("robotId");
    }

    public boolean isTaken() {
        return robotTakingId != EntityRobotBase.NULL_ROBOT_ID;
    }

    public long robotIdTaking() {
        return robotTakingId;
    }

    public BlockPos index() {
        return pos;
    }

    @Override
    public String toString() {
        return "{" + pos + ", " + side + " :" + robotTakingId + "}";
    }

    public boolean linkIsDocked() {
        if (robotTaking() != null) {
            return robotTaking().getDockingStation() == this;
        } else {
            return false;
        }
    }

    public boolean canRelease() {
        return !isMainStation() && !linkIsDocked();
    }

    public boolean isInitialized() {
        return true;
    }

    public abstract Iterable<StatementSlot> getActiveActions();

    public IInjectable getItemOutput() {
        return null;
    }

    public EnumPipePart getItemOutputSide() {
        return EnumPipePart.CENTER;
    }

    public IInventory getItemInput() {
        return null;
    }

    public EnumPipePart getItemInputSide() {
        return EnumPipePart.CENTER;
    }

    public IFluidHandler getFluidOutput() {
        return null;
    }

    public EnumPipePart getFluidOutputSide() {
        return EnumPipePart.CENTER;
    }

    public IFluidHandler getFluidInput() {
        return null;
    }

    public EnumPipePart getFluidInputSide() {
        return EnumPipePart.CENTER;
    }

    public boolean providesPower() {
        return false;
    }

    public IRequestProvider getRequestProvider() {
        return null;
    }

    public void onChunkUnload() {

    }
}
