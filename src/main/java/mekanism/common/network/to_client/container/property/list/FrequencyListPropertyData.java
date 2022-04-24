package mekanism.common.network.to_client.container.property.list;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.common.lib.frequency.Frequency;
import net.minecraft.network.FriendlyByteBuf;

public class FrequencyListPropertyData<FREQUENCY extends Frequency> extends ListPropertyData<FREQUENCY> {

    public FrequencyListPropertyData(short property, @Nonnull List<FREQUENCY> values) {
        super(property, ListType.FREQUENCY, values);
    }

    public static <FREQUENCY extends Frequency> FrequencyListPropertyData<FREQUENCY> read(short property, int elements, FriendlyByteBuf buffer) {
        List<FREQUENCY> values = new ArrayList<>(elements);
        for (int i = 0; i < elements; i++) {
            values.add(Frequency.readFromPacket(buffer));
        }
        return new FrequencyListPropertyData<>(property, values);
    }

    @Override
    protected void writeListElement(FriendlyByteBuf buffer, FREQUENCY value) {
        value.write(buffer);
    }
}