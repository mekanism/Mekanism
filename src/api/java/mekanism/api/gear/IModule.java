package mekanism.api.gear;

import java.util.function.IntSupplier;
import mekanism.api.gear.config.ModuleConfig;
import mekanism.api.text.IHasTextComponent;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

/// Interface that describes various methods that modules have.
///
/// @apiNote This interface should not be directly implemented as it is mostly used to expose various parts of a module to the implemented [ICustomModule].
public interface IModule<MODULE extends ICustomModule<MODULE>> {

    /// Gets the data/module type of this module instance.
    @SuppressWarnings("unchecked")
    default ModuleData<MODULE> getData() {
        return (ModuleData<MODULE>) getUntypedData();
    }

    /// Gets the data/module type of this module instance.
    ///
    /// @since 10.7.11
    ModuleData<?> getUntypedData();

    /// Gets the holder for the data/module type of this module instance.
    ///
    /// @since 10.7.11
    Holder<ModuleData<?>> getDataHolder();

    /// Gets the config with the given name.
    ///
    /// @param name Name of the config to look up.
    ///
    /// @return Immutable config instance, or `null` if no config was found with the given name.
    ///
    /// @since 10.6.0
    @Nullable
    <TYPE> ModuleConfig<TYPE> getConfig(Identifier name);

    /// Gets the value of a boolean config with the given name.
    ///
    /// @param name Name of the config to look up.
    ///
    /// @return The value of the stored config, or `false` if the config does not exist.
    ///
    /// @since 10.6.0
    default boolean getBooleanConfigOrFalse(Identifier name) {
        ModuleConfig<Boolean> config = getConfig(name);
        return config != null && config.get();
    }

    /// {@return the config with the given name}
    ///
    /// @param name Name of the config to look up.
    ///
    /// @throws IllegalArgumentException If this module does not contain a config with the given name.
    /// @since 10.6.0
    default <TYPE> ModuleConfig<TYPE> getConfigOrThrow(Identifier name) {
        ModuleConfig<TYPE> config = getConfig(name);
        if (config == null) {
            throw new IllegalArgumentException("Expected module to contain a config with name " + name);
        }
        return config;
    }

    /// Gets the custom module implementation this module references.
    MODULE getCustomInstance();

    /// Gets the number of installed modules of this type.
    int getInstalledCount();

    /// Gets if this module is currently enabled in the Module Tweaker.
    ///
    /// @return `true` if this module is enabled.
    boolean isEnabled();

    /// Gets if this module type ([#getData()]) can currently handle mode changes and if this module is configured to handle mode changes in the Module Tweaker.
    ///
    /// @return `true` if this module can handle mode changes.
    boolean handlesModeChange();

    /// Gets if this module type ([#getData()]) can currently handle radial mode changes.
    ///
    /// @return `true` if this module can handle radial mode changes.
    ///
    /// @since 10.3.2
    boolean handlesRadialModeChange();

    /// Gets if this module type ([#getData()]) can currently handle mode changes, either via radial means or other.
    ///
    /// @return `true` if this module can handle mode changes.
    ///
    /// @since 10.5.0
    boolean handlesAnyModeChange();

    /// Helper to display the mode change message.
    ///
    /// @param player   Player to send the message to.
    /// @param modeName Name of the mode being changed.
    /// @param mode     New mode value.
    void displayModeChange(Player player, Component modeName, IHasTextComponent mode);

    /// Helper to toggle the enabled state of this module and send a message saying the given module was enabled or disabled.
    ///
    /// @param itemAccess  The item access representing the item the module is installed on.
    /// @param player      Player to send the message to.
    /// @param modeName    Text to display that was either enabled or disabled.
    /// @param transaction The transaction that this operation is part of. May be `null`
    void toggleEnabled(ItemAccess itemAccess, Player player, Component modeName, @Nullable TransactionContext transaction);

    /// Helper to replace the given config for this module.
    ///
    /// @param provider    Holder lookup provider so that we can look up enchantments if applicable.
    /// @param itemAccess  The item access representing the item the module is installed on.
    /// @param transaction The transaction that this operation is part of. May be `null`
    /// @param config      Config to replace.
    ///
    /// @throws IllegalStateException If this module isn't actually installed on the item access, or there is no config with the same name found installed on this module.
    /// @since 10.8.0
    void replaceModuleConfig(HolderLookup.Provider provider, ItemAccess itemAccess, @Nullable TransactionContext transaction, ModuleConfig<?> config);

