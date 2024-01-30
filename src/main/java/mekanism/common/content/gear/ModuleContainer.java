package mekanism.common.content.gear;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import mekanism.api.MekanismAPI;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IHUDElement;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleContainer;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.gear.ModuleData;
import mekanism.api.providers.IModuleDataProvider;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public sealed class ModuleContainer implements IModuleContainer permits InvalidModuleContainer {

    public static ModuleContainer create(IAttachmentHolder holder) {
        if (holder instanceof ItemStack stack && IModuleHelper.INSTANCE.isModuleContainer(stack)) {
            return new ModuleContainer(stack);
        }
        //If someone tries to attach this to something that isn't a stack or isn't a module container item return an instance that NO-OPs
        // and won't write any data when serialized
        return InvalidModuleContainer.INSTANCE;
    }

    private final Map<ModuleData<?>, Module<?>> modules;
    private final Map<ModuleData<?>, Module<?>> modulesView;

    final ItemStack container;

    private ModuleContainer(ItemStack container) {
        this(container, new LinkedHashMap<>());
        //Load legacy data
        //TODO - 1.21?: Remove this way of loading legacy data
        if (ItemDataUtils.hasData(this.container, NBTConstants.MODULES, Tag.TAG_COMPOUND)) {
            deserializeNBT(ItemDataUtils.getCompound(this.container, NBTConstants.MODULES));
            //Remove the legacy data now that it has been parsed and loaded
            ItemDataUtils.removeData(this.container, NBTConstants.MODULES);
        }
    }

    ModuleContainer(ItemStack container, Map<ModuleData<?>, Module<?>> modules) {
        this.container = container;
        this.modules = modules;
        this.modulesView = Collections.unmodifiableMap(this.modules);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag modulesTag = new CompoundTag();
        modules.forEach((type, module) -> modulesTag.put(type.getRegistryName().toString(), module.save()));
        return modulesTag;
    }

    @Override
    public void deserializeNBT(CompoundTag modulesTag) {
        this.modules.clear();
        for (String name : modulesTag.getAllKeys()) {
            //Try to get the registry name and then look it up in the module registry
            ResourceLocation registryName = ResourceLocation.tryParse(name);
            ModuleData<?> moduleType = registryName == null ? null : MekanismAPI.MODULE_REGISTRY.get(registryName);
            if (moduleType != null) {
                Module<?> module = createNewModule(moduleType, modulesTag.getCompound(name));
                if (module.getInstalledCount() > 0) {
                    //Basic validation check, this should always pass, but just in case
                    this.modules.put(moduleType, module);
                }
            }
        }
    }

    private <MODULE extends ICustomModule<MODULE>> Module<MODULE> createNewModule(ModuleData<MODULE> type, CompoundTag nbt) {
        Module<MODULE> module = new Module<>(type, this);
        module.read(nbt);
        return module;
    }

    @Override
    public Map<ModuleData<?>, Module<?>> typedModules() {
        return modulesView;
    }

    @Override
    public Collection<Module<?>> modules() {
        return typedModules().values();
    }

    @Override
    public boolean isContainerOnCooldown(Player player) {
        return player.getCooldowns().isOnCooldown(container.getItem());
    }

    @Override
    public boolean isInstance(Class<?> clazz) {
        return clazz.isInstance(container.getItem());
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <MODULE extends ICustomModule<MODULE>> Module<MODULE> get(IModuleDataProvider<MODULE> typeProvider) {
        return (Module<MODULE>) modules.get(typeProvider.getModuleData());
    }

    @Override
    public boolean has(IModuleDataProvider<?> type) {
        return modules.containsKey(type.getModuleData());
    }

    @Override
    public Set<ModuleData<?>> supportedTypes() {
        return IModuleHelper.INSTANCE.getSupported(container.getItem());
    }

    public boolean canInstall(IModuleDataProvider<?> typeProvider) {
        if (supports(typeProvider)) {
            IModule<?> module = get(typeProvider);
            return module == null || module.getInstalledCount() < module.getData().getMaxStackSize();
        }
        return false;
    }

    public void removeModule(IModuleDataProvider<?> typeProvider) {
        ModuleData<?> type = typeProvider.getModuleData();
        Module<?> module = modules.get(type);
        if (module != null && module.remove()) {
            modules.remove(type);
        }
    }

    public void addModule(IModuleDataProvider<?> typeProvider) {
        boolean hadModule = has(typeProvider);
        modules.computeIfAbsent(typeProvider.getModuleData(), type -> createNewModule(type, new CompoundTag())).add(hadModule);
    }

    @Override
    public List<IHUDElement> getHUDElements(Player player) {
        if (modules.isEmpty()) {
            return List.of();
        }
        List<IHUDElement> ret = new ArrayList<>();
        for (Module<?> module : modules()) {
            if (module.renderHUD()) {
                module.addHUDElements(player, ret);
            }
        }
        return ret;
    }

    @Override
    public List<Component> getHUDStrings(Player player) {
        if (modules.isEmpty()) {
            return List.of();
        }
        List<Component> ret = new ArrayList<>();
        for (Module<?> module : modules()) {
            if (module.renderHUD()) {
                module.addHUDStrings(player, ret);
            }
        }
        return ret;
    }

    @Override
    public boolean isCompatible(IModuleContainer other) {
        if (other == this) {
            return true;
        } else if (getClass() != other.getClass() || installedCount() != other.installedCount()) {
            return false;
        }
        for (Map.Entry<ModuleData<?>, Module<?>> entry : modules.entrySet()) {
            IModule<?> otherModule = other.get(entry.getKey());
            if (otherModule == null || !entry.getValue().isCompatible(otherModule)) {
                return false;
            }
        }
        return true;
    }
}