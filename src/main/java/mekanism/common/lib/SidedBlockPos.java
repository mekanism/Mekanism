package mekanism.common.lib;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.LongList;
import mekanism.api.SerializationConstants;
import mekanism.common.content.transporter.TransporterPathfinder.Destination;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.Direction;

public record SidedBlockPos(long pos, Direction side) {

    //TODO - 1.21.11: Should we just use Direction.CODEC to store the side? Most likely
    public static final Codec<SidedBlockPos> CODEC = RecordCodecBuilder.create(instance -> instance.group(
          Codec.LONG.fieldOf(SerializationConstants.POSITION).forGetter(SidedBlockPos::pos),
          Direction.LEGACY_ID_CODEC.fieldOf(SerializationConstants.SIDE).forGetter(SidedBlockPos::side)
    ).apply(instance, SidedBlockPos::new));

    public static SidedBlockPos get(Destination destination) {
        LongList path = destination.getPath();
        long pos = path.getLong(0);
        Direction sideOfDest = WorldUtils.sideDifference(path.getLong(1), pos);
        return new SidedBlockPos(pos, sideOfDest);
    }
}