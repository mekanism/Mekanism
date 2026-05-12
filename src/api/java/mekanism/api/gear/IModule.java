package mekanism.api.gear;

import java.util.function.LongSupplier;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.gear.config.ModuleConfig;
import mekanism.api.text.IHasTextComponent;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

/**
 * Interface that describes various methods that modules have.
 *
 * @apiNote This interface should not be directly implemented as it is mostly used to expose various parts of a module to the implemented {@link ICustomModule}.
 */
@NothingNullByDefault
public interface IModule<MODULE extends ICustomModule<MODULE>> {

    /**
     * Gets the data/module type of this module instance.
     */
    @SuppressWarnings("unchecked")
    default ModuleData<MODULE> getData() {
        return (ModuleData<MODULE>) getUntypedData();
    }

    /**
     * Gets the data/module type of this module instance.
     *
     * @since 10.7.11
     */
    ModuleData<?> getUntypedData();

    /**
     * Gets the holder for the data/module type of this module instance.
     *
     * @since 10.7.11
     */
    Holder<ModuleData<?>> getDataHolder();

    /**
     * Gets the config with the given name.
     *
     * @param name Name of the config to look up.
     *
     * @return Immutable config instance, or {@code null} if no config was found with the given name.
     *
     * @since 10.6.0
     */
    @Nullable
    <TYPE> ModuleConfig<TYPE> getConfig(Identifier name);

    /**
     * Gets the value of a boolean config with the given name.
     *
     * @param name Name of the config to look up.
     *
     * @return The value of the stored config, or {@code false} if the config does not exist.
     *
     * @since 10.6.0
     */
    default boolean getBooleanConfigOrFalse(Identifier name) {
        ModuleConfig<Boolean> config = getConfig(name);
        return config != null && config.get();
    }

    /**
     * {@return the config with the given name}
     *
     * @param name Name of the config to look up.
     *
     * @throws IllegalArgumentException If this module does not contain a config with the given name.
     * @since 10.6.0
     */
    default <TYPE> ModuleConfig<TYPE> getConfigOrThrow(Identifier name) {
        ModuleConfig<TYPE> config = getConfig(name);
        if (config == null) {
            throw new IllegalArgumentException("Expected module to contain a config with name " + name);
        }
        return config;
    }

    /**
     * Gets the custom module implementation this module references.
     */
    MODULE getCustomInstance();

    /**
     * Gets the number of installed modules of this type.
     */
    int getInstalledCount();

    /**
     * Gets if this module is currently enabled in the Module Tweaker.
     *
     * @return {@code true} if this module is enabled.
     */
    boolean isEnabled();

    /**
     * Gets if this module type ({@link #getData()}) can currently handle mode changes and if this module is configured to handle mode changes in the Module Tweaker.
     *
     * @return {@code true} if this module can handle mode changes.
     */
    boolean handlesModeChange();

    /**
     * Gets if this module type ({@link #getData()}) can currently handle radial mode changes.
     *
     * @return {@code true} if this module can handle radial mode changes.
     *
     * @since 10.3.2
     */
    boolean handlesRadialModeChange();

    /**
     * Gets if this module type ({@link #getData()}) can currently handle mode changes, either via radial means or other.
     *
     * @return {@code true} if this module can handle mode changes.
     *
     * @since 10.5.0
     */
    boolean handlesAnyModeChange();

    /**
     * Helper to display the mode change message.
     *
     * @param player   Player to send the message to.
     * @param modeName Name of the mode being changed.
     * @param mode     New mode value.
     */
    void displayModeChange(Player player, Component modeName, IHasTextComponent mode);

    /**
     * Helper to toggle the enabled state of this module and send a message saying the given module was enabled or disabled.
     *
     * @param player   Player to send the message to.
     * @param modeName Text to display that was either enabled or disabled.
     */
    void toggleEnabled(IModuleContainer moduleContainer, ItemStack stack, Player player, Component modeName);

    /**
     * Helper to get the energy container of the item this module is installed on.
     *
     * @param stack The stack this module is installed on.
     *
     * @return Energy container or {@code null} if something failed.
     */
    @Nullable
    IEnergyContainer getEnergyContainer(ItemStack stack);//TODO - 26.1: Evaluate usages and probably try to remove this method

    //TODO - 26.1: Docs and re-evaluate this method
    @Nullable
    IStrictEnergyHandler getEnergyHandler(ItemStack stack);

    /**
     * Helper to check if there is at least a certain amount of energy stored in {@link #getEnergyContainer(ItemStack)}.
     *
     * @param stack          The stack this module is installed on.
     * @param energySupplier Supplier that provides the minimum amount of required energy to check.
     *
     * @return {@code true} if there is no energy cost or there is at least that amount of energy stored in the {@link #getEnergyContainer(ItemStack)}.
     *
     * @since 10.4.0
     */
    default boolean hasEnoughEnergy(ItemStack stack, LongSupplier energySupplier) {
        return hasEnoughEnergy(stack, energySupplier.getAsLong());
    }

    /**
     * Helper to check if there is at least a certain amount of energy stored in {@link #getEnergyContainer(ItemStack)}.
     *
     * @param stack  The stack this module is installed on.
     * @param energy Minimum amount of required energy to check.
     *
     * @return {@code true} if there is no energy cost or there is at least that amount of energy stored in the {@link #getEnergyContainer(ItemStack)}.
     *
     * @since 10.4.0
     */
    boolean hasEnoughEnergy(ItemStack stack, long energy);

    /**
     * Helper to use energy from the item this module is installed on.
     *
     * @param wearer Wearer/User of the item the module is installed on.
     * @param stack  The stack this module is installed on.
     * @param energy Energy to use.
     *
     * @return Actual amount of energy used.
     *
     * @implNote By default, this method does not use any energy from players that are in creative.
     */
    long useEnergy(@Nullable LivingEntity wearer, ItemStack stack, long energy, @Nullable TransactionContext transaction);//TODO - 26.1: Make energy usage transactional for modules

    /**
     * Helper to use energy from the item this module is installed on. If {@code checkCreative} is {@code false} this method will return 0 for players in creative or
     * spectator.
     *
     * @param wearer       Wearer/User of the item the module is installed on.
     * @param stack        The stack this module is installed on.
     * @param energy       Energy to use.
     * @param freeCreative {@code true} to not use any energy from the item if the wearer is in creative.
     *
     * @return Actual amount of energy used.
     */
    long useEnergy(@Nullable LivingEntity wearer, ItemStack stack, long energy, @Nullable TransactionContext transaction, boolean freeCreative);

    /**
     * Helper to use energy from the given energy container. If the {@code energyContainer} is null this will return 0. If {@code checkCreative} is {@code false} this
     * method will return 0 for players in creative or spectator.
     *
     * @param wearer          Wearer/User of the item the module is installed on.
     * @param energyContainer Energy container, most likely retrieved from {@link #getEnergyContainer(ItemStack)}.
     * @param energy          Energy to use.
     * @param freeCreative    {@code true} to not use any energy from the item if the wearer is in creative.
     *
     * @return Actual amount of energy used.
     *
     * @apiNote This method is mostly for use in not having to look up the energy container multiple times.
     */
    long useEnergy(@Nullable LivingEntity wearer, @Nullable IEnergyContainer energyContainer, long energy, @Nullable TransactionContext transaction, boolean freeCreative);

    //TODO - 26.1: Docs
    long useEnergy(@Nullable LivingEntity wearer, @Nullable IStrictEnergyHandler energyHandler, long energy, @Nullable TransactionContext transaction, boolean freeCreative);
}