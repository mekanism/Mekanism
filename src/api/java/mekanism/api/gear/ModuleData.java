package mekanism.api.gear;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.gear.config.ModuleConfigItemCreator;
import mekanism.api.providers.IItemProvider;
import mekanism.api.providers.IModuleDataProvider;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.common.util.NonNullSupplier;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class ModuleData<MODULE extends ICustomModule<MODULE>> implements IModuleDataProvider<MODULE> {

    private final NonNullSupplier<MODULE> supplier;
    private final IItemProvider itemProvider;
    private final int maxStackSize;
    private final Rarity rarity;
    private final int exclusive;
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
     */
    @NotNull
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
        IForgeRegistry<ModuleData<?>> registry = MekanismAPI.moduleRegistry();
        return registry == null ? null : registry.getKey(this);
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
         */
        public static <MODULE extends ICustomModule<MODULE>> ModuleDataBuilder<MODULE> custom(NonNullSupplier<MODULE> customModule, IItemProvider itemProvider) {
            return new ModuleDataBuilder<>(customModule, itemProvider);
        }

        private final NonNullSupplier<MODULE> supplier;
        private final IItemProvider itemProvider;
        private Rarity rarity = Rarity.COMMON;
        private int maxStackSize = 1;
        private int exclusive;
        private boolean handlesModeChange;
        private boolean rendersHUD;
        private boolean noDisable;
        private boolean disabledByDefault;

        private ModuleDataBuilder(NonNullSupplier<MODULE> supplier, IItemProvider itemProvider) {
            this.supplier = Objects.requireNonNull(supplier, "Supplier cannot be null.");
            this.itemProvider = Objects.requireNonNull(itemProvider, "Item provider cannot be null.");
        }

        /**
         * Sets the rarity of this module type.
         *
         * @param rarity Rarity of the module type.
         */
        public ModuleDataBuilder<MODULE> rarity(Rarity rarity) {
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
         * Marks this module type as exclusive. Exclusive modules only work one-at-a-time; when one is enabled, incompatible modules will be automatically disabled.
         *
         * @param mask {@link ExclusiveFlag} mask
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
         * @since 10.2.3
         */
        public ModuleDataBuilder<MODULE> exclusive(ExclusiveFlag... flags) {
            return exclusive(flags.length == 0 ? ExclusiveFlag.ANY : ExclusiveFlag.getCompoundMask(flags));
        }

        /**
         * Marks this module type as being able to handle mode changes. In addition to using this method
         * {@link ICustomModule#changeMode(IModule, Player, ItemStack, int, boolean)} should be implemented.
         */
        public ModuleDataBuilder<MODULE> handlesModeChange() {
            handlesModeChange = true;
            return this;
        }

        /**
         * Marks this module type as having HUD elements to render. In addition to using this method {@link ICustomModule#addHUDElements(IModule, Player, Consumer)} or
         * {@link ICustomModule#addHUDStrings(IModule, Player, Consumer)} should be implemented.
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

    /**
     * Enum of flags for module exclusivity channels
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