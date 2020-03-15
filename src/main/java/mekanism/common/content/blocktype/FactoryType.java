package mekanism.common.content.blocktype;

import java.util.function.Supplier;
import mekanism.api.text.IHasTranslationKey;
import mekanism.common.MekanismLang;
import mekanism.common.content.blocktype.Machine.FactoryMachine;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismMachineTypes;

public enum FactoryType implements IHasTranslationKey {
    SMELTING("smelting", MekanismLang.SMELTING, () -> MekanismMachineTypes.ENERGIZED_SMELTER, () -> MekanismBlocks.ENERGIZED_SMELTER),
    ENRICHING("enriching", MekanismLang.ENRICHING, () -> MekanismMachineTypes.ENRICHMENT_CHAMBER, () -> MekanismBlocks.ENRICHMENT_CHAMBER),
    CRUSHING("crushing", MekanismLang.CRUSHING, () -> MekanismMachineTypes.CRUSHER, () -> MekanismBlocks.CRUSHER),
    COMPRESSING("compressing", MekanismLang.COMPRESSING, () -> MekanismMachineTypes.OSMIUM_COMPRESSOR, () -> MekanismBlocks.OSMIUM_COMPRESSOR),
    COMBINING("combining", MekanismLang.COMBINING, () -> MekanismMachineTypes.COMBINER, () -> MekanismBlocks.COMBINER),
    PURIFYING("purifying", MekanismLang.PURIFYING, () -> MekanismMachineTypes.PURIFICATION_CHAMBER, () -> MekanismBlocks.PURIFICATION_CHAMBER),
    INJECTING("injecting", MekanismLang.INJECTING, () -> MekanismMachineTypes.CHEMICAL_INJECTION_CHAMBER, () -> MekanismBlocks.CHEMICAL_INJECTION_CHAMBER),
    INFUSING("infusing", MekanismLang.INFUSING, () -> MekanismMachineTypes.METALLURGIC_INFUSER, () -> MekanismBlocks.METALLURGIC_INFUSER),
    SAWING("sawing", MekanismLang.SAWING, () -> MekanismMachineTypes.PRECISION_SAWMILL, () -> MekanismBlocks.PRECISION_SAWMILL);

    private final String registryNameComponent;
    private final MekanismLang langEntry;
    private final Supplier<FactoryMachine<?>> baseMachine;
    private final Supplier<BlockRegistryObject<?, ?>> baseBlock;

    FactoryType(String registryNameComponent, MekanismLang langEntry, Supplier<FactoryMachine<?>> baseMachine, Supplier<BlockRegistryObject<?, ?>> baseBlock) {
        this.registryNameComponent = registryNameComponent;
        this.langEntry = langEntry;
        this.baseMachine = baseMachine;
        this.baseBlock = baseBlock;
    }

    public String getRegistryNameComponent() {
        return registryNameComponent;
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