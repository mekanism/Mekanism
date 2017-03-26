package buildcraft.api.core;

import io.netty.buffer.ByteBuf;

@Deprecated
public interface INetworkLoadable_BC8<T> {
    T readFromByteBuf(ByteBuf buf);

    void writeToByteBuf(ByteBuf buf);
}
