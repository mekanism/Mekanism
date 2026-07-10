package mekanism.api.gear;

import com.mojang.datafixers.util.Either;
import java.util.function.Predicate;
import mekanism.api.MekanismAPI;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.gear.IHUDElement.HUDColor;
import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.EquipmentSlot;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

/// Helper class for interacting with and creating custom modules on the client side. Do not try to interact with this class on the server side.
///
/// @see IClientModuleHelper#INSTANCE
/// @since 10.8.0
public interface IClientModuleHelper {

    /// Provides access to Mekanism's implementation of [IClientModuleHelper].
    IClientModuleHelper INSTANCE = MekanismAPI.getService(IClientModuleHelper.class);

    /// Helper method to create a HUD element with a given icon, text, and color.
    ///
    /// @param icon  Element sprite, bound to the gui atlas.
    /// @param text  Text to display.
    /// @param color Color to render the icon and text in.
    ///
    /// @return A new HUD element.
    IHUDElement hudElement(Identifier icon, Component text, HUDColor color);

    /// Helper method to create a HUD element representing an enabled state with a given icon.
    ///
    /// @param icon          Element sprite, bound to the gui atlas.
    /// @param enabled`true` if the element should use the enabled text and color, `false` if it should use the disabled text and color.
    ///
    /// @return A new HUD element.
    IHUDElement hudElementEnabled(Identifier icon, boolean enabled);

    /// Helper method to create a HUD element representing a ratio with a given icon.
    ///
    /// @param icon  Element sprite, bound to the gui atlas.
    /// @param ratio Ratio. Values below 0.1 will display using [HUDColor#DANGER], values above 0.1 and below 0.2 will display using [HUDColor#WARNING], and values above
    /// 0.2 will display using [HUDColor#REGULAR].
    ///
    /// @return A new HUD element.
    IHUDElement hudElementPercent(Identifier icon, double ratio);

    /// Adds a file that contains overrides and models for some custom modules.
    ///
    /// @param location Asset location assumed to be for a model file that loads an obj file. The [Identifier] for the modules Mekanism adds is
    /// `mekanism:entity/mekasuit_modules`
    ///
    /// @apiNote Must only be called on the client side and from [FMLClientSetupEvent].
    void addMekaSuitModuleModels(Identifier location);

    /// Adds a model spec for a specific MekaSuit Module to allow it to render as part of the MekaSuit when installed and enabled. This method causes the "active" model
    /// to always be selected.
    ///
    /// @param name       Unique name that will be checked for in all the module model files. For third party mods it is recommended this contains your modid.
    /// @param moduleData [ModuleData] to associate this spec with.
    /// @param slotType   Equipment position the spec will be used for.
    ///
    /// @apiNote Must only be called on the client side and from [FMLClientSetupEvent].
    /// @see #addMekaSuitModuleModelSpec(String, Holder, EquipmentSlot, Predicate)
    default void addMekaSuitModuleModelSpec(String name, Holder<ModuleData<?>> moduleData, EquipmentSlot slotType) {
        addMekaSuitModuleModelSpec(name, moduleData, slotType, ConstantPredicates.alwaysTrue());
    }

    /// Adds a model spec for a specific MekaSuit Module to allow it to render as part of the MekaSuit when installed and enabled.
    ///
    /// @param name       Unique name that will be checked for in all the module model files. For third party mods it is recommended this contains your modid.
    /// @param moduleData [ModuleData] to associate this spec with.
    /// @param slotType   Equipment position the spec will be used for.
    /// @param isActive   Predicate to check if an entity should use the active or inactive model.
    /// @param <AVATAR>   Client player or similar, for use by the first person hand render. **Do not** require a specific implementation of this generic or things may
    /// crash.
    ///
    /// @apiNote Must only be called on the client side and from [FMLClientSetupEvent].
    <AVATAR extends Avatar & ClientAvatarEntity> void addMekaSuitModuleModelSpec(String name, Holder<ModuleData<?>> moduleData, EquipmentSlot slotType, Predicate<Either<HumanoidRenderState, AVATAR>> isActive);
}