package mekanism.api.security;

import mekanism.api.MekanismAPI;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.capabilities.EntityCapability;
import org.jspecify.annotations.Nullable;

/// Utility class for interacting with Mekanism's security system when applied to entities.
///
/// @see IEntitySecurityUtils#INSTANCE
/// @since 10.5.0
public interface IEntitySecurityUtils extends ITypedSecurityUtils<Entity> {

    /// Provides access to Mekanism's implementation of [IEntitySecurityUtils].
    ///
    /// @since 10.5.0
    IEntitySecurityUtils INSTANCE = MekanismAPI.getService(IEntitySecurityUtils.class);

    /// {@return the entity capability representing owner objects}
    EntityCapability<IOwnerObject, @Nullable Void> ownerCapability();

    @Nullable
    @Override
    default IOwnerObject ownerCapability(@Nullable Entity entity) {
        return entity == null ? null : entity.getCapability(ownerCapability());
    }

    /// {@return the entity capability representing security objects}
    EntityCapability<ISecurityObject, @Nullable Void> securityCapability();

    @Nullable
    @Override
    default ISecurityObject securityCapability(@Nullable Entity entity) {
        return entity == null ? null : entity.getCapability(securityCapability());
    }
}