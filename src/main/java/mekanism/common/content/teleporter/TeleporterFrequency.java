package mekanism.common.content.teleporter;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Set;
import java.util.UUID;
import mekanism.api.Coord4D;
import mekanism.api.NBTConstants;
import mekanism.api.text.EnumColor;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.frequency.IColorableFrequency;
import mekanism.common.tile.interfaces.ITileWrapper;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public class TeleporterFrequency extends Frequency implements IColorableFrequency {

    private final Set<Coord4D> activeCoords = new ObjectOpenHashSet<>();
    private EnumColor color = EnumColor.PURPLE;

    /**
     * @param uuid Should only be null if we have incomplete data that we are loading
     */
    public TeleporterFrequency(String n, @Nullable UUID uuid) {
        super(FrequencyType.TELEPORTER, n, uuid);
    }

    public TeleporterFrequency() {
        super(FrequencyType.TELEPORTER);
    }

    public Set<Coord4D> getActiveCoords() {
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

    private Coord4D getCoord(BlockEntity tile) {
        if (tile instanceof ITileWrapper tileWrapper) {
            //Note: This should be the case the majority of the time, and allows us to use the cached coord4d object
            return tileWrapper.getTileCoord();
        }
        return new Coord4D(tile);
    }

    public Coord4D getClosestCoords(Coord4D coord) {
        Coord4D closest = null;
        for (Coord4D iterCoord : activeCoords) {
            if (iterCoord.equals(coord)) {
                continue;
            }
            if (closest == null) {
                closest = iterCoord;
                continue;
            }

            if (coord.dimension != closest.dimension && coord.dimension == iterCoord.dimension) {
                closest = iterCoord;
            } else if (coord.dimension != closest.dimension || coord.dimension == iterCoord.dimension) {
                if (coord.distanceTo(closest) > coord.distanceTo(iterCoord)) {
                    closest = iterCoord;
                }
            }
        }
        return closest;
    }

    @Override
    protected void read(CompoundTag nbtTags) {
        super.read(nbtTags);
        NBTUtils.setEnumIfPresent(nbtTags, NBTConstants.COLOR, EnumColor::byIndexStatic, color -> this.color = color);
    }

    @Override
    protected void read(FriendlyByteBuf dataStream) {
        super.read(dataStream);
        this.color = dataStream.readEnum(EnumColor.class);
    }

    @Override
    public void write(CompoundTag nbtTags) {
        super.write(nbtTags);
        NBTUtils.writeEnum(nbtTags, NBTConstants.COLOR, color);
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        super.write(buffer);
        buffer.writeEnum(color);
    }
}
