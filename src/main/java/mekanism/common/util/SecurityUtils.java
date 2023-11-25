package mekanism.common.util;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.functions.TriConsumer;
import mekanism.api.security.IOwnerObject;
import mekanism.api.security.ISecurityObject;
import mekanism.api.security.ISecurityUtils;
import mekanism.api.security.SecurityMode;
import mekanism.api.text.EnumColor;
import mekanism.client.MekanismClient;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.base.MekanismPermissions;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.security.SecurityData;
import mekanism.common.lib.security.SecurityFrequency;
import mekanism.common.network.to_client.PacketSecurityUpdate;
import mekanism.common.util.text.OwnerDisplay;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.server.permission.PermissionAPI;
import org.jetbrains.annotations.NotNull;
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

    @Nullable
    @Override
    public UUID getOwnerUUID(Object provider) {
        Objects.requireNonNull(provider, "Capability provider may not be null.");
        IOwnerObject ownerObject = Capabilities.OWNER_OBJECT.getCapability(provider);
        return ownerObject == null ? null : ownerObject.getOwnerUUID();
    }

    @Nullable
    @Override
    public UUID getOwnerUUID(ItemStack stack) {
        Objects.requireNonNull(stack, "Capability provider may not be null.");
        if (!stack.isEmpty()) {
            IOwnerObject ownerObject = Capabilities.OWNER_OBJECT.getCapability(stack);
            if (ownerObject != null) {
                return ownerObject.getOwnerUUID();
            }
        }
        return null;
    }

    @Override
    public boolean canAccess(Player player, @Nullable Object provider) {
        //If the player is an op allow bypassing any restrictions
        return isOp(player) || canAccess(player.getUUID(), provider, player.level().isClientSide);
    }

    @Override
    public boolean canAccessObject(Player player, ISecurityObject security) {
        //If the player is an op allow bypassing any restrictions
        return isOp(player) || canAccessObject(player.getUUID(), security, player.level().isClientSide);
    }

    @Override
    public boolean canAccess(@Nullable UUID player, @Nullable Object provider, boolean isClient) {
        if (!MekanismConfig.general.allowProtection.get() || provider == null) {
            //If protection is disabled, access is always granted
            return true;
        }
        //Note: We don't just use getSecurityObject here as we support checking access to things that are only owned and don't have security
        ISecurityObject securityCapability = Capabilities.SECURITY_OBJECT.getCapability(provider);
        if (securityCapability == null) {
            //If it is an owner item but not a security item make sure the owner matches
            IOwnerObject ownerCapability = Capabilities.OWNER_OBJECT.getCapability(provider);
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
    public boolean canAccessObject(@Nullable UUID player, @NotNull ISecurityObject security, boolean isClient) {
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
                SecurityFrequency frequency = FrequencyType.SECURITY.getManager(null).getFrequency(owner);
                //If we have no frequency handle it as if it was private, otherwise check if the player is trusted
                yield frequency != null && frequency.getTrustedUUIDs().contains(player);
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
        SecurityFrequency frequency = FrequencyType.SECURITY.getManager(null).getFrequency(uuid);
        return frequency == null ? SecurityData.DUMMY : new SecurityData(frequency);
    }

    @Override
    public SecurityMode getSecurityMode(@Nullable Object provider, boolean isClient) {
        if (provider == null || !MekanismConfig.general.allowProtection.get()) {
            return SecurityMode.PUBLIC;
        }
        ISecurityObject security = Capabilities.SECURITY_OBJECT.getCapability(provider);
        if (security != null) {
            return getEffectiveSecurityMode(security, isClient);
        }
        IOwnerObject ownerObject = Capabilities.OWNER_OBJECT.getCapability(provider);
        return ownerObject == null ? SecurityMode.PUBLIC : SecurityMode.PRIVATE;
    }

    @Override
    public SecurityMode getEffectiveSecurityMode(ISecurityObject securityObject, boolean isClient) {
        Objects.requireNonNull(securityObject, "Security object may not be null.");
        return getFinalData(securityObject, isClient).mode();
    }

    public void incrementSecurityMode(Player player, Object provider) {
        ISecurityObject security = Capabilities.SECURITY_OBJECT.getCapability(provider);
        if (security != null && security.ownerMatches(player)) {
            security.setSecurityMode(security.getSecurityMode().getNext());
        }
    }

    public void decrementSecurityMode(Player player, Object provider) {
        ISecurityObject security = Capabilities.SECURITY_OBJECT.getCapability(provider);
        if (security != null && security.ownerMatches(player)) {
            security.setSecurityMode(security.getSecurityMode().getPrevious());
        }
    }

    public InteractionResultHolder<ItemStack> claimOrOpenGui(Level level, Player player, InteractionHand hand, TriConsumer<ServerPlayer, InteractionHand, ItemStack> openGui) {
        ItemStack stack = player.getItemInHand(hand);
        if (!tryClaimItem(level, player, stack)) {
            if (!canAccessOrDisplayError(player, stack)) {
                return InteractionResultHolder.fail(stack);
            } else if (!level.isClientSide) {
                openGui.accept((ServerPlayer) player, hand, stack);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    public boolean tryClaimItem(Level level, Player player, ItemStack stack) {
        IOwnerObject ownerObject = Capabilities.OWNER_OBJECT.getCapability(stack);
        if (ownerObject != null && ownerObject.getOwnerUUID() == null) {
            if (!level.isClientSide) {
                ownerObject.setOwnerUUID(player.getUUID());
                Mekanism.packetHandler().sendToAll(new PacketSecurityUpdate(player.getUUID()));
                player.sendSystemMessage(MekanismUtils.logFormat(MekanismLang.NOW_OWN));
            }
            return true;
        }
        return false;
    }

    @Override
    public void displayNoAccess(Player player) {
        Objects.requireNonNull(player, "Player may not be null.");
        player.sendSystemMessage(MekanismUtils.logFormat(EnumColor.RED, MekanismLang.NO_ACCESS));
    }

    public void addOwnerTooltip(ItemStack stack, List<Component> tooltip) {
        IOwnerObject ownerObject = Capabilities.OWNER_OBJECT.getCapability(stack);
        if (ownerObject != null) {
            tooltip.add(OwnerDisplay.of(MekanismUtils.tryGetClientPlayer(), ownerObject.getOwnerUUID()).getTextComponent());
        }
    }

    @Override
    public void addSecurityTooltip(ItemStack stack, List<Component> tooltip) {
        Objects.requireNonNull(stack, "Stack to add tooltip for may not be null.");
        Objects.requireNonNull(tooltip, "List of tooltips to add to may not be null.");
        addOwnerTooltip(stack, tooltip);
        ISecurityObject security = Capabilities.SECURITY_OBJECT.getCapability(stack);
        if (security != null) {
            SecurityData data = getFinalData(security, true);
            tooltip.add(MekanismLang.SECURITY.translateColored(EnumColor.GRAY, data.mode()));
            if (data.override()) {
                tooltip.add(MekanismLang.SECURITY_OVERRIDDEN.translateColored(EnumColor.RED));
            }
        }
    }

    public void securityChanged(Set<Player> playersUsing, Object target, SecurityMode old, SecurityMode mode) {
        //If the mode changed and the new security mode is more restrictive than the old one
        // and there are players using the security object
        if (moreRestrictive(old, mode) && !playersUsing.isEmpty()) {
            //then double check that all the players are actually supposed to be able to access the GUI
            for (Player player : new ObjectOpenHashSet<>(playersUsing)) {
                if (!canAccess(player, target)) {
                    //and if they can't then boot them out
                    player.closeContainer();
                }
            }
        }
    }
}