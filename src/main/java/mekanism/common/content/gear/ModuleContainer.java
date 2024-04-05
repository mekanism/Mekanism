package mekanism.common.content.gear;

import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
public final class ModuleContainer implements IModuleContainer {

    public static ModuleContainer create(IAttachmentHolder holder) {
        if (holder instanceof ItemStack stack && IModuleHelper.INSTANCE.isModuleContainer(stack)) {
            return new ModuleContainer(stack, new Object2IntLinkedOpenHashMap<>());
        }
        throw new IllegalArgumentException("Attempted to attach a ModuleContainer to an object that does not support containing modules.");
    }

    private final Map<ModuleData<?>, Module<?>> modules = new LinkedHashMap<>();
    private final Map<ModuleData<?>, Module<?>> modulesView = Collections.unmodifiableMap(modules);
    private final Object2IntMap<Enchantment> enchantments;
    private final Object2IntMap<Enchantment> enchantmentsView;

    final ItemStack container;

    private ModuleContainer(ItemStack container, Object2IntMap<Enchantment> enchantments) {
        this.container = container;
        this.enchantments = enchantments;
        this.enchantmentsView = Object2IntMaps.unmodifiable(this.enchantments);
    }

    @Nullable
    @Override
    public CompoundTag serializeNBT() {
        if (modules.isEmpty() && enchantments.isEmpty()) {
            return null;
        }
        CompoundTag modulesTag = new CompoundTag();
        for (Entry<ModuleData<?>, Module<?>> entry : modules.entrySet()) {
            modulesTag.put(entry.getKey().getRegistryName().toString(), entry.getValue().save());
        }
        //Add any extra data we may be tracking that isn't specifically modules to module info
        CompoundTag extraData = new CompoundTag();
        if (!enchantments.isEmpty()) {
            ListTag enchantmentNbt = new ListTag();
            for (Object2IntMap.Entry<Enchantment> entry : enchantments.object2IntEntrySet()) {
                enchantmentNbt.add(EnchantmentHelper.storeEnchantment(EnchantmentHelper.getEnchantmentId(entry.getKey()), entry.getIntValue()));
            }
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
        //TODO: Do we actually want to save the enchantments or just cache them and lazily recreate the map?
        if (modulesTag.contains(NBTConstants.EXTRA_DATA, Tag.TAG_COMPOUND)) {
            CompoundTag extraData = modulesTag.getCompound(NBTConstants.EXTRA_DATA);
            if (extraData.contains(NBTConstants.ENCHANTMENTS, Tag.TAG_LIST)) {
                ListTag enchantmentNbt = extraData.getList(NBTConstants.ENCHANTMENTS, Tag.TAG_COMPOUND);
                enchantments.putAll(EnchantmentHelper.deserializeEnchantments(enchantmentNbt));
            }
        }
    }

    @Nullable
    public ModuleContainer copy(IAttachmentHolder holder) {
        if (modules.isEmpty() && enchantments.isEmpty()) {
            return null;
        }
        if (!(holder instanceof ItemStack stack) || !IModuleHelper.INSTANCE.isModuleContainer(stack)) {
            return null;
        }
        ModuleContainer copy = new ModuleContainer(stack, new Object2IntLinkedOpenHashMap<>(enchantments));
        for (Map.Entry<ModuleData<?>, Module<?>> entry : modules.entrySet()) {
            ModuleData<?> type = entry.getKey();
            //Copy the modules by saving and deserializing it against the new stack
            //TODO - 1.20.4: Can we make a way of copying this that doesn't require serialization and deserialization?
            copy.modules.put(type, copy.createNewModule(type, entry.getValue().save()));
        }
        return copy;
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
    public Map<Enchantment, Integer> moduleBasedEnchantments() {//TODO - 1.20.5: Change this to returning an Object2IntMap
        return enchantmentsView;
    }

    @Internal
    @Override
    public void setEnchantmentLevel(Enchantment enchantment, int level) {
        if (level == 0) {
            enchantments.removeInt(enchantment);
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

    public void removeModule(IModuleDataProvider<?> typeProvider, int toRemove) {
        ModuleData<?> type = typeProvider.getModuleData();
        Module<?> module = modules.get(type);
        if (module != null && module.remove(toRemove)) {
            modules.remove(type);
        }
    }

    public int addModule(IModuleDataProvider<?> typeProvider, int toInstall) {
        ModuleData<?> moduleType = typeProvider.getModuleData();
        boolean hadModule = has(moduleType);
        Module<?> module = modules.get(moduleType);
        if (module == null) {
            module = createNewModule(moduleType, new CompoundTag());
            modules.put(moduleType, module);
        }
        return module.add(!hadModule, toInstall);
    }

    @Override
    public List<IHUDElement> getHUDElements(Player player) {
        if (modules.isEmpty()) {
            return Collections.emptyList();
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
            return Collections.emptyList();
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