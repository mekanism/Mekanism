package mekanism.api.gear;

import com.mojang.serialization.Codec;
import io.netty.handler.codec.EncoderException;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.Supplier;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.gear.config.ModuleBooleanConfig;
import mekanism.api.gear.config.ModuleConfig;
import mekanism.api.providers.IItemProvider;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
@SuppressWarnings("removal")
public class ModuleData<MODULE extends ICustomModule<MODULE>> implements mekanism.api.providers.IModuleDataProvider<MODULE> {

    private final Function<@NotNull IModule<MODULE>, @NotNull MODULE> constructor;
    private final Int2ObjectMap<ConstructedConfigData> configData;
    @Deprecated(forRemoval = true, since = "10.7.11")
    private final IItemProvider itemProvider;
    @Nullable//TODO - 1.22: Make this nonnull
    private final Holder<Item> itemHolder;
    private final int maxStackSize;
    private final int exclusive;
    private final boolean noDisable;
    @Nullable
    private String translationKey;
    @Nullable
    private String descriptionTranslationKey;

    /**
     * Creates a new module data from the given builder.
     */
    public ModuleData(ModuleDataBuilder<MODULE> builder) {
        this.constructor = builder.constructor;
        this.itemHolder = builder.itemHolder;
        this.itemProvider = builder.itemProvider;
        this.maxStackSize = builder.maxStackSize;
        this.exclusive = builder.exclusive;
        this.noDisable = builder.noDisable;
        this.configData = new Int2ObjectOpenHashMap<>(maxStackSize);
        //Handle copying the configs and ensuring the lists are immutable
        builder.ensureConfigsInitialized();
        for (ObjectIterator<Int2ObjectMap.Entry<ConfigData>> iterator = Int2ObjectMaps.fastIterator(builder.configData); iterator.hasNext(); ) {
            Int2ObjectMap.Entry<ConfigData> entry = iterator.next();
            this.configData.put(entry.getIntKey(), entry.getValue().construct());
        }
        if (this.configData.size() < maxStackSize) {
            //There are missing entries, or we don't have size based configs, just default them to being pointers to the same data
            ConstructedConfigData defaultData = this.configData.get(1);
            for (int i = 2; i <= maxStackSize; i++) {
                ConstructedConfigData sizedData = configData.get(i);
                if (sizedData == null) {
                    //If we don't already have data for that element (likely will always be the case),
                    // then set it to point to the default value, so we can more easily look it up
                    configData.put(i, defaultData);
                }
            }
        }
    }

    @NotNull
    @Override
    public final ModuleData<MODULE> getModuleData() {
        return this;
    }

    /**
     * Gets the provider for the item that this module type corresponds to and is used in the Modification Station to install this module type.
     *
     * @deprecated Use {@link #getItemHolder()} instead
     */
    @NotNull
    @Deprecated(forRemoval = true, since = "10.7.11")
    public final IItemProvider getItemProvider() {
        return itemProvider;
    }

    /**
     * Gets the holder for the item that this module type corresponds to and is used in the Modification Station to install this module type.
     *
     * @since 10.7.11
     */
    @NotNull
    public final Holder<Item> getItemHolder() {
        return itemHolder == null ? itemProvider.asItem().builtInRegistryHolder() : itemHolder;
    }

    /**
     * Gets a new instance of the custom module this data is for.
     *
     * @since 10.6.0
     */
    @NotNull
    public final MODULE create(IModule<MODULE> module) {
        return constructor.apply(module);
    }

    /**
     * Gets the max stack size for this module type. This determines how many modules of this type can be installed on any piece of gear.
     */
    public final int getMaxStackSize() {
        return maxStackSize;
    }

    /**
     * Exclusive modules only work one-at-a-time; when one is enabled, incompatible modules will be automatically disabled.
     *
     * @param mask Mask of all {@link ExclusiveFlag flags} to check exclusivity against.
     *
     * @return {@code true} if this module type is exclusive of the given flags
     *
     * @since 10.2.3
     */
    public final boolean isExclusive(int mask) {
        return (exclusive & mask) != 0;
    }

