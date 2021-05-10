package mekanism.api.gear;

import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.gear.config.ModuleConfigItemCreator;
import mekanism.api.text.IHasTextComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
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
     * @param hudStringAdder Accepts and adds HUD strings.
     */
    default void addHUDStrings(IModule<MODULE> module, Consumer<ITextComponent> hudStringAdder) {
    }

    /**
     * Called to collect any HUD elements that should be displayed when the MekaSuit is rendering the HUD. This will only be called if {@link ModuleData#rendersHUD()} is
     * {@code true}.
     *
     * @param module          Module instance.
     * @param hudElementAdder Accepts and adds HUD elements.
     *
     * @apiNote See {@link IModuleHelper} for various helpers to create HUD elements.
     */
    default void addHUDElements(IModule<MODULE> module, Consumer<IHUDElement> hudElementAdder) {
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
}