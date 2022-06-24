package mekanism.common.network.to_client.container.property.list;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.common.lib.frequency.Frequency;
import net.minecraft.network.FriendlyByteBuf;

public class FrequencyListPropertyData<FREQUENCY extends Frequency> extends ListPropertyData<FREQUENCY> {

    public FrequencyListPropertyData(short property, @Nonnull List<FREQUENCY> values) {
        super(property, ListType.FREQUENCY, values);
    }

    static <FREQUENCY extends Frequency> FrequencyListPropertyData<FREQUENCY> read(short property, ListPropertyReader<FREQUENCY> reader) {
        return new FrequencyListPropertyData<>(property, reader.apply(Frequency::readFromPacket));
    }

    @Override
    protected void writeListElement(FriendlyByteBuf buffer, FREQUENCY value) {
        value.write(buffer);
    }
}