    /**
     * Gets the mask of {@link ExclusiveFlag} for this module type.
     *
     * @since 10.2.3
     */
    public final int getExclusiveFlags() {
        return exclusive;
    }

    /**
     * Retrieves the codec for (de)serializing configs on modules of this type, when there are the given number of them installed.
     *
     * @param installed Number of installed modules.
     *
     * @throws IllegalArgumentException If the number of installed modules is less than one.
     * @since 10.6.0
     */
    public final Codec<List<ModuleConfig<?>>> configCodecs(int installed) {
        return getConfigData(installed).codec();
    }

    /**
     * Retrieves the stream codec for encoding and decoding configs on modules of this type, when there are the given number of them installed.
     *
     * @param installed Number of installed modules.
     *
     * @throws IllegalArgumentException If the number of installed modules is less than one.
     * @since 10.6.0
     */
    public final StreamCodec<RegistryFriendlyByteBuf, List<ModuleConfig<?>>> configStreamCodecs(int installed) {
        return getConfigData(installed).streamCodec();
    }

    /**
     * Retrieves the default configs for modules of this type, when there are the given number of them installed.
     *
     * @param installed Number of installed modules.
     *
     * @return Default configs.
     *
     * @throws IllegalArgumentException If the number of installed modules is less than one.
     * @since 10.6.0
     */
    public final List<ModuleConfig<?>> defaultConfigs(int installed) {
        return getConfigData(installed).configs();
    }

    /**
     * Retrieves the default config that has the given name.
     *
     * @param installed Number of installed modules to lookup the default configs for.
     * @param name      Name of the module.
     *
     * @return Default config, or {@code null} if no config with the given name was found.
     *
     * @throws IllegalArgumentException If the number of installed modules is less than one.
     * @since 10.6.0
     */
    @Nullable
    public final ModuleConfig<?> getNamedConfig(int installed, ResourceLocation name) {
        for (ModuleConfig<?> config : getConfigData(installed).configs()) {
            if (config.name().equals(name)) {
                return config;
            }
        }
        return null;
    }

    /**
     * Helper to clamp the number of installed modules to within the max stack size in case it is for some reason greater.
     */
    private ConstructedConfigData getConfigData(int installed) {
        if (installed < 1) {
            throw new IllegalArgumentException("Installed number must be at least 1");
        }
        return this.configData.get(Math.min(installed, maxStackSize));
    }

    /**
     * Gets if this module type can be disabled via the Module Tweaker.
     *
     * @return {@code false} if this module type can be disabled.
     */
    public final boolean isNoDisable() {
        return noDisable;
    }

    @Override
    public String getTranslationKey() {
        if (translationKey == null) {
            translationKey = Util.makeDescriptionId("module", MekanismAPI.MODULE_REGISTRY.getKeyOrNull(this));
        }
        return translationKey;
    }

    /**
     * Gets the translation key for the description of this module type.
     */
    public String getDescriptionTranslationKey() {
        if (descriptionTranslationKey == null) {
            descriptionTranslationKey = Util.makeDescriptionId("description", MekanismAPI.MODULE_REGISTRY.getKeyOrNull(this));
        }
        return descriptionTranslationKey;
    }

    @Override
    public final String toString() {
        return Util.getRegisteredName(MekanismAPI.MODULE_REGISTRY, this);
    }

