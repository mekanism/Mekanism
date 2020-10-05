package mekanism.common.util;

import java.util.UUID;
import mekanism.api.text.EnumColor;
import mekanism.client.MekanismClient;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.security.IOwnerItem;
import mekanism.common.lib.security.ISecurityItem;
import mekanism.common.lib.security.ISecurityTile;
import mekanism.common.lib.security.ISecurityTile.SecurityMode;
import mekanism.common.lib.security.SecurityData;
import mekanism.common.lib.security.SecurityFrequency;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Util;
import net.minecraftforge.api.distmarker.Dist;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static boolean canAccess(PlayerEntity player, ItemStack stack) {
        // If protection is disabled, access is always granted
        if (!MekanismConfig.general.allowProtection.get()) {
            return true;
        }
        if (!(stack.getItem() instanceof ISecurityItem) && stack.getItem() instanceof IOwnerItem) {
            UUID owner = ((IOwnerItem) stack.getItem()).getOwnerUUID(stack);
            return owner == null || owner.equals(player.getUniqueID());
        }
        if (stack.isEmpty() || !(stack.getItem() instanceof ISecurityItem)) {
            return true;
        }
        ISecurityItem security = (ISecurityItem) stack.getItem();
        if (MekanismUtils.isOp(player)) {
            return true;
        }
        return canAccess(security.getSecurity(stack), player, security.getOwnerUUID(stack));
    }

    public static boolean canAccess(PlayerEntity player, TileEntity tile) {
        if (!(tile instanceof ISecurityTile) || !((ISecurityTile) tile).hasSecurity()) {
            //If this tile does not have security allow access
            return true;
        }
        ISecurityTile security = (ISecurityTile) tile;
        if (MekanismUtils.isOp(player)) {
            return true;
        }
        return canAccess(security.getSecurity().getMode(), player, security.getSecurity().getOwnerUUID());
    }

    private static boolean canAccess(SecurityMode mode, PlayerEntity player, UUID owner) {
        // If protection is disabled, access is always granted
        if (!MekanismConfig.general.allowProtection.get()) {
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
        if (uuid != null) {
            return FrequencyType.SECURITY.getManager(null).getFrequency(uuid);
        }
        return null;
    }

    public static void displayNoAccess(PlayerEntity player) {
        player.sendMessage(MekanismLang.LOG_FORMAT.translateColored(EnumColor.DARK_BLUE, MekanismLang.MEKANISM, EnumColor.RED, MekanismLang.NO_ACCESS), Util.DUMMY_UUID);
    }

    public static SecurityMode getSecurity(ISecurityTile security, Dist side) {
        if (!security.hasSecurity()) {
            return SecurityMode.PUBLIC;
        }
        if (side.isDedicatedServer()) {
            SecurityFrequency freq = security.getSecurity().getFreq();
            if (freq != null && freq.isOverridden()) {
                return freq.getSecurityMode();
            }
        } else if (side.isClient()) {
            SecurityData data = MekanismClient.clientSecurityMap.get(security.getSecurity().getOwnerUUID());
            if (data != null && data.override) {
                return data.mode;
            }
        }
        return security.getSecurity().getMode();
    }

    public static SecurityMode getSecurity(ItemStack stack, Dist side) {
        ISecurityItem security = (ISecurityItem) stack.getItem();
        SecurityMode mode = security.getSecurity(stack);
        if (security.getOwnerUUID(stack) != null) {
            if (side.isDedicatedServer()) {
                SecurityFrequency freq = getFrequency(security.getOwnerUUID(stack));
                if (freq != null && freq.isOverridden()) {
                    mode = freq.getSecurityMode();
                }
            } else if (side.isClient()) {
                SecurityData data = MekanismClient.clientSecurityMap.get(security.getOwnerUUID(stack));
                if (data != null && data.override) {
                    mode = data.mode;
                }
            }
        }
        return mode;
    }

    public static boolean isOverridden(ItemStack stack, Dist side) {
        ISecurityItem security = (ISecurityItem) stack.getItem();
        if (security.getOwnerUUID(stack) == null) {
            return false;
        }
        if (side.isDedicatedServer()) {
            SecurityFrequency freq = getFrequency(security.getOwnerUUID(stack));
            return freq != null && freq.isOverridden();
        }
        SecurityData data = MekanismClient.clientSecurityMap.get(security.getOwnerUUID(stack));
        return data != null && data.override;
    }

    public static boolean isOverridden(TileEntity tile, Dist side) {
        ISecurityTile security = (ISecurityTile) tile;
        if (!security.hasSecurity() || security.getSecurity().getOwnerUUID() == null) {
            return false;
        }
        if (side.isDedicatedServer()) {
            SecurityFrequency freq = getFrequency(security.getSecurity().getOwnerUUID());
            return freq != null && freq.isOverridden();
        }
        SecurityData data = MekanismClient.clientSecurityMap.get(security.getSecurity().getOwnerUUID());
        return data != null && data.override;
    }
}