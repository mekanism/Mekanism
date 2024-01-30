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
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.capabilities.ItemCapability;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

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
    private final Map<Enchantment, Integer> enchantments;
    private final Map<Enchantment, Integer> enchantmentsView;

    final ItemStack container;

    private ModuleContainer(ItemStack container) {
        this(container, new LinkedHashMap<>(), new LinkedHashMap<>());
        //TODO - 1.21?: Remove this way of loading legacy data
        //Load legacy modules
        if (ItemDataUtils.hasData(this.container, NBTConstants.MODULES, Tag.TAG_COMPOUND)) {
            CompoundTag legacyModules = ItemDataUtils.getCompound(this.container, NBTConstants.MODULES);
            CompoundTag extraData = new CompoundTag();
            if (ItemDataUtils.hasData(this.container, NBTConstants.ENCHANTMENTS, Tag.TAG_LIST)) {
                //Handle legacy enchantments from enchantment modules
                ListTag enchantmentTag = ItemDataUtils.getList(this.container, NBTConstants.ENCHANTMENTS);
                extraData.put(NBTConstants.ENCHANTMENTS, enchantmentTag);
            }
            if (!extraData.isEmpty()) {
                legacyModules = legacyModules.copy();
                legacyModules.put(NBTConstants.EXTRA_DATA, extraData);
            }
            deserializeNBT(legacyModules);
            //Remove the legacy data now that it has been parsed and loaded
            ItemDataUtils.removeData(this.container, NBTConstants.MODULES);
            ItemDataUtils.removeData(this.container, NBTConstants.ENCHANTMENTS);
        }
    }

    ModuleContainer(ItemStack container, Map<ModuleData<?>, Module<?>> modules, Map<Enchantment, Integer> enchantments) {
        this.container = container;
        this.modules = modules;
        this.modulesView = Collections.unmodifiableMap(this.modules);
        this.enchantments =enchantments;
        this.enchantmentsView = Collections.unmodifiableMap(this.enchantments);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag modulesTag = new CompoundTag();
        modules.forEach((type, module) -> modulesTag.put(type.getRegistryName().toString(), module.save()));
        //Add any extra data we may be tracking that isn't specifically modules to module info
        CompoundTag extraData = new CompoundTag();
        if (!enchantments.isEmpty()) {
            ListTag enchantmentNbt = new ListTag();
            enchantments.forEach((enchantment, level) -> enchantmentNbt.add(EnchantmentHelper.storeEnchantment(EnchantmentHelper.getEnchantmentId(enchantment), level)));
            extraData.put(NBTConstants.ENCHANTMENTS, enchantmentNbt);
        }
        if (!extraData.isEmpty()) {
            modulesTag.put(NBTConstants.EXTRA_DATA, extraData);
        }
        return modulesTag;
    }

    @Override
    public void deserializeNBT(CompoundTag modulesTag) {
        this.modules.clear();
        for (String name : modulesTag.getAllKeys()) {
            //Try to get the registry name and then look it up in the module registry
            ResourceLocation registryName = ResourceLocation.tryParse(name);
            ModuleData<?> moduleType = registryName == null ? null : MekanismAPI.MODULE_REGISTRY.get(registryName);
            //Ensure it exists as there won't be a module for the extra tag we shoehorn into the compound
            if (moduleType != null) {
                Module<?> module = createNewModule(moduleType, modulesTag.getCompound(name));
                if (module.getInstalledCount() > 0) {
                    //Basic validation check, this should always pass, but just in case
                    this.modules.put(moduleType, module);
                }
            }
        }
        if (modulesTag.contains(NBTConstants.EXTRA_DATA, Tag.TAG_COMPOUND)) {
            CompoundTag extraData = modulesTag.getCompound(NBTConstants.EXTRA_DATA);
            if (extraData.contains(NBTConstants.ENCHANTMENTS, Tag.TAG_LIST)) {
                ListTag enchantmentNbt = extraData.getList(NBTConstants.ENCHANTMENTS, Tag.TAG_COMPOUND);
                enchantments.putAll(EnchantmentHelper.deserializeEnchantments(enchantmentNbt));
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
    public Map<Enchantment, Integer> moduleBasedEnchantments() {
        return enchantmentsView;
    }

    @Internal
    @Override
    public void setEnchantmentLevel(Enchantment enchantment, int level) {
        if (level == 0) {
            enchantments.remove(enchantment);
        } else {
            enchantments.put(enchantment, level);
        }
    }

    @Override
    public ItemStack getPreviewStack() {
        return container.copy();
    }

    @Nullable
    @Override
    public <T, C> T getCapabilityFromStack(ItemCapability<T, C> capability, @UnknownNullability C context) {
        return container.getCapability(capability, context);
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
        modules.computeIfAbsent(typeProvider.getModuleData(), type -> createNewModule(type, new CompoundTag())).add(!hadModule);
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
        } else if (getClass() != other.getClass() || installedCount() != other.installedCount() || enchantments.size() != other.moduleBasedEnchantments().size()) {
            return false;
        }
        for (Map.Entry<ModuleData<?>, Module<?>> entry : modules.entrySet()) {
            IModule<?> otherModule = other.get(entry.getKey());
            if (otherModule == null || !entry.getValue().isCompatible(otherModule)) {
                return false;
            }
        }
        //Note: We skip checking whether the enchantments match as the values *should* be the same if all the modules and their configs line up
        return true;
    }
}