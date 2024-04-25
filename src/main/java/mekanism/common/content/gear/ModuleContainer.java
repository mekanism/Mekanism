package mekanism.common.content.gear;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.neoforge.capabilities.ItemCapability;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

@NothingNullByDefault//TODO - 1.20.5: Do we want the modules to be sorted?
public record ModuleContainer(Map<ModuleData<?>, Module<?>> typedModules, ItemEnchantments enchantments) implements IModuleContainer {

    public static final ModuleContainer EMPTY = new ModuleContainer(Collections.emptyMap(), ItemEnchantments.EMPTY);

    public static final Codec<ModuleContainer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
          Codec.unboundedMap(MekanismAPI.MODULE_REGISTRY.byNameCodec(), CompoundTag.CODEC)
                      .xmap(map -> {
                          Map<ModuleData<?>, Module<?>> modules = new LinkedHashMap<>(map.size());
                          for (Map.Entry<ModuleData<?>, CompoundTag> entry : map.entrySet()) {
                              //TODO - 1.20.5: FIGURE THIS OUT
                          }
                          return modules;
                      }, modules -> {
                          Map<ModuleData<?>, CompoundTag> map = new LinkedHashMap<>(modules.size());
                          for (Map.Entry<ModuleData<?>, Module<?>> entry : modules.entrySet()) {
                              map.put(entry.getKey(), entry.getValue().save());
                          }
                          return map;
                      }).fieldOf(NBTConstants.MODULES).forGetter(ModuleContainer::typedModules),
          ItemEnchantments.CODEC.fieldOf(NBTConstants.ENCHANTMENTS).forGetter(ModuleContainer::enchantments)
    ).apply(instance, ModuleContainer::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ModuleContainer> STREAM_CODEC = StreamCodec.composite(
          ByteBufCodecs.<RegistryFriendlyByteBuf, ModuleData<?>, CompoundTag, Map<ModuleData<?>, CompoundTag>>map(LinkedHashMap::new, ByteBufCodecs.registry(MekanismAPI.MODULE_REGISTRY_NAME), ByteBufCodecs.TRUSTED_COMPOUND_TAG)
                      .map(map -> {
                          Map<ModuleData<?>, Module<?>> modules = new LinkedHashMap<>(map.size());
                          for (Map.Entry<ModuleData<?>, CompoundTag> entry : map.entrySet()) {
                              //TODO - 1.20.5: FIGURE THIS OUT
                          }
                          return modules;
                      }, modules -> {
                          Map<ModuleData<?>, CompoundTag> map = new LinkedHashMap<>(modules.size());
                          for (Map.Entry<ModuleData<?>, Module<?>> entry : modules.entrySet()) {
                              map.put(entry.getKey(), entry.getValue().save());
                          }
                          return map;
                      }), ModuleContainer::typedModules,
          ItemEnchantments.STREAM_CODEC, ModuleContainer::enchantments,
          ModuleContainer::new
    );

    public ModuleContainer {
        //Make the map unmodifiable to ensure we don't accidentally mutate it
        typedModules = Collections.unmodifiableMap(typedModules);
    }

    /*@Nullable
    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
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
            extraData.put(NBTConstants.ENCHANTMENTS, ItemEnchantments.CODEC.encodeStart(NbtOps.INSTANCE, enchantments).getOrThrow());
        }
        if (!extraData.isEmpty()) {
            modulesTag.put(NBTConstants.EXTRA_DATA, extraData);
        }
        return modulesTag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag modulesTag) {
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
            if (extraData.contains(NBTConstants.ENCHANTMENTS)) {
                enchantments = ItemEnchantments.CODEC.decode(NbtOps.INSTANCE, extraData.get(NBTConstants.ENCHANTMENTS)).getOrThrow().getFirst();
            }
        }
    }*/

    private <MODULE extends ICustomModule<MODULE>> Module<MODULE> createNewModule(ModuleData<MODULE> type, CompoundTag nbt) {
        Module<MODULE> module = new Module<>(type, this);
        module.read(nbt);
        return module;
    }

    @Override
    public Collection<Module<?>> modules() {
        return typedModules().values();
    }

    @Override
    public ItemEnchantments moduleBasedEnchantments() {//TODO - 1.20.5: Change this to returning an Object2IntMap
        return enchantments;
    }

    @Internal
    @Override
    public void setEnchantmentLevel(Enchantment enchantment, int level) {
        //TODO - 1.20.5: Figure out
        /*if (level == 0) {
            enchantments.removeInt(enchantment);
        } else {
            enchantments.put(enchantment, level);
        }*/
    }

    //TODO - 1.20.5: Figure out
    ItemStack container() {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack getPreviewStack() {
        return container().copy();
    }

    @Nullable
    @Override
    public <T, C> T getCapabilityFromStack(ItemCapability<T, C> capability, @UnknownNullability C context) {
        return container().getCapability(capability, context);
    }

    @Override
    public boolean isContainerOnCooldown(Player player) {
        return player.getCooldowns().isOnCooldown(container().getItem());
    }

    @Override
    public boolean isInstance(Class<?> clazz) {
        return clazz.isInstance(container().getItem());
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <MODULE extends ICustomModule<MODULE>> Module<MODULE> get(IModuleDataProvider<MODULE> typeProvider) {
        return (Module<MODULE>) typedModules.get(typeProvider.getModuleData());
    }

    @Override
    public Set<ModuleData<?>> supportedTypes() {
        return IModuleHelper.INSTANCE.getSupported(container().getItem());
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
        Module<?> module = typedModules.get(type);
        if (module != null && module.remove(toRemove)) {
            //TODO - 1.20.5: Fix this
            //typedModules.remove(type);
        }
    }

    public int addModule(IModuleDataProvider<?> typeProvider, int toInstall) {
        ModuleData<?> moduleType = typeProvider.getModuleData();
        boolean hadModule = has(moduleType);
        Module<?> module = typedModules.get(moduleType);
        if (module == null) {
            module = createNewModule(moduleType, new CompoundTag());
            //TODO - 1.20.5: Fix this
            //typedModules.put(moduleType, module);
        }
        return module.add(!hadModule, toInstall);
    }

    @Override
    public List<IHUDElement> getHUDElements(Player player) {
        if (typedModules.isEmpty()) {
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
        if (typedModules.isEmpty()) {
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
}