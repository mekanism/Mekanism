package mekanism.common.lib;

import java.util.List;
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

public record SidedBlockPos(BlockPos pos, Direction side) {

    public static SidedBlockPos get(Destination destination) {
        List<BlockPos> path = destination.getPath();
        BlockPos pos = path.get(0);
        Direction sideOfDest = WorldUtils.sideDifference(path.get(1), pos);
        return new SidedBlockPos(pos, sideOfDest);
    }

    @Nullable
    public static SidedBlockPos deserialize(CompoundTag tag) {
        if (tag.contains(SerializationConstants.SIDE, Tag.TAG_INT)) {
            BlockPos pos = NbtUtils.readBlockPos(tag, SerializationConstants.POSITION).orElse(null);
            if (pos != null) {
                return new SidedBlockPos(pos, Direction.from3DDataValue(tag.getInt(SerializationConstants.SIDE)));
            }
        }
        return null;
    }

    public SidedBlockPos {
        pos = pos.immutable();
    }

    public CompoundTag serialize() {
        CompoundTag target = new CompoundTag();
        target.put(SerializationConstants.POSITION, NbtUtils.writeBlockPos(pos));
        NBTUtils.writeEnum(target, SerializationConstants.SIDE, side);
        return target;
    }
}