    /// Helper to get the energy handler of the item this module is installed on.
    ///
    /// @param itemAccess             The item access representing the item this module is installed on.
    /// @param bypassExtractionLimits `true` to bypass any extraction limits the energy container on the module holder might have.
    ///
    /// @return Energy handler or `null` if something failed.
    ///
    /// @since 10.8.0
    @Nullable
    EnergyHandler getEnergyHandler(ItemAccess itemAccess, boolean bypassExtractionLimits);

    /// Helper to check if there is at least a certain amount of energy stored in [#getEnergyHandler(ItemAccess, boolean)].
    ///
    /// @param itemAccess     The item access representing the item this module is installed on.
    /// @param energySupplier Supplier that provides the minimum amount of required energy to check.
    ///
    /// @return `true` if there is no energy cost or there is at least that amount of energy stored in the [#getEnergyHandler(ItemAccess, boolean)].
    ///
    /// @see #hasEnoughEnergy(LivingEntity, ItemAccess, int, TransactionContext) For validating it has enough and can be extracted, while just returning true for players in creative.
    /// @since 10.4.0
    default boolean hasEnoughEnergy(ItemAccess itemAccess, IntSupplier energySupplier) {
        return hasEnoughEnergy(itemAccess, energySupplier.getAsInt());
    }

    /// Helper to check if there is at least a certain amount of energy stored in [#getEnergyHandler(ItemAccess, boolean)].
    ///
    /// @param itemAccess The item access representing the item this module is installed on.
    /// @param energy     Minimum amount of required energy to check.
    ///
    /// @return `true` if there is no energy cost or there is at least that amount of energy stored in the [#getEnergyHandler(ItemAccess, boolean)].
    ///
    /// @since 10.4.0
    boolean hasEnoughEnergy(ItemAccess itemAccess, int energy);

    /// Helper to check if there is at least a certain amount of energy stored and usable in [#getEnergyHandler(ItemAccess, boolean)].
    ///
    /// @param wearer      Wearer/User of the item the module is installed on.
    /// @param itemAccess  The item access representing the item this module is installed on.
    /// @param energy      Amount of energy to try and extract.
    /// @param transaction The transaction that this operation is part of. May be `null`
    ///
    /// @return `true` if there is no energy cost, the player is in creative, or the given amount of energy could be extracted from the [#getEnergyHandler(ItemAccess,
    /// boolean)].
    ///
    /// @see #hasEnoughEnergy(LivingEntity, ItemAccess, int, TransactionContext, boolean)
    /// @since 10.8.0
    default boolean hasEnoughEnergy(@Nullable LivingEntity wearer, ItemAccess itemAccess, int energy, @Nullable TransactionContext transaction) {
        return hasEnoughEnergy(wearer, itemAccess, energy, transaction, true);
    }

    /// Helper to check if there is at least a certain amount of energy stored and usable in [#getEnergyHandler(ItemAccess, boolean)].
    ///
    /// @param wearer       Wearer/User of the item the module is installed on.
    /// @param itemAccess   The item access representing the item this module is installed on.
    /// @param energy       Amount of energy to try and extract.
    /// @param transaction  The transaction that this operation is part of. May be `null`
    /// @param freeCreative `true` to not use any energy from the item if the wearer is in creative.
    ///
    /// @return `true` if there is no energy cost, the player is in creative (and `freeCreative` is `true`), or the given amount of energy could be extracted from the
    /// [#getEnergyHandler(ItemAccess, boolean)].
    ///
    /// @see #hasEnoughEnergy(LivingEntity, ItemAccess, int, TransactionContext)
    /// @since 10.8.0
    boolean hasEnoughEnergy(@Nullable LivingEntity wearer, ItemAccess itemAccess, int energy, @Nullable TransactionContext transaction, boolean freeCreative);

    /// Helper to calculate what a rate should be limited to based on the module's energy usage.
    ///
    /// @param wearer      Wearer/User of the item the module is installed on.
    /// @param itemAccess  The item access representing the item this module is installed on.
    /// @param energyUsage Energy usage.
    /// @param rate        Current rate without factoring energy into account.
    /// @param transaction The transaction that this operation is part of. May be `null`
    ///
    /// @return The rate after factoring energy into account, or `rate` if the wearer is in creative.
    ///
    /// @see #getEnergyRateLimit(LivingEntity, ItemAccess, int, int, TransactionContext, boolean)
    /// @since 10.8.0
    default int getEnergyRateLimit(@Nullable LivingEntity wearer, ItemAccess itemAccess, int energyUsage, int rate, @Nullable TransactionContext transaction) {
        return getEnergyRateLimit(wearer, itemAccess, energyUsage, rate, transaction, true);
    }

