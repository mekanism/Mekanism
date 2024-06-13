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
import net.minecraft.resources.ResourceKey;
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

    //Do not use this method if you have any config options
    public ModuleRegistryObject<SimpleEnchantmentAwareModule> registerEnchantBased(String name, ResourceKey<Enchantment> enchantment, IItemProvider itemProvider,
          UnaryOperator<ModuleDataBuilder<SimpleEnchantmentAwareModule>> builderModifier) {
        return registerInstanced(name, () -> new SimpleEnchantmentAwareModule(enchantment), itemProvider, builderModifier);
    }

    //Do not use this method if you have any config options
    public <MODULE extends ICustomModule<MODULE>> ModuleRegistryObject<MODULE> registerInstanced(String name, Supplier<MODULE> constructor,
          IItemProvider itemProvider, UnaryOperator<ModuleDataBuilder<MODULE>> builderModifier) {
        return registerBuilder(name, () -> builderModifier.apply(ModuleDataBuilder.customInstanced(constructor, itemProvider)));
    }

    public <MODULE extends ICustomModule<MODULE>> ModuleRegistryObject<MODULE> registerBuilder(String name, Supplier<ModuleDataBuilder<MODULE>> builder) {
        return (ModuleRegistryObject<MODULE>) register(name, () -> new ModuleData<>(builder.get()));
    }

    public record SimpleEnchantmentAwareModule(ResourceKey<Enchantment> enchantment) implements EnchantmentAwareModule<SimpleEnchantmentAwareModule> {
    }
}