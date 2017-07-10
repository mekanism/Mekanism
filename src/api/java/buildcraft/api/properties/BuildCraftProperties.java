package buildcraft.api.properties;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.EnumFacing;

import buildcraft.api.enums.EnumDecoratedBlock;
import buildcraft.api.enums.EnumEngineType;
import buildcraft.api.enums.EnumFillerPattern;
import buildcraft.api.enums.EnumLaserTableType;
import buildcraft.api.enums.EnumMachineState;
import buildcraft.api.enums.EnumOptionalSnapshotType;
import buildcraft.api.enums.EnumPowerStage;
import buildcraft.api.enums.EnumSpring;

public final class BuildCraftProperties {
    public static final IProperty<EnumFacing> BLOCK_FACING = PropertyEnum.create("facing", EnumFacing.class, EnumFacing.Plane.HORIZONTAL.facings());
    public static final IProperty<EnumFacing> BLOCK_FACING_6 = PropertyEnum.create("facing", EnumFacing.class);

    public static final IProperty<EnumDyeColor> BLOCK_COLOR = PropertyEnum.create("color", EnumDyeColor.class);
    public static final IProperty<EnumSpring> SPRING_TYPE = PropertyEnum.create("type", EnumSpring.class);
    public static final IProperty<EnumEngineType> ENGINE_TYPE = PropertyEnum.create("type", EnumEngineType.class);
    public static final IProperty<EnumLaserTableType> LASER_TABLE_TYPE = PropertyEnum.create("type", EnumLaserTableType.class);
    public static final IProperty<EnumMachineState> MACHINE_STATE = PropertyEnum.create("state", EnumMachineState.class);
    public static final IProperty<EnumPowerStage> ENERGY_STAGE = PropertyEnum.create("stage", EnumPowerStage.class);
    public static final IProperty<EnumOptionalSnapshotType> SNAPSHOT_TYPE = PropertyEnum.create("snapshot_type", EnumOptionalSnapshotType.class);
    public static final IProperty<EnumDecoratedBlock> DECORATED_BLOCK = PropertyEnum.create("decoration_type", EnumDecoratedBlock.class);

    public static final IProperty<Integer> GENERIC_PIPE_DATA = PropertyInteger.create("pipe_data", 0, 15);
    public static final IProperty<Integer> LED_POWER = PropertyInteger.create("led_power", 0, 3);

    public static final IProperty<Boolean> JOINED_BELOW = PropertyBool.create("joined_below");
    public static final IProperty<Boolean> MOVING = PropertyBool.create("moving");
    public static final IProperty<Boolean> LED_DONE = PropertyBool.create("led_done");
    public static final IProperty<Boolean> ACTIVE = PropertyBool.create("active");
    public static final IProperty<Boolean> VALID = PropertyBool.create("valid");

    public static final IProperty<Boolean> CONNECTED_UP = PropertyBool.create("connected_up");
    public static final IProperty<Boolean> CONNECTED_DOWN = PropertyBool.create("connected_down");
    public static final IProperty<Boolean> CONNECTED_EAST = PropertyBool.create("connected_east");
    public static final IProperty<Boolean> CONNECTED_WEST = PropertyBool.create("connected_west");
    public static final IProperty<Boolean> CONNECTED_NORTH = PropertyBool.create("connected_north");
    public static final IProperty<Boolean> CONNECTED_SOUTH = PropertyBool.create("connected_south");

    public static final Map<EnumFacing, IProperty<Boolean>> CONNECTED_MAP;

    public static final IProperty<EnumFillerPattern> FILLER_PATTERN = PropertyEnum.create("pattern", EnumFillerPattern.class);

    // Block state setting flags -these are used by World.markAndNotifyBlock and World.setBlockState. These flags can be
    // added together to pass the additions
    public static final int UPDATE_NONE = 0;
    /** This updates the neighbouring blocks that the new block is set. It also updates the comparator output of this
     * block. */
    public static final int UPDATE_NEIGHBOURS = 1;
    /** This will mark the block for an update next tick, as well as send an update to the client (if this is a server
     * world). */
    public static final int MARK_BLOCK_FOR_UPDATE = 2;
    /** This will mark the block for an update, even if this is a client world. It is useless to use this if
     * world.isRemote returns false. */
    public static final int UPDATE_EVEN_CLIENT = 4 + MARK_BLOCK_FOR_UPDATE; // 6

    // Pre-added flags- pass these as-is to the World.markAndNotifyBlock and World.setBlockState methods.
    /** This will do what both {@link #UPDATE_NEIGHBOURS} and {@link #MARK_BLOCK_FOR_UPDATE} do. */
    public static final int MARK_THIS_AND_NEIGHBOURS = UPDATE_NEIGHBOURS + MARK_BLOCK_FOR_UPDATE;
    /** This will update everything about this block. */
    public static final int UPDATE_ALL = UPDATE_NEIGHBOURS + MARK_BLOCK_FOR_UPDATE + UPDATE_EVEN_CLIENT;

    static {
        Map<EnumFacing, IProperty<Boolean>> map = Maps.newEnumMap(EnumFacing.class);
        map.put(EnumFacing.DOWN, CONNECTED_DOWN);
        map.put(EnumFacing.UP, CONNECTED_UP);
        map.put(EnumFacing.EAST, CONNECTED_EAST);
        map.put(EnumFacing.WEST, CONNECTED_WEST);
        map.put(EnumFacing.NORTH, CONNECTED_NORTH);
        map.put(EnumFacing.SOUTH, CONNECTED_SOUTH);
        CONNECTED_MAP = Maps.immutableEnumMap(map);
    }

    /** Deactivate constructor */
    private BuildCraftProperties() {}
}
