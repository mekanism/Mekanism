package mekanism.common.network.to_client.container.property.list;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.common.network.BasePacketHandler;
import net.minecraft.network.FriendlyByteBuf;

public class StringListPropertyData extends ListPropertyData<String> {

    public StringListPropertyData(short property, @Nonnull List<String> values) {
        super(property, ListType.STRING, values);
    }

    static StringListPropertyData read(short property, ListPropertyReader<String> reader) {
        return new StringListPropertyData(property, reader.apply(BasePacketHandler::readString));
    }

    @Override
    protected void writeListElement(FriendlyByteBuf buffer, String value) {
        buffer.writeUtf(value);
    }
}