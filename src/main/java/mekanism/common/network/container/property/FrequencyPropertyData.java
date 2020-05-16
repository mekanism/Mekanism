package mekanism.common.network.container.property;

import javax.annotation.Nullable;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.network.container.PacketUpdateContainerFrequency;
import net.minecraft.network.PacketBuffer;

public class FrequencyPropertyData<FREQUENCY extends Frequency> extends PropertyData {

    @Nullable
    private final FREQUENCY value;

    public FrequencyPropertyData(short property, @Nullable FREQUENCY value) {
        super(PropertyType.FREQUENCY, property);
        this.value = value;
    }

    public static <FREQUENCY extends Frequency> FrequencyPropertyData<FREQUENCY> readFrequency(short property, PacketBuffer buffer) {
        return new FrequencyPropertyData<>(property, buffer.readBoolean() ? (FREQUENCY) Frequency.readFromPacket(buffer) : null);
    }

    @Override
    public PacketUpdateContainerFrequency<FREQUENCY> getSinglePacket(short windowId) {
        return new PacketUpdateContainerFrequency<>(windowId, getProperty(), value);
    }

    @Override
    public void handleWindowProperty(MekanismContainer container) {
        container.handleWindowProperty(getProperty(), value);
    }

    @Override
    public void writeToPacket(PacketBuffer buffer) {
        super.writeToPacket(buffer);
        if (value == null) {
            buffer.writeBoolean(false);
        } else {
            buffer.writeBoolean(true);
            value.write(buffer);
        }
    }
}