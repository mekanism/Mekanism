package mekanism.common.registration.impl;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import mekanism.api.text.EnumColor;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.component.containers.type.ContainerType;
import mekanism.common.component.containers.type.IContainerType;
import mekanism.common.content.gear.ModuleHelper;
import mekanism.common.item.ItemModule;
import mekanism.common.registration.MekanismDeferredHolder;
import mekanism.common.registration.MekanismDeferredRegister;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

public class ItemDeferredRegister extends MekanismDeferredRegister<Item> {

    public ItemDeferredRegister(String modid) {
        super(Registries.ITEM, modid, ItemRegistryObject::new);
    }

    @Override
    public void register(IEventBus bus) {
        super.register(bus);
        bus.addListener(RegisterCapabilitiesEvent.class, event -> forEntries(registryObject -> registryObject.registerCapabilities(event)));
        //Listen at the lowest priority so that it happens after our elements have been registered
        // and then see if any need to apply attachments
        bus.addListener(EventPriority.LOWEST, RegisterEvent.class, event -> {
            if (event.getRegistryKey().equals(Registries.ITEM)) {
                forEntries(registryObject -> registryObject.attachDefaultContainers(bus));
            }
        });

        bus.addListener(EventPriority.LOWEST, ModifyDefaultComponentsEvent.class, event -> forEntries(registryObject -> {
            if (ContainerType.anySupports(registryObject)) {
                event.modify(registryObject, (components, _, item) -> {
                    for (IContainerType<?, ?> type : ContainerType.TYPES) {
                        type.addDefault(item, components);
                    }
                });
            }
        }));
    }

    private void forEntries(Consumer<ItemRegistryObject<?>> consumer) {
        for (Holder<Item> entry : getEntries()) {
            //Note: All entries should be of this type
            if (entry instanceof ItemRegistryObject<?> registryObject) {
                consumer.accept(registryObject);
            } else if (SharedConstants.IS_RUNNING_IN_IDE) {
                throw new IllegalStateException("Expected entry to be an ItemRegistryObject");
            }
        }
    }

    public ItemRegistryObject<Item> register(String name) {
        return registerItem(name, Item::new);
    }

    public ItemRegistryObject<Item> registerSimple(String name, UnaryOperator<Properties> propertyModifier) {
        return registerItem(name, properties -> new Item(propertyModifier.apply(properties)));
    }

    public ItemRegistryObject<Item> register(String name, Rarity rarity) {
        return registerItem(name, properties -> new Item(properties.rarity(rarity)));
    }

    public ItemRegistryObject<Item> register(String name, EnumColor color) {
        return registerItem(name, properties -> new Item(properties) {
            @Override
            public Component getName(ItemStack stack) {
                return TextComponentUtil.build(color, super.getName(stack));
            }
        });
    }

    public ItemRegistryObject<ItemModule> registerModule(ModuleRegistryObject<?> moduleDataSupplier) {
        return registerModule(moduleDataSupplier, Rarity.COMMON);
    }

    public ItemRegistryObject<ItemModule> registerModule(ModuleRegistryObject<?> moduleData, Rarity rarity) {
        //Note: We use the internal helper just in case we end up needing to know it is an ItemModule instead of just an Item somewhere
        return registerItem("module_" + moduleData.getName(), properties -> ModuleHelper.get().createModuleItem(moduleData, properties.rarity(rarity)));
    }

    public <ITEM extends Item> ItemRegistryObject<ITEM> registerItem(String name, Function<Item.Properties, ITEM> sup) {
        return register(name, key -> sup.apply(new Item.Properties().setId(ResourceKey.create(getRegistryKey(), key))));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <ITEM extends Item> ItemRegistryObject<ITEM> register(String name, Function<Identifier, ? extends ITEM> func) {
        return (ItemRegistryObject<ITEM>) super.register(name, func);
    }

    public ItemRegistryObject<SpawnEggItem> registerSpawnEgg(MekanismDeferredHolder<EntityType<?>, ? extends EntityType<? extends Mob>> entityTypeProvider) {
        return registerItem(entityTypeProvider.getName() + "_spawn_egg", props -> new SpawnEggItem(props.spawnEgg(entityTypeProvider.get())));
    }

    public static class StrictProperties extends Item.Properties {

        private boolean durabilitySet, toolSet;

        @Override
        public Item.Properties durability(int maxDamage) {
            if (!durabilitySet) {
                durabilitySet = true;
                return super.durability(maxDamage);
            }
            return this;
        }

        @Override
        public <T> Item.Properties component(DataComponentType<T> component, T value) {
            if (component == net.minecraft.core.component.DataComponents.TOOL) {
                if (toolSet) {
                    return this;
                }
                toolSet = true;
            }
            return super.component(component, value);
        }
    }
}