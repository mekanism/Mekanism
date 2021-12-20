package mekanism.api.gear;

import java.util.Objects;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.functions.FloatSupplier;
import mekanism.api.gear.config.ModuleConfigItemCreator;
import mekanism.api.math.FloatingLongSupplier;
import mekanism.api.text.IHasTextComponent;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;

/**
 * Interface used to describe and implement custom modules. Instances of this should be returned via the {@link ModuleData}.
 */
@ParametersAreNonnullByDefault
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
    default void tickServer(IModule<MODULE> module, PlayerEntity player) {
    }

    /**
     * Called each tick on the client side when installed in a MekaSuit and set to enabled.
     *
     * @param module Module instance.
     * @param player Player wearing the MekaSuit.
     */
    default void tickClient(IModule<MODULE> module, PlayerEntity player) {
    }

    /**
     * Called to collect any HUD strings that should be displayed. This will only be called if {@link ModuleData#rendersHUD()} is {@code true}.
     *
     * @param module         Module instance.
     * @param player         Player using the Meka-Tool or wearing the MekaSuit. In general this will be the client player, but is passed to make sidedness safer and
     *                       easier.
     * @param hudStringAdder Accepts and adds HUD strings.
     */
    default void addHUDStrings(IModule<MODULE> module, PlayerEntity player, Consumer<ITextComponent> hudStringAdder) {
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
    default void addHUDElements(IModule<MODULE> module, PlayerEntity player, Consumer<IHUDElement> hudElementAdder) {
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
     * Called to change the mode of the module. This will only be called if {@link ModuleData#handlesModeChange()} is {@code true}. {@link
     * IModule#displayModeChange(PlayerEntity, ITextComponent, IHasTextComponent)} is provided to help display the mode change when {@code displayChangeMessage} is {@code
     * true}.
     *
     * @param module               Module instance.
     * @param player               The player who made the mode change.
     * @param stack                The stack to change the mode of.
     * @param shift                The amount to shift the mode by, may be negative for indicating the mode should decrease.
     * @param displayChangeMessage {@code true} if a message should be displayed when the mode changes
     */
    default void changeMode(IModule<MODULE> module, PlayerEntity player, ItemStack stack, int shift, boolean displayChangeMessage) {
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
     * @return Result type or {@link ActionResultType#PASS} to pass.
     */
    @Nonnull
    default ActionResultType onItemUse(IModule<MODULE> module, ItemUseContext context) {
        return ActionResultType.PASS;
    }

    /**
     * Called when the Meka-Tool is used on an entity to allow modules to implement custom interact behavior.
     *
     * @param module Module instance.
     * @param player Player using the Meka-Tool.
     * @param entity Entity type being interacted with.
     * @param hand   Hand used.
     *
     * @return Result type or {@link ActionResultType#PASS} to pass.
     */
    @Nonnull
    default ActionResultType onInteract(IModule<MODULE> module, PlayerEntity player, LivingEntity entity, Hand hand) {
        return ActionResultType.PASS;
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
    @Nonnull
    default ModuleDispenseResult onDispense(IModule<MODULE> module, IBlockSource source) {
        return ModuleDispenseResult.DEFAULT;
    }

    //TODO - 1.18: Switch this to a record
    class ModuleDamageAbsorbInfo {

        private final FloatSupplier absorptionRatio;
        private final FloatingLongSupplier energyCost;

        /**
         * @param absorptionRatio Ratio of damage this module can absorb up to, returns a value between zero and one.
         * @param energyCost      Energy cost per point of damage reduced.
         */
        public ModuleDamageAbsorbInfo(FloatSupplier absorptionRatio, FloatingLongSupplier energyCost) {
            this.absorptionRatio = Objects.requireNonNull(absorptionRatio, "Absorption ratio supplier cannot be null");
            this.energyCost = Objects.requireNonNull(energyCost, "Energy cost supplier cannot be null");
        }

        /**
         * Gets the ratio of damage this module can absorb up to, returns a value between zero and one.
         */
        public FloatSupplier getAbsorptionRatio() {
            return absorptionRatio;
        }

        /**
         * Gets the energy cost per point of damage reduced.
         */
        public FloatingLongSupplier getEnergyCost() {
            return energyCost;
        }
    }

    /**
     * Represents the different result states of {@link ICustomModule#onDispense(IModule, IBlockSource)}.
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