    /// Helper to calculate what a rate should be limited to based on the module's energy usage.
    ///
    /// @param wearer       Wearer/User of the item the module is installed on.
    /// @param itemAccess   The item access representing the item this module is installed on.
    /// @param energyUsage  Energy usage.
    /// @param rate         Current rate without factoring energy into account.
    /// @param transaction  The transaction that this operation is part of. May be `null`
    /// @param freeCreative `true` to not use any energy from the item if the wearer is in creative.
    ///
    /// @return The rate after factoring energy into account, or `rate` if the wearer is in creative and `freeCreative` is `true`.
    ///
    /// @see #getEnergyRateLimit(LivingEntity, ItemAccess, int, int, TransactionContext)
    /// @since 10.8.0
    int getEnergyRateLimit(@Nullable LivingEntity wearer, ItemAccess itemAccess, int energyUsage, int rate, @Nullable TransactionContext transaction, boolean freeCreative);

    /// Helper to use an exact amount of energy from the item this module is installed on.
    ///
    /// @param wearer      Wearer/User of the item the module is installed on.
    /// @param itemAccess  The item access representing the item this module is installed on.
    /// @param energy      Energy to use.
    /// @param transaction The transaction that this operation is part of. May be `null`
    ///
    /// @return `true` if the specified amount of energy could be used, or if the wearer is in creative.
    ///
    /// @see #useAllEnergy(LivingEntity, ItemAccess, int, TransactionContext, boolean)
    /// @since 10.8.0
    default boolean useAllEnergy(@Nullable LivingEntity wearer, ItemAccess itemAccess, int energy, @Nullable TransactionContext transaction) {
        return useAllEnergy(wearer, itemAccess, energy, transaction, true);
    }

    /// Helper to use an exact amount of energy from the item this module is installed on.
    ///
    /// @param wearer       Wearer/User of the item the module is installed on.
    /// @param itemAccess   The item access representing the item this module is installed on.
    /// @param energy       Energy to use.
    /// @param transaction  The transaction that this operation is part of. May be `null`
    /// @param freeCreative `true` to not use any energy from the item if the wearer is in creative.
    ///
    /// @return `true` if the specified amount of energy could be used, or if the wearer is in creative and `freeCreative` is `true`.
    ///
    /// @see #useAllEnergy(LivingEntity, ItemAccess, int, TransactionContext)
    /// @since 10.8.0
    boolean useAllEnergy(@Nullable LivingEntity wearer, ItemAccess itemAccess, int energy, @Nullable TransactionContext transaction, boolean freeCreative);

    /// Helper to use up to the given amount of energy from the item this module is installed on.
    ///
    /// @param wearer      Wearer/User of the item the module is installed on.
    /// @param itemAccess  The item access representing the item this module is installed on.
    /// @param energy      Energy to use.
    /// @param transaction The transaction that this operation is part of. May be `null`
    ///
    /// @return the amount of energy that was used, or the given `energy` if the player is in creative.
    ///
    /// @see #usePossibleEnergy(LivingEntity, ItemAccess, int, TransactionContext, boolean)
    /// @since 10.8.0
    default int usePossibleEnergy(@Nullable LivingEntity wearer, ItemAccess itemAccess, int energy, @Nullable TransactionContext transaction) {
        return usePossibleEnergy(wearer, itemAccess, energy, transaction, true);
    }

    /// Helper to use up to the given amount of energy from the item this module is installed on.
    ///
    /// @param wearer       Wearer/User of the item the module is installed on.
    /// @param itemAccess   The item access representing the item this module is installed on.
    /// @param energy       Energy to use.
    /// @param transaction  The transaction that this operation is part of. May be `null`
    /// @param freeCreative `true` to not use any energy from the item if the wearer is in creative.
    ///
    /// @return the amount of energy that was used, or the given `energy` if the player is in creative and `freeCreative` is `true`
    ///
    /// @see #usePossibleEnergy(LivingEntity, ItemAccess, int, TransactionContext)
    /// @since 10.8.0
    int usePossibleEnergy(@Nullable LivingEntity wearer, ItemAccess itemAccess, int energy, @Nullable TransactionContext transaction, boolean freeCreative);
}