package mekanism.common.network.container.property;

import javax.annotation.Nonnull;
import mekanism.api.math.FloatingLong;
import mekanism.common.inventory.container.MekanismContainer;
import net.minecraft.network.PacketBuffer;

public class FloatingLongPropertyData extends PropertyData {

    @Nonnull
    private final FloatingLong value;

    public FloatingLongPropertyData(short property, @Nonnull FloatingLong value) {
        super(PropertyType.FLOATING_LONG, property);
        this.value = value;
    }

    @Override
    public void handleWindowProperty(MekanismContainer container) {
        container.handleWindowProperty(getProperty(), value);
    }

    @Override
    public void writeToPacket(PacketBuffer buffer) {
        super.writeToPacket(buffer);
        value.writeToBuffer(buffer);
    }
}