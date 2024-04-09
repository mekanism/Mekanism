package mekanism.common.lib.security;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.security.IOwnerObject;
import mekanism.api.security.ISecurityObject;
import mekanism.api.security.ISecurityUtils;
import mekanism.api.security.SecurityMode;
import mekanism.api.text.EnumColor;
import mekanism.client.MekanismClient;
import mekanism.common.MekanismLang;
import mekanism.common.base.MekanismPermissions;
import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.util.MekanismUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.server.permission.PermissionAPI;
import org.jetbrains.annotations.Nullable;

/**
 * @apiNote Do not instantiate this class directly as it will be done via the service loader. Instead, access instances of this via {@link ISecurityUtils#INSTANCE}
 */
@NothingNullByDefault
public final class SecurityUtils implements ISecurityUtils {

    public static SecurityUtils get() {
        return (SecurityUtils) INSTANCE;
    }

    /**
     * Whether ops can bypass security and a given player is considered an Op.
     *
     * @param p - player to check
     *
     * @return if the player has operator privileges
     */
    private boolean isOp(Player p) {
        Objects.requireNonNull(p, "Player may not be null.");
        return MekanismConfig.general.opsBypassRestrictions.get() && p instanceof ServerPlayer player &&
               PermissionAPI.getPermission(player, MekanismPermissions.BYPASS_SECURITY);
    }

    @Override
    public boolean canAccess(Player player, Supplier<@Nullable ISecurityObject> securityProvider, Supplier<@Nullable IOwnerObject> ownerProvider) {
        //If the player is an op allow bypassing any restrictions
        return isOp(player) || canAccess(player.getUUID(), securityProvider, ownerProvider, player.level().isClientSide);
    }

    @Override
    public <PROVIDER> boolean canAccess(Player player, PROVIDER provider, Function<PROVIDER, @Nullable ISecurityObject> securityProvider, Function<PROVIDER, @Nullable IOwnerObject> ownerProvider) {
        return isOp(player) || canAccess(player.getUUID(), provider, securityProvider, ownerProvider, player.level().isClientSide);
    }

    @Override
    public boolean canAccess(@Nullable UUID player, Supplier<@Nullable ISecurityObject> securityProvider, Supplier<@Nullable IOwnerObject> ownerProvider, boolean isClient) {
        if (!MekanismConfig.general.allowProtection.get()) {
            //If protection is disabled, access is always granted
            return true;
        }
        //Note: We don't just use getSecurityObject here as we support checking access to things that are only owned and don't have security
        ISecurityObject securityCapability = securityProvider.get();
        if (securityCapability == null) {
            //If it is an owner item but not a security item make sure the owner matches
            IOwnerObject ownerCapability = ownerProvider.get();
            if (ownerCapability != null) {
                //If it is an owner object but not a security object make sure the owner matches
                UUID owner = ownerCapability.getOwnerUUID();
                return owner == null || owner.equals(player);
            }
            //Otherwise, if there is no owner AND no security, access is always granted
            return true;
        }
        return canAccessObject(player, securityCapability, isClient);
    }

    @Override
    public <PROVIDER> boolean canAccess(@Nullable UUID player, PROVIDER provider, Function<PROVIDER, @Nullable ISecurityObject> securityProvider,
          Function<PROVIDER, @Nullable IOwnerObject> ownerProvider, boolean isClient) {
        if (!MekanismConfig.general.allowProtection.get()) {
            //If protection is disabled, access is always granted
            return true;
        }
        //Note: We don't just use getSecurityObject here as we support checking access to things that are only owned and don't have security
        ISecurityObject securityCapability = securityProvider.apply(provider);
        if (securityCapability == null) {
            //If it is an owner item but not a security item make sure the owner matches
            IOwnerObject ownerCapability = ownerProvider.apply(provider);
            if (ownerCapability != null) {
                //If it is an owner object but not a security object make sure the owner matches
                UUID owner = ownerCapability.getOwnerUUID();
                return owner == null || owner.equals(player);
            }
            //Otherwise, if there is no owner AND no security, access is always granted
            return true;
        }
        return canAccessObject(player, securityCapability, isClient);
    }

    @Override
    public boolean canAccessObject(Player player, ISecurityObject security) {
        //If the player is an op allow bypassing any restrictions
        return isOp(player) || canAccessObject(player.getUUID(), security, player.level().isClientSide);
    }

    @Override
    public boolean canAccessObject(@Nullable UUID player, ISecurityObject security, boolean isClient) {
        Objects.requireNonNull(security, "Security object may not be null.");
        if (!MekanismConfig.general.allowProtection.get()) {
            //If protection is disabled, access is always granted
            return true;
        }
        UUID owner = security.getOwnerUUID();
        if (owner == null || owner.equals(player)) {
            return true;
        }
        return switch (getEffectiveSecurityMode(security, isClient)) {
            case PUBLIC -> true;
            case PRIVATE -> false;
            case TRUSTED -> {
                if (player == null) {
                    yield false;
                } else if (isClient) {
                    //If we are the client, then we just return true and assume that we can access the frequency
                    // as we don't know which players are set as trusted
                    //TODO: Technically in single player if the player is the single player owner we could hackily reach across
                    // sides but I don't think there is much benefit to doing so for how complex it is to do
                    yield true;
                }
                SecurityFrequency frequency = FrequencyType.SECURITY.getManager(null, SecurityMode.PUBLIC).getFrequency(owner);
                //If we have no frequency handle it as if it was private, otherwise check if the player is trusted
                yield frequency != null && frequency.isTrusted(player);
            }
        };
    }

    @Override
    public boolean moreRestrictive(SecurityMode base, SecurityMode overridden) {
        Objects.requireNonNull(base, "Base security mode may not be null.");
        Objects.requireNonNull(base, "Override security mode may not be null.");
        return switch (overridden) {
            //If the override mode is public it is never more restrictive than the normal level
            case PUBLIC -> false;
            //If the override mode is private it is only more restrictive if the base isn't already private
            case PRIVATE -> base != SecurityMode.PRIVATE;
            //If the override mode is trusted it is only more restrictive if the normal level was public
            case TRUSTED -> base == SecurityMode.PUBLIC;
        };
    }

    public SecurityData getFinalData(ISecurityObject securityObject, boolean isClient) {
        if (!MekanismConfig.general.allowProtection.get()) {
            return SecurityData.DUMMY;
        }
        SecurityData data = getData(securityObject.getOwnerUUID(), isClient);
        SecurityMode mode = securityObject.getSecurityMode();
        if (data.override() && moreRestrictive(mode, data.mode())) {
            //If our frequency's data is set to override, and it is more restrictive than the current mode,
            // return the data for our frequency
            return data;
        }
        return new SecurityData(mode, false);
    }

    private SecurityData getData(@Nullable UUID uuid, boolean isClient) {
        if (uuid == null) {
            return SecurityData.DUMMY;
        } else if (isClient) {
            return MekanismClient.clientSecurityMap.getOrDefault(uuid, SecurityData.DUMMY);
        }
        SecurityFrequency frequency = FrequencyType.SECURITY.getManager(null, SecurityMode.PUBLIC).getFrequency(uuid);
        return frequency == null ? SecurityData.DUMMY : new SecurityData(frequency);
    }

    @Override
    public SecurityMode getSecurityMode(Supplier<@Nullable ISecurityObject> securityProvider, Supplier<@Nullable IOwnerObject> ownerProvider, boolean isClient) {
        if (!MekanismConfig.general.allowProtection.get()) {
            return SecurityMode.PUBLIC;
        }
        ISecurityObject security = securityProvider.get();
        if (security != null) {
            return getEffectiveSecurityMode(security, isClient);
        }
        IOwnerObject ownerObject = ownerProvider.get();
        return ownerObject == null ? SecurityMode.PUBLIC : SecurityMode.PRIVATE;
    }

    @Override
    public <PROVIDER> SecurityMode getSecurityMode(PROVIDER provider, Function<PROVIDER, @Nullable ISecurityObject> securityProvider,
          Function<PROVIDER, @Nullable IOwnerObject> ownerProvider, boolean isClient) {
        if (!MekanismConfig.general.allowProtection.get()) {
            return SecurityMode.PUBLIC;
        }
        ISecurityObject security = securityProvider.apply(provider);
        if (security != null) {
            return getEffectiveSecurityMode(security, isClient);
        }
        IOwnerObject ownerObject = ownerProvider.apply(provider);
        return ownerObject == null ? SecurityMode.PUBLIC : SecurityMode.PRIVATE;
    }

    @Override
    public SecurityMode getEffectiveSecurityMode(ISecurityObject securityObject, boolean isClient) {
        Objects.requireNonNull(securityObject, "Security object may not be null.");
        return getFinalData(securityObject, isClient).mode();
    }

    public void incrementSecurityMode(Player player, @Nullable ISecurityObject security) {
        if (security != null && security.ownerMatches(player)) {
            security.setSecurityMode(security.getSecurityMode().getNext());
        }
    }

    public void decrementSecurityMode(Player player, @Nullable ISecurityObject security) {
        if (security != null && security.ownerMatches(player)) {
            security.setSecurityMode(security.getSecurityMode().getPrevious());
        }
    }

    @Override
    public void displayNoAccess(Player player) {
        Objects.requireNonNull(player, "Player may not be null.");
        player.sendSystemMessage(MekanismUtils.logFormat(EnumColor.RED, MekanismLang.NO_ACCESS));
    }

    public boolean isTrusted(SecurityMode mode, @Nullable UUID ownerUUID, UUID playerUUID) {
        if (ownerUUID != null && mode == SecurityMode.TRUSTED) {
            SecurityFrequency frequency = FrequencyType.SECURITY.getManager(null, SecurityMode.PUBLIC).getFrequency(ownerUUID);
            return frequency != null && frequency.isTrusted(playerUUID);
        }
        return false;
    }
}