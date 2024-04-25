package mekanism.common.registration.impl;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import mekanism.api.MekanismAPI;
import mekanism.api.gear.EnchantmentBasedModule;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.ModuleData;
import mekanism.api.gear.ModuleData.ModuleDataBuilder;
import mekanism.api.providers.IItemProvider;
import mekanism.common.registration.MekanismDeferredRegister;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.NotNull;

public class ModuleDeferredRegister extends MekanismDeferredRegister<ModuleData<?>> {

    public ModuleDeferredRegister(String modid) {
        super(MekanismAPI.MODULE_REGISTRY_NAME, modid, ModuleRegistryObject::new);
    }

    public ModuleRegistryObject<?> registerMarker(String name, IItemProvider itemProvider) {
        return registerMarker(name, itemProvider, UnaryOperator.identity());
    }

    public ModuleRegistryObject<?> registerMarker(String name, IItemProvider itemProvider, UnaryOperator<ModuleDataBuilder<?>> builderModifier) {
        return register(name, builderModifier.apply(ModuleDataBuilder.marker(itemProvider)));
    }

    public <MODULE extends ICustomModule<MODULE>> ModuleRegistryObject<MODULE> register(String name, Supplier<MODULE> supplier, IItemProvider itemProvider) {
        return register(name, supplier, itemProvider, UnaryOperator.identity());
    }

    public <MODULE extends ICustomModule<MODULE>> ModuleRegistryObject<MODULE> register(String name, Supplier<MODULE> supplier, IItemProvider itemProvider,
          UnaryOperator<ModuleDataBuilder<MODULE>> builderModifier) {
        return register(name, builderModifier.apply(ModuleDataBuilder.custom(supplier, itemProvider)));
    }

    public ModuleRegistryObject<?> registerEnchantBased(String name, Supplier<Enchantment> enchantment, IItemProvider itemProvider,
          UnaryOperator<ModuleDataBuilder<?>> builderModifier) {
        return register(name, builderModifier.apply(ModuleDataBuilder.custom(() -> new EnchantmentBasedModule() {
            @NotNull
            @Override
            public Enchantment getEnchantment() {
                return enchantment.get();
            }
        }, itemProvider)));
    }

    public <MODULE extends ICustomModule<MODULE>> ModuleRegistryObject<MODULE> register(String name, ModuleDataBuilder<MODULE> builder) {
        return (ModuleRegistryObject<MODULE>) register(name, () -> new ModuleData<>(builder));
    }
}