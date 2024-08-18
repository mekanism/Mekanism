package mekanism.common.lib;

import it.unimi.dsi.fastutil.longs.LongList;
import mekanism.api.SerializationConstants;
import mekanism.common.content.transporter.TransporterPathfinder.Destination;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;

public record SidedBlockPos(long pos, Direction side) {

    public static SidedBlockPos get(Destination destination) {
        LongList path = destination.getPath();
        long pos = path.getLong(0);
        Direction sideOfDest = WorldUtils.sideDifference(path.getLong(1), pos);
        return new SidedBlockPos(pos, sideOfDest);
    }

    @Nullable
    public static SidedBlockPos deserialize(CompoundTag tag) {
        if (tag.contains(SerializationConstants.SIDE, Tag.TAG_INT)) {
            long pos = Long.MAX_VALUE;
            if (tag.contains(SerializationConstants.POSITION, Tag.TAG_INT_ARRAY)) {
                //old version
                pos = NbtUtils.readBlockPos(tag, SerializationConstants.POSITION).map(BlockPos::asLong).orElse(Long.MAX_VALUE);
            } else if (tag.contains(SerializationConstants.POSITION, Tag.TAG_LONG)) {
                pos = tag.getLong(SerializationConstants.POSITION);
            }
            if (pos != Long.MAX_VALUE) {
                return new SidedBlockPos(pos, Direction.from3DDataValue(tag.getInt(SerializationConstants.SIDE)));
            }
        }
        return null;
    }

    public CompoundTag serialize() {
        CompoundTag target = new CompoundTag();
        target.putLong(SerializationConstants.POSITION, pos);
        NBTUtils.writeEnum(target, SerializationConstants.SIDE, side);
        return target;
    }
}