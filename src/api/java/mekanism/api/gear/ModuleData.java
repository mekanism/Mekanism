package mekanism.api.gear;

import com.mojang.serialization.Codec;
import io.netty.handler.codec.EncoderException;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.gear.config.ModuleBooleanConfig;
import mekanism.api.gear.config.ModuleConfig;
import mekanism.api.providers.IItemProvider;
import mekanism.api.providers.IModuleDataProvider;
import net.minecraft.Util;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class ModuleData<MODULE extends ICustomModule<MODULE>> implements IModuleDataProvider<MODULE> {

    private final Int2ObjectMap<ConfigData> configData = new Int2ObjectOpenHashMap<>();
    private final Function<@NotNull IModule<MODULE>, @NotNull MODULE> constructor;
    private final IItemProvider itemProvider;
    private final int maxStackSize;
    private final int exclusive;
    private final boolean handlesModeChange;
    private final boolean modeChangeDisabledByDefault;
    private final boolean rendersHUD;
    private final boolean noDisable;
    private final boolean disabledByDefault;
    @Nullable
    private String translationKey;
    @Nullable
    private String descriptionTranslationKey;

    /**
     * Creates a new module data from the given builder.
     */
    public ModuleData(ModuleDataBuilder<MODULE> builder) {
        this.constructor = builder.constructor;
        this.itemProvider = builder.itemProvider;
        this.maxStackSize = builder.maxStackSize;
        this.exclusive = builder.exclusive;
        this.handlesModeChange = builder.handlesModeChange;
        this.modeChangeDisabledByDefault = builder.modeChangeDisabledByDefault;
        this.rendersHUD = builder.rendersHUD;
        this.noDisable = builder.noDisable;
        this.disabledByDefault = builder.disabledByDefault;
        //Handle copying the configs and ensuring the lists are immutable
        builder.ensureConfigsInitialized();
        for (Int2ObjectMap.Entry<ConfigData> entry : builder.configData.int2ObjectEntrySet()) {
            this.configData.put(entry.getIntKey(), entry.getValue().toImmutable());
        }
        if (this.configData.size() < maxStackSize) {
            //There are missing entries, or we don't have size based configs, just default them to being pointers to the same data
            ConfigData defaultData = this.configData.get(1);
            for (int i = 2; i <= maxStackSize; i++) {
                ConfigData sizedData = configData.get(i);
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
     */
    @NotNull
    public final IItemProvider getItemProvider() {
        return itemProvider;
    }

    /**
     * Gets a new instance of the custom module this data is for.
     *
     * @since 10.6.0
     */
    @NotNull//TODO - 1.20.5: Adjust this slightly so that it that this is where you can grab config values if you want pointers to them
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
     * Gets if this module type is able to handle mode changes.
     *
     * @return {@code true} if this module type can handle mode changes.
     */
    public final boolean handlesModeChange() {
        return handlesModeChange;
    }

    //TODO - 1.20.5: Docs
    public final Codec<List<ModuleConfig<?>>> configCodecs(int installed) {
        //TODO - 1.20.5: Re-evaluate this cast
        return new ModuleConfigListCodec((List) this.configData.get(installed).codecs());
    }

    //TODO - 1.20.5: Docs
    public final StreamCodec<RegistryFriendlyByteBuf, List<ModuleConfig<?>>> configStreamCodecs(int installed) {
        List<StreamCodec<? super RegistryFriendlyByteBuf, ? extends ModuleConfig<?>>> streamCodecs = this.configData.get(installed).streamCodecs();
        int size = streamCodecs.size();
        return new StreamCodec<>() {
            @Override
            public List<ModuleConfig<?>> decode(RegistryFriendlyByteBuf buffer) {
                List<ModuleConfig<?>> configs = new ArrayList<>(size);
                for (StreamCodec<? super RegistryFriendlyByteBuf, ? extends ModuleConfig<?>> streamCodec : streamCodecs) {
                    configs.add(streamCodec.decode(buffer));
                }
                return configs;
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buffer, List<ModuleConfig<?>> configs) {
                if (configs.size() != size) {
                    throw new EncoderException("Number of configs to encode does not match the number of stream codecs we have");
                }
                for (int i = 0; i < size; i++) {
                    StreamCodec<? super RegistryFriendlyByteBuf, ModuleConfig<?>> streamCodec = (StreamCodec<? super RegistryFriendlyByteBuf, ModuleConfig<?>>) streamCodecs.get(i);
                    streamCodec.encode(buffer, configs.get(i));
                }
            }
        };
    }

    //TODO - 1.20.5: Docs
    public final List<ModuleConfig<?>> defaultConfigs(int installed) {
        return this.configData.get(installed).configs();
    }

    /**
     * Gets if this module type is has mode change disabled by default in the Module Tweaker.
     *
     * @return {@code true} if this module type's mode change ability is disabled by default.
     *
     * @since 10.3.6
     */
    public final boolean isModeChangeDisabledByDefault() {
        return modeChangeDisabledByDefault;
    }

    /**
     * Gets if this module type has any data that should be added to the HUD.
     *
     * @return {@code true} if this module type has data to add to the HUD.
     */
    public final boolean rendersHUD() {
        return rendersHUD;
    }

    /**
     * Gets if this module type can be disabled via the Module Tweaker.
     *
     * @return {@code false} if this module type can be disabled.
     */
    public final boolean isNoDisable() {
        return noDisable;
    }

    /**
     * Gets if this module type is disabled by default in the Module Tweaker.
     *
     * @return {@code true} if this module type is disabled by default.
     */
    public final boolean isDisabledByDefault() {
        return disabledByDefault;
    }

    @Override
    public String getTranslationKey() {
        if (translationKey == null) {
            translationKey = Util.makeDescriptionId("module", getRegistryName());
        }
        return translationKey;
    }

    /**
     * Gets the translation key for the description of this module type.
     */
    public String getDescriptionTranslationKey() {
        if (descriptionTranslationKey == null) {
            descriptionTranslationKey = Util.makeDescriptionId("description", getRegistryName());
        }
        return descriptionTranslationKey;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public final ResourceLocation getRegistryName() {
        //May be null if called before the object is registered
        return MekanismAPI.MODULE_REGISTRY.getKey(this);
    }

    private record ConfigData(List<Codec<? extends ModuleConfig<?>>> codecs, List<StreamCodec<? super RegistryFriendlyByteBuf, ? extends ModuleConfig<?>>> streamCodecs,
                              List<ModuleConfig<?>> configs) {

        private ConfigData toImmutable() {
            return new ConfigData(java.util.List.copyOf(codecs), List.copyOf(streamCodecs), List.copyOf(configs));
        }

        private ConfigData copy() {
            return new ConfigData(new ArrayList<>(codecs), new ArrayList<>(streamCodecs), new ArrayList<>(configs));
        }
    }

    /**
     * Builder for setting various values of {@link ModuleData}.
     */
    public static class ModuleDataBuilder<MODULE extends ICustomModule<MODULE>> {

        private static final ModuleConfig<Boolean> ENABLED_BY_DEFAULT = new ModuleBooleanConfig(ModuleConfig.ENABLED_KEY, true);
        private static final ModuleConfig<Boolean> DISABLED_BY_DEFAULT = new ModuleBooleanConfig(ModuleConfig.ENABLED_KEY, false);
        private static final ModuleBooleanConfig HANDLES_MODE_CHANGE_ENABLED = new ModuleBooleanConfig(ModuleConfig.HANDLES_MODE_CHANGE_KEY, true);
        private static final ModuleBooleanConfig HANDLES_MODE_CHANGE_DISABLED = new ModuleBooleanConfig(ModuleConfig.HANDLES_MODE_CHANGE_KEY, false);
        private static final ModuleBooleanConfig RENDER_HUD = new ModuleBooleanConfig(ModuleConfig.RENDER_HUD_KEY, true);

        private record MarkerModule() implements ICustomModule<MarkerModule> {
        }
        private static final MarkerModule MARKER_MODULE = new MarkerModule();
        private static final Function<IModule<MarkerModule>, MarkerModule> MARKER_MODULE_SUPPLIER = module -> MARKER_MODULE;

        /**
         * Helper creator for creating a module that has no special implementation details and is only used mainly as a marker for if it is installed and how many are
         * installed.
         *
         * @param itemProvider Provider for the item that this module corresponds to and is used in the Modification Station to install this module.
         */
        @SuppressWarnings({"rawtypes", "unchecked"})
        public static ModuleDataBuilder<?> marker(IItemProvider itemProvider) {
            return new ModuleDataBuilder(MARKER_MODULE_SUPPLIER, itemProvider);
        }

        /**
         * Helper creator for creating a custom module. The given module supplier should return a <strong>NEW</strong> instance each time it is called as any config items
         * created will be stored in the module object returned.
         *
         * @param customModule Supplier for the custom module this data is for.
         * @param itemProvider Provider for the item that this module corresponds to and is used in the Modification Station to install this module.
         *
         * @apiNote Strictly speaking a new instance of the custom module does not need to be returned if {@link ICustomModule#init(IModule, ModuleConfigItemCreator)}
         * creates no config items so there is no unique data, but it is easier to just return a new instance each time unless you are using
         * {@link #marker(IItemProvider)}.
         *
         * @since 10.6.0
         */
        //TODO - 1.20.5: Update docs and mention getting the config values in the constructor
        // We should also note that you can just resolve the actual value and store that as modules have to be immutable for data component purposes
        public static <MODULE extends ICustomModule<MODULE>> ModuleDataBuilder<MODULE> custom(@NotNull Function<IModule<MODULE>, @NotNull MODULE> customModule, IItemProvider itemProvider) {
            return new ModuleDataBuilder<>(customModule, itemProvider);
        }

        private final Int2ObjectMap<ConfigData> configData = new Int2ObjectOpenHashMap<>();
        private final Function<@NotNull IModule<MODULE>, @NotNull MODULE> constructor;
        private final IItemProvider itemProvider;
        private int maxStackSize = 1;
        private int exclusive;
        private boolean handlesModeChange;
        private boolean modeChangeDisabledByDefault;
        private boolean rendersHUD;
        private boolean noDisable;
        private boolean disabledByDefault;

        private ModuleDataBuilder(Function<@NotNull IModule<MODULE>, @NotNull MODULE> constructor, IItemProvider itemProvider) {
            this.constructor = Objects.requireNonNull(constructor, "Custom module constructor cannot be null.");
            this.itemProvider = Objects.requireNonNull(itemProvider, "Item provider cannot be null.");
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

        //TODO - 1.20.5: Pass these to the module data, also we probably want to make it so that we validate there are no duplicate keys added?
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
                configData.put(1, new ConfigData(codecs, streamCodecs, configs));
            }
        }

        //TODO - 1.20.5: Do we want to do it like this or just let each module define its own codec? with a helper for the base codec?
        public ModuleDataBuilder<MODULE> addConfig(ModuleBooleanConfig defaultConfig) {
            return addConfig(defaultConfig, ModuleBooleanConfig.CODEC, ModuleBooleanConfig.STREAM_CODEC);
        }

        //TODO - 1.20.5: Docs
        public <TYPE, CONFIG extends ModuleConfig<TYPE>> ModuleDataBuilder<MODULE> addConfig(CONFIG defaultConfig, Codec<CONFIG> codec,
              StreamCodec<? super RegistryFriendlyByteBuf, CONFIG> streamCodec) {
            ensureConfigsInitialized();
            for (ConfigData data : configData.values()) {
                //Add the config to all installed counts
                data.configs().add(defaultConfig);
                data.codecs().add(codec);
                data.streamCodecs().add(streamCodec);
            }
            return this;
        }

        //TODO - 1.20.5: Docs
        public <TYPE, CONFIG extends ModuleConfig<TYPE>> ModuleDataBuilder<MODULE> addInstalledCountConfig(IntFunction<CONFIG> defaultConfig,
              IntFunction<Codec<CONFIG>> codec, IntFunction<StreamCodec<? super RegistryFriendlyByteBuf, CONFIG>> streamCodec) {
            return addInstalledCountConfig(installed -> true, defaultConfig, codec, streamCodec);
        }

        //TODO - 1.20.5: Docs
        public <TYPE, CONFIG extends ModuleConfig<TYPE>> ModuleDataBuilder<MODULE> addMaxInstalledConfig(IntFunction<CONFIG> defaultConfig,
              IntFunction<Codec<CONFIG>> codec, IntFunction<StreamCodec<? super RegistryFriendlyByteBuf, CONFIG>> streamCodec) {
            return addInstalledCountConfig(installed -> installed == maxStackSize, defaultConfig, codec, streamCodec);
        }

        //TODO - 1.20.5: Docs
        public <TYPE, CONFIG extends ModuleConfig<TYPE>> ModuleDataBuilder<MODULE> addInstalledCountConfig(IntPredicate shouldAdd,
              IntFunction<CONFIG> defaultConfig, IntFunction<Codec<CONFIG>> codec, IntFunction<StreamCodec<? super RegistryFriendlyByteBuf, CONFIG>> streamCodec) {
            if (maxStackSize == 1) {
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
         * {@link ICustomModule#addHUDElements(IModule, IModuleContainer, ItemStack, Player, Consumer)} or {@link ICustomModule#addHUDStrings(IModule, IModuleContainer, ItemStack, Player, Consumer)} should
         * be implemented.
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