    private record ConstructedConfigData(List<ModuleConfig<?>> configs, Codec<List<ModuleConfig<?>>> codec,
                                         StreamCodec<RegistryFriendlyByteBuf, List<ModuleConfig<?>>> streamCodec) {

        private static ConstructedConfigData create(List<ModuleConfig<?>> configs, List<Codec<? extends ModuleConfig<?>>> codecs,
              List<StreamCodec<? super RegistryFriendlyByteBuf, ? extends ModuleConfig<?>>> streamCodecs) {
            return new ConstructedConfigData(configs, new ModuleConfigListCodec(codecs, configs), new StreamCodec<>() {
                @Override
                public List<ModuleConfig<?>> decode(RegistryFriendlyByteBuf buffer) {
                    List<ModuleConfig<?>> configs = new ArrayList<>(streamCodecs.size());
                    for (StreamCodec<? super RegistryFriendlyByteBuf, ? extends ModuleConfig<?>> streamCodec : streamCodecs) {
                        configs.add(streamCodec.decode(buffer));
                    }
                    return configs;
                }

                @Override
                @SuppressWarnings("unchecked")
                public void encode(RegistryFriendlyByteBuf buffer, List<ModuleConfig<?>> configs) {
                    int size = streamCodecs.size();
                    if (configs.size() != size) {
                        throw new EncoderException("Number of configs to encode does not match the number of stream codecs we have");
                    }
                    for (int i = 0; i < size; i++) {
                        StreamCodec<? super RegistryFriendlyByteBuf, ModuleConfig<?>> streamCodec =
                              (StreamCodec<? super RegistryFriendlyByteBuf, ModuleConfig<?>>) streamCodecs.get(i);
                        streamCodec.encode(buffer, configs.get(i));
                    }
                }
            });
        }
    }

    private record ConfigData(List<ModuleConfig<?>> configs, List<Codec<? extends ModuleConfig<?>>> codecs,
                              List<StreamCodec<? super RegistryFriendlyByteBuf, ? extends ModuleConfig<?>>> streamCodecs) {

        private ConstructedConfigData construct() {
            Set<ResourceLocation> uniqueNames = new HashSet<>(configs.size());
            for (ModuleConfig<?> config : configs) {
                if (!uniqueNames.add(config.name())) {
                    throw new IllegalStateException("Duplicate module config name " + config.name());
                }
            }
            return ConstructedConfigData.create(List.copyOf(configs), List.copyOf(codecs), List.copyOf(streamCodecs));
        }

        private ConfigData copy() {
            return new ConfigData(new ArrayList<>(configs), new ArrayList<>(codecs), new ArrayList<>(streamCodecs));
        }
    }

    /**
     * Builder for setting various values of {@link ModuleData}.
     */
    public static class ModuleDataBuilder<MODULE extends ICustomModule<MODULE>> {

        private static final ModuleConfig<Boolean> ENABLED_BY_DEFAULT = ModuleBooleanConfig.create(ModuleConfig.ENABLED_KEY, true);
        private static final ModuleConfig<Boolean> DISABLED_BY_DEFAULT = ModuleBooleanConfig.create(ModuleConfig.ENABLED_KEY, false);
        private static final ModuleBooleanConfig HANDLES_MODE_CHANGE_ENABLED = ModuleBooleanConfig.create(ModuleConfig.HANDLES_MODE_CHANGE_KEY, true);
        private static final ModuleBooleanConfig HANDLES_MODE_CHANGE_DISABLED = ModuleBooleanConfig.create(ModuleConfig.HANDLES_MODE_CHANGE_KEY, false);
        private static final ModuleBooleanConfig RENDER_HUD = ModuleBooleanConfig.create(ModuleConfig.RENDER_HUD_KEY, true);

        private record MarkerModule() implements ICustomModule<MarkerModule> {
        }

        private static final MarkerModule MARKER_MODULE = new MarkerModule();
        private static final Function<IModule<MarkerModule>, MarkerModule> MARKER_MODULE_SUPPLIER = module -> MARKER_MODULE;

