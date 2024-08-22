package mekanism.common.content.blocktype;

import java.util.Locale;
import java.util.function.Supplier;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.text.IHasTranslationKey.IHasEnumNameTranslationKey;
import mekanism.api.text.ILangEntry;
import mekanism.common.MekanismLang;
import mekanism.common.content.blocktype.Machine.FactoryMachine;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.registries.MekanismBlockTypes;
import mekanism.common.registries.MekanismBlocks;

@NothingNullByDefault
public enum FactoryType implements IHasEnumNameTranslationKey {
    SMELTING("smelting", MekanismLang.SMELTING, () -> MekanismBlockTypes.ENERGIZED_SMELTER, () -> MekanismBlocks.ENERGIZED_SMELTER),
    ENRICHING("enriching", MekanismLang.ENRICHING, () -> MekanismBlockTypes.ENRICHMENT_CHAMBER, () -> MekanismBlocks.ENRICHMENT_CHAMBER),
    CRUSHING("crushing", MekanismLang.CRUSHING, () -> MekanismBlockTypes.CRUSHER, () -> MekanismBlocks.CRUSHER),
    COMPRESSING("compressing", MekanismLang.COMPRESSING, () -> MekanismBlockTypes.OSMIUM_COMPRESSOR, () -> MekanismBlocks.OSMIUM_COMPRESSOR),
    COMBINING("combining", MekanismLang.COMBINING, () -> MekanismBlockTypes.COMBINER, () -> MekanismBlocks.COMBINER),
    PURIFYING("purifying", MekanismLang.PURIFYING, () -> MekanismBlockTypes.PURIFICATION_CHAMBER, () -> MekanismBlocks.PURIFICATION_CHAMBER),
    INJECTING("injecting", MekanismLang.INJECTING, () -> MekanismBlockTypes.CHEMICAL_INJECTION_CHAMBER, () -> MekanismBlocks.CHEMICAL_INJECTION_CHAMBER),
    INFUSING("infusing", MekanismLang.INFUSING, () -> MekanismBlockTypes.METALLURGIC_INFUSER, () -> MekanismBlocks.METALLURGIC_INFUSER),
    SAWING("sawing", MekanismLang.SAWING, () -> MekanismBlockTypes.PRECISION_SAWMILL, () -> MekanismBlocks.PRECISION_SAWMILL);

    private final String registryNameComponent;
    private final ILangEntry langEntry;
    private final Supplier<FactoryMachine<?>> baseMachine;
    private final Supplier<BlockRegistryObject<?, ?>> baseBlock;

    FactoryType(String registryNameComponent, ILangEntry langEntry, Supplier<FactoryMachine<?>> baseMachine, Supplier<BlockRegistryObject<?, ?>> baseBlock) {
        this.registryNameComponent = registryNameComponent;
        this.langEntry = langEntry;
        this.baseMachine = baseMachine;
        this.baseBlock = baseBlock;
    }

    public String getRegistryNameComponent() {
        return registryNameComponent;
    }

    public String getRegistryNameComponentCapitalized() {
        String name = getRegistryNameComponent();
        return name.substring(0, 1).toUpperCase(Locale.ROOT) + name.substring(1);
    }

    public FactoryMachine<?> getBaseMachine() {
        return baseMachine.get();
    }

    public BlockRegistryObject<?, ?> getBaseBlock() {
        return baseBlock.get();
    }

    @Override
    public String getTranslationKey() {
        return langEntry.getTranslationKey();
    }
}