package mekanism.api.gear;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.functions.FloatSupplier;
import mekanism.api.radial.RadialData;
import mekanism.api.radial.mode.IRadialMode;
import mekanism.api.radial.mode.NestedRadialMode;
import mekanism.api.text.IHasTextComponent;
import net.minecraft.core.TypedInstance;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

/**
 * Interface used to describe and implement custom modules. Instances of this should be returned via the {@link ModuleData}.
 */
@NothingNullByDefault
public interface ICustomModule<MODULE extends ICustomModule<MODULE>> {

    /// Called each tick on the server side when installed in a MekaSuit and set to enabled.
    ///
    /// @param module      Module instance.
    /// @param itemAccess  The item access that the module is installed on. The access can be mutated, but other modules' data should not be modified here as the modules
    /// are only looked up once before calling tickServer on all the installed modules.
    /// @param player      Player wearing the MekaSuit.
    /// @param transaction The transaction that this operation is part of.
    ///
    /// @since 10.8.0
    default void tickServer(IModule<MODULE> module, ItemAccess itemAccess, Player player, TransactionContext transaction) {
    }

    /// Called each tick on the client side when installed in a MekaSuit and set to enabled.
    ///
    /// @param module      Module instance.
    /// @param itemAccess  The item access that the module is installed on.
    /// @param player      Player wearing the MekaSuit.
    /// @param transaction The transaction that this operation is part of.
    ///
    /// @since 10.8.0
    default void tickClient(IModule<MODULE> module, ItemAccess itemAccess, Player player, TransactionContext transaction) {
    }

    /// Called to collect any HUD strings that should be displayed. This will only be called if the module is configured to render to the HUD, and
    /// [mekanism.api.gear.ModuleData.ModuleDataBuilder#rendersHUD()] was called.
    ///
    /// @param module          Module instance.
    /// @param moduleContainer The container this module is part of.
    /// @param instance        The item instance this module is installed on.
    /// @param player          Player using the Meka-Tool or wearing the MekaSuit. In general this will be the client player, but is passed to make sidedness safer and
    /// easier.
    /// @param hudStringAdder  Accepts and adds HUD strings.
    default <ITEM extends TypedInstance<Item> & DataComponentGetter> void addHUDStrings(IModule<MODULE> module, IModuleContainer moduleContainer, ITEM instance,
          Player player, Consumer<Component> hudStringAdder) {
    }

    /// Called to collect any HUD elements that should be displayed when the MekaSuit is rendering the HUD. This will only be called if the module is configured to render
    /// to the HUD, and [mekanism.api.gear.ModuleData.ModuleDataBuilder#rendersHUD()] was called.
    ///
    /// @param module          Module instance.
    /// @param moduleContainer The container this module is part of.
    /// @param instance        The item instance this module is installed on.
    /// @param player          Player using the Meka-Tool or wearing the MekaSuit. In general this will be the client player, but is passed to make sidedness safer and
    /// easier.
    /// @param hudElementAdder Accepts and adds HUD elements.
    ///
    /// @apiNote See [IModuleHelper] for various helpers to create HUD elements.
    default <ITEM extends TypedInstance<Item> & DataComponentGetter> void addHUDElements(IModule<MODULE> module, IModuleContainer moduleContainer, ITEM instance,
          Player player, Consumer<IHUDElement> hudElementAdder) {
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

    /// Called to get the text component to display when the mode is changed via the scroll wheel. This will only be called if [IModule#handlesModeChange()] is `true`.
    ///
    /// @param module   Module instance.
    /// @param instance The item instance this module is installed on.
    ///
    /// @return Mode display text or `null` if no text should be displayed.
    ///
    /// @since 10.3.2
    @Nullable
    default <ITEM extends TypedInstance<Item> & DataComponentGetter> Component getModeScrollComponent(IModule<MODULE> module, ITEM instance) {
        return null;
    }

    /// Called to change the mode of the module. This will only be called if [IModule#handlesModeChange()] is `true`.
    ///
    /// [IModule#displayModeChange(Player, Component, IHasTextComponent)] is provided to help display the mode change when `displayChangeMessage` is `true`.
    ///
    /// @param module               Module instance.
    /// @param player               The player who made the mode change.
    /// @param itemAccess           The item access representing the item this module and should act upon to change the mode of.
    /// @param shift                The amount to shift the mode by, may be negative for indicating the mode should decrease.
    /// @param displayChangeMessage `true` if a message should be displayed when the mode changes
    /// @param transaction          The transaction that this operation is part of. May be `null`
    ///
    /// @see #canChangeModeWhenDisabled(IModule)
    default void changeMode(IModule<MODULE> module, Player player, ItemAccess itemAccess, int shift, boolean displayChangeMessage, @Nullable TransactionContext transaction) {
    }

    /// Called by the Meka-Tool to attempt to add all supported radial types of the module. This will only be called if [IModule#handlesModeChange()] is `true`.
    ///
    /// @param module   Module instance.
    /// @param instance The item instance to get the supported radial types of.
    /// @param adder    Consumer used to add any supported radial modes.
    ///
    /// @see #canChangeRadialModeWhenDisabled(IModule)
    /// @since 10.3.2
    default <ITEM extends TypedInstance<Item> & DataComponentGetter> void addRadialModes(IModule<MODULE> module, ITEM instance, Consumer<NestedRadialMode> adder) {
    }

    /// Called by the Meka-Tool to attempt to get the mode of the module for the given radial data. This will only be called if [IModule#handlesModeChange()] is `true`,
    /// but may be called when this module does not support or handle the given radial type, so the radial type should be validated.
    ///
    /// @param module     Module instance.
    /// @param instance   The item instance to get the mode of.
    /// @param radialData Radial data of the mode being retrieved.
    /// @param <MODE>     Radial Mode.
    ///
    /// @return Radial Mode if this module can handle the given Radial Data, or `null` if it can't.
    ///
    /// @see #canChangeRadialModeWhenDisabled(IModule)
    /// @since 10.3.2
    @Nullable
    default <ITEM extends TypedInstance<Item> & DataComponentGetter, MODE extends IRadialMode> MODE getMode(IModule<MODULE> module, ITEM instance, RadialData<MODE> radialData) {
        return null;
    }

    /// Called by the Meka-Tool to attempt to set the mode of the module for the given radial data. This will only be called if [IModule#handlesModeChange()] is `true`,
    /// but may be called when this module does not support or handle the given radial type, so the radial type should be validated.
    ///
    /// @param <MODE>      Radial Mode.
    /// @param module      Module instance.
    /// @param player      The player who is attempting to set the mode.
    /// @param itemAccess  The item access representing the item this module is installed on.
    /// @param radialData  Radial data of the mode being set.
    /// @param mode        Mode to attempt to set if this module can handle modes of this type.
    /// @param transaction The transaction that this operation is part of. May be `null`
    ///
    /// @return `true` if this module was able to handle the given radial data.
    ///
    /// @see #canChangeRadialModeWhenDisabled(IModule)
    /// @since 10.3.2
    default <MODE extends IRadialMode> boolean setMode(IModule<MODULE> module, Player player, ItemAccess itemAccess, RadialData<MODE> radialData, MODE mode,
          @Nullable TransactionContext transaction) {
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

    /// Called when this module is added to an item.
    ///
    /// Due to the way [`data components`][net.minecraft.core.component.DataComponentType] work, the instance of the [ICustomModule] that this method gets called on is as
    /// follows:
    /// - The item access has been updated to know about the updated state (including any changes to enchantments if this module is also a [EnchantmentAwareModule])
    /// - The [ICustomModule] instance is one created after installing the modules
    ///
    /// @param module      Module instance.
    /// @param itemAccess  The item access that the module being added to. The current state of the access represents the stack after addition.
    /// @param first       `true`     if it is the first module of this type installed.
    /// @param transaction The transaction that this operation is part of. If this method is called it can be assumed the passed transaction will be committed.
    ///
    /// @apiNote This method may be called when more than one module is added at once, so it is important to get the installed count from the module rather than assume it
    /// just went up by one.
    default void onAdded(IModule<MODULE> module, ItemAccess itemAccess, boolean first, TransactionContext transaction) {
    }

    /// Called when this module is removed from an item.
    ///
    /// Due to the way [`data components`][net.minecraft.core.component.DataComponentType] work, the instance of the [ICustomModule] that this method gets called on is as
    /// follows:
    /// - The item access has been updated to know about the updated state (including any changes to enchantments if this module is also a [EnchantmentAwareModule])
    /// - If this was not the `wasLast` module removed, this behaves similarly to [#onAdded(IModule, ItemAccess, boolean, TransactionContext)] in that the [ICustomModule]
    /// instance is one created after uninstalling the the modules. **However**, if all the modules of this type have been removed and `wasLast == true`, then this method
    /// is instead called on the previously installed [ICustomModule] instance, and it should be assumed that the number of installed modules is zero, rather than
    /// querying it via [IModule#getInstalledCount()]. Do note, even in this case, the item access still points to the updated state, as specified above.
    ///
    /// @param module      Module instance.
    /// @param itemAccess  The item access that the module being removed was installed on. The current state of the access represents the stack after removal.
    /// @param wasLast     `true`   if it was the last module of this type installed.
    /// @param transaction The transaction that this operation is part of. If this method is called it can be assumed the passed transaction will be committed.
    ///
    /// @apiNote This method may be called when more than one module is removed at once, so it is important to get the installed count from the module rather than assume
    /// it just down up by one.
    default void onRemoved(IModule<MODULE> module, ItemAccess itemAccess, boolean wasLast, TransactionContext transaction) {
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

    /// Called when the Meka-Tool is used to allow modules to implement custom use behavior.
    ///
    /// @param module      Module instance.
    /// @param context     Use context.
    /// @param transaction The transaction that this operation is part of.
    ///
    /// @return Result type or [InteractionResult#PASS] to pass.
    default InteractionResult onItemUse(IModule<MODULE> module, UseOnContext context, TransactionContext transaction) {
        return InteractionResult.PASS;
    }

    /// Called to check if this module allows the Meka-Tool to perform a specific [net.neoforged.neoforge.common.ItemAbility].
    ///
    /// @param module          Module instance.
    /// @param moduleContainer The container this module is part of.
    /// @param instance        The item instance this module is installed on.
    /// @param ability         Item ability to check.
    default <ITEM extends TypedInstance<Item> & DataComponentGetter> boolean canPerformAction(IModule<MODULE> module, IModuleContainer moduleContainer, ITEM instance, ItemAbility ability) {
        return false;
    }

    /// Called when the Meka-Tool is used on an entity to allow modules to implement custom interact behavior.
    ///
    /// @param module      Module instance.
    /// @param player      Player using the Meka-Tool.
    /// @param entity      Entity type being interacted with.
    /// @param hand        Hand used.
    /// @param itemAccess  The item access representing the item this module is installed on and is being used to interact with an entity.
    /// @param transaction The transaction that this operation is part of.
    ///
    /// @return Result type or [InteractionResult#PASS] to pass. If the result is [InteractionResult.Success], then the transaction will be committed.
    default InteractionResult onInteract(IModule<MODULE> module, Player player, LivingEntity entity, InteractionHand hand, ItemAccess itemAccess, TransactionContext transaction) {
        return InteractionResult.PASS;
    }

    /// Called on enabled modules when the Meka-Tool or MekaSuit is "dispensed" from a dispenser. The MekaSuit will prioritize performing the vanilla armor dispense
    /// behavior of equipping on entities before checking if any of the modules have a custom behavior.
    ///
    /// @param module      Module instance.
    /// @param itemAccess  The item access representing the item this module is installed on and is being dispensed.
    /// @param source      Dispenser source information.
    /// @param transaction The transaction that this operation is part of.
    ///
    /// @return The [ModuleDispenseResult] defining how this dispenser should behave, and if the transaction should be committed.
    default ModuleDispenseResult onDispense(IModule<MODULE> module, ItemAccess itemAccess, BlockSource source, TransactionContext transaction) {
        return ModuleDispenseResult.DEFAULT;
    }

    /// @param absorptionRatio Ratio of damage this module can absorb up to, returns a value between zero and one.
    /// @param energyCost      Energy cost per point of damage reduced.
    record ModuleDamageAbsorbInfo(FloatSupplier absorptionRatio, IntSupplier energyCost) {

        /// @param absorptionRatio Ratio of damage this module can absorb up to, returns a value between zero and one.
        /// @param energyCost      Energy cost per point of damage reduced.
        public ModuleDamageAbsorbInfo {
            Objects.requireNonNull(absorptionRatio, "Absorption ratio supplier cannot be null");
            Objects.requireNonNull(energyCost, "Energy cost supplier cannot be null");
        }
    }

    /// Represents the different result states of [ICustomModule#onDispense(IModule, ItemAccess, BlockSource, TransactionContext)].
    enum ModuleDispenseResult {
        /// Represents that the module did perform some logic and that the transaction should be committed and no further modules should be checked.
        HANDLED,
        /// Represents that the module did not preform any behavior and to continue checking other installed modules, and then dispense/drop the item.
        DEFAULT,
        /// Represents that the module did not perform any behavior and to continue checking other installed modules, but dispensing/dropping the item should be prevented
        /// so that the item can continue being used in the dispenser on future redstone interaction.
        FAIL_PREVENT_DROP
    }
}