        /**
         * Helper creator for creating a module that has no special implementation details and is only used mainly as a marker for if it is installed and how many are
         * installed.
         *
         * @param itemProvider Provider for the item that this module corresponds to and is used in the Modification Station to install this module.
         *
         * @deprecated Use {@link #marker(Holder)} instead
         */
        @SuppressWarnings({"rawtypes", "unchecked"})
        @Deprecated(forRemoval = true, since = "10.7.11")
        public static ModuleDataBuilder<?> marker(IItemProvider itemProvider) {
            //Note: We don't use customInstanced, so that we have the same instance between all our marker modules
            return new ModuleDataBuilder(MARKER_MODULE_SUPPLIER, itemProvider, true);
        }

        /**
         * Helper creator for creating a module that has no special implementation details and is only used mainly as a marker for if it is installed and how many are
         * installed.
         *
         * @param item Holder for the item that this module corresponds to and is used in the Modification Station to install this module.
         *
         * @since 10.7.11
         */
        @SuppressWarnings({"rawtypes", "unchecked"})
        public static ModuleDataBuilder<?> marker(Holder<Item> item) {
            //Note: We don't use customInstanced, so that we have the same instance between all our marker modules
            return new ModuleDataBuilder(MARKER_MODULE_SUPPLIER, item, true);
        }

        /**
         * Helper creator for creating a custom module. The given module supports no custom config options, and the returned instance should be immutable, and will be
         * re-used for every instance of this module.
         *
         * @param customModule Constructor/factory for the custom module this data is for.
         * @param itemProvider Provider for the item that this module corresponds to and is used in the Modification Station to install this module.
         *
         * @since 10.6.0
         *
         * @deprecated Use {@link #customInstanced(Supplier, Holder)} instead
         */
        @Deprecated(forRemoval = true, since = "10.7.11")
        public static <MODULE extends ICustomModule<MODULE>> ModuleDataBuilder<MODULE> customInstanced(Supplier<@NotNull MODULE> customModule, IItemProvider itemProvider) {
            MODULE customModuleInstance = customModule.get();
            Function<IModule<MODULE>, MODULE> function = module -> customModuleInstance;
            return new ModuleDataBuilder<>(function, itemProvider, true);
        }

        /**
         * Helper creator for creating a custom module. The given module supports no custom config options, and the returned instance should be immutable, and will be
         * re-used for every instance of this module.
         *
         * @param customModule Constructor/factory for the custom module this data is for.
         * @param item         Holder for the item that this module corresponds to and is used in the Modification Station to install this module.
         *
         * @since 10.7.11
         */
        public static <MODULE extends ICustomModule<MODULE>> ModuleDataBuilder<MODULE> customInstanced(Supplier<@NotNull MODULE> customModule, Holder<Item> item) {
            MODULE customModuleInstance = customModule.get();
            Function<IModule<MODULE>, MODULE> function = module -> customModuleInstance;
            return new ModuleDataBuilder<>(function, item, true);
        }

        /**
         * Helper creator for creating a custom module. The given module constructor should return an immutable instance for the custom module that is used to store any
         * custom config options. It is safe to retrieve and locally store the config values in this instance, as the constructor will be called again if any config
         * values change. If the module does not use any config values besides the builtin three (enabled, handles mode change, render hud), it is safe to always return
         * the same module instance.
         *
         * @param customModule Constructor/factory for the custom module this data is for.
         * @param itemProvider Provider for the item that this module corresponds to and is used in the Modification Station to install this module.
         *
         * @since 10.6.0
         *
         * @deprecated Use {@link #custom(Function, Holder)} instead
         */
        @Deprecated(forRemoval = true, since = "10.7.11")
        public static <MODULE extends ICustomModule<MODULE>> ModuleDataBuilder<MODULE> custom(Function<IModule<MODULE>, @NotNull MODULE> customModule,
              IItemProvider itemProvider) {
            return new ModuleDataBuilder<>(customModule, itemProvider, false);
        }

