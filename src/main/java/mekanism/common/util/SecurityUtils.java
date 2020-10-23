package mekanism.common.util;

import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.text.EnumColor;
import mekanism.client.MekanismClient;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.security.IOwnerItem;
import mekanism.common.lib.security.ISecurityItem;
import mekanism.common.lib.security.ISecurityObject;
import mekanism.common.lib.security.ISecurityTile;
import mekanism.common.lib.security.SecurityData;
import mekanism.common.lib.security.SecurityFrequency;
import mekanism.common.lib.security.SecurityMode;
import mekanism.common.network.PacketSecurityUpdate;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Util;
import net.minecraftforge.api.distmarker.Dist;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static boolean canAccess(PlayerEntity player, Object object) {
        ISecurityObject security;
        if (object instanceof ItemStack) {
            ItemStack stack = (ItemStack) object;
            if (!(stack.getItem() instanceof ISecurityItem) && stack.getItem() instanceof IOwnerItem) {
                //If it is an owner item but not a security item make sure the owner matches
                if (!MekanismConfig.general.allowProtection.get() || MekanismUtils.isOp(player)) {
                    //If protection is disabled or the player is an op and bypass restrictions are enabled, access is always granted
                    return true;
                }
                UUID owner = ((IOwnerItem) stack.getItem()).getOwnerUUID(stack);
                return owner == null || owner.equals(player.getUniqueID());
            }
            security = wrapSecurityItem(stack);
        } else if (object instanceof ISecurityObject) {
            security = (ISecurityObject) object;
        } else {
            //The object doesn't have security so there are no security restrictions
            return true;
        }
        return !security.hasSecurity() || canAccess(security.getSecurityMode(), player, security.getOwnerUUID());
    }

    private static boolean canAccess(SecurityMode mode, PlayerEntity player, UUID owner) {
        if (!MekanismConfig.general.allowProtection.get() || MekanismUtils.isOp(player)) {
            //If protection is disabled or the player is an op and bypass restrictions are enabled, access is always granted
            return true;
        }
        if (owner == null || player.getUniqueID().equals(owner)) {
            return true;
        }
        SecurityFrequency freq = getFrequency(owner);
        if (freq == null) {
            return true;
        }
        if (freq.isOverridden()) {
            mode = freq.getSecurityMode();
        }
        if (mode == SecurityMode.PUBLIC) {
            return true;
        } else if (mode == SecurityMode.TRUSTED) {
            return freq.getTrustedUUIDs().contains(player.getUniqueID());
        }
        return false;
    }

    public static SecurityFrequency getFrequency(UUID uuid) {
        return uuid == null ? null : FrequencyType.SECURITY.getManager(null).getFrequency(uuid);
    }

    public static void displayNoAccess(PlayerEntity player) {
        player.sendMessage(MekanismLang.LOG_FORMAT.translateColored(EnumColor.DARK_BLUE, MekanismLang.MEKANISM, EnumColor.RED, MekanismLang.NO_ACCESS), Util.DUMMY_UUID);
    }

    public static SecurityMode getSecurity(ISecurityObject security, Dist side) {
        if (!security.hasSecurity()) {
            return SecurityMode.PUBLIC;
        }
        if (side.isDedicatedServer()) {
            SecurityFrequency freq;
            if (security instanceof ISecurityTile) {
                freq = ((ISecurityTile) security).getSecurity().getFrequency();
            } else {
                freq = getFrequency(security.getOwnerUUID());
            }
            if (freq != null && freq.isOverridden()) {
                return freq.getSecurityMode();
            }
        } else if (side.isClient()) {
            SecurityData data = MekanismClient.clientSecurityMap.get(security.getOwnerUUID());
            if (data != null && data.override) {
                return data.mode;
            }
        }
        return security.getSecurityMode();
    }

    public static boolean isOverridden(ISecurityObject security, Dist side) {
        if (!security.hasSecurity() || security.getOwnerUUID() == null) {
            return false;
        }
        if (side.isDedicatedServer()) {
            SecurityFrequency freq = getFrequency(security.getOwnerUUID());
            return freq != null && freq.isOverridden();
        }
        SecurityData data = MekanismClient.clientSecurityMap.get(security.getOwnerUUID());
        return data != null && data.override;
    }

    public static void claimItem(PlayerEntity player, ItemStack stack) {
        if (stack.getItem() instanceof IOwnerItem) {
            ((IOwnerItem) stack.getItem()).setOwnerUUID(stack, player.getUniqueID());
            Mekanism.packetHandler.sendToAll(new PacketSecurityUpdate(player.getUniqueID(), null));
            player.sendMessage(MekanismLang.LOG_FORMAT.translateColored(EnumColor.DARK_BLUE, MekanismLang.MEKANISM, EnumColor.GRAY, MekanismLang.NOW_OWN),
                  Util.DUMMY_UUID);
        }
    }

    public static ISecurityObject wrapSecurityItem(@Nonnull ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ISecurityItem)) {
            return ISecurityObject.NO_SECURITY;
        }
        return new ISecurityObject() {

            @Nullable
            @Override
            public UUID getOwnerUUID() {
                return ((ISecurityItem) stack.getItem()).getOwnerUUID(stack);
            }

            @Nullable
            @Override
            public String getOwnerName() {
                UUID ownerUUID = getOwnerUUID();
                return ownerUUID == null ? null : MekanismClient.clientUUIDMap.get(ownerUUID);
            }

            @Override
            public SecurityMode getSecurityMode() {
                return ((ISecurityItem) stack.getItem()).getSecurity(stack);
            }

            @Override
            public void setSecurityMode(SecurityMode mode) {
                ((ISecurityItem) stack.getItem()).setSecurity(stack, mode);
            }
        };
    }
}