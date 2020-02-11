package mekanism.api.block;

import mekanism.api.text.APILang;
import mekanism.api.text.IHasTranslationKey;

public enum FactoryType implements IHasTranslationKey {
    SMELTING("smelting", APILang.SMELTING),
    ENRICHING("enriching", APILang.ENRICHING),
    CRUSHING("crushing", APILang.CRUSHING),
    COMPRESSING("compressing", APILang.COMPRESSING),
    COMBINING("combining", APILang.COMBINING),
    PURIFYING("purifying", APILang.PURIFYING),
    INJECTING("injecting", APILang.INJECTING),
    INFUSING("infusing", APILang.INFUSING),
    SAWING("sawing", APILang.SAWING);

    private final String registryNameComponent;
    private final APILang langEntry;

    FactoryType(String registryNameComponent, APILang langEntry) {
        this.registryNameComponent = registryNameComponent;
        this.langEntry = langEntry;
    }

    public String getRegistryNameComponent() {
        return registryNameComponent;
    }

    @Override
    public String getTranslationKey() {
        return langEntry.getTranslationKey();
    }
}