        /**
         * Helper creator for creating a custom module. The given module constructor should return an immutable instance for the custom module that is used to store any
         * custom config options. It is safe to retrieve and locally store the config values in this instance, as the constructor will be called again if any config
         * values change. If the module does not use any config values besides the builtin three (enabled, handles mode change, render hud), it is safe to always return
         * the same module instance.
         *
         * @param customModule Constructor/factory for the custom module this data is for.
         * @param item         Holder for the item that this module corresponds to and is used in the Modification Station to install this module.
         *
         * @since 10.7.11
         */
        public static <MODULE extends ICustomModule<MODULE>> ModuleDataBuilder<MODULE> custom(Function<IModule<MODULE>, @NotNull MODULE> customModule, Holder<Item> item) {
            return new ModuleDataBuilder<>(customModule, item, false);
        }

        private final Int2ObjectMap<ConfigData> configData = new Int2ObjectOpenHashMap<>();
        private final Function<@NotNull IModule<MODULE>, @NotNull MODULE> constructor;
        private final IItemProvider itemProvider;
        @Nullable//TODO - 1.22: Make this nonnull
        private final Holder<Item> itemHolder;
        private final boolean isInstanced;
        private int maxStackSize = 1;
        private int exclusive;
        private boolean handlesModeChange;
        private boolean modeChangeDisabledByDefault;
        private boolean rendersHUD;
        private boolean noDisable;
        private boolean disabledByDefault;

        private ModuleDataBuilder(Function<@NotNull IModule<MODULE>, @NotNull MODULE> constructor, Holder<Item> item, boolean isInstanced) {
            this.constructor = Objects.requireNonNull(constructor, "Custom module constructor cannot be null.");
            this.itemHolder = Objects.requireNonNull(item, "Item holder cannot be null.");
            this.itemProvider = this.itemHolder::value;
            this.isInstanced = isInstanced;
        }

        private ModuleDataBuilder(Function<@NotNull IModule<MODULE>, @NotNull MODULE> constructor, IItemProvider itemProvider, boolean isInstanced) {
            this.constructor = Objects.requireNonNull(constructor, "Custom module constructor cannot be null.");
            this.itemProvider = Objects.requireNonNull(itemProvider, "Item provider cannot be null.");
            this.itemHolder = null;
            this.isInstanced = isInstanced;
        }

        /**
         * Sets the max stack size for this module type. This determines how many modules of this type can be installed on any piece of gear.
         *
         * @param maxStackSize Max stack size.
         */
        public ModuleDataBuilder<MODULE> maxStackSize(int maxStackSize) {
            if (maxStackSize <= 0) {
                throw new IllegalArgumentException("Max stack size must be at least one.");
            } else if (!configData.isEmpty()) {
                throw new IllegalStateException("Max stack size should be set before adding any configs.");
            }
            this.maxStackSize = maxStackSize;
            return this;
        }

        /**
         * Marks this module type as exclusive. Exclusive modules only work one-at-a-time; when one is enabled, incompatible modules will be automatically disabled.
         *
         * @param mask {@link ExclusiveFlag} mask
         *
         * @since 10.2.3
         */
        public ModuleDataBuilder<MODULE> exclusive(int mask) {
            exclusive = mask;
            return this;
        }

        /**
         * Marks this module type as exclusive. Exclusive modules only work one-at-a-time; when one is enabled, incompatible modules will be automatically disabled.
         *
         * @param flags {@link ExclusiveFlag} flags for the exclusive mask
         *
         * @since 10.2.3
         */
        public ModuleDataBuilder<MODULE> exclusive(ExclusiveFlag... flags) {
            return exclusive(flags.length == 0 ? ExclusiveFlag.ANY : ExclusiveFlag.getCompoundMask(flags));
        }

        /**
         * Marks this module type as being able to handle mode changes. In addition to using this method
         * {@link ICustomModule#changeMode(IModule, Player, IModuleContainer, ItemStack, int, boolean)} should be implemented.
         */
        public ModuleDataBuilder<MODULE> handlesModeChange() {
            if (!configData.isEmpty()) {
                throw new IllegalStateException("Mode change behavior must be set before adding any configs.");
            }
            handlesModeChange = true;
            return this;
        }

