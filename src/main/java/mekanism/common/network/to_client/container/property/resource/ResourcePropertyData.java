package mekanism.common.network.to_client.container.property.resource;

import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.network.to_client.container.property.PropertyData;
import mekanism.common.network.to_client.container.property.PropertyType;
import net.neoforged.neoforge.transfer.resource.Resource;

public class ResourcePropertyData<RESOURCE extends Resource> extends PropertyData {

    private final RESOURCE value;

    protected ResourcePropertyData(PropertyType propertyType, short property, RESOURCE value) {
        super(propertyType, property);
        this.value = value;
    }

    @Override
    public void handleWindowProperty(MekanismContainer container) {
        container.handleWindowProperty(getProperty(), value);
    }

    public RESOURCE getValue() {
        return value;
    }
}