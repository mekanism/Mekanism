package mekanism.common.tile;

import com.mojang.authlib.GameProfile;
import java.util.Optional;
import java.util.UUID;
import mekanism.api.IContentsListener;
import mekanism.api.security.ISecurityUtils;
import mekanism.api.security.SecurityMode;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.inventory.container.ISecurityContainer;
import mekanism.common.inventory.slot.SecurityInventorySlot;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.security.SecurityFrequency;
import mekanism.common.network.to_client.security.PacketSyncSecurity;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.IBoundingBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntitySecurityDesk extends TileEntityMekanism implements IBoundingBlock {

    private SecurityInventorySlot unlockSlot;
    private SecurityInventorySlot lockSlot;

    public TileEntitySecurityDesk(BlockPos pos, BlockState state) {
        super(MekanismBlocks.SECURITY_DESK, pos, state);
    }

    @NotNull
    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        InventorySlotHelper builder = InventorySlotHelper.forSide(facingSupplier);
        builder.addSlot(unlockSlot = SecurityInventorySlot.unlock(this::getOwnerUUID, listener, 146, 18));
        builder.addSlot(lockSlot = SecurityInventorySlot.lock(listener, 146, 97));
        return builder.build();
    }

    @Override
    protected boolean onUpdateServer() {
        boolean sendUpdatePacket = super.onUpdateServer();
        SecurityFrequency frequency = getFreq();
        UUID ownerUUID = getOwnerUUID();
        if (ownerUUID != null && frequency != null) {
            unlockSlot.unlock(ownerUUID);
            lockSlot.lock(ownerUUID, frequency);
        }
        return sendUpdatePacket;
    }

    /**
     * Only call on the server side
     */
    public void toggleOverride() {
        SecurityFrequency frequency = getFreq();
        if (frequency != null) {
            frequency.setOverridden(!frequency.isOverridden());
            markForSave();
            // send the security update to other players; this change will be visible on machine security tabs
            PacketDistributor.sendToAllPlayers(new PacketSyncSecurity(frequency));
            validateAccess();
        }
    }

    /**
     * Validates access for anyone who might be accessing a GUI that changed security modes
     */
    private void validateAccess() {
        if (hasLevel()) {
            MinecraftServer server = getWorldNN().getServer();
            if (server != null) {
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    closeIfNoAccess(player);
                }
            }
        }
    }

    public void removeTrusted(int index) {
        SecurityFrequency frequency = getFreq();
        if (frequency != null) {
            UUID removed = frequency.removeTrusted(index);
            markForSave();
            if (removed != null && hasLevel()) {
                MinecraftServer server = getWorldNN().getServer();
                if (server != null) {
                    closeIfNoAccess(server.getPlayerList().getPlayer(removed));
                }
            }
        }
    }

    private void closeIfNoAccess(@Nullable Player player) {
        if (player != null && player.containerMenu instanceof ISecurityContainer container && !container.canPlayerAccess(player)) {
            //Boot any players out of the container if they no longer have access to viewing it
            player.closeContainer();
        }
    }

    public void setSecurityDeskMode(SecurityMode mode) {
        SecurityFrequency frequency = getFreq();
        if (frequency != null) {
            SecurityMode old = frequency.getSecurity();
            if (old != mode) {
                //TODO - 1.20.4: Is this fine, or do we need to make security frequencies override the identity
                frequency.setSecurityMode(mode);
                markForSave();
                // send the security update to other players; this change will be visible on machine security tabs
                PacketDistributor.sendToAllPlayers(new PacketSyncSecurity(frequency));
                if (ISecurityUtils.INSTANCE.moreRestrictive(old, mode)) {
                    validateAccess();
                }
            }
        }
    }

    public void addTrusted(String name) {
        if (level != null) {
            MinecraftServer server = level.getServer();
            if (server != null) {
                GameProfileCache profileCache = server.getProfileCache();
                if (profileCache != null) {
                    SecurityFrequency frequency = getFreq();
                    if (frequency != null) {
                        Optional<GameProfile> gameProfile = profileCache.get(name);
                        //noinspection OptionalIsPresent - Capturing lambda
                        if (gameProfile.isPresent()) {
                            GameProfile profile = gameProfile.get();
                            frequency.addTrusted(profile.getId(), profile.getName());
                        }
                    }
                }
            }
        }
    }

    public SecurityFrequency getFreq() {
        return getFrequency(FrequencyType.SECURITY);
    }
}