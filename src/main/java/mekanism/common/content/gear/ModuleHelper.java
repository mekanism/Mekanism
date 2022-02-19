package mekanism.common.content.gear;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
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
import mekanism.common.Mekanism;
import mekanism.common.item.ItemModule;
import mekanism.common.registries.MekanismItems;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import mekanism.common.util.text.TextUtils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.InterModComms;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ModuleHelper implements IModuleHelper {

    public static final ModuleHelper INSTANCE = new ModuleHelper();

    private final Map<String, ModuleData<?>> legacyModuleLookup = new Object2ObjectOpenHashMap<>();
    private final Map<Item, Set<ModuleData<?>>> supportedModules = new Object2ObjectOpenHashMap<>(5);
    private final Map<ModuleData<?>, Set<Item>> supportedContainers = new Object2ObjectOpenHashMap<>();

    @Deprecated//TODO - 1.18: Remove this
    public void gatherLegacyModules() {
        for (ModuleData<?> moduleData : MekanismAPI.moduleRegistry()) {
            String legacyName = moduleData.getLegacyName();
            if (legacyName != null) {
                legacyModuleLookup.put(legacyName, moduleData);
            }
        }
    }

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
            Object body = message.getMessageSupplier().get();
            if (body instanceof IModuleDataProvider) {
                IModuleDataProvider<?> moduleDataProvider = (IModuleDataProvider<?>) body;
                supportedModulesBuilder.add(moduleDataProvider.getModuleData());
                logDebugReceivedIMC(imcMethod, message.getSenderModId(), moduleDataProvider);
            } else if (body instanceof IModuleDataProvider[]) {
                for (IModuleDataProvider<?> moduleDataProvider : ((IModuleDataProvider<?>[]) body)) {
                    supportedModulesBuilder.add(moduleDataProvider.getModuleData());
                    logDebugReceivedIMC(imcMethod, message.getSenderModId(), moduleDataProvider);
                }
            } else {
                Mekanism.logger.warn("Received IMC message for '{}' from mod '{}' with an invalid body.", imcMethod, message.getSenderModId());
            }
        });
        Set<ModuleData<?>> supported = supportedModulesBuilder.build();
        if (!supported.isEmpty()) {
            Item item = moduleContainer.getItem();
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
        return supportedModules.getOrDefault(container.getItem(), Collections.emptySet());
    }

    @Override
    public Set<Item> getSupported(IModuleDataProvider<?> typeProvider) {
        return supportedContainers.getOrDefault(typeProvider.getModuleData(), Collections.emptySet());
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
            CompoundNBT modulesTag = ItemDataUtils.getCompound(container, NBTConstants.MODULES);
            return load(container, typeProvider.getModuleData(), modulesTag, null);
        }
        return null;
    }

    @Override
    public List<Module<?>> loadAll(ItemStack container) {
        if (container.getItem() instanceof IModuleContainerItem) {
            List<Module<?>> modules = new ArrayList<>();
            CompoundNBT modulesTag = ItemDataUtils.getCompound(container, NBTConstants.MODULES);
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
            CompoundNBT modulesTag = ItemDataUtils.getCompound(container, NBTConstants.MODULES);
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
    public Collection<ModuleData<?>> loadAllTypes(ItemStack container) {
        if (container.getItem() instanceof IModuleContainerItem) {
            return loadAllTypes(ItemDataUtils.getCompound(container, NBTConstants.MODULES));
        }
        return Collections.emptyList();
    }

    private Set<ModuleData<?>> loadAllTypes(CompoundNBT modulesTag) {
        //We use a set so in case there is a duplicate entry somehow between legacy and non legacy,
        // we only include it once in the returned set. This shouldn't happen, but it is a just in case thing
        //TODO - 1.18: After removing legacy types we might as well change this to a list
        Set<ModuleData<?>> moduleTypes = new HashSet<>();
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
        //Try looking up by legacy name first as they are all valid resource locations they just would end up in minecraft's domain
        ModuleData<?> legacy = legacyModuleLookup.get(name);
        if (legacy != null) {
            //If we found one return it
            return legacy;
        }
        //Otherwise, try getting the registry name and then looking it up in the module registry
        ResourceLocation registryName = ResourceLocation.tryParse(name);
        return registryName == null ? null : MekanismAPI.moduleRegistry().getValue(registryName);
    }

    @Nullable
    private <MODULE extends ICustomModule<MODULE>> Module<MODULE> load(ItemStack container, ModuleData<MODULE> type, CompoundNBT modulesTag,
          @Nullable Class<? extends ICustomModule<?>> typeFilter) {
        String registryName = type.getRegistryName().toString();
        if (modulesTag.contains(registryName, NBT.TAG_COMPOUND)) {
            return load(type, container, modulesTag, registryName, typeFilter);
        }
        String legacyName = type.getLegacyName();
        if (legacyName != null && modulesTag.contains(legacyName, NBT.TAG_COMPOUND)) {
            return load(type, container, modulesTag, legacyName, typeFilter);
        }
        return null;
    }

    @Nullable
    private <MODULE extends ICustomModule<MODULE>> Module<MODULE> load(ModuleData<MODULE> type, ItemStack container, CompoundNBT modulesTag, String key,
          @Nullable Class<? extends ICustomModule<?>> typeFilter) {
        //TODO - 1.18: When removing the legacy handling from the above method just inline this method
        Module<MODULE> module = new Module<>(type, container);
        if (typeFilter == null || typeFilter.isInstance(module.getCustomInstance())) {
            module.read(modulesTag.getCompound(key));
            return module;
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
    public IHUDElement hudElement(ResourceLocation icon, ITextComponent text, HUDColor color) {
        return HUDElement.of(icon, text, HUDElement.HUDColor.from(color));
    }
}