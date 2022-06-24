package mekanism.common.network.to_client.container.property;

import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.network.BasePacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.Nullable;

public class FrequencyPropertyData<FREQUENCY extends Frequency> extends PropertyData {

    @Nullable
    private final FREQUENCY value;

    public FrequencyPropertyData(short property, @Nullable FREQUENCY value) {
        super(PropertyType.FREQUENCY, property);
        this.value = value;
    }

    public static <FREQUENCY extends Frequency> FrequencyPropertyData<FREQUENCY> readFrequency(short property, FriendlyByteBuf buffer) {
        return new FrequencyPropertyData<>(property, BasePacketHandler.readOptional(buffer, Frequency::readFromPacket));
    }

    @Override
    public void handleWindowProperty(MekanismContainer container) {
        container.handleWindowProperty(getProperty(), value);
    }

    @Override
    public void writeToPacket(FriendlyByteBuf buffer) {
        super.writeToPacket(buffer);
        BasePacketHandler.writeOptional(buffer, value, (buf, val) -> val.write(buf));
    }
}