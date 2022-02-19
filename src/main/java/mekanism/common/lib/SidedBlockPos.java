package mekanism.common.lib;

import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import mekanism.api.NBTConstants;
import mekanism.common.content.transporter.TransporterPathfinder.Destination;
import mekanism.common.util.WorldUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants.NBT;

public class SidedBlockPos {

    public static SidedBlockPos get(Destination destination) {
        List<BlockPos> path = destination.getPath();
        BlockPos pos = path.get(0);
        Direction sideOfDest = WorldUtils.sideDifference(path.get(1), pos);
        return new SidedBlockPos(pos, sideOfDest);
    }

    @Nullable
    public static SidedBlockPos deserialize(CompoundNBT tag) {
        if (tag.contains(NBTConstants.X, NBT.TAG_INT) && tag.contains(NBTConstants.Y, NBT.TAG_INT) && tag.contains(NBTConstants.Z, NBT.TAG_INT) &&
            tag.contains(NBTConstants.SIDE, NBT.TAG_INT)) {
            BlockPos pos = new BlockPos(tag.getInt(NBTConstants.X), tag.getInt(NBTConstants.Y), tag.getInt(NBTConstants.Z));
            Direction side = Direction.from3DDataValue(tag.getInt(NBTConstants.SIDE));
            return new SidedBlockPos(pos, side);
        }
        return null;
    }

    private final BlockPos pos;
    private final Direction side;

    public SidedBlockPos(BlockPos pos, Direction side) {
        this.pos = pos.immutable();
        this.side = side;
    }

    public BlockPos getPos() {
        return pos;
    }

    public Direction getSide() {
        return side;
    }

    public CompoundNBT serialize() {
        CompoundNBT target = new CompoundNBT();
        target.putInt(NBTConstants.X, pos.getX());
        target.putInt(NBTConstants.Y, pos.getY());
        target.putInt(NBTConstants.Z, pos.getZ());
        target.putInt(NBTConstants.SIDE, side.ordinal());
        return target;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SidedBlockPos that = (SidedBlockPos) o;
        return pos.equals(that.pos) && side == that.side;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pos, side);
    }
}