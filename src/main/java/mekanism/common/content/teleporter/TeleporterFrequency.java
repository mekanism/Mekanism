package mekanism.common.content.teleporter;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Set;
import java.util.UUID;
import mekanism.api.NBTConstants;
import mekanism.api.text.EnumColor;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.frequency.IColorableFrequency;
import mekanism.common.tile.interfaces.ITileWrapper;
import mekanism.common.util.NBTUtils;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public class TeleporterFrequency extends Frequency implements IColorableFrequency {

    private final Set<GlobalPos> activeCoords = new ObjectOpenHashSet<>();
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
        GlobalPos closest = null;
        for (GlobalPos iterCoord : activeCoords) {
            if (iterCoord.equals(pos)) {
                continue;
            }
            if (closest == null) {
                closest = iterCoord;
                continue;
            }

            if (pos.dimension() != closest.dimension() && pos.dimension() == iterCoord.dimension()) {
                closest = iterCoord;
            } else if (pos.dimension() != closest.dimension() || pos.dimension() == iterCoord.dimension()) {
                if (pos.pos().distSqr(closest.pos()) > pos.pos().distSqr(iterCoord.pos())) {
                    closest = iterCoord;
                }
            }
        }
        return closest;
    }

    @Override
    protected void read(HolderLookup.Provider provider, CompoundTag nbtTags) {
        super.read(provider, nbtTags);
        NBTUtils.setEnumIfPresent(nbtTags, NBTConstants.COLOR, EnumColor.BY_ID, color -> this.color = color);
    }

    @Override
    protected void read(RegistryFriendlyByteBuf dataStream) {
        super.read(dataStream);
        this.color = dataStream.readEnum(EnumColor.class);
    }

    @Override
    public void write(HolderLookup.Provider provider, CompoundTag nbtTags) {
        super.write(provider, nbtTags);
        NBTUtils.writeEnum(nbtTags, NBTConstants.COLOR, color);
    }

    @Override
    public void write(RegistryFriendlyByteBuf buffer) {
        super.write(buffer);
        buffer.writeEnum(color);
    }
}
