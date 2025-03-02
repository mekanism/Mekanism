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
import mekanism.common.registration.MekanismDeferredRegister;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;

public class ModuleDeferredRegister extends MekanismDeferredRegister<ModuleData<?>> {

    public ModuleDeferredRegister(String modid) {
        super(MekanismAPI.MODULE_REGISTRY_NAME, modid, ModuleRegistryObject::new);
    }

    public ModuleRegistryObject<?> registerMarker(String name, Supplier<Holder<Item>>  item) {
        return registerMarker(name, item, UnaryOperator.identity());
    }

    public ModuleRegistryObject<?> registerMarker(String name, Supplier<Holder<Item>> item, UnaryOperator<ModuleDataBuilder<?>> builderModifier) {
        return registerBuilder(name, () -> builderModifier.apply(ModuleDataBuilder.marker(item.get())));
    }

    public <MODULE extends ICustomModule<MODULE>> ModuleRegistryObject<MODULE> register(String name, Function<IModule<MODULE>, MODULE> constructor,
          Supplier<Holder<Item>> item, UnaryOperator<ModuleDataBuilder<MODULE>> builderModifier) {
        return registerBuilder(name, () -> builderModifier.apply(ModuleDataBuilder.custom(constructor, item.get())));
    }

    //Do not use this method if you have any config options
    public ModuleRegistryObject<SimpleEnchantmentAwareModule> registerEnchantBased(String name, ResourceKey<Enchantment> enchantment, Supplier<Holder<Item>> item,
          UnaryOperator<ModuleDataBuilder<SimpleEnchantmentAwareModule>> builderModifier) {
        return registerInstanced(name, () -> new SimpleEnchantmentAwareModule(enchantment), item, builderModifier);
    }

    //Do not use this method if you have any config options
    public <MODULE extends ICustomModule<MODULE>> ModuleRegistryObject<MODULE> registerInstanced(String name, Supplier<MODULE> constructor,
          Supplier<Holder<Item>> item, UnaryOperator<ModuleDataBuilder<MODULE>> builderModifier) {
        return registerBuilder(name, () -> builderModifier.apply(ModuleDataBuilder.customInstanced(constructor, item.get())));
    }

    public <MODULE extends ICustomModule<MODULE>> ModuleRegistryObject<MODULE> registerBuilder(String name, Supplier<ModuleDataBuilder<MODULE>> builder) {
        return (ModuleRegistryObject<MODULE>) register(name, () -> new ModuleData<>(builder.get()));
    }

    public record SimpleEnchantmentAwareModule(ResourceKey<Enchantment> enchantment) implements EnchantmentAwareModule<SimpleEnchantmentAwareModule> {
    }
}