package mekanism.common.content.filter;

import mekanism.common.integration.computer.annotation.ComputerMethod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public interface IFilter<FILTER extends IFilter<FILTER>> {

    FILTER clone();

    @ComputerMethod(threadSafe = true)
    FilterType getFilterType();

    CompoundTag write(CompoundTag nbtTags);

    void read(CompoundTag nbtTags);

    void write(FriendlyByteBuf buffer);

    void read(FriendlyByteBuf dataStream);

    boolean hasFilter();

    @ComputerMethod(threadSafe = true)
    boolean isEnabled();

    @ComputerMethod(threadSafe = true)
    void setEnabled(boolean enabled);
}