package mekanism.common.network.container.property.list;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.common.PacketHandler;
import mekanism.common.network.container.list.ListType;
import mekanism.common.network.container.list.PacketUpdateContainerStringList;
import net.minecraft.network.PacketBuffer;

public class StringListPropertyData extends ListPropertyData<String> {

    public StringListPropertyData(short property, @Nonnull List<String> values) {
        super(property, ListType.STRING, values);
    }

    public static StringListPropertyData read(short property, int elements, PacketBuffer buffer) {
        List<String> values = new ArrayList<>(elements);
        for (int i = 0; i < elements; i++) {
            values.add(PacketHandler.readString(buffer));
        }
        return new StringListPropertyData(property, values);
    }


    @Override
    protected void writeListElements(PacketBuffer buffer) {
        for (String value : values) {
            buffer.writeString(value);
        }
    }

    @Override
    public PacketUpdateContainerStringList getSinglePacket(short windowId) {
        return new PacketUpdateContainerStringList(windowId, getProperty(), values);
    }
}