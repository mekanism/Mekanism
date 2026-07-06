package mekanism.common.content.gear;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import mekanism.api.MekanismIMC;
import mekanism.api.MekanismIMC.ModuleContainerTarget;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.gear.ModuleData;
import mekanism.common.Mekanism;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.registries.MekanismItems;
import mekanism.common.util.InventoryUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.TypedInstance;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Rarity;
import net.neoforged.fml.event.lifecycle.InterModProcessEvent;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;

/// @apiNote Do not instantiate this class directly as it will be done via the service loader. Instead, access instances of this via [IModuleHelper#INSTANCE]
public class ModuleHelper implements IModuleHelper {//TODO - 26.2: Evaluate moving at least some of this stuff to being defined via datamaps or at least datapack

    public static ModuleHelper get() {
        return (ModuleHelper) INSTANCE;
    }

    private final Set<Item> moduleContainers = new ReferenceOpenHashSet<>();
    private final Map<Item, Set<ModuleData<?>>> supportedModules = new Reference2ObjectArrayMap<>(5);
    private final Map<ModuleData<?>, Set<Item>> supportedContainers = new IdentityHashMap<>();
    private final Map<ModuleData<?>, Set<ModuleData<?>>> conflictingModules = new IdentityHashMap<>();

    public void processIMC(InterModProcessEvent event) {
        Map<Item, String> moduleContainers = addModuleContainers(event);
        this.moduleContainers.addAll(moduleContainers.keySet());
        Map<ModuleData<?>, ImmutableSet.Builder<Item>> supportedContainersBuilderMap = new IdentityHashMap<>();
        for (Map.Entry<Item, String> entry : moduleContainers.entrySet()) {
            mapSupportedModules(event, entry.getValue(), entry.getKey(), supportedContainersBuilderMap);
        }
        for (Map.Entry<ModuleData<?>, ImmutableSet.Builder<Item>> entry : supportedContainersBuilderMap.entrySet()) {
            supportedContainers.put(entry.getKey(), entry.getValue().build());
        }
    }

    private Map<Item, String> addModuleContainers(InterModProcessEvent event) {
        Map<Item, String> moduleContainers = new Reference2ObjectArrayMap<>(5);
        Set<String> imcMethods = new HashSet<>(5);
        event.getIMCStream(MekanismIMC.ADD_MODULE_CONTAINER::equals).forEach(message -> {
            if (message.messageSupplier().get() instanceof ModuleContainerTarget(Holder<Item> container, String imcMethod)) {
                Mekanism.logger.debug("Received IMC message '{}' from '{}' for new module container '{}' with an imcMethod '{}'.", MekanismIMC.ADD_MODULE_CONTAINER,
                      message.senderModId(), container.getRegisteredName(), imcMethod);
                if (moduleContainers.put(container.value(), imcMethod) != null) {
                    Mekanism.logger.error("Received IMC message for '{}' from mod '{}' for an item '{}' that has already been registered as a container.",
                          MekanismIMC.ADD_MODULE_CONTAINER, message.senderModId(), container.getRegisteredName());
                }
                if (!imcMethods.add(imcMethod)) {
                    Mekanism.logger.error("Received IMC message for '{}' from mod '{}' for an item '{}' with an imcMethod '{}' that that has already been registered.",
                          MekanismIMC.ADD_MODULE_CONTAINER, message.senderModId(), container.getRegisteredName(), imcMethod);
                }
            } else {
                Mekanism.logger.warn("Received IMC message for '{}' from mod '{}' with an invalid body.", MekanismIMC.ADD_MODULE_CONTAINER, message.senderModId());
            }
        });
        return moduleContainers;
    }

    private void mapSupportedModules(InterModProcessEvent event, String imcMethod, Item moduleContainer,
          Map<ModuleData<?>, ImmutableSet.Builder<Item>> supportedContainersBuilderMap) {
        ImmutableSet.Builder<ModuleData<?>> supportedModulesBuilder = ImmutableSet.builder();
        event.getIMCStream(imcMethod::equals).forEach(message -> {
            Object body = message.messageSupplier().get();
            if (body instanceof Holder<?> holder) {
                if (holder.value() instanceof ModuleData<?> moduleData) {
                    supportedModulesBuilder.add(moduleData);
                    logDebugReceivedIMC(imcMethod, message.senderModId(), moduleData);
                } else {
                    //Holder for something other than modules
                    Mekanism.logger.warn("Received IMC message for '{}' from mod '{}' with an invalid body.", imcMethod, message.senderModId());
                }
            } else if (body instanceof HolderSet<?> holderSet) {
                for (Holder<?> holder : holderSet) {
                    if (holder.value() instanceof ModuleData<?> moduleData) {
                        supportedModulesBuilder.add(moduleData);
                        logDebugReceivedIMC(imcMethod, message.senderModId(), moduleData);
                    } else {
                        //Holder set for something other than modules
                        Mekanism.logger.warn("Received IMC message for '{}' from mod '{}' with an invalid body.", imcMethod, message.senderModId());
                        break;
                    }
                }
            } else {
                Mekanism.logger.warn("Received IMC message for '{}' from mod '{}' with an invalid body.", imcMethod, message.senderModId());
            }
        });
        Set<ModuleData<?>> supported = supportedModulesBuilder.build();
        if (!supported.isEmpty()) {
            supportedModules.put(moduleContainer, supported);
            for (ModuleData<?> data : supported) {
                supportedContainersBuilderMap.computeIfAbsent(data, d -> ImmutableSet.builder()).add(moduleContainer);
            }
        }
    }

    private void logDebugReceivedIMC(String imcMethod, String senderModId, ModuleData<?> moduleData) {
        Mekanism.logger.debug("Received IMC message '{}' from '{}' for module '{}'.", imcMethod, senderModId, moduleData);
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
        return supportedModules.getOrDefault(item, Collections.emptySet());
    }

    @Override
    public Set<Item> getSupportedItems(Holder<ModuleData<?>> typeProvider) {
        return supportedContainers.getOrDefault(typeProvider.value(), Collections.emptySet());
    }

    @Override
    public Set<ModuleData<?>> getConflicting(Holder<ModuleData<?>> type) {
        ModuleData<?> moduleType = type.value();
        Set<ModuleData<?>> conflicting = conflictingModules.get(moduleType);
        if (conflicting == null) {
            conflicting = new ReferenceOpenHashSet<>();
            for (Item item : getSupportedItems(type)) {
                for (ModuleData<?> other : getSupported(item)) {
                    if (moduleType != other && moduleType.isExclusive(other.getExclusiveFlags())) {
                        conflicting.add(other);
                    }
                }
            }
            conflicting = Collections.unmodifiableSet(conflicting);
            conflictingModules.put(moduleType, conflicting);
        }
        return conflicting;
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
        return moduleContainers.contains(item);
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
}