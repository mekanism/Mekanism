package mekanism.api.gear;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.functions.FloatSupplier;
import mekanism.api.radial.RadialData;
import mekanism.api.radial.mode.IRadialMode;
import mekanism.api.radial.mode.NestedRadialMode;
import mekanism.api.text.IHasTextComponent;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Interface used to describe and implement custom modules. Instances of this should be returned via the {@link ModuleData}.
 */
@NothingNullByDefault
public interface ICustomModule<MODULE extends ICustomModule<MODULE>> {

    /**
     * Called each tick on the server side when installed in a MekaSuit and set to enabled.
     *
     * @param module          Module instance.
     * @param moduleContainer The container this module is part of.
     * @param stack           The stack this module is installed on.
     * @param player          Player wearing the MekaSuit.
     */
    default void tickServer(IModule<MODULE> module, IModuleContainer moduleContainer, ItemStack stack, Player player) {
    }

    /**
     * Called each tick on the client side when installed in a MekaSuit and set to enabled.
     *
     * @param module          Module instance.
     * @param moduleContainer The container this module is part of.
     * @param stack           The stack this module is installed on.
     * @param player          Player wearing the MekaSuit.
     */
    default void tickClient(IModule<MODULE> module, IModuleContainer moduleContainer, ItemStack stack, Player player) {
    }

    /**
     * Called to collect any HUD strings that should be displayed. This will only be called if the module is configured to render to the HUD, and
     * {@link mekanism.api.gear.ModuleData.ModuleDataBuilder#rendersHUD()} was called.
     *
     * @param module          Module instance.
     * @param moduleContainer The container this module is part of.
     * @param stack           The stack this module is installed on.odule is installed on.
     * @param player          Player using the Meka-Tool or wearing the MekaSuit. In general this will be the client player, but is passed to make sidedness safer and
     *                        easier.
     * @param hudStringAdder  Accepts and adds HUD strings.
     */
    default void addHUDStrings(IModule<MODULE> module, IModuleContainer moduleContainer, ItemStack stack, Player player, Consumer<Component> hudStringAdder) {
    }

    /**
     * Called to collect any HUD elements that should be displayed when the MekaSuit is rendering the HUD. This will only be called if the module is configured to render
     * to the HUD, and {@link mekanism.api.gear.ModuleData.ModuleDataBuilder#rendersHUD()} was called.
     *
     * @param module          Module instance.
     * @param moduleContainer The container this module is part of.
     * @param stack           The stack this module is installed on.
     * @param player          Player using the Meka-Tool or wearing the MekaSuit. In general this will be the client player, but is passed to make sidedness safer and
     *                        easier.
     * @param hudElementAdder Accepts and adds HUD elements.
     *
     * @apiNote See {@link IModuleHelper} for various helpers to create HUD elements.
     */
    default void addHUDElements(IModule<MODULE> module, IModuleContainer moduleContainer, ItemStack stack, Player player, Consumer<IHUDElement> hudElementAdder) {
    }

    /**
     * Called to check if this module can change modes when disabled or if it should be skipped. This should be overridden for modules where the mode change key toggles
     * whether the module is active.
     *
     * @param module Module instance.
     *
     * @return {@code true} if this module can change modes when disabled.
     */
    default boolean canChangeModeWhenDisabled(IModule<MODULE> module) {
        return false;
    }

    /**
     * Called to check if this module has any radial modes that can be changed when disabled or if it should be skipped. This should be overridden for modules where the
     * radial menu allows toggling whether the module is active.
     *
     * @param module Module instance.
     *
     * @return {@code true} if this module has radial modes that can be changed while disabled.
     *
     * @since 10.3.2
     */
    default boolean canChangeRadialModeWhenDisabled(IModule<MODULE> module) {
        return false;
    }

    /**
     * Called to get the text component to display when the mode is changed via the scroll wheel. This will only be called if {@link IModule#handlesModeChange()} is
     * {@code true}.
     *
     * @param module Module instance.
     * @param stack  The stack to get the mode of.
     *
     * @return Mode display text or {@code null} if no text should be displayed.
     *
     * @since 10.3.2
     */
    @Nullable
    default Component getModeScrollComponent(IModule<MODULE> module, ItemStack stack) {
        return null;
    }

    /**
     * Called to change the mode of the module. This will only be called if {@link IModule#handlesModeChange()} is {@code true}.
     * {@link IModule#displayModeChange(Player, Component, IHasTextComponent)} is provided to help display the mode change when {@code displayChangeMessage} is
     * {@code true}.
     *
     * @param module               Module instance.
     * @param player               The player who made the mode change.
     * @param moduleContainer      The container this module is part of.
     * @param stack                The stack to change the mode of.
     * @param shift                The amount to shift the mode by, may be negative for indicating the mode should decrease.
     * @param displayChangeMessage {@code true} if a message should be displayed when the mode changes
     *
     * @see #canChangeModeWhenDisabled(IModule)
     */
    default void changeMode(IModule<MODULE> module, Player player, IModuleContainer moduleContainer, ItemStack stack, int shift, boolean displayChangeMessage) {
    }

