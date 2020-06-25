package mekanism.common.network.container.property.list;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.common.lib.frequency.Frequency;
import net.minecraft.network.PacketBuffer;

public class FrequencyListPropertyData<FREQUENCY extends Frequency> extends ListPropertyData<FREQUENCY> {

    public FrequencyListPropertyData(short property, @Nonnull List<FREQUENCY> values) {
        super(property, ListType.FREQUENCY, values);
    }

    public static <FREQUENCY extends Frequency> FrequencyListPropertyData<FREQUENCY> read(short property, int elements, PacketBuffer buffer) {
        List<FREQUENCY> values = new ArrayList<>(elements);
        for (int i = 0; i < elements; i++) {
            values.add(Frequency.readFromPacket(buffer));
        }
        return new FrequencyListPropertyData<>(property, values);
    }


    @Override
    protected void writeListElements(PacketBuffer buffer) {
        for (FREQUENCY value : values) {
            value.write(buffer);
        }
    }
}