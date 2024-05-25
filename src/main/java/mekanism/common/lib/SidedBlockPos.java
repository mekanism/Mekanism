package mekanism.common.lib;

import java.util.List;
import mekanism.api.SerializationConstants;
import mekanism.common.content.transporter.TransporterPathfinder.Destination;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;

public record SidedBlockPos(BlockPos pos, Direction side) {

    public static SidedBlockPos get(Destination destination) {
        List<BlockPos> path = destination.getPath();
        BlockPos pos = path.get(0);
        Direction sideOfDest = WorldUtils.sideDifference(path.get(1), pos);
        return new SidedBlockPos(pos, sideOfDest);
    }

    @Nullable
    public static SidedBlockPos deserialize(CompoundTag tag) {
        if (tag.contains(SerializationConstants.X, Tag.TAG_INT) && tag.contains(SerializationConstants.Y, Tag.TAG_INT) && tag.contains(SerializationConstants.Z, Tag.TAG_INT) &&
            tag.contains(SerializationConstants.SIDE, Tag.TAG_INT)) {
            BlockPos pos = new BlockPos(tag.getInt(SerializationConstants.X), tag.getInt(SerializationConstants.Y), tag.getInt(SerializationConstants.Z));
            Direction side = Direction.from3DDataValue(tag.getInt(SerializationConstants.SIDE));
            return new SidedBlockPos(pos, side);
        }
        return null;
    }

    public SidedBlockPos {
        pos = pos.immutable();
    }

    public CompoundTag serialize() {
        CompoundTag target = new CompoundTag();
        target.putInt(SerializationConstants.X, pos.getX());
        target.putInt(SerializationConstants.Y, pos.getY());
        target.putInt(SerializationConstants.Z, pos.getZ());
        NBTUtils.writeEnum(target, SerializationConstants.SIDE, side);
        return target;
    }
}