        /**
         * Marks this module type as having mode change disabled by default. Requires {@link #handlesModeChange()} to be set first.
         *
         * @since 10.3.6
         */
        public ModuleDataBuilder<MODULE> modeChangeDisabledByDefault() {
            if (!handlesModeChange) {
                throw new IllegalStateException("Cannot have a module type that has mode change disabled by default but doesn't support changing modes.");
            } else if (!configData.isEmpty()) {
                throw new IllegalStateException("Mode change being disabled by default must be done before adding any configs.");
            }
            modeChangeDisabledByDefault = true;
            return this;
        }

        private void ensureConfigsInitialized() {
            if (configData.isEmpty()) {
                List<StreamCodec<? super RegistryFriendlyByteBuf, ? extends ModuleConfig<?>>> streamCodecs = new ArrayList<>();
                List<Codec<? extends ModuleConfig<?>>> codecs = new ArrayList<>();
                List<ModuleConfig<?>> configs = new ArrayList<>();
                configs.add(disabledByDefault ? DISABLED_BY_DEFAULT : ENABLED_BY_DEFAULT);
                codecs.add(ModuleBooleanConfig.CODEC);
                streamCodecs.add(ModuleBooleanConfig.STREAM_CODEC);
                if (handlesModeChange) {
                    configs.add(modeChangeDisabledByDefault ? HANDLES_MODE_CHANGE_DISABLED : HANDLES_MODE_CHANGE_ENABLED);
                    codecs.add(ModuleBooleanConfig.CODEC);
                    streamCodecs.add(ModuleBooleanConfig.STREAM_CODEC);
                }
                if (rendersHUD) {
                    configs.add(RENDER_HUD);
                    codecs.add(ModuleBooleanConfig.CODEC);
                    streamCodecs.add(ModuleBooleanConfig.STREAM_CODEC);
                }
                configData.put(1, new ConfigData(configs, codecs, streamCodecs));
            }
        }

        /**
         * Helper to add a boolean based module config option that is always present.
         *
         * @param defaultConfig Default value for the config option.
         *
         * @throws IllegalStateException if this module type is instanced based.
         * @since 10.6.0
         */
        public ModuleDataBuilder<MODULE> addConfig(ModuleBooleanConfig defaultConfig) {
            return addConfig(defaultConfig, ModuleBooleanConfig.CODEC, ModuleBooleanConfig.STREAM_CODEC);
        }

        /**
         * Adds a module config option that is always present.
         *
         * @param defaultConfig Default value for the config option.
         * @param codec         Codec for (de)serializing the config.
         * @param streamCodec   Stream codec for encoding and decoding the config over the network.
         * @param <TYPE>        Type of the config object that is being stored.
         * @param <CONFIG>      Config object type.
         *
         * @throws IllegalStateException if this module type is instanced based.
         * @since 10.6.0
         */
        public <TYPE, CONFIG extends ModuleConfig<TYPE>> ModuleDataBuilder<MODULE> addConfig(CONFIG defaultConfig, Codec<CONFIG> codec,
              StreamCodec<? super RegistryFriendlyByteBuf, CONFIG> streamCodec) {
            if (isInstanced) {
                throw new IllegalStateException("Custom configs are not supported for instance based modules");
            }
            ensureConfigsInitialized();
            for (ConfigData data : configData.values()) {
                //Add the config to all installed counts
                data.configs().add(defaultConfig);
                data.codecs().add(codec);
                data.streamCodecs().add(streamCodec);
            }
            return this;
        }

