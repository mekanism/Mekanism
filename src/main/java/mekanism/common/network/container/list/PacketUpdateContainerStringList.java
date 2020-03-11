package mekanism.common.network.container.list;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.common.PacketHandler;
import net.minecraft.network.PacketBuffer;

public class PacketUpdateContainerStringList extends PacketUpdateContainerList<String> {

    public PacketUpdateContainerStringList(short windowId, short property, @Nonnull List<String> values) {
        super(windowId, property, values);
    }

    public static PacketUpdateContainerStringList decode(PacketBuffer buffer) {
        short windowId = buffer.readUnsignedByte();
        short property = buffer.readShort();
        int elements = buffer.readVarInt();
        List<String> values = new ArrayList<>(elements);
        for (int i = 0; i < elements; i++) {
            values.add(PacketHandler.readString(buffer));
        }
        return new PacketUpdateContainerStringList(windowId, property, values);
    }

    @Override
    protected void writeListElements(PacketBuffer buffer) {
        for (String value : values) {
            buffer.writeString(value);
        }
    }
}