package mekanism.api.gear;

import java.util.Objects;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.gear.config.ModuleConfigItemCreator;
import mekanism.api.providers.IItemProvider;
import mekanism.api.providers.IModuleDataProvider;
import mekanism.api.text.IHasTranslationKey;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.util.Util;
import net.minecraftforge.common.util.NonNullSupplier;
import net.minecraftforge.registries.ForgeRegistryEntry;

@FieldsAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ModuleData<MODULE extends ICustomModule<MODULE>> extends ForgeRegistryEntry<ModuleData<?>> implements IHasTranslationKey, IModuleDataProvider<MODULE> {

    /**
     * Helper method to get the Class "with" the generic for use in passing to {@link net.minecraftforge.registries.DeferredRegister#create(Class, String)} as just using
     * {@code ModuleData.class} doesn't "work" as it doesn't have the generic that is expected based on what is passed as the forge registry entry.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static Class<ModuleData<?>> getClassWithGeneric() {
        return (Class) ModuleData.class;
    }

    @Nullable
    @Deprecated
    private final String legacyName;
    private final NonNullSupplier<MODULE> supplier;
    private final IItemProvider itemProvider;
    private final int maxStackSize;
    private final Rarity rarity;
    private final boolean exclusive;
    private final boolean handlesModeChange;
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
        this.legacyName = builder.legacyName;
        this.supplier = builder.supplier;
        this.itemProvider = builder.itemProvider;
        this.rarity = builder.rarity;
        this.maxStackSize = builder.maxStackSize;
        this.exclusive = builder.exclusive;
        this.handlesModeChange = builder.handlesModeChange;
        this.rendersHUD = builder.rendersHUD;
        this.noDisable = builder.noDisable;
        this.disabledByDefault = builder.disabledByDefault;
    }

    @Nonnull
    @Override
    public final ModuleData<MODULE> getModuleData() {
        return this;
    }

    /**
     * Gets the legacy name for this module. This is mainly used to be able to load legacy modules from before the module system was exposed to the API and modules had a
     * slightly different naming scheme.
     *
     * @deprecated Will be removed in 1.18
     */
    @Nullable
    @Deprecated//TODO - 1.18: Remove
    public final String getLegacyName() {
        return legacyName;
    }

    /**
     * Gets the provider for the item that this module type corresponds to and is used in the Modification Station to install this module type.
     */
    @Nonnull
    public final IItemProvider getItemProvider() {
        return itemProvider;
    }

    /**
     * Gets a new instance of the custom module this data is for.
     */
    @Nonnull
    public final MODULE get() {
        return supplier.get();
    }

    /**
     * Gets the rarity of this module type.
     */
    public final Rarity getRarity() {
        return rarity;
    }

    /**
     * Gets the max stack size for this module type. This determines how many modules of this type can be installed on any piece of gear.
     */
    public final int getMaxStackSize() {
        return maxStackSize;
    }

    /**
     * Exclusive modules only work one-at-a-time; when one is enabled, others will be automatically disabled.
     *
     * @return {@code true} if this module type is exclusive.
     */
    public final boolean isExclusive() {
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

    /**
     * Builder for setting various values of {@link ModuleData}.
     */
    public static class ModuleDataBuilder<MODULE extends ICustomModule<MODULE>> {

        @SuppressWarnings("rawtypes")
        private static final ICustomModule<?> MARKER_MODULE = new ICustomModule() {
        };
        private static final NonNullSupplier<ICustomModule<?>> MARKER_MODULE_SUPPLIER = () -> MARKER_MODULE;

        /**
         * Helper creator for creating a module that has no special implementation details and is only used mainly as a marker for if it is installed and how many are
         * installed.
         *
         * @param itemProvider Provider for the item that this module corresponds to and is used in the Modification Station to install this module.
         */
        @SuppressWarnings({"rawtypes", "unchecked"})
        public static ModuleDataBuilder<?> marker(@Nonnull IItemProvider itemProvider) {
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
         * creates no config items so there is no unique data, but it is easier to just return a new instance each time unless you are using {@link
         * #marker(IItemProvider)}.
         */
        public static <MODULE extends ICustomModule<MODULE>> ModuleDataBuilder<MODULE> custom(@Nonnull NonNullSupplier<MODULE> customModule,
              @Nonnull IItemProvider itemProvider) {
            return new ModuleDataBuilder<>(customModule, itemProvider);
        }

        private final NonNullSupplier<MODULE> supplier;
        private final IItemProvider itemProvider;
        @Nullable
        @Deprecated
        private String legacyName;
        private Rarity rarity = Rarity.COMMON;
        private int maxStackSize = 1;
        private boolean exclusive;
        private boolean handlesModeChange;
        private boolean rendersHUD;
        private boolean noDisable;
        private boolean disabledByDefault;

        private ModuleDataBuilder(@Nonnull NonNullSupplier<MODULE> supplier, @Nonnull IItemProvider itemProvider) {
            this.supplier = Objects.requireNonNull(supplier, "Supplier cannot be null.");
            this.itemProvider = Objects.requireNonNull(itemProvider, "Item provider cannot be null.");
        }

        /**
         * Sets the legacy name for this module. This should probably not ever be used outside of modules built into Mekanism, as it is mainly used to be able to load
         * legacy modules from before the module system was exposed to the API and modules had a slightly different naming scheme.
         *
         * @param legacyName Legacy name of the module.
         *
         * @deprecated Will be removed in 1.18
         */
        @Deprecated//TODO - 1.18: Remove
        public ModuleDataBuilder<MODULE> legacyName(@Nonnull String legacyName) {
            this.legacyName = Objects.requireNonNull(legacyName, "Legacy name should not be null if specified.");
            return this;
        }

        /**
         * Sets the rarity of this module type.
         *
         * @param rarity Rarity of the module type.
         */
        public ModuleDataBuilder<MODULE> rarity(@Nonnull Rarity rarity) {
            this.rarity = Objects.requireNonNull(rarity, "Rarity cannot be null.");
            return this;
        }

        /**
         * Sets the max stack size for this module type. This determines how many modules of this type can be installed on any piece of gear.
         *
         * @param maxStackSize Max stack size.
         */
        public ModuleDataBuilder<MODULE> maxStackSize(int maxStackSize) {
            if (maxStackSize <= 0) {
                throw new IllegalArgumentException("Max stack size must be at least one.");
            }
            this.maxStackSize = maxStackSize;
            return this;
        }

        /**
         * Marks this module type as exclusive. Exclusive modules only work one-at-a-time; when one is enabled, others will be automatically disabled.
         */
        public ModuleDataBuilder<MODULE> exclusive() {
            exclusive = true;
            return this;
        }

        /**
         * Marks this module type as being able to handle mode changes. In addition to using this method {@link ICustomModule#changeMode(IModule, PlayerEntity, ItemStack,
         * int, boolean)} should be implemented.
         */
        public ModuleDataBuilder<MODULE> handlesModeChange() {
            handlesModeChange = true;
            return this;
        }

        /**
         * Marks this module type as having HUD elements to render. In addition to using this method {@link ICustomModule#addHUDElements(IModule, Consumer)} or {@link
         * ICustomModule#addHUDStrings(IModule, Consumer)} should be implemented.
         */
        public ModuleDataBuilder<MODULE> rendersHUD() {
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
            }
            disabledByDefault = true;
            return this;
        }
    }
}