        /**
         * Adds a module config option that is dependent on the number of modules installed (mostly useful for {@link mekanism.api.gear.config.ModuleEnumConfig}).
         *
         * @param defaultConfig Int function that provides the default value for the config option when the given number of modules are installed.
         * @param codec         Int function that provides the codec for (de)serializing the config when the given number of modules are installed.
         * @param streamCodec   Int function that provides the stream codec for encoding and decoding the config over the network when the given number of modules are
         *                      installed.
         * @param <TYPE>        Type of the config object that is being stored.
         * @param <CONFIG>      Config object type.
         *
         * @throws IllegalStateException if this module type is instanced based, or if the max stack size is one.
         * @since 10.6.0
         */
        public <TYPE, CONFIG extends ModuleConfig<TYPE>> ModuleDataBuilder<MODULE> addInstalledCountConfig(IntFunction<CONFIG> defaultConfig,
              IntFunction<Codec<CONFIG>> codec, IntFunction<StreamCodec<? super RegistryFriendlyByteBuf, CONFIG>> streamCodec) {
            return addInstalledCountConfig(installed -> true, defaultConfig, codec, streamCodec);
        }

        /**
         * Helper to add a module config option that is dependent on the number of modules installed (mostly useful for
         * {@link mekanism.api.gear.config.ModuleEnumConfig}), but that is only added when the max number of modules is installed.
         *
         * @param defaultConfig Int function that provides the default value for the config option when the given number of modules are installed.
         * @param codec         Int function that provides the codec for (de)serializing the config when the given number of modules are installed.
         * @param streamCodec   Int function that provides the stream codec for encoding and decoding the config over the network when the given number of modules are
         *                      installed.
         * @param <TYPE>        Type of the config object that is being stored.
         * @param <CONFIG>      Config object type.
         *
         * @throws IllegalStateException if this module type is instanced based, or if the max stack size is one.
         * @since 10.6.0
         */
        public <TYPE, CONFIG extends ModuleConfig<TYPE>> ModuleDataBuilder<MODULE> addMaxInstalledConfig(IntFunction<CONFIG> defaultConfig,
              IntFunction<Codec<CONFIG>> codec, IntFunction<StreamCodec<? super RegistryFriendlyByteBuf, CONFIG>> streamCodec) {
            return addInstalledCountConfig(installed -> installed == maxStackSize, defaultConfig, codec, streamCodec);
        }

        /**
         * Adds a module config option that is dependent on the number of modules installed (mostly useful for {@link mekanism.api.gear.config.ModuleEnumConfig}). The
         * config option is only added if the given int predicate is met.
         *
         * @param shouldAdd     Predicate that determines whether the config should be added for the given install count.
         * @param defaultConfig Int function that provides the default value for the config option when the given number of modules are installed.
         * @param codec         Int function that provides the codec for (de)serializing the config when the given number of modules are installed.
         * @param streamCodec   Int function that provides the stream codec for encoding and decoding the config over the network when the given number of modules are
         *                      installed.
         * @param <TYPE>        Type of the config object that is being stored.
         * @param <CONFIG>      Config object type.
         *
         * @throws IllegalStateException if this module type is instanced based, or if the max stack size is one.
         * @since 10.6.0
         */
        public <TYPE, CONFIG extends ModuleConfig<TYPE>> ModuleDataBuilder<MODULE> addInstalledCountConfig(IntPredicate shouldAdd,
              IntFunction<CONFIG> defaultConfig, IntFunction<Codec<CONFIG>> codec, IntFunction<StreamCodec<? super RegistryFriendlyByteBuf, CONFIG>> streamCodec) {
            if (isInstanced) {
                throw new IllegalStateException("Custom configs are not supported for instance based modules");
            } else if (maxStackSize == 1) {
                throw new IllegalStateException("Cannot add an config that is based on the number of installed modules when the max amount is one");
            }
            ensureConfigsInitialized();
            ConfigData data = configData.get(1);
            for (int i = 2; i <= maxStackSize; i++) {
                ConfigData sizedData = configData.get(i);
                if (sizedData == null) {
                    //If we don't already have data for that element, then copy the one that ignores size
                    // Note: We can assume that there are none that require it to be size of one as otherwise
                    // we would have already copied it over when setting that one up, and we wouldn't need to copy it now
                    configData.put(i, sizedData = data.copy());
                }
                if (shouldAdd.test(i)) {
                    sizedData.codecs().add(codec.apply(i));
                    sizedData.configs().add(defaultConfig.apply(i));
                    sizedData.streamCodecs().add(streamCodec.apply(i));
                }
            }
            //Add the codec and config for the unsized data at the values they should be
            if (shouldAdd.test(1)) {
                data.codecs().add(codec.apply(1));
                data.configs().add(defaultConfig.apply(1));
                data.streamCodecs().add(streamCodec.apply(1));
            }
            return this;
        }

