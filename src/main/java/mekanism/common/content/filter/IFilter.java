package mekanism.common.content.filter;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

public interface IFilter<FILTER extends IFilter<FILTER>> {

    FILTER clone();

    FilterType getFilterType();

    CompoundNBT write(CompoundNBT nbtTags);

    void read(CompoundNBT nbtTags);

    void write(PacketBuffer buffer);

    void read(PacketBuffer dataStream);

    boolean hasFilter();
}