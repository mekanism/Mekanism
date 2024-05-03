package mekanism.common.registration.impl;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import mekanism.api.MekanismAPI;
import mekanism.api.gear.EnchantmentAwareModule;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.ModuleData;
import mekanism.api.gear.ModuleData.ModuleDataBuilder;
import mekanism.api.providers.IItemProvider;
import mekanism.common.registration.MekanismDeferredRegister;
import net.minecraft.world.item.enchantment.Enchantment;

public class ModuleDeferredRegister extends MekanismDeferredRegister<ModuleData<?>> {

    public ModuleDeferredRegister(String modid) {
        super(MekanismAPI.MODULE_REGISTRY_NAME, modid, ModuleRegistryObject::new);
    }

    public ModuleRegistryObject<?> registerMarker(String name, IItemProvider itemProvider) {
        return registerMarker(name, itemProvider, UnaryOperator.identity());
    }

    public ModuleRegistryObject<?> registerMarker(String name, IItemProvider itemProvider, UnaryOperator<ModuleDataBuilder<?>> builderModifier) {
        return registerBuilder(name, () -> builderModifier.apply(ModuleDataBuilder.marker(itemProvider)));
    }

    public <MODULE extends ICustomModule<MODULE>> ModuleRegistryObject<MODULE> register(String name, Function<IModule<MODULE>, MODULE> constructor,
          IItemProvider itemProvider, UnaryOperator<ModuleDataBuilder<MODULE>> builderModifier) {
        return registerBuilder(name, () -> builderModifier.apply(ModuleDataBuilder.custom(constructor, itemProvider)));
    }

    public ModuleRegistryObject<?> registerEnchantBased(String name, Supplier<Enchantment> enchantment, IItemProvider itemProvider,
          UnaryOperator<ModuleDataBuilder<?>> builderModifier) {
        return registerBuilder(name, () -> {
            SimpleEnchantmentAwareModule customModule = new SimpleEnchantmentAwareModule(enchantment.get());
            Function<IModule<SimpleEnchantmentAwareModule>, SimpleEnchantmentAwareModule> function = module -> customModule;
            return builderModifier.apply(ModuleDataBuilder.custom(function, itemProvider));
        });
    }

    public <MODULE extends ICustomModule<MODULE>> ModuleRegistryObject<MODULE> registerBuilder(String name, Supplier<ModuleDataBuilder<MODULE>> builder) {
        return (ModuleRegistryObject<MODULE>) register(name, () -> new ModuleData<>(builder.get()));
    }

    private record SimpleEnchantmentAwareModule(Enchantment enchantment) implements EnchantmentAwareModule<SimpleEnchantmentAwareModule> {
    }
}