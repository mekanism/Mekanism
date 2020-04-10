package mekanism.common.network.container.list;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.common.content.filter.BaseFilter;
import mekanism.common.content.filter.IFilter;
import net.minecraft.network.PacketBuffer;

public class PacketUpdateContainerFilterList<FILTER extends IFilter<?>> extends PacketUpdateContainerList<FILTER> {

    public PacketUpdateContainerFilterList(short windowId, short property, @Nonnull List<FILTER> values) {
        super(windowId, property, values);
    }

    public static <FILTER extends IFilter<?>> PacketUpdateContainerFilterList<FILTER> decode(PacketBuffer buffer) {
        short windowId = buffer.readUnsignedByte();
        short property = buffer.readShort();
        int elements = buffer.readVarInt();
        List<FILTER> values = new ArrayList<>(elements);
        for (int i = 0; i < elements; i++) {
            values.add((FILTER) BaseFilter.readFromPacket(buffer));
        }
        return new PacketUpdateContainerFilterList<>(windowId, property, values);
    }

    @Override
    protected void writeListElements(PacketBuffer buffer) {
        for (FILTER value : values) {
            value.write(buffer);
        }
    }
}