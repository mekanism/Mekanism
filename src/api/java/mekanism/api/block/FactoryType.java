package mekanism.api.block;

public enum FactoryType {
    SMELTING("smelting"),
    ENRICHING("enriching"),
    CRUSHING("crushing"),
    COMPRESSING("compressing"),
    COMBINING("combining"),
    PURIFYING("purifying"),
    INJECTING("injecting"),
    INFUSING("infusing"),
    SAWING("sawing");

    private final String registryNameComponent;

    FactoryType(String registryNameComponent) {
        this.registryNameComponent = registryNameComponent;
    }

    public String getRegistryNameComponent() {
        return registryNameComponent;
    }
}