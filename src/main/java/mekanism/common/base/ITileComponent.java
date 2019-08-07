package mekanism.common.base;

import mekanism.api.TileNetworkList;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

public interface ITileComponent {

    void tick();

    void read(CompoundNBT nbtTags);

    void read(PacketBuffer dataStream);

    void write(CompoundNBT nbtTags);

    void write(TileNetworkList data);

    void invalidate();
}