        /**
         * Marks this module type as having HUD elements to render. In addition to using this method
         * {@link ICustomModule#addHUDElements(IModule, IModuleContainer, ItemStack, Player, Consumer)} or
         * {@link ICustomModule#addHUDStrings(IModule, IModuleContainer, ItemStack, Player, Consumer)} should be implemented.
         */
        public ModuleDataBuilder<MODULE> rendersHUD() {
            if (!configData.isEmpty()) {
                throw new IllegalStateException("Whether the module renders a hud must be done before adding any configs.");
            }
            rendersHUD = true;
            return this;
        }

        /**
         * Marks this module type as not being able to be disabled via the Module Tweaker.
         *
         * @apiNote Cannot be used in conjunction with {@link #disabledByDefault()}.
         */
        public ModuleDataBuilder<MODULE> noDisable() {
            if (disabledByDefault) {
                throw new IllegalStateException("Cannot have a module type that is unable to be disabled and also disabled by default.");
            }
            noDisable = true;
            return this;
        }

        /**
         * Marks this module type as disabled by default in the Module Tweaker.
         *
         * @apiNote Cannot be used in conjunction with {@link #noDisable()}.
         */
        public ModuleDataBuilder<MODULE> disabledByDefault() {
            if (noDisable) {
                throw new IllegalStateException("Cannot have a module type that is unable to be disabled and also disabled by default.");
            } else if (!configData.isEmpty()) {
                throw new IllegalStateException("Being disabled by default must be done before adding any configs.");
            }
            disabledByDefault = true;
            return this;
        }
    }

    /**
     * Enum of flags for module exclusivity channels
     *
     * @since 10.2.3
     */
    public enum ExclusiveFlag {
        /**
         * This flag indicates that this module uses interaction without a target
         */
        INTERACT_EMPTY,
        /**
         * This flag indicates that this module uses interaction with an entity
         */
        INTERACT_ENTITY,
        /**
         * This flag indicates that this module uses interaction with a block
         */
        INTERACT_BLOCK,
        /**
         * This flag indicates that this module changes what pressing jump does
         */
        OVERRIDE_JUMP,
        /**
         * This flag indicates that this module changes what blocks drop
         */
        OVERRIDE_DROPS;

        /**
         * Gets the mask for this flag
         */
        public int getMask() {
            return 1 << ordinal();
        }

        /**
         * Helper to get the mask of the combination of the given input flags
         *
         * @param flags {@link ExclusiveFlag Flags} to combine into a mask.
         *
         * @return Mask representing all the given {@link ExclusiveFlag flags}.
         */
        public static int getCompoundMask(ExclusiveFlag... flags) {
            return Arrays.stream(flags).mapToInt(ExclusiveFlag::getMask).reduce(NONE, (result, mask) -> result | mask);
        }

        /**
         * The mask for no flags
         */
        public static final int NONE = 0;

        /**
         * The mask for the combination of all flags
         */
        public static final int ANY = -1;

        /**
         * The mask for the combination of all interact flags
         */
        public static final int INTERACT_ANY = getCompoundMask(INTERACT_EMPTY, INTERACT_ENTITY, INTERACT_BLOCK);
    }
}