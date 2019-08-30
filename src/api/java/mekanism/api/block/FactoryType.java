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
    /*
        COMBINING("Combining", MekanismBlock.COMBINER, MachineFuelType.DOUBLE, false, Recipe.COMBINER),
        SAWING("Sawing", MekanismBlock.PRECISION_SAWMILL, MachineFuelType.CHANCE, false, Recipe.PRECISION_SAWMILL);
     */

    private final String registryNameComponent;

    FactoryType(String registryNameComponent) {
        this.registryNameComponent = registryNameComponent;
    }

    public String getRegistryNameComponent() {
        return registryNameComponent;
    }

    public boolean isBasicMachine() {
        return this == SMELTING || this == ENRICHING || this == CRUSHING || this == INFUSING;
    }

    public boolean isAdvancedMachine() {
        return this == COMPRESSING || this == PURIFYING || this == INJECTING;
    }

    public boolean isDoubleMachine() {
        return this == COMBINING;
    }

    public boolean isChanceMachine() {
        return this == SAWING;
    }
}