package mekanism.common.content.gear;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.MekanismAPI;
import mekanism.api.MekanismIMC;
import mekanism.api.NBTConstants;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IHUDElement;
import mekanism.api.gear.IHUDElement.HUDColor;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.gear.ModuleData;
import mekanism.api.providers.IItemProvider;
import mekanism.api.providers.IModuleDataProvider;
import mekanism.client.model.MekanismModelCache;
import mekanism.client.render.armor.MekaSuitArmor;
import mekanism.common.Mekanism;
import mekanism.common.item.ItemModule;
import mekanism.common.registries.MekanismItems;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import mekanism.common.util.text.TextUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.InterModComms;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ModuleHelper implements IModuleHelper {

    public static final ModuleHelper INSTANCE = new ModuleHelper();

    private ModuleHelper() {
    }

    private final Map<Item, Set<ModuleData<?>>> supportedModules = new Object2ObjectOpenHashMap<>(5);
    private final Map<ModuleData<?>, Set<Item>> supportedContainers = new Object2ObjectOpenHashMap<>();
    private final Map<ModuleData<?>, Set<ModuleData<?>>> conflictingModules = new Object2ObjectOpenHashMap<>();

    public void processIMC() {
        Map<ModuleData<?>, ImmutableSet.Builder<Item>> supportedContainersBuilderMap = new Object2ObjectOpenHashMap<>();
        mapSupportedModules(MekanismIMC.ADD_MEKA_TOOL_MODULES, MekanismItems.MEKA_TOOL, supportedContainersBuilderMap);
        mapSupportedModules(MekanismIMC.ADD_MEKA_SUIT_HELMET_MODULES, MekanismItems.MEKASUIT_HELMET, supportedContainersBuilderMap);
        mapSupportedModules(MekanismIMC.ADD_MEKA_SUIT_BODYARMOR_MODULES, MekanismItems.MEKASUIT_BODYARMOR, supportedContainersBuilderMap);
        mapSupportedModules(MekanismIMC.ADD_MEKA_SUIT_PANTS_MODULES, MekanismItems.MEKASUIT_PANTS, supportedContainersBuilderMap);
        mapSupportedModules(MekanismIMC.ADD_MEKA_SUIT_BOOTS_MODULES, MekanismItems.MEKASUIT_BOOTS, supportedContainersBuilderMap);
        for (Map.Entry<ModuleData<?>, ImmutableSet.Builder<Item>> entry : supportedContainersBuilderMap.entrySet()) {
            supportedContainers.put(entry.getKey(), entry.getValue().build());
        }
    }

    private void mapSupportedModules(String imcMethod, IItemProvider moduleContainer, Map<ModuleData<?>, ImmutableSet.Builder<Item>> supportedContainersBuilderMap) {
        ImmutableSet.Builder<ModuleData<?>> supportedModulesBuilder = ImmutableSet.builder();
        InterModComms.getMessages(Mekanism.MODID, imcMethod::equals).forEach(message -> {
            Object body = message.messageSupplier().get();
            if (body instanceof IModuleDataProvider moduleDataProvider) {
                supportedModulesBuilder.add(moduleDataProvider.getModuleData());
                logDebugReceivedIMC(imcMethod, message.senderModId(), moduleDataProvider);
            } else if (body instanceof IModuleDataProvider<?>[] providers) {
                for (IModuleDataProvider<?> moduleDataProvider : providers) {
                    supportedModulesBuilder.add(moduleDataProvider.getModuleData());
                    logDebugReceivedIMC(imcMethod, message.senderModId(), moduleDataProvider);
                }
            } else {
                Mekanism.logger.warn("Received IMC message for '{}' from mod '{}' with an invalid body.", imcMethod, message.senderModId());
            }
        });
        Set<ModuleData<?>> supported = supportedModulesBuilder.build();
        if (!supported.isEmpty()) {
            Item item = moduleContainer.asItem();
            supportedModules.put(item, supported);
            for (ModuleData<?> data : supported) {
                supportedContainersBuilderMap.computeIfAbsent(data, d -> ImmutableSet.builder()).add(item);
            }
        }
    }

    private void logDebugReceivedIMC(String imcMethod, String senderModId, IModuleDataProvider<?> moduleDataProvider) {
        Mekanism.logger.debug("Received '{}' IMC message from '{}' for module ''{}.", imcMethod, senderModId, moduleDataProvider.getRegistryName());
    }

    @Override
    public ItemModule createModuleItem(IModuleDataProvider<?> moduleDataProvider, Item.Properties properties) {
        return new ItemModule(moduleDataProvider, properties);
    }

    @Override
    public Set<ModuleData<?>> getSupported(ItemStack container) {
        return getSupported(container.getItem());
    }

    private Set<ModuleData<?>> getSupported(Item item) {
        return supportedModules.getOrDefault(item, Collections.emptySet());
    }

    @Override
    public Set<Item> getSupported(IModuleDataProvider<?> typeProvider) {
        return supportedContainers.getOrDefault(typeProvider.getModuleData(), Collections.emptySet());
    }

    @Override
    public Set<ModuleData<?>> getConflicting(IModuleDataProvider<?> typeProvider) {
        return conflictingModules.computeIfAbsent(typeProvider.getModuleData(), moduleType -> getSupported(typeProvider).stream().flatMap(item -> getSupported(item).stream())
              .filter(other -> moduleType != other && moduleType.isExclusive(other.getExclusiveFlags())).collect(Collectors.toSet()));
    }

    @Override
    public boolean isEnabled(ItemStack container, IModuleDataProvider<?> typeProvider) {
        IModule<?> m = load(container, typeProvider);
        return m != null && m.isEnabled();
    }

    @Nullable
    @Override
    public <MODULE extends ICustomModule<MODULE>> Module<MODULE> load(ItemStack container, IModuleDataProvider<MODULE> typeProvider) {
        if (container.getItem() instanceof IModuleContainerItem) {
            CompoundTag modulesTag = ItemDataUtils.getCompound(container, NBTConstants.MODULES);
            return load(container, typeProvider.getModuleData(), modulesTag, null);
        }
        return null;
    }

    @Override
    public List<Module<?>> loadAll(ItemStack container) {
        if (container.getItem() instanceof IModuleContainerItem) {
            List<Module<?>> modules = new ArrayList<>();
            CompoundTag modulesTag = ItemDataUtils.getCompound(container, NBTConstants.MODULES);
            for (ModuleData<?> moduleType : loadAllTypes(modulesTag)) {
                Module<?> module = load(container, moduleType, modulesTag, null);
                if (module != null) {
                    modules.add(module);
                }
            }
            return modules;
        }
        return Collections.emptyList();
    }

    @Override
    public <MODULE extends ICustomModule<?>> List<Module<? extends MODULE>> loadAll(ItemStack container, Class<MODULE> moduleClass) {
        if (container.getItem() instanceof IModuleContainerItem) {
            List<Module<? extends MODULE>> modules = new ArrayList<>();
            CompoundTag modulesTag = ItemDataUtils.getCompound(container, NBTConstants.MODULES);
            for (ModuleData<?> moduleType : loadAllTypes(modulesTag)) {
                Module<?> module = load(container, moduleType, modulesTag, moduleClass);
                if (module != null) {
                    modules.add((Module<? extends MODULE>) module);
                }
            }
            return modules;
        }
        return Collections.emptyList();
    }

    @Override
    public List<ModuleData<?>> loadAllTypes(ItemStack container) {
        if (container.getItem() instanceof IModuleContainerItem) {
            return loadAllTypes(ItemDataUtils.getCompound(container, NBTConstants.MODULES));
        }
        return Collections.emptyList();
    }

    private List<ModuleData<?>> loadAllTypes(CompoundTag modulesTag) {
        List<ModuleData<?>> moduleTypes = new ArrayList<>();
        for (String name : modulesTag.getAllKeys()) {
            ModuleData<?> moduleType = getModuleTypeFromName(name);
            if (moduleType != null) {
                moduleTypes.add(moduleType);
            }
        }
        return moduleTypes;
    }

    @Nullable
    private ModuleData<?> getModuleTypeFromName(String name) {
        //Otherwise, try getting the registry name and then looking it up in the module registry
        ResourceLocation registryName = ResourceLocation.tryParse(name);
        return registryName == null ? null : MekanismAPI.moduleRegistry().getValue(registryName);
    }

    @Nullable
    private <MODULE extends ICustomModule<MODULE>> Module<MODULE> load(ItemStack container, ModuleData<MODULE> type, CompoundTag modulesTag,
          @Nullable Class<? extends ICustomModule<?>> typeFilter) {
        String registryName = type.getRegistryName().toString();
        if (modulesTag.contains(registryName, Tag.TAG_COMPOUND)) {
            Module<MODULE> module = new Module<>(type, container);
            if (typeFilter == null || typeFilter.isInstance(module.getCustomInstance())) {
                module.read(modulesTag.getCompound(registryName));
                return module;
            }
        }
        return null;
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
    public synchronized void addMekaSuitModuleModelSpec(String name, IModuleDataProvider<?> moduleDataProvider, EquipmentSlot slotType, Predicate<LivingEntity> isActive) {
        MekaSuitArmor.registerModule(name, moduleDataProvider, slotType, isActive);
    }
}