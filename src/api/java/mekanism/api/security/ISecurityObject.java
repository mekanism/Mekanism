package mekanism.api.security;

import java.util.function.Supplier;
import mekanism.api.annotations.NothingNullByDefault;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

/**
 * Expose this as a capability on items, entities, or block entities to represent it is an object that have "security". It is <strong>IMPORTANT</strong> to make sure that
 * if you expose this as a capability, you <em>must</em> also expose this object as an {@link IOwnerObject owner} capability.
 *
 * @apiNote The exposed capability should not care about side, and in general will be interacted with via the null side.
 * <br><br>
 * While an {@link IOwnerObject owner object} <em>must</em> be exposed if this object is exposed, it is not safe to assume that just because an exposed
 * {@link IOwnerObject} is an instance of an {@link ISecurityObject} that the object actually has security. The only way to know for certain if it does is by checking if
 * the provider exposes a security capability.
 * @since 10.2.1
 */
@NothingNullByDefault
public interface ISecurityObject extends IOwnerObject {

    /**
     * Gets the current security mode of this object.
     *
     * @return Current security mode.
     *
     * @apiNote To retrieve the "effective" security mode of this object {@link ISecurityUtils#getSecurityMode(Supplier, Supplier, boolean)} or
     * {@link ISecurityUtils#getEffectiveSecurityMode(ISecurityObject, boolean)} should be used instead.
     * @see ISecurityUtils#getSecurityMode(Supplier, Supplier, boolean)
     * @see ISecurityUtils#getEffectiveSecurityMode(ISecurityObject, boolean)
     */
    SecurityMode getSecurityMode();

    /// Sets the security mode of this object.
    ///
    /// @param mode        Security mode.
    /// @param transaction The current transaction context if any that this operation is part of.
    ///
    /// @implNote Only implementations for items currently support rolling back via the passed in transaction.
    /// @apiNote This method should not be called by addons unless it is on one of your own objects; for example to transfer the set security mode from an item stack to
    /// an entity when placing an entity.
    /// @since 10.8.0
    void setSecurityMode(SecurityMode mode, @Nullable TransactionContext transaction);
}