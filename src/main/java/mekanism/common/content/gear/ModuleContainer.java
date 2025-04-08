package mekanism.common.content.gear;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.SequencedCollection;
import java.util.SequencedMap;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.gear.EnchantmentAwareModule;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IHUDElement;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleContainer;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.gear.ModuleData;
import mekanism.api.gear.config.ModuleConfig;
import mekanism.common.lib.codec.SequencedCollectionCodec;
import mekanism.common.lib.collection.EmptySequencedMap;
import mekanism.common.registries.MekanismDataComponents;
import net.minecraft.core.Holder;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

@NothingNullByDefault
public record ModuleContainer(SequencedMap<ModuleData<?>, Module<?>> typedModules, ItemEnchantments enchantments) implements IModuleContainer {

    public static final ModuleContainer EMPTY = new ModuleContainer(EmptySequencedMap.emptyMap(), ItemEnchantments.EMPTY);

    public static final Codec<ModuleContainer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
          new SequencedCollectionCodec<>(Module.CODEC).fieldOf(SerializationConstants.MODULES).forGetter(container -> container.typedModules().sequencedValues()),
          ItemEnchantments.CODEC.fieldOf(SerializationConstants.ENCHANTMENTS).forGetter(ModuleContainer::enchantments)
    ).apply(instance, ModuleContainer::create));
    public static final StreamCodec<RegistryFriendlyByteBuf, ModuleContainer> STREAM_CODEC = StreamCodec.composite(
          Module.STREAM_CODEC.apply(streamCodec -> ByteBufCodecs.collection(ArrayList::new, streamCodec)), container -> container.typedModules().sequencedValues(),
          ItemEnchantments.STREAM_CODEC, ModuleContainer::enchantments,
          ModuleContainer::create
    );

    private static ModuleContainer create(SequencedCollection<Module<?>> modules, ItemEnchantments enchantments) {
        SequencedMap<ModuleData<?>, Module<?>> typedModules = new LinkedHashMap<>(modules.size());
        for (Module<?> module : modules) {
            typedModules.put(module.getUntypedData(), module);
        }
        return new ModuleContainer(typedModules, enchantments);
    }

    public ModuleContainer {
        //Make the map unmodifiable to ensure we don't accidentally mutate it
        typedModules = Collections.unmodifiableSequencedMap(typedModules);
    }

    @Override
    public Collection<Module<?>> modules() {
        return typedModules().values();
    }

    @Override
    public ItemEnchantments moduleBasedEnchantments() {
        return enchantments;
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <MODULE extends ICustomModule<MODULE>> Module<MODULE> getUnchecked(Holder<ModuleData<?>> type) {
        return (Module<MODULE>) get(type);
    }

    @Nullable
    @Override
    public Module<?> get(Holder<ModuleData<?>> type) {
        return typedModules.get(type.value());
    }

    @Nullable
    public Module<?> getRaw(ModuleData<?> type) {
        return typedModules.get(type);
    }

    @Override
    public List<IHUDElement> getHUDElements(Player player, ItemStack stack) {
        if (typedModules.isEmpty()) {
            return Collections.emptyList();
        }
        List<IHUDElement> ret = new ArrayList<>();
        for (Module<?> module : modules()) {
            module.addHUDElements(player, this, stack, ret);
        }
        return ret;
    }

    @Override
    public List<Component> getHUDStrings(Player player, ItemStack stack) {
        if (typedModules.isEmpty()) {
            return Collections.emptyList();
        }
        List<Component> ret = new ArrayList<>();
        for (Module<?> module : modules()) {
            module.addHUDStrings(player, this, stack, ret);
        }
        return ret;
    }

    @Override
    public ModuleContainer replaceModuleConfig(HolderLookup.Provider provider, ItemStack stack, Holder<ModuleData<?>> type, ModuleConfig<?> config) {
        return replaceModuleConfig(provider, stack, type, config, false);
    }

    /**
     * Helper to replace the given config for the installed module of the given type.
     *
     * @param stack  The stack the container is stored on.
     * @param type   Module type to replace the config for.
     * @param config Config to replace.
     *
     * @return New immutable module container with the config using the replaced value.
     *
     * @throws IllegalStateException    If no module of the given type is installed, or there is no config with the same name is not found installed on the module of the
     *                                  given type.
     * @throws IllegalArgumentException If fromPacket is true, and the config does not represent a value that is valid for the module.
     */
    public ModuleContainer replaceModuleConfig(HolderLookup.Provider provider, ItemStack stack, Holder<ModuleData<?>> type, ModuleConfig<?> config, boolean fromPacket) {
        Module<?> module = get(type);
        if (module == null) {
            throw new IllegalArgumentException("Module container does not contain any modules of type " + type);
        }
        if (config.name().equals(ModuleConfig.ENABLED_KEY)) {
            if (module.isEnabled() == (boolean) config.get()) {
                return this;//State matches no change needed
            }
            //Toggle the enabled state including any side effects changing that config may have
            return toggleEnabled(provider, stack, type, module);
        } else if (config.name().equals(ModuleConfig.HANDLES_MODE_CHANGE_KEY)) {
            if (module.handlesModeChangeRaw() == (boolean) config.get()) {
                return this;//State matches no change needed
            } else if (fromPacket && module.getConfig(ModuleConfig.HANDLES_MODE_CHANGE_KEY) == null) {
                //Illegal state, got a packet for mode change key, but it doesn't support mode changes
                return this;
            }
            //Toggle the handle mode state including any side effects changing that config may have
            return toggleHandlesModeChange(stack, type.value(), module);
        }

        Module<?> replacedModule = module.withReplacedConfig(config, fromPacket);
        if (module == replacedModule) {
            //If nothing actually changed we don't need to bother updating the instance on the stack
            return this;
        }
        SequencedMap<ModuleData<?>, Module<?>> copiedModules = new LinkedHashMap<>(typedModules);
        copiedModules.put(type.value(), replacedModule);
        return updateContainer(stack, copiedModules, null);
    }

    ModuleContainer toggleEnabled(HolderLookup.Provider provider, ItemStack stack, Holder<ModuleData<?>> type) {
        Module<?> module = get(type);
        if (module == null) {
            throw new IllegalArgumentException("Module container does not contain any modules of type " + type);
        }
        return toggleEnabled(provider, stack, type, module);
    }

    private ModuleContainer toggleEnabled(HolderLookup.Provider provider, ItemStack stack, Holder<ModuleData<?>> type, Module<?> module) {
        boolean setEnabled = !module.isEnabled();
        module = module.withReplacedConfig(module.<Boolean>getConfigOrThrow(ModuleConfig.ENABLED_KEY).with(setEnabled));

        ItemEnchantments.Mutable adjustedEnchantments = updateEnchantment(provider, module, null);
        SequencedMap<ModuleData<?>, Module<?>> copiedModules = new LinkedHashMap<>(typedModules);
        copiedModules.put(type.value(), module);

        //If we are becoming enabled, and we handle mode change or have some exclusivity flags
        // then we will need to recheck other installed modules
        if (setEnabled) {
            adjustedEnchantments = disableOtherExclusives(provider, type, module, copiedModules, adjustedEnchantments);
        }

        return updateContainer(stack, copiedModules, adjustedEnchantments);
    }

    @Nullable
    private ItemEnchantments.Mutable disableOtherExclusives(HolderLookup.Provider provider, Holder<ModuleData<?>> type, Module<?> module,
          SequencedMap<ModuleData<?>, Module<?>> copiedModules, @Nullable ItemEnchantments.Mutable adjustedEnchantments) {
        boolean handlesModeChange = module.handlesModeChange();
        ModuleData<?> moduleType = type.value();
        int exclusiveFlags = moduleType.getExclusiveFlags();
        if (handlesModeChange || exclusiveFlags != 0) {
            for (Module<?> otherModule : modules()) {
                ModuleData<?> otherType = otherModule.getUntypedData();
                if (otherType != moduleType) {
                    // disable other exclusive modules if this is an exclusive module, as this one will now be active
                    if (otherType.isExclusive(exclusiveFlags) && otherModule.isEnabled()) {
                        ModuleConfig<Boolean> disabledConfig = otherModule.<Boolean>getConfigOrThrow(ModuleConfig.ENABLED_KEY).with(false);
                        //Update the other module
                        otherModule = otherModule.withReplacedConfig(disabledConfig);
                        copiedModules.put(otherType, otherModule);
                        //And adjust the current enchantments if necessary
                        adjustedEnchantments = updateEnchantment(provider, otherModule, adjustedEnchantments);
                    }
                    //TODO - 1.21: Figure out if we need to check against the original other module before it is disabled
                    // which is what previously happened or if checking it here is fine
                    // Given handlesModeChange takes the enabled state into account that means previously this would always be true
                    // if handlesModeChange && otherType.handlesModeChange
                    // Now it is true if handlesModeChange && otherType.handlesModeChange && otherModule.customInstance.canChangeModeWhenDisabled
                    if (handlesModeChange && otherModule.handlesModeChange()) {
                        ModuleConfig<Boolean> modeChangeConfig = otherModule.<Boolean>getConfigOrThrow(ModuleConfig.HANDLES_MODE_CHANGE_KEY).with(false);
                        //Update the other module
                        otherModule = otherModule.withReplacedConfig(modeChangeConfig);
                        copiedModules.put(otherType, otherModule);
                    }
                }
            }
        }
        return adjustedEnchantments;
    }

    @Nullable
    private ItemEnchantments.Mutable updateEnchantment(HolderLookup.Provider provider, Module<?> module, @Nullable ItemEnchantments.Mutable adjustedEnchantments) {
        if (module.getCustomInstance() instanceof EnchantmentAwareModule<?> enchantmentBased) {
            Optional<Reference<Enchantment>> enchantment = provider.holder(enchantmentBased.enchantment());
            int level = getEnchantmentLevel(module);
            if (enchantment.isPresent() && enchantments.getLevel(enchantment.get()) != level) {
                if (adjustedEnchantments == null) {
                    adjustedEnchantments = new ItemEnchantments.Mutable(enchantments);
                }
                adjustedEnchantments.set(enchantment.get(), level);
            }
        }
        return adjustedEnchantments;
    }

    @SuppressWarnings("unchecked")
    private static <MODULE extends EnchantmentAwareModule<MODULE>> int getEnchantmentLevel(Module<?> module) {
        Module<MODULE> enchantBased = (Module<MODULE>) module;
        return enchantBased.getCustomInstance().getLevelFor(enchantBased);
    }

    private <MODULE extends ICustomModule<MODULE>> ModuleContainer toggleHandlesModeChange(ItemStack stack, ModuleData<?> type, Module<MODULE> module) {
        boolean setHandles = !module.handlesModeChange();
        module = module.withReplacedConfig(module.<Boolean>getConfigOrThrow(ModuleConfig.HANDLES_MODE_CHANGE_KEY).with(setHandles));

        SequencedMap<ModuleData<?>, Module<?>> copiedModules = new LinkedHashMap<>(typedModules);
        copiedModules.put(type, module);

        //If we are becoming enabled, and we handle mode change then we need to force disable it for other installed modules
        if (setHandles && module.handlesModeChange()) {
            for (Module<?> otherModule : modules()) {
                ModuleData<?> otherType = otherModule.getUntypedData();
                //If it is a different module, and it handles mode change then we want to disable it handling mode changes
                //TODO - 1.21: Validate this functionality compared to how 1.20.4 worked. Mainly what was the behavior when enabling a module
                // that had its mode handling set to false because of this
                if (otherType != type && otherModule.handlesModeChange()) {
                    ModuleConfig<Boolean> modeChangeConfig = otherModule.<Boolean>getConfigOrThrow(ModuleConfig.HANDLES_MODE_CHANGE_KEY).with(false);
                    copiedModules.put(otherType, otherModule.withReplacedConfig(modeChangeConfig));
                }
            }
        }

        return updateContainer(stack, copiedModules, null);
    }

    public int installedCount(ModuleData<?> type) {
        Module<?> module = typedModules.get(type);
        return module == null ? 0 : module.getInstalledCount();
    }

    public boolean canInstall(ItemStack stack, Holder<ModuleData<?>> type) {
        if (IModuleHelper.INSTANCE.supports(stack.getItemHolder(), type)) {
            IModule<?> module = get(type);
            return module == null || module.getInstalledCount() < type.value().getMaxStackSize();
        }
        return false;
    }

    /**
     * @param toInstall Number of modules to try and install.
     *
     * @return number installed
     */
    public <MODULE extends ICustomModule<MODULE>> int addModule(HolderLookup.Provider provider, ItemStack stack, Holder<ModuleData<?>> typeProvider, int toInstall) {
        ModuleData<?> type = typeProvider.value();
        Module<MODULE> module = getUnchecked(typeProvider);
        boolean wasFirst = module == null;
        if (wasFirst) {
            toInstall = Math.min(toInstall, type.getMaxStackSize());
            module = new Module<>(typeProvider, toInstall);
        } else {
            //Clamp based on how many modules we have room to add
            toInstall = Math.min(toInstall, type.getMaxStackSize() - module.getInstalledCount());
            if (toInstall == 0) {
                //Nothing to actually install because we are already at the max stack size
                return 0;
            }
            module = module.withReplacedInstallCount(module.getInstalledCount() + toInstall);
        }
        //Add the module to the list of tracked and known modules if necessary or replace the existing value
        SequencedMap<ModuleData<?>, Module<?>> copiedModules = new LinkedHashMap<>(typedModules);
        copiedModules.put(type, module);
        //Update what the enchantment level is at after the installation
        ItemEnchantments.Mutable adjustedEnchantments = updateEnchantment(provider, module, null);
        //Disable any other modules that are exclusive in regard to the newly installed module
        adjustedEnchantments = disableOtherExclusives(provider, typeProvider, module, copiedModules, adjustedEnchantments);

        ModuleContainer replacedContainer = updateContainer(stack, copiedModules, adjustedEnchantments);
        //Call the added method on the new module instance with the new container
        module.getCustomInstance().onAdded(module, replacedContainer, stack, wasFirst);
        return toInstall;
    }

    public <MODULE extends ICustomModule<MODULE>> void removeModule(HolderLookup.Provider provider, ItemStack stack, Holder<ModuleData<?>> typeProvider,
          @Range(from = 1, to = Integer.MAX_VALUE) int toRemove) {
        ModuleData<?> type = typeProvider.value();
        Module<MODULE> module = getUnchecked(typeProvider);
        if (module != null) {
            //Theoretically we are only calling this within the max stack size, but double check
            toRemove = Math.min(toRemove, type.getMaxStackSize());
            int installed = module.getInstalledCount() - toRemove;
            boolean wasLast = installed == 0;

            SequencedMap<ModuleData<?>, Module<?>> copiedModules = new LinkedHashMap<>(typedModules);
            ItemEnchantments.Mutable adjustedEnchantments = null;
            if (wasLast) {
                //Remove the module
                copiedModules.remove(type);
                //Remove any corresponding enchantment
                if (module.getCustomInstance() instanceof EnchantmentAwareModule<?> enchantmentBased) {
                    Optional<Reference<Enchantment>> enchantment = provider.holder(enchantmentBased.enchantment());
                    if (enchantment.isPresent() && enchantments.getLevel(enchantment.get()) != 0) {
                        adjustedEnchantments = new ItemEnchantments.Mutable(enchantments);
                        adjustedEnchantments.set(enchantment.get(), 0);
                    }
                }
            } else {//update the module with the new installed count
                module = module.withReplacedInstallCount(installed);
                copiedModules.put(type, module);
                //Update the level of any corresponding enchantment
                adjustedEnchantments = updateEnchantment(provider, module, null);
            }
            ModuleContainer replacedContainer = updateContainer(stack, copiedModules, adjustedEnchantments);
            module.getCustomInstance().onRemoved(module, replacedContainer, stack, wasLast);
        }
    }

    private ModuleContainer updateContainer(ItemStack stack, SequencedMap<ModuleData<?>, Module<?>> copiedModules, @Nullable ItemEnchantments.Mutable adjustedEnchantments) {
        ModuleContainer replacedContainer = new ModuleContainer(copiedModules, adjustedEnchantments == null ? enchantments : adjustedEnchantments.toImmutable());
        stack.set(MekanismDataComponents.MODULE_CONTAINER, replacedContainer);
        return replacedContainer;
    }
}