package mekanism.api.security;

import java.util.function.Consumer;
import mekanism.api.MekanismAPI;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import org.jspecify.annotations.Nullable;

/// Utility class for interacting with Mekanism's security system when applied to items.
///
/// @see IItemSecurityUtils#INSTANCE
/// @since 10.5.0
public interface IItemSecurityUtils extends ITypedSecurityUtils<ItemAccess> {

    /// Provides access to Mekanism's implementation of [IItemSecurityUtils].
    ///
    /// @since 10.5.0
    IItemSecurityUtils INSTANCE = MekanismAPI.getService(IItemSecurityUtils.class);

    /// {@return the item capability representing security objects}
    ItemCapability<ISecurityObject, ItemAccess> securityCapability();

    @Nullable
    @Override
    default ISecurityObject securityCapability(@Nullable ItemAccess itemAccess) {
        return itemAccess == null ? null : itemAccess.getCapability(securityCapability());
    }

    /// {@return the item capability representing owner objects}
    ItemCapability<IOwnerObject, ItemAccess> ownerCapability();

    @Nullable
    @Override
    default IOwnerObject ownerCapability(@Nullable ItemAccess itemAccess) {
        return itemAccess == null ? null : itemAccess.getCapability(ownerCapability());
    }

    /// Adds any owner data that the given stack has to the passed in list of tooltips. If the stack does not expose an owner then the corresponding data will not be
    /// added.
    ///
    /// @param itemAccess Item access to retrieve the owner data from.
    /// @param context    Tooltip context.
    /// @param player     The player viewing the tooltip.
    /// @param builder    Handles adding the extra tooltips.
    ///
    /// @apiNote While this method won't crash if called on the server it won't render quite right due to not having access to the player, so it is best to only call this
    /// on the client.
    /// @see #addSecurityTooltip(ItemAccess, Item.TooltipContext, Player, Consumer)
    /// @since 10.8.0 Previously had fewer parameters.
    void addOwnerTooltip(ItemAccess itemAccess, Item.TooltipContext context, @Nullable Player player, Consumer<Component> builder);

    /// Adds any owner and security data that the given stack has to the passed in list of tooltips. If the stack does not expose an owner or any security data then the
    /// corresponding data will not be added.
    ///
    /// @param itemAccess Item access to retrieve the owner and any security data from.
    /// @param context    Tooltip context.
    /// @param player     The player viewing the tooltip.
    /// @param builder    Handles adding the extra tooltips.
    ///
    /// @apiNote While this method won't crash if called on the server it won't render quite right due to not having access to the player, so it is best to only call this
    /// on the client.
    /// @see #addOwnerTooltip(ItemAccess, Item.TooltipContext, Player, Consumer)
    /// @since 10.8.0 Previously had fewer parameters.
    void addSecurityTooltip(ItemAccess itemAccess, Item.TooltipContext context, @Nullable Player player, Consumer<Component> builder);
}