    /**
     * Called by the Meka-Tool to attempt to add all supported radial types of the module. This will only be called if {@link IModule#handlesModeChange()} is
     * {@code true}.
     *
     * @param module Module instance.
     * @param stack  The stack to get the supported radial types of.
     * @param adder  Consumer used to add any supported radial modes.
     *
     * @see #canChangeRadialModeWhenDisabled(IModule)
     * @since 10.3.2
     */
    default void addRadialModes(IModule<MODULE> module, ItemStack stack, Consumer<NestedRadialMode> adder) {
    }

    /**
     * Called by the Meka-Tool to attempt to get the mode of the module for the given radial data. This will only be called if {@link IModule#handlesModeChange()} is
     * {@code true}, but may be called when this module does not support or handle the given radial type, so the radial type should be validated.
     *
     * @param module     Module instance.
     * @param stack      The stack to get the mode of.
     * @param radialData Radial data of the mode being retrieved.
     * @param <MODE>     Radial Mode.
     *
     * @return Radial Mode if this module can handle the given Radial Data, or {@code null} if it can't.
     *
     * @see #canChangeRadialModeWhenDisabled(IModule)
     * @since 10.3.2
     */
    @Nullable
    default <MODE extends IRadialMode> MODE getMode(IModule<MODULE> module, ItemStack stack, RadialData<MODE> radialData) {
        return null;
    }

    /**
     * Called by the Meka-Tool to attempt to set the mode of the module for the given radial data. This will only be called if {@link IModule#handlesModeChange()} is
     * {@code true}, but may be called when this module does not support or handle the given radial type, so the radial type should be validated.
     *
     * @param <MODE>          Radial Mode.
     * @param module          Module instance.
     * @param player          The player who is attempting to set the mode.
     * @param moduleContainer The container this module is part of.
     * @param stack           The stack this module is installed on.
     * @param radialData      Radial data of the mode being set.
     * @param mode            Mode to attempt to set if this module can handle modes of this type.
     *
     * @return {@code true} if this module was able to handle the given radial data.
     *
     * @see #canChangeRadialModeWhenDisabled(IModule)
     * @since 10.3.2
     */
    default <MODE extends IRadialMode> boolean setMode(IModule<MODULE> module, Player player, IModuleContainer moduleContainer, ItemStack stack, RadialData<MODE> radialData, MODE mode) {
        return false;
    }

    /**
     * Called when this module is enabled to modify the attributes of the item this module is installed on. (MekaSuit or Meka-Tool)
     *
     * @param module Module instance.
     * @param event  Event that provides helper to use to modify the attributes on the stack.
     *
     * @since 10.6.3
     */
    default void adjustAttributes(IModule<MODULE> module, ItemAttributeModifierEvent event) {
    }

    /**
     * Called when this module is added to an item.
     * <p>
     * Due to the way {@link net.minecraft.core.component.DataComponentType data components} work, the instance of the {@link ICustomModule} that this method gets called
     * on is as follows:
     * <ul>
     *     <li>The passed in {@link IModuleContainer module container} instance is the new one that contains the added module in its known modules</li>
     *     <li>The stack has been updated to know about the updated state (including any changes to enchantments if this module is also a {@link EnchantmentAwareModule})</li>
     *     <li>The {@link ICustomModule} instance is one created after installing the modules</li>
     * </ul>
     *
     * @param module          Module instance.
     * @param moduleContainer The container this module is part of.
     * @param stack           The stack this module is installed on.
     * @param first           {@code true} if it is the first module of this type installed.
     *
     * @apiNote This method may be called when more than one module is added at once, so it is important to get the installed count from the module rather than assume it
     * just went up by one.
     */
    default void onAdded(IModule<MODULE> module, IModuleContainer moduleContainer, ItemStack stack, boolean first) {
    }

    /**
     * Called when this module is removed from an item.
     * <p>
     * Due to the way {@link net.minecraft.core.component.DataComponentType data components} work, the instance of the {@link ICustomModule} that this method gets called
     * on is as follows:
     *
     * <ul>
     *     <li>
     *         The passed in {@link IModuleContainer module container} instance is the new one that contains the reduced number of modules (or none if last) in its known
     *         modules
     *     </li>
     *     <li>The stack has been updated to know about the updated state (including any changes to enchantments if this module is also a {@link EnchantmentAwareModule})</li>
     *     <li>
     *         If this was not the {@code wasLast} module removed, this behaves similarly to {@link #onAdded(IModule, IModuleContainer, ItemStack, boolean)} in that the
     *         {@link ICustomModule} instance is one created after uninstalling the the modules. <strong>However</strong>, if all the modules of this type have been
     *         removed and {@code wasLast == true}, then this method is instead called on the previously installed {@link ICustomModule} instance, and it should be assumed
     *         that the number of installed modules is zero, rather than querying it via {@link IModule#getInstalledCount()}. Do note, even in this case, the stack and
     *         {@link IModuleContainer module container} still point to the updated state, as specified above.
     *     </li>
     * </ul>
     *
     * @param module          Module instance.
     * @param moduleContainer The container this module is part of.
     * @param stack           The stack this module is installed on.
     * @param wasLast         {@code true} if it was the last module of this type installed.
     *
     * @apiNote This method may be called when more than one module is removed at once, so it is important to get the installed count from the module rather than assume
     * it just down up by one.
     */
    default void onRemoved(IModule<MODULE> module, IModuleContainer moduleContainer, ItemStack stack, boolean wasLast) {
    }

    /**
     * Gets information about if and how this module blocks a given type of damage.
     *
     * @param module       Module instance.
     * @param damageSource Source of the damage.
     *
     * @return Information about how damage can be absorbed, or {@code null} if the given damage type cannot be absorbed.
     */
    @Nullable
    default ModuleDamageAbsorbInfo getDamageAbsorbInfo(IModule<MODULE> module, DamageSource damageSource) {
        return null;
    }

    /**
     * Called when the Meka-Tool is used to allow modules to implement custom use behavior.
     *
     * @param module  Module instance.
     * @param context Use context.
     *
     * @return Result type or {@link InteractionResult#PASS} to pass.
     */
    default InteractionResult onItemUse(IModule<MODULE> module, UseOnContext context) {
        return InteractionResult.PASS;
    }

    /**
     * Called to check if this module allows the Meka-Tool to perform a specific {@link net.neoforged.neoforge.common.ItemAbility}.
     *
     * @param module          Module instance.
     * @param moduleContainer The container this module is part of.
     * @param stack           The stack this module is installed on.
     * @param ability         Item ability to check.
     */
    default boolean canPerformAction(IModule<MODULE> module, IModuleContainer moduleContainer, ItemStack stack, ItemAbility ability) {
        return false;
    }

    /**
     * Called when the Meka-Tool is used on an entity to allow modules to implement custom interact behavior.
     *
     * @param module          Module instance.
     * @param player          Player using the Meka-Tool.
     * @param entity          Entity type being interacted with.
     * @param hand            Hand used.
     * @param moduleContainer The container this module is part of.
     * @param stack           Stack the module is installed on and is being used to interact with an entity.
     *
     * @return Result type or {@link InteractionResult#PASS} to pass.
     */
    default InteractionResult onInteract(IModule<MODULE> module, Player player, LivingEntity entity, InteractionHand hand, IModuleContainer moduleContainer, ItemStack stack) {
        return InteractionResult.PASS;
    }

    /**
     * Called on enabled modules when the Meka-Tool or MekaSuit is "dispensed" from a dispenser. The MekaSuit will prioritize performing the vanilla armor dispense
     * behavior of equipping on entities before checking if any of the modules have a custom behavior.
     *
     * @param module          Module instance.
     * @param moduleContainer The container this module is part of.
     * @param stack           The stack this module is installed on.
     * @param source          Dispenser source information.
     *
     * @return The {@link ModuleDispenseResult} defining how this dispenser should behave.
     */
    default ModuleDispenseResult onDispense(IModule<MODULE> module, IModuleContainer moduleContainer, ItemStack stack, BlockSource source) {
        return ModuleDispenseResult.DEFAULT;
    }

    /**
     * @param absorptionRatio Ratio of damage this module can absorb up to, returns a value between zero and one.
     * @param energyCost      Energy cost per point of damage reduced.
     */
    record ModuleDamageAbsorbInfo(@NotNull FloatSupplier absorptionRatio, @NotNull LongSupplier energyCost) {

        /**
         * @param absorptionRatio Ratio of damage this module can absorb up to, returns a value between zero and one.
         * @param energyCost      Energy cost per point of damage reduced.
         */
        public ModuleDamageAbsorbInfo {
            Objects.requireNonNull(absorptionRatio, "Absorption ratio supplier cannot be null");
            Objects.requireNonNull(energyCost, "Energy cost supplier cannot be null");
        }
    }

    /**
     * Represents the different result states of {@link ICustomModule#onDispense(IModule, IModuleContainer, ItemStack, BlockSource)}.
     */
    enum ModuleDispenseResult {
        /**
         * Represents that the module did perform some logic and that no further modules should be checked.
         */
        HANDLED,
        /**
         * Represents that the module did not preform any behavior and to continue checking other installed modules, and then dispense/drop the item.
         */
        DEFAULT,
        /**
         * Represents that the module did not perform any behavior and to continue checking other installed modules, but dispensing/dropping the item should be prevented
         * so that the item can continue being used in the dispenser on future redstone interaction.
         */
        FAIL_PREVENT_DROP
    }
}