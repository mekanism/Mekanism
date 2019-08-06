package mekanism.common.base;

import io.netty.buffer.ByteBuf;
import mekanism.api.TileNetworkList;
import net.minecraft.nbt.CompoundNBT;

public interface ITileComponent {

    void tick();

    void read(CompoundNBT nbtTags);

    void read(ByteBuf dataStream);

    void write(CompoundNBT nbtTags);

    void write(TileNetworkList data);

    void invalidate();
}