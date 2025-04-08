package mekanism.common.content.gear;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import mekanism.api.MekanismIMC;
import mekanism.api.MekanismIMC.ModuleContainerTarget;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.gear.IHUDElement;
import mekanism.api.gear.IHUDElement.HUDColor;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.gear.ModuleData;
import mekanism.api.providers.IModuleDataProvider;
import mekanism.client.model.MekanismModelCache;
import mekanism.client.render.armor.MekaSuitArmor;
import mekanism.common.Mekanism;
import mekanism.common.item.ItemModule;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import mekanism.common.util.text.TextUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.event.lifecycle.InterModProcessEvent;
import org.jetbrains.annotations.Nullable;

/**
 * @apiNote Do not instantiate this class directly as it will be done via the service loader. Instead, access instances of this via {@link IModuleHelper#INSTANCE}
 */
@NothingNullByDefault
public class ModuleHelper implements IModuleHelper {//TODO - 1.22: Evaluate moving at least some of this stuff to being defined via datamaps or at least datapack

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

    @SuppressWarnings("removal")
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
            }
            //TODO - 1.22: Remove these two deprecated branches
            else if (body instanceof IModuleDataProvider<?> moduleDataProvider) {
                ModuleData<?> moduleData = moduleDataProvider.getModuleData();
                supportedModulesBuilder.add(moduleData);
                logDebugReceivedIMC(imcMethod, message.senderModId(), moduleData);
            } else if (body instanceof IModuleDataProvider<?>[] providers) {
                for (IModuleDataProvider<?> moduleDataProvider : providers) {
                    ModuleData<?> moduleData = moduleDataProvider.getModuleData();
                    supportedModulesBuilder.add(moduleData);
                    logDebugReceivedIMC(imcMethod, message.senderModId(), moduleData);
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
    public ItemModule createModuleItem(Supplier<Holder<ModuleData<?>>> moduleDataSupplier, Properties properties) {
        return new ItemModule(moduleDataSupplier, properties);
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
    public ModuleContainer getModuleContainer(ItemStack stack) {
        return isModuleContainer(stack) ? stack.getOrDefault(MekanismDataComponents.MODULE_CONTAINER, ModuleContainer.EMPTY) : null;
    }

    @Override
    public boolean isModuleContainer(Item item) {
        return moduleContainers.contains(item);
    }

    @Override
    public IHUDElement hudElementEnabled(ResourceLocation icon, boolean enabled) {
        return hudElement(icon, OnOff.caps(enabled, false).getTextComponent(), enabled ? HUDColor.REGULAR : HUDColor.FADED);
    }

    @Override
    public IHUDElement hudElementPercent(ResourceLocation icon, double ratio) {
        return hudElement(icon, TextUtils.getPercent(ratio), ratio > 0.2 ? HUDColor.REGULAR : (ratio > 0.1 ? HUDColor.WARNING : HUDColor.DANGER));
    }

    @Override
    public IHUDElement hudElement(ResourceLocation icon, Component text, HUDColor color) {
        return HUDElement.of(icon, text, HUDElement.HUDColor.from(color));
    }

    @Override
    public synchronized void addMekaSuitModuleModels(ResourceLocation location) {
        MekanismModelCache.INSTANCE.registerMekaSuitModuleModel(location);
    }

    @Override
    public synchronized void addMekaSuitModuleModelSpec(String name, Holder<ModuleData<?>> moduleData, EquipmentSlot slotType, Predicate<LivingEntity> isActive) {
        MekaSuitArmor.registerModule(name, moduleData, slotType, isActive);
    }
}