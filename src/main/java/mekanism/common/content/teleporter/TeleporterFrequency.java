package mekanism.common.content.teleporter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import mekanism.api.SerializationConstants;
import mekanism.api.security.SecurityMode;
import mekanism.api.text.EnumColor;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.FrequencyTypes;
import mekanism.common.lib.frequency.IColorableFrequency;
import mekanism.common.tile.interfaces.ITileWrapper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public class TeleporterFrequency extends Frequency implements IColorableFrequency {

    public static final Codec<TeleporterFrequency> CODEC = RecordCodecBuilder.create(instance -> baseCodec(instance)
          .and(EnumColor.CODEC.fieldOf(SerializationConstants.COLOR).forGetter(TeleporterFrequency::getColor))
          .apply(instance, (name, owner, securityMode, color) -> {
              TeleporterFrequency frequency = new TeleporterFrequency(name, owner.orElse(null), securityMode);
              frequency.color = color;
              return frequency;
          }));
    public static final StreamCodec<ByteBuf, TeleporterFrequency> STREAM_CODEC = StreamCodec.composite(
          baseStreamCodec(TeleporterFrequency::new), Function.identity(),
          EnumColor.STREAM_CODEC, TeleporterFrequency::getColor,
          (frequency, color) -> {
              frequency.color = color;
              return frequency;
          }
    );


    private final Set<GlobalPos> activeCoords = new ObjectOpenHashSet<>();
    private EnumColor color = EnumColor.PURPLE;

    /**
     * @param uuid Should only be null if we have incomplete data that we are loading
     */
    public TeleporterFrequency(String n, @Nullable UUID uuid, SecurityMode securityMode) {
        super(FrequencyTypes.TELEPORTER, n, uuid, securityMode);
    }

    private TeleporterFrequency(String name, @Nullable UUID owner, String ownerName, SecurityMode securityMode) {
        super(FrequencyTypes.TELEPORTER, name, owner, ownerName, securityMode);
    }

    public Set<GlobalPos> getActiveCoords() {
        return activeCoords;
    }

    @Override
    public int getSyncHash() {
        int code = super.getSyncHash();
        code = 31 * code + color.ordinal();
        return code;
    }

    @Override
    public EnumColor getColor() {
        return color;
    }

    @Override
    public void setColor(EnumColor color) {
        if (this.color != color) {
            this.color = color;
            this.dirty = true;
        }
    }

    @Override
    public boolean update(BlockEntity tile) {
        boolean changedData = super.update(tile);
        activeCoords.add(getCoord(tile));
        return changedData;
    }

    @Override
    public boolean onDeactivate(BlockEntity tile) {
        boolean changedData = super.onDeactivate(tile);
        activeCoords.remove(getCoord(tile));
        return changedData;
    }

    private GlobalPos getCoord(BlockEntity tile) {
        if (tile instanceof ITileWrapper tileWrapper) {
            //Note: This should be the case the majority of the time, and allows us to use the cached coord4d object
            return tileWrapper.getTileGlobalPos();
        }
        return GlobalPos.of(tile.getLevel().dimension(), tile.getBlockPos());
    }

    public GlobalPos getClosestCoords(GlobalPos pos) {
        return getClosestCoords(pos.dimension(), pos.pos());
    }

    private static boolean areEqual(GlobalPos pos, ResourceKey<Level> checkDim, BlockPos checkPos) {
        return pos.dimension() == checkDim && pos.pos().equals(checkPos);
    }

    public GlobalPos getClosestCoords(ResourceKey<Level> dimension, BlockPos pos) {
        GlobalPos closest = null;
        for (GlobalPos iterCoord : activeCoords) {
            if (areEqual(iterCoord, dimension, pos)) {
                continue;
            }
            if (closest == null) {
                closest = iterCoord;
                continue;
            }

            if (dimension != closest.dimension() && dimension == iterCoord.dimension()) {
                closest = iterCoord;
            } else if (dimension != closest.dimension() || dimension == iterCoord.dimension()) {
                if (pos.distSqr(closest.pos()) > pos.distSqr(iterCoord.pos())) {
                    closest = iterCoord;
                }
            }
        }
        return closest;
    }
}
