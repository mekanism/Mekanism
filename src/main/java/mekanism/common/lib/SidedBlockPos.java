package mekanism.common.lib;

import java.util.List;
import javax.annotation.Nullable;
import mekanism.api.NBTConstants;
import mekanism.common.content.transporter.TransporterPathfinder.Destination;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public record SidedBlockPos(BlockPos pos, Direction side) {

    public static SidedBlockPos get(Destination destination) {
        List<BlockPos> path = destination.getPath();
        BlockPos pos = path.get(0);
        Direction sideOfDest = WorldUtils.sideDifference(path.get(1), pos);
        return new SidedBlockPos(pos, sideOfDest);
    }

    @Nullable
    public static SidedBlockPos deserialize(CompoundTag tag) {
        if (tag.contains(NBTConstants.X, Tag.TAG_INT) && tag.contains(NBTConstants.Y, Tag.TAG_INT) && tag.contains(NBTConstants.Z, Tag.TAG_INT) &&
            tag.contains(NBTConstants.SIDE, Tag.TAG_INT)) {
            BlockPos pos = new BlockPos(tag.getInt(NBTConstants.X), tag.getInt(NBTConstants.Y), tag.getInt(NBTConstants.Z));
            Direction side = Direction.from3DDataValue(tag.getInt(NBTConstants.SIDE));
            return new SidedBlockPos(pos, side);
        }
        return null;
    }

    public SidedBlockPos {
        pos = pos.immutable();
    }

    public CompoundTag serialize() {
        CompoundTag target = new CompoundTag();
        target.putInt(NBTConstants.X, pos.getX());
        target.putInt(NBTConstants.Y, pos.getY());
        target.putInt(NBTConstants.Z, pos.getZ());
        NBTUtils.writeEnum(target, NBTConstants.SIDE, side);
        return target;
    }
}