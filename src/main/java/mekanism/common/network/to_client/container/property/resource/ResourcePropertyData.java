package mekanism.common.network.to_client.container.property.resource;

import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.network.to_client.container.property.PropertyData;
import mekanism.common.network.to_client.container.property.PropertyType;
import net.neoforged.neoforge.transfer.resource.Resource;
import org.jetbrains.annotations.NotNull;

public class ResourcePropertyData<RESOURCE extends Resource> extends PropertyData {

    @NotNull
    private final RESOURCE value;

    protected ResourcePropertyData(PropertyType propertyType, short property, @NotNull RESOURCE value) {
        super(propertyType, property);
        this.value = value;
    }

    @Override
    public void handleWindowProperty(MekanismContainer container) {
        container.handleWindowProperty(getProperty(), value);
    }

    @NotNull
    public RESOURCE getValue() {
        return value;
    }
}