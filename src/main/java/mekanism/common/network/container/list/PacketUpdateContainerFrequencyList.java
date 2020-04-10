package mekanism.common.network.container.list;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.common.frequency.Frequency;
import net.minecraft.network.PacketBuffer;

public class PacketUpdateContainerFrequencyList<FREQUENCY extends Frequency> extends PacketUpdateContainerList<FREQUENCY> {

    public PacketUpdateContainerFrequencyList(short windowId, short property, @Nonnull List<FREQUENCY> values) {
        super(windowId, property, values);
    }

    public static <FREQUENCY extends Frequency> PacketUpdateContainerFrequencyList<FREQUENCY> decode(PacketBuffer buffer) {
        short windowId = buffer.readUnsignedByte();
        short property = buffer.readShort();
        int elements = buffer.readVarInt();
        List<FREQUENCY> values = new ArrayList<>(elements);
        for (int i = 0; i < elements; i++) {
            values.add((FREQUENCY) Frequency.readFromPacket(buffer));
        }
        return new PacketUpdateContainerFrequencyList<>(windowId, property, values);
    }

    @Override
    protected void writeListElements(PacketBuffer buffer) {
        for (FREQUENCY value : values) {
            value.write(buffer);
        }
    }
}