package mekanism.common.content.filter;

import java.util.Objects;
import mekanism.api.NBTConstants;
import mekanism.common.content.miner.MinerItemStackFilter;
import mekanism.common.content.miner.MinerMaterialFilter;
import mekanism.common.content.miner.MinerModIDFilter;
import mekanism.common.content.miner.MinerTagFilter;
import mekanism.common.content.oredictionificator.OredictionificatorItemFilter;
import mekanism.common.content.qio.filter.QIOItemStackFilter;
import mekanism.common.content.qio.filter.QIOModIDFilter;
import mekanism.common.content.qio.filter.QIOTagFilter;
import mekanism.common.content.transporter.SorterItemStackFilter;
import mekanism.common.content.transporter.SorterMaterialFilter;
import mekanism.common.content.transporter.SorterModIDFilter;
import mekanism.common.content.transporter.SorterTagFilter;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.Nullable;

public abstract class BaseFilter<FILTER extends BaseFilter<FILTER>> implements IFilter<FILTER> {

    //Enabled by default
    private boolean enabled = true;

    //Mark it as abstract, so it does not think clone is being implemented by Object
    @Override
    public abstract FILTER clone();

    @Override
    public int hashCode() {
        //Hash the filter type to ensure things like material and item filters don't collide on their hash if everything
        // else except their type is equal
        return Objects.hash(getFilterType(), enabled);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        //TODO: Eventually it might be nice to go back to having some way to not allow duplicate filters that are duplicates except for a few states
        // for example different enabled state or different allow default state for sorter filters
        BaseFilter<?> other = (BaseFilter<?>) o;
        return enabled == other.enabled;
    }

    @Override
    public final boolean isEnabled() {
        return enabled;
    }

    @Override
    public final void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public CompoundTag write(CompoundTag nbtTags) {
        NBTUtils.writeEnum(nbtTags, NBTConstants.TYPE, getFilterType());
        nbtTags.putBoolean(NBTConstants.ENABLED, isEnabled());
        return nbtTags;
    }

    @Override
    public void read(CompoundTag nbtTags) {
        NBTUtils.setBooleanIfPresentElse(nbtTags, NBTConstants.ENABLED, true, this::setEnabled);
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeEnum(getFilterType());
        buffer.writeBoolean(isEnabled());
    }

    @Override
    public void read(FriendlyByteBuf buffer) {
        setEnabled(buffer.readBoolean());
    }

    @Nullable
    public static IFilter<?> readFromNBT(CompoundTag nbt) {
        if (nbt.contains(NBTConstants.TYPE, Tag.TAG_INT)) {
            IFilter<?> filter = fromType(FilterType.byIndexStatic(nbt.getInt(NBTConstants.TYPE)));
            filter.read(nbt);
            return filter;
        }
        return null;
    }

    public static IFilter<?> readFromPacket(FriendlyByteBuf dataStream) {
        IFilter<?> filter = fromType(dataStream.readEnum(FilterType.class));
        filter.read(dataStream);
        return filter;
    }

    public static IFilter<?> fromType(FilterType filterType) {
        return switch (filterType) {
            case MINER_ITEMSTACK_FILTER -> new MinerItemStackFilter();
            case MINER_MATERIAL_FILTER -> new MinerMaterialFilter();
            case MINER_MODID_FILTER -> new MinerModIDFilter();
            case MINER_TAG_FILTER -> new MinerTagFilter();
            case SORTER_ITEMSTACK_FILTER -> new SorterItemStackFilter();
            case SORTER_MATERIAL_FILTER -> new SorterMaterialFilter();
            case SORTER_MODID_FILTER -> new SorterModIDFilter();
            case SORTER_TAG_FILTER -> new SorterTagFilter();
            case OREDICTIONIFICATOR_ITEM_FILTER -> new OredictionificatorItemFilter();
            case QIO_ITEMSTACK_FILTER -> new QIOItemStackFilter();
            case QIO_MODID_FILTER -> new QIOModIDFilter();
            case QIO_TAG_FILTER -> new QIOTagFilter();
        };
    }
}