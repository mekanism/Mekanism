package mekanism.common.lib.security;

import java.util.Set;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.security.IEntitySecurityUtils;
import mekanism.api.security.IOwnerObject;
import mekanism.api.security.ISecurityObject;
import mekanism.api.security.SecurityMode;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.capabilities.EntityCapability;

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
        SecurityUtils.get().securityChanged(playersUsing, player -> canAccess(player, target), old, mode);
    }
}