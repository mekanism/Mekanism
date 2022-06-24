package mekanism.common.content.filter;

import javax.annotation.Nullable;
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

public abstract class BaseFilter<FILTER extends BaseFilter<FILTER>> implements IFilter<FILTER> {

    //Mark it as abstract, so it does not think clone is being implemented by Object
    @Override
    public abstract FILTER clone();

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object o);

    @Override
    public CompoundTag write(CompoundTag nbtTags) {
        NBTUtils.writeEnum(nbtTags, NBTConstants.TYPE, getFilterType());
        return nbtTags;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeEnum(getFilterType());
    }

    @Nullable
    public static IFilter<?> readFromNBT(CompoundTag nbt) {
        if (nbt.contains(NBTConstants.TYPE, Tag.TAG_INT)) {
            IFilter<?> filter = fromType(FilterType.byIndexStatic(nbt.getInt(NBTConstants.TYPE)));
            if (filter != null) {
                filter.read(nbt);
            }
            return filter;
        }
        return null;
    }

    @Nullable
    public static IFilter<?> readFromPacket(FriendlyByteBuf dataStream) {
        IFilter<?> filter = fromType(dataStream.readEnum(FilterType.class));
        if (filter != null) {
            filter.read(dataStream);
        }
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