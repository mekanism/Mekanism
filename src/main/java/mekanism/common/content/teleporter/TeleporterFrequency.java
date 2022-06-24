package mekanism.common.content.teleporter;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.api.NBTConstants;
import mekanism.api.text.EnumColor;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.frequency.IColorableFrequency;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;

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
        this.color = color;
    }

    @Override
    public void update(BlockEntity tile) {
        super.update(tile);
        activeCoords.add(new Coord4D(tile));
    }

    @Override
    public void onDeactivate(BlockEntity tile) {
        super.onDeactivate(tile);
        activeCoords.remove(new Coord4D(tile));
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
        NBTUtils.setEnumIfPresent(nbtTags, NBTConstants.COLOR, EnumColor::byIndexStatic, this::setColor);
    }

    @Override
    protected void read(FriendlyByteBuf dataStream) {
        super.read(dataStream);
        setColor(dataStream.readEnum(EnumColor.class));
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
