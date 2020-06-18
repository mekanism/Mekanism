package mekanism.common.content.filter;

import javax.annotation.Nullable;
import mekanism.api.NBTConstants;
import mekanism.common.content.miner.MinerItemStackFilter;
import mekanism.common.content.miner.MinerMaterialFilter;
import mekanism.common.content.miner.MinerModIDFilter;
import mekanism.common.content.miner.MinerTagFilter;
import mekanism.common.content.qio.filter.QIOItemStackFilter;
import mekanism.common.content.qio.filter.QIOTagFilter;
import mekanism.common.content.transporter.SorterItemStackFilter;
import mekanism.common.content.transporter.SorterMaterialFilter;
import mekanism.common.content.transporter.SorterModIDFilter;
import mekanism.common.content.transporter.SorterTagFilter;
import mekanism.common.tile.machine.TileEntityOredictionificator.OredictionificatorFilter;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.util.Constants.NBT;

public abstract class BaseFilter<FILTER extends BaseFilter<FILTER>> implements IFilter<FILTER> {

    //Mark it as abstract so it does not think clone is being implemented by Object
    @Override
    public abstract FILTER clone();

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object o);

    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        nbtTags.putInt(NBTConstants.TYPE, getFilterType().ordinal());
        return nbtTags;
    }

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeEnumValue(getFilterType());
    }

    @Nullable
    public static IFilter<?> readFromNBT(CompoundNBT nbt) {
        if (nbt.contains(NBTConstants.TYPE, NBT.TAG_INT)) {
            IFilter<?> filter = fromType(FilterType.byIndexStatic(nbt.getInt(NBTConstants.TYPE)));
            if (filter != null) {
                filter.read(nbt);
            }
            return filter;
        }
        return null;
    }

    @Nullable
    public static IFilter<?> readFromPacket(PacketBuffer dataStream) {
        IFilter<?> filter = fromType(dataStream.readEnumValue(FilterType.class));
        if (filter != null) {
            filter.read(dataStream);
        }
        return filter;
    }

    private static IFilter<?> fromType(FilterType filterType) {
        switch (filterType) {
            case MINER_ITEMSTACK_FILTER:
                return new MinerItemStackFilter();
            case MINER_MATERIAL_FILTER:
                return new MinerMaterialFilter();
            case MINER_MODID_FILTER:
                return new MinerModIDFilter();
            case MINER_TAG_FILTER:
                return new MinerTagFilter();
            case SORTER_ITEMSTACK_FILTER:
                return new SorterItemStackFilter();
            case SORTER_MATERIAL_FILTER:
                return new SorterMaterialFilter();
            case SORTER_MODID_FILTER:
                return new SorterModIDFilter();
            case SORTER_TAG_FILTER:
                return new SorterTagFilter();
            case OREDICTIONIFICATOR:
                return new OredictionificatorFilter();
            case QIO_ITEMSTACK_FILTER:
                return new QIOItemStackFilter();
            case QIO_TAG_FILTER:
                return new QIOTagFilter();
            default:
                return null;
        }
    }
}