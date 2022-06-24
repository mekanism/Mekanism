package mekanism.common.network.to_client.container.property;

import mekanism.api.math.FloatingLong;
import mekanism.common.inventory.container.MekanismContainer;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

public class FloatingLongPropertyData extends PropertyData {

    @NotNull
    private final FloatingLong value;

    public FloatingLongPropertyData(short property, @NotNull FloatingLong value) {
        super(PropertyType.FLOATING_LONG, property);
        this.value = value;
    }

    @Override
    public void handleWindowProperty(MekanismContainer container) {
        container.handleWindowProperty(getProperty(), value);
    }

    @Override
    public void writeToPacket(FriendlyByteBuf buffer) {
        super.writeToPacket(buffer);
        value.writeToBuffer(buffer);
    }
}