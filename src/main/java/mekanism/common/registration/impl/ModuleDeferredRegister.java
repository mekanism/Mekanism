package mekanism.common.registration.impl;

import java.util.function.UnaryOperator;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.ModuleData;
import mekanism.api.gear.ModuleData.ModuleDataBuilder;
import mekanism.api.providers.IItemProvider;
import mekanism.common.registration.WrappedDeferredRegister;
import net.minecraftforge.common.util.NonNullSupplier;

public class ModuleDeferredRegister extends WrappedDeferredRegister<ModuleData<?>> {

    public ModuleDeferredRegister(String modid) {
        super(modid, ModuleData.getClassWithGeneric());
    }

    public ModuleRegistryObject<?> registerMarkerLegacy(String name, IItemProvider itemProvider, UnaryOperator<ModuleDataBuilder<?>> builderModifier) {
        return register(name, builderModifier.apply(ModuleDataBuilder.marker(itemProvider)).legacyName(name));
    }

    public <MODULE extends ICustomModule<MODULE>> ModuleRegistryObject<MODULE> registerLegacy(String name, NonNullSupplier<MODULE> supplier, IItemProvider itemProvider,
          UnaryOperator<ModuleDataBuilder<MODULE>> builderModifier) {
        return register(name, builderModifier.apply(ModuleDataBuilder.custom(supplier, itemProvider)).legacyName(name));
    }

    public ModuleRegistryObject<?> registerMarker(String name, IItemProvider itemProvider, UnaryOperator<ModuleDataBuilder<?>> builderModifier) {
        return register(name, builderModifier.apply(ModuleDataBuilder.marker(itemProvider)));
    }

    public <MODULE extends ICustomModule<MODULE>> ModuleRegistryObject<MODULE> register(String name, NonNullSupplier<MODULE> supplier, IItemProvider itemProvider,
          UnaryOperator<ModuleDataBuilder<MODULE>> builderModifier) {
        return register(name, builderModifier.apply(ModuleDataBuilder.custom(supplier, itemProvider)));
    }

    public <MODULE extends ICustomModule<MODULE>> ModuleRegistryObject<MODULE> register(String name, ModuleDataBuilder<MODULE> builder) {
        return register(name, () -> new ModuleData<>(builder), ModuleRegistryObject::new);
    }
}