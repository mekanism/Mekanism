package mekanism.api.gear;

import java.util.Objects;
import java.util.function.Consumer;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.functions.FloatSupplier;
import mekanism.api.gear.config.ModuleConfigItemCreator;
import mekanism.api.math.FloatingLongSupplier;
import mekanism.api.radial.RadialData;
import mekanism.api.radial.mode.IRadialMode;
import mekanism.api.radial.mode.NestedRadialMode;
import mekanism.api.text.IHasTextComponent;
import net.minecraft.core.BlockSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraftforge.common.ToolAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Interface used to describe and implement custom modules. Instances of this should be returned via the {@link ModuleData}.
 */
@NothingNullByDefault
public interface ICustomModule<MODULE extends ICustomModule<MODULE>> {

    /**
     * Called when initializing a new module instance and the backing custom module. This can be used to create module config items that will show up in the ModuleTweaker
     * and can be used to control various settings of this module.
     *
     * @param module            Module instance.
     * @param configItemCreator Helper to create module config items.
     */
    default void init(IModule<MODULE> module, ModuleConfigItemCreator configItemCreator) {
    }

    /**
     * Called each tick on the server side when installed in a MekaSuit and set to enabled.
     *
     * @param module Module instance.
     * @param player Player wearing the MekaSuit.
     */
    default void tickServer(IModule<MODULE> module, Player player) {
    }

    /**
     * Called each tick on the client side when installed in a MekaSuit and set to enabled.
     *
     * @param module Module instance.
     * @param player Player wearing the MekaSuit.
     */
    default void tickClient(IModule<MODULE> module, Player player) {
    }

    /**
     * Called to collect any HUD strings that should be displayed. This will only be called if {@link ModuleData#rendersHUD()} is {@code true}.
     *
     * @param module         Module instance.
     * @param player         Player using the Meka-Tool or wearing the MekaSuit. In general this will be the client player, but is passed to make sidedness safer and
     *                       easier.
     * @param hudStringAdder Accepts and adds HUD strings.
     */
    default void addHUDStrings(IModule<MODULE> module, Player player, Consumer<Component> hudStringAdder) {
    }

    /**
     * Called to collect any HUD elements that should be displayed when the MekaSuit is rendering the HUD. This will only be called if {@link ModuleData#rendersHUD()} is
     * {@code true}.
     *
     * @param module          Module instance.
     * @param player          Player using the Meka-Tool or wearing the MekaSuit. In general this will be the client player, but is passed to make sidedness safer and
     *                        easier.
     * @param hudElementAdder Accepts and adds HUD elements.
     *
     * @apiNote See {@link IModuleHelper} for various helpers to create HUD elements.
     */
    default void addHUDElements(IModule<MODULE> module, Player player, Consumer<IHUDElement> hudElementAdder) {
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
     * Called to get the text component to display when the mode is changed via the scroll wheel.  This will only be called if {@link ModuleData#handlesModeChange()} is
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
     * Called to change the mode of the module. This will only be called if {@link ModuleData#handlesModeChange()} is {@code true}.
     * {@link IModule#displayModeChange(Player, Component, IHasTextComponent)} is provided to help display the mode change when {@code displayChangeMessage} is
     * {@code true}.
     *
     * @param module               Module instance.
     * @param player               The player who made the mode change.
     * @param stack                The stack to change the mode of.
     * @param shift                The amount to shift the mode by, may be negative for indicating the mode should decrease.
     * @param displayChangeMessage {@code true} if a message should be displayed when the mode changes
     *
     * @see #canChangeModeWhenDisabled(IModule)
     */
    default void changeMode(IModule<MODULE> module, Player player, ItemStack stack, int shift, boolean displayChangeMessage) {
    }

    /**
     * Called by the Meka-Tool to attempt to add all supported radial types of the module. This will only be called if {@link ModuleData#handlesModeChange()} is
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
     * Called by the Meka-Tool to attempt to get the mode of the module for the given radial data. This will only be called if {@link ModuleData#handlesModeChange()} is
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
     * Called by the Meka-Tool to attempt to set the mode of the module for the given radial data. This will only be called if {@link ModuleData#handlesModeChange()} is
     * {@code true}, but may be called when this module does not support or handle the given radial type, so the radial type should be validated.
     *
     * @param module     Module instance.
     * @param player     The player who is attempting to set the mode.
     * @param stack      The stack to set the mode of.
     * @param radialData Radial data of the mode being set.
     * @param mode       Mode to attempt to set if this module can handle modes of this type.
     * @param <MODE>     Radial Mode.
     *
     * @return {@code true} if this module was able to handle the given radial data.
     *
     * @see #canChangeRadialModeWhenDisabled(IModule)
     * @since 10.3.2
     */
    default <MODE extends IRadialMode> boolean setMode(IModule<MODULE> module, Player player, ItemStack stack, RadialData<MODE> radialData, MODE mode) {
        return false;
    }

    /**
     * Called when this module is added to an item.
     *
     * @param module Module instance.
     * @param first  {@code true} if it is the first module of this type installed.
     */
    default void onAdded(IModule<MODULE> module, boolean first) {
    }

    /**
     * Called when this module is removed from an item.
     *
     * @param module Module instance.
     * @param last   {@code true} if it was the last module of this type installed.
     */
    default void onRemoved(IModule<MODULE> module, boolean last) {
    }

    /**
     * Called when the enabled state of this module changes.
     *
     * @param module Module instance.
     */
    default void onEnabledStateChange(IModule<MODULE> module) {
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
     * Called to check if this module allows the Meka-Tool to perform a specific {@link ToolAction}.
     *
     * @param module Module instance.
     * @param action Tool action to check.
     */
    default boolean canPerformAction(IModule<MODULE> module, ToolAction action) {
        return false;
    }

    /**
     * Called when the Meka-Tool is used on an entity to allow modules to implement custom interact behavior.
     *
     * @param module Module instance.
     * @param player Player using the Meka-Tool.
     * @param entity Entity type being interacted with.
     * @param hand   Hand used.
     *
     * @return Result type or {@link InteractionResult#PASS} to pass.
     */
    default InteractionResult onInteract(IModule<MODULE> module, Player player, LivingEntity entity, InteractionHand hand) {
        return InteractionResult.PASS;
    }

    /**
     * Called on enabled modules when the Meka-Tool or MekaSuit is "dispensed" from a dispenser. The MekaSuit will prioritize performing the vanilla armor dispense
     * behavior of equipping on entities before checking if any of the modules have a custom behavior.
     *
     * @param module Module instance.
     * @param source Dispenser source information.
     *
     * @return The {@link ModuleDispenseResult} defining how this dispenser should behave.
     */
    default ModuleDispenseResult onDispense(IModule<MODULE> module, BlockSource source) {
        return ModuleDispenseResult.DEFAULT;
    }

    /**
     * @param absorptionRatio Ratio of damage this module can absorb up to, returns a value between zero and one.
     * @param energyCost      Energy cost per point of damage reduced.
     */
    record ModuleDamageAbsorbInfo(@NotNull FloatSupplier absorptionRatio, @NotNull FloatingLongSupplier energyCost) {

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
     * Represents the different result states of {@link ICustomModule#onDispense(IModule, BlockSource)}.
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