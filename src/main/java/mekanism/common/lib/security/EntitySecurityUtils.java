package mekanism.common.lib.security;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Set;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.security.IEntitySecurityUtils;
import mekanism.api.security.IOwnerObject;
import mekanism.api.security.ISecurityObject;
import mekanism.api.security.ISecurityUtils;
import mekanism.api.security.SecurityMode;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.capabilities.EntityCapability;
import org.jetbrains.annotations.Nullable;

/**
 * @apiNote Do not instantiate this class directly as it will be done via the service loader. Instead, access instances of this via {@link IEntitySecurityUtils#INSTANCE}
 */
@NothingNullByDefault
public class EntitySecurityUtils implements IEntitySecurityUtils {

    private static final EntityCapability<IOwnerObject, Void> OWNER_CAPABILITY = EntityCapability.createVoid(Capabilities.OWNER_OBJECT_NAME, IOwnerObject.class);
    private static final EntityCapability<ISecurityObject, Void> SECURITY_CAPABILITY = EntityCapability.createVoid(Capabilities.SECURITY_OBJECT_NAME, ISecurityObject.class);

    public static EntitySecurityUtils get() {
        return (EntitySecurityUtils) INSTANCE;
    }

    @Override
    public EntityCapability<IOwnerObject, Void> ownerCapability() {
        return OWNER_CAPABILITY;
    }

    @Override
    public EntityCapability<ISecurityObject, Void> securityCapability() {
        return SECURITY_CAPABILITY;
    }

    public void securityChanged(Set<Player> playersUsing, Entity target, SecurityMode old, SecurityMode mode) {
        //If the mode changed and the new security mode is more restrictive than the old one
        // and there are players using the security object
        if (!playersUsing.isEmpty() && ISecurityUtils.INSTANCE.moreRestrictive(old, mode)) {
            //then double check that all the players are actually supposed to be able to access the GUI
            CachingCapabilityLookup lookup = new CachingCapabilityLookup(target);
            for (Player player : new ObjectOpenHashSet<>(playersUsing)) {
                if (!ISecurityUtils.INSTANCE.canAccess(player, lookup, CachingCapabilityLookup::securityCapability, CachingCapabilityLookup::ownerCapability)) {
                    //and if they can't then boot them out
                    player.closeContainer();
                }
            }
        }
    }

    private static class CachingCapabilityLookup {

        private final Entity target;
        @Nullable
        private ISecurityObject securityObject;
        @Nullable
        private IOwnerObject ownerObject;

        public CachingCapabilityLookup(Entity target) {
            this.target = target;
        }

        @Nullable
        ISecurityObject securityCapability() {
            if (securityObject == null) {
                securityObject = INSTANCE.securityCapability(target);
            }
            return securityObject;
        }

        @Nullable
        IOwnerObject ownerCapability() {
            if (ownerObject == null) {
                ownerObject = INSTANCE.ownerCapability(target);
            }
            return ownerObject;
        }
    }
}