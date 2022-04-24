package mekanism.common.util;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.functions.TriConsumer;
import mekanism.api.security.IOwnerObject;
import mekanism.api.security.ISecurityObject;
import mekanism.api.security.ISecurityUtils;
import mekanism.api.security.SecurityMode;
import mekanism.api.text.EnumColor;
import mekanism.client.MekanismClient;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.security.SecurityData;
import mekanism.common.lib.security.SecurityFrequency;
import mekanism.common.network.to_client.PacketSecurityUpdate;
import mekanism.common.util.text.OwnerDisplay;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

@ParametersAreNonnullByDefault
public final class SecurityUtils implements ISecurityUtils {

    public static final SecurityUtils INSTANCE = new SecurityUtils();

    private SecurityUtils() {
    }

    /**
     * Whether ops can bypass security and a given player is considered an Op.
     *
     * @param p - player to check
     *
     * @return if the player has operator privileges
     */
    private boolean isOp(@Nonnull Player p) {
        Objects.requireNonNull(p, "Player may not be null.");
        return MekanismConfig.general.opsBypassRestrictions.get() && p instanceof ServerPlayer player && player.server.getPlayerList().isOp(player.getGameProfile());
    }

    @Nullable
    @Override
    public UUID getOwnerUUID(@Nonnull ICapabilityProvider provider) {
        Objects.requireNonNull(provider, "Capability provider may not be null.");
        return provider.getCapability(Capabilities.OWNER_OBJECT).resolve().map(IOwnerObject::getOwnerUUID).orElse(null);
    }

    @Override
    public boolean canAccess(@Nonnull Player player, @Nullable ICapabilityProvider provider) {
        //If the player is an op allow bypassing any restrictions
        return isOp(player) || canAccess(player.getUUID(), provider, player.level.isClientSide);
    }

    @Override
    public boolean canAccessObject(@Nonnull Player player, @Nonnull ISecurityObject security) {
        //If the player is an op allow bypassing any restrictions
        return isOp(player) || canAccessObject(player.getUUID(), security, player.level.isClientSide);
    }

    @Override
    public boolean canAccess(@Nullable UUID player, @Nullable ICapabilityProvider provider, boolean isClient) {
        if (!MekanismConfig.general.allowProtection.get() || provider == null) {
            //If protection is disabled, access is always granted
            return true;
        }
        //Note: We don't just use getSecurityObject here as we support checking access to things that are only owned and don't have security
        Optional<ISecurityObject> securityCapability = provider.getCapability(Capabilities.SECURITY_OBJECT).resolve();
        if (securityCapability.isEmpty()) {
            //If it is an owner item but not a security item make sure the owner matches
            Optional<IOwnerObject> ownerCapability = provider.getCapability(Capabilities.OWNER_OBJECT).resolve();
            if (ownerCapability.isPresent()) {
                //If it is an owner object but not a security object make sure the owner matches
                UUID owner = ownerCapability.get().getOwnerUUID();
                return owner == null || owner.equals(player);
            }
            //Otherwise, if there is no owner AND no security, access is always granted
            return true;
        }
        return canAccessObject(player, securityCapability.get(), isClient);
    }

    @Override
    public boolean canAccessObject(@Nullable UUID player, @Nonnull ISecurityObject security, boolean isClient) {
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
    public SecurityMode getSecurityMode(@Nullable ICapabilityProvider provider, boolean isClient) {
        if (provider == null || !MekanismConfig.general.allowProtection.get()) {
            return SecurityMode.PUBLIC;
        }
        return provider.getCapability(Capabilities.SECURITY_OBJECT).map(security -> getEffectiveSecurityMode(security, isClient))
              .orElseGet(() -> provider.getCapability(Capabilities.OWNER_OBJECT).isPresent() ? SecurityMode.PRIVATE : SecurityMode.PUBLIC);
    }

    @Override
    public SecurityMode getEffectiveSecurityMode(@Nonnull ISecurityObject securityObject, boolean isClient) {
        Objects.requireNonNull(securityObject, "Security object may not be null.");
        return getFinalData(securityObject, isClient).mode();
    }

    public void incrementSecurityMode(Player player, ICapabilityProvider provider) {
        provider.getCapability(Capabilities.SECURITY_OBJECT).ifPresent(security -> {
            if (security.ownerMatches(player)) {
                security.setSecurityMode(security.getSecurityMode().getNext());
            }
        });
    }

    public InteractionResultHolder<ItemStack> claimOrOpenGui(@Nonnull Level level, @Nonnull Player player, @Nonnull InteractionHand hand,
          @Nonnull TriConsumer<ServerPlayer, InteractionHand, ItemStack> openGui) {
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

    public boolean tryClaimItem(@Nonnull Level level, @Nonnull Player player, @Nonnull ItemStack stack) {
        Optional<IOwnerObject> capability = stack.getCapability(Capabilities.OWNER_OBJECT).resolve();
        if (capability.isPresent()) {
            IOwnerObject ownerObject = capability.get();
            if (ownerObject.getOwnerUUID() == null) {
                if (!level.isClientSide) {
                    ownerObject.setOwnerUUID(player.getUUID());
                    Mekanism.packetHandler().sendToAll(new PacketSecurityUpdate(player.getUUID()));
                    player.sendMessage(MekanismUtils.logFormat(MekanismLang.NOW_OWN), Util.NIL_UUID);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public void displayNoAccess(Player player) {
        Objects.requireNonNull(player, "Player may not be null.");
        player.sendMessage(MekanismUtils.logFormat(EnumColor.RED, MekanismLang.NO_ACCESS), Util.NIL_UUID);
    }

    public void addOwnerTooltip(@Nonnull ItemStack stack, @Nonnull List<Component> tooltip) {
        stack.getCapability(Capabilities.OWNER_OBJECT).ifPresent(ownerObject ->
              tooltip.add(OwnerDisplay.of(MekanismUtils.tryGetClientPlayer(), ownerObject.getOwnerUUID()).getTextComponent()));
    }

    @Override
    public void addSecurityTooltip(@Nonnull ItemStack stack, @Nonnull List<Component> tooltip) {
        Objects.requireNonNull(stack, "Stack to add tooltip for may not be null.");
        Objects.requireNonNull(tooltip, "List of tooltips to add to may not be null.");
        addOwnerTooltip(stack, tooltip);
        stack.getCapability(Capabilities.SECURITY_OBJECT).ifPresent(security -> {
            SecurityData data = getFinalData(security, true);
            tooltip.add(MekanismLang.SECURITY.translateColored(EnumColor.GRAY, data.mode()));
            if (data.override()) {
                tooltip.add(MekanismLang.SECURITY_OVERRIDDEN.translateColored(EnumColor.RED));
            }
        });
    }

    public void securityChanged(Set<Player> playersUsing, ICapabilityProvider target, SecurityMode old, SecurityMode mode) {
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