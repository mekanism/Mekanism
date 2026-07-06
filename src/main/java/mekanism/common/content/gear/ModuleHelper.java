package mekanism.common.content.gear;

import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import it.unimi.dsi.fastutil.objects.ReferenceSets;
import java.util.Map;
import java.util.Set;
import mekanism.api.datamaps.IMekanismDataMapTypes;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.gear.ModuleData;
import mekanism.common.Mekanism;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.registries.MekanismItems;
import mekanism.common.util.InventoryUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.TypedInstance;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.datamaps.DataMapsUpdatedEvent;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;

/// @apiNote Do not instantiate this class directly as it will be done via the service loader. Instead, access instances of this via [IModuleHelper#INSTANCE]
public class ModuleHelper implements IModuleHelper {

    public static ModuleHelper get() {
        return (ModuleHelper) INSTANCE;
    }

    private AllModuleData allModuleData = AllModuleData.EMPTY;

    public void processDataMaps(DataMapsUpdatedEvent event) {
        event.ifRegistry(Registries.ITEM, itemRegistry -> allModuleData = AllModuleData.fromDataMaps(itemRegistry));
    }

    @Override
    public Item.Properties applyModuleContainerProperties(Item.Properties properties) {
        return properties.component(MekanismDataComponents.MODULE_CONTAINER, ModuleContainer.EMPTY);
    }

    @Override
    public void dropModuleContainerContents(ItemEntity entity, DamageSource source) {
        InventoryUtils.dropItemContents(entity, source);
    }

    @Override
    public Set<ModuleData<?>> getSupported(Item item) {
        return allModuleData.getSupported(item);
    }

    @Override
    public Set<Item> getSupportedItems(Holder<ModuleData<?>> typeProvider) {
        return allModuleData.getSupportedItems(typeProvider);
    }

    @Override
    public Set<ModuleData<?>> getConflicting(Holder<ModuleData<?>> type) {
        return allModuleData.getConflicting(type);
    }

    @Override
    @Nullable
    public <ITEM extends TypedInstance<Item> & DataComponentGetter> ModuleContainer getModuleContainer(ITEM instance) {
        return isModuleContainer(instance) ? getModuleContainerUnsafe(instance) : null;
    }

    public ModuleContainer getModuleContainerUnsafe(DataComponentGetter dataComponentGetter) {
        return dataComponentGetter.getOrDefault(MekanismDataComponents.MODULE_CONTAINER, ModuleContainer.EMPTY);
    }

    @Override
    public boolean isModuleContainer(Item item) {
        return allModuleData.containers.contains(item);
    }

    private DataComponentPatch getPatch(Holder<ModuleData<?>> module) {
        Rarity rarity = module.value().getRarity();
        DataComponentPatch.Builder builder = DataComponentPatch.builder()
              .set(dataComponent(), module);
        if (rarity != Rarity.COMMON) {
            builder.set(DataComponents.RARITY, module.value().getRarity());
        }
        return builder.build();
    }

    @Override
    public ItemStackTemplate asTemplate(Holder<ModuleData<?>> module, int amount) {
        return new ItemStackTemplate(MekanismItems.MODULE, amount, getPatch(module));
    }

    @Override
    public ItemStack asStack(Holder<ModuleData<?>> module, int amount) {
        return new ItemStack(MekanismItems.MODULE, amount, getPatch(module));
    }

    @Override
    public ItemResource asResource(Holder<ModuleData<?>> module) {
        return ItemResource.of((Holder<Item>) MekanismItems.MODULE, getPatch(module));
    }

    @Override
    public DataComponentType<Holder<ModuleData<?>>> dataComponent() {
        return MekanismDataComponents.MODULE_TYPE.get();
    }

    private record AllModuleData(
          ReferenceSet<Item> containers,
          Reference2ObjectMap<Item, ReferenceSet<ModuleData<?>>> supportedModules,
          Reference2ObjectMap<ModuleData<?>, ReferenceSet<Item>> supportedContainers,
          Reference2ObjectMap<ModuleData<?>, ReferenceSet<ModuleData<?>>> conflictingModules
    ) {

        private static final AllModuleData EMPTY = new AllModuleData(ReferenceSets.emptySet(), Reference2ObjectMaps.emptyMap(), Reference2ObjectMaps.emptyMap(), Reference2ObjectMaps.emptyMap());

        public static AllModuleData fromDataMaps(Registry<Item> itemRegistry) {
            Map<ResourceKey<Item>, HolderSet<ModuleData<?>>> dataMap = itemRegistry.getDataMap(IMekanismDataMapTypes.INSTANCE.supportedModules());
            if (dataMap.isEmpty()) {
                return EMPTY;
            }

            ReferenceSet<Item> moduleContainers = new ReferenceOpenHashSet<>();
            Reference2ObjectMap<Item, ReferenceSet<ModuleData<?>>> supportedModules = new Reference2ObjectOpenHashMap<>();
            Reference2ObjectMap<ModuleData<?>, ReferenceSet<Item>> supportedContainers = new Reference2ObjectOpenHashMap<>();

            for (Map.Entry<ResourceKey<Item>, HolderSet<ModuleData<?>>> entry : dataMap.entrySet()) {
                ResourceKey<Item> key = entry.getKey();
                Item moduleContainer = itemRegistry.getValue(key);
                if (moduleContainer == null) {
                    Mekanism.logger.error("Unable to locate module container item: '{}' for supported modules: {}", key.identifier(), entry.getValue());
                } else {
                    ReferenceSet<ModuleData<?>> modules = new ReferenceOpenHashSet<>();
                    for (Holder<ModuleData<?>> moduleHolder : entry.getValue()) {
                        ModuleData<?> module = moduleHolder.value();
                        modules.add(module);
                        supportedContainers.computeIfAbsent(module, _ -> new ReferenceOpenHashSet<>())
                              .add(moduleContainer);
                    }
                    if (modules.isEmpty()) {
                        Mekanism.logger.warn("Attempted to add zero supported modules to module container item: '{}'; not added as a module container.", key.identifier());
                    } else {
                        moduleContainers.add(moduleContainer);
                        supportedModules.put(moduleContainer, modules);
                    }
                }
            }
            if (moduleContainers.isEmpty()) {
                return EMPTY;
            }
            //Calculate conflicting modules
            Reference2ObjectMap<ModuleData<?>, ReferenceSet<ModuleData<?>>> conflictingModules = new Reference2ObjectOpenHashMap<>();
            for (ObjectIterator<Reference2ObjectMap.Entry<ModuleData<?>, ReferenceSet<Item>>> iterator = Reference2ObjectMaps.fastIterator(supportedContainers); iterator.hasNext(); ) {
                Reference2ObjectMap.Entry<ModuleData<?>, ReferenceSet<Item>> entry = iterator.next();
                ModuleData<?> moduleType = entry.getKey();
                ReferenceSet<ModuleData<?>> conflicting = new ReferenceOpenHashSet<>();
                for (Item item : entry.getValue()) {
                    for (ModuleData<?> other : supportedModules.getOrDefault(item, ReferenceSets.emptySet())) {
                        if (moduleType != other && moduleType.isExclusive(other.getExclusiveFlags())) {
                            conflicting.add(other);
                        }
                    }
                }
                if (!conflicting.isEmpty()) {
                    conflictingModules.put(moduleType, conflicting);
                }
            }
            return new AllModuleData(ReferenceSets.unmodifiable(moduleContainers), unmodifiable(supportedModules),
                  unmodifiable(supportedContainers), unmodifiable(conflictingModules));
        }

        private static <KEY, VALUE> Reference2ObjectMap<KEY, ReferenceSet<VALUE>> unmodifiable(Reference2ObjectMap<KEY, ReferenceSet<VALUE>> map) {
            for (ObjectIterator<Reference2ObjectMap.Entry<KEY, ReferenceSet<VALUE>>> iterator = Reference2ObjectMaps.fastIterator(map); iterator.hasNext(); ) {
                Reference2ObjectMap.Entry<KEY, ReferenceSet<VALUE>> entry = iterator.next();
                entry.setValue(ReferenceSets.unmodifiable(entry.getValue()));
            }
            return map.isEmpty() ? Reference2ObjectMaps.emptyMap() : Reference2ObjectMaps.unmodifiable(map);
        }

        public Set<ModuleData<?>> getSupported(Item item) {
            return supportedModules.getOrDefault(item, ReferenceSets.emptySet());
        }

        public Set<Item> getSupportedItems(Holder<ModuleData<?>> type) {
            return supportedContainers.getOrDefault(type.value(), ReferenceSets.emptySet());
        }

        public Set<ModuleData<?>> getConflicting(Holder<ModuleData<?>> type) {
            return conflictingModules.getOrDefault(type.value(), ReferenceSets.emptySet());
        }
    }
}