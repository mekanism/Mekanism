package mekanism.common.tile;

import java.util.UUID;
import javax.annotation.Nonnull;
import mekanism.api.IContentsListener;
import mekanism.api.MekanismAPI;
import mekanism.api.security.ISecurityUtils;
import mekanism.api.security.SecurityMode;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.inventory.container.ISecurityContainer;
import mekanism.common.inventory.slot.SecurityInventorySlot;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.security.SecurityFrequency;
import mekanism.common.network.to_client.PacketSecurityUpdate;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.IBoundingBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.server.ServerLifecycleHooks;

public class TileEntitySecurityDesk extends TileEntityMekanism implements IBoundingBlock {

    private SecurityInventorySlot unlockSlot;
    private SecurityInventorySlot lockSlot;

    public TileEntitySecurityDesk(BlockPos pos, BlockState state) {
        super(MekanismBlocks.SECURITY_DESK, pos, state);
        //Even though there are inventory slots make this return none as accessible by automation, as then people could lock items to other
        // people unintentionally. We also disable the security object capability so that we only provide access to the security desk as an
        // "owner object" which means that all access checks will be handled as requiring the owner
        addDisabledCapabilities(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Capabilities.SECURITY_OBJECT);
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
        builder.addSlot(unlockSlot = SecurityInventorySlot.unlock(this::getOwnerUUID, listener, 146, 18));
        builder.addSlot(lockSlot = SecurityInventorySlot.lock(listener, 146, 97));
        return builder.build();
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        SecurityFrequency frequency = getFreq();
        UUID ownerUUID = getOwnerUUID();
        if (ownerUUID != null && frequency != null) {
            unlockSlot.unlock(ownerUUID);
            lockSlot.lock(ownerUUID, frequency);
        }
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
            Mekanism.packetHandler().sendToAll(new PacketSecurityUpdate(frequency));
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
                ISecurityUtils securityUtils = MekanismAPI.getSecurityUtils();
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    if (player.containerMenu instanceof ISecurityContainer container && !securityUtils.canAccess(player, container.getSecurityObject())) {
                        //Boot any players out of the container if they no longer have access to viewing it
                        player.closeContainer();
                    }
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
                    Player player = server.getPlayerList().getPlayer(removed);
                    if (player != null && player.containerMenu instanceof ISecurityContainer container &&
                        !MekanismAPI.getSecurityUtils().canAccess(player, container.getSecurityObject())) {
                        //If the player that got removed from being trusted no longer has access to view the container they were viewing
                        // boot them out of it
                        player.closeContainer();
                    }
                }
            }
        }
    }

    public void setSecurityDeskMode(SecurityMode mode) {
        SecurityFrequency frequency = getFreq();
        if (frequency != null) {
            SecurityMode old = frequency.getSecurityMode();
            if (old != mode) {
                frequency.setSecurityMode(mode);
                markForSave();
                // send the security update to other players; this change will be visible on machine security tabs
                Mekanism.packetHandler().sendToAll(new PacketSecurityUpdate(frequency));
                if (MekanismAPI.getSecurityUtils().moreRestrictive(old, mode)) {
                    validateAccess();
                }
            }
        }
    }

    public void addTrusted(String name) {
        SecurityFrequency frequency = getFreq();
        if (frequency != null) {
            ServerLifecycleHooks.getCurrentServer().getProfileCache().get(name).ifPresent(profile -> {
                frequency.addTrusted(profile.getId(), profile.getName());
                markForSave();
            });
        }
    }

    public SecurityFrequency getFreq() {
        return getFrequency(FrequencyType.SECURITY);
    }

    @Override
    public boolean isOffsetCapabilityDisabled(@Nonnull Capability<?> capability, Direction side, @Nonnull Vec3i offset) {
        //Don't allow proxying any capabilities by marking them all as disabled
        return true;
    }
}