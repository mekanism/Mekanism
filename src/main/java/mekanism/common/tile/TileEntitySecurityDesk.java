package mekanism.common.tile;

import com.mojang.authlib.GameProfile;
import java.util.Collections;
import java.util.UUID;
import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.api.NBTConstants;
import mekanism.common.Mekanism;
import mekanism.common.base.IBoundingBlock;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.frequency.Frequency;
import mekanism.common.frequency.FrequencyManager;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.inventory.container.sync.SyncableEnum;
import mekanism.common.inventory.container.sync.list.SyncableStringList;
import mekanism.common.inventory.slot.SecurityInventorySlot;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.security.IOwnerItem;
import mekanism.common.security.ISecurityItem;
import mekanism.common.security.SecurityFrequency;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.items.CapabilityItemHandler;

public class TileEntitySecurityDesk extends TileEntityMekanism implements IBoundingBlock {

    public UUID ownerUUID;
    public String clientOwner;

    public SecurityFrequency frequency;

    private SecurityInventorySlot unlockSlot;
    private SecurityInventorySlot lockSlot;

    public TileEntitySecurityDesk() {
        super(MekanismBlocks.SECURITY_DESK);
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
        builder.addSlot(unlockSlot = SecurityInventorySlot.unlock(() -> ownerUUID, this, 146, 18));
        builder.addSlot(lockSlot = SecurityInventorySlot.lock(this, 146, 97));
        return builder.build();
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        if (ownerUUID != null && frequency != null) {
            //TODO: Move the locking unlocking logic into the SecurityInventorySlot
            if (!unlockSlot.isEmpty()) {
                ItemStack itemStack = unlockSlot.getStack();
                if (itemStack.getItem() instanceof IOwnerItem) {
                    IOwnerItem item = (IOwnerItem) itemStack.getItem();
                    if (item.getOwnerUUID(itemStack) != null) {
                        if (item.getOwnerUUID(itemStack).equals(ownerUUID)) {
                            item.setOwnerUUID(itemStack, null);
                            if (item instanceof ISecurityItem) {
                                ((ISecurityItem) item).setSecurity(itemStack, SecurityMode.PUBLIC);
                            }
                        }
                    }
                }
            }

            if (!lockSlot.isEmpty()) {
                ItemStack stack = lockSlot.getStack();
                if (stack.getItem() instanceof IOwnerItem) {
                    IOwnerItem item = (IOwnerItem) stack.getItem();
                    UUID stackOwner = item.getOwnerUUID(stack);
                    if (stackOwner == null) {
                        item.setOwnerUUID(stack, stackOwner = this.ownerUUID);
                    }
                    if (stackOwner.equals(this.ownerUUID)) {
                        if (item instanceof ISecurityItem) {
                            ((ISecurityItem) item).setSecurity(stack, frequency.securityMode);
                        }
                    }
                }
            }
        }

        if (frequency == null && ownerUUID != null) {
            setFrequency(ownerUUID);
        }

        FrequencyManager manager = getManager(frequency);
        if (manager != null) {
            if (frequency != null && !frequency.valid) {
                frequency = (SecurityFrequency) manager.validateFrequency(ownerUUID, Coord4D.get(this), frequency);
            }
            if (frequency != null) {
                frequency = (SecurityFrequency) manager.update(Coord4D.get(this), frequency);
            }
        } else {
            frequency = null;
        }
    }

    public FrequencyManager getManager(Frequency freq) {
        if (ownerUUID == null || freq == null) {
            return null;
        }
        return Mekanism.securityFrequencies;
    }

    public void setFrequency(UUID owner) {
        FrequencyManager manager = Mekanism.securityFrequencies;
        manager.deactivate(Coord4D.get(this));
        for (Frequency freq : manager.getFrequencies()) {
            if (freq.ownerUUID.equals(owner)) {
                frequency = (SecurityFrequency) freq;
                frequency.activeCoords.add(Coord4D.get(this));
                sendUpdatePacket();
                return;
            }
        }

        Frequency freq = new SecurityFrequency(owner).setPublic(true);
        freq.activeCoords.add(Coord4D.get(this));
        manager.addFrequency(freq);
        frequency = (SecurityFrequency) freq;
        markDirty(false);
        sendUpdatePacket();
    }

    public void toggleOverride() {
        if (frequency != null) {
            frequency.override = !frequency.override;
            markDirty(false);
        }
    }

    public void removeTrusted(int index) {
        if (frequency != null) {
            frequency.removeTrusted(index);
            markDirty(false);
        }
    }

    public void setSecurityMode(SecurityMode mode) {
        if (frequency != null) {
            frequency.securityMode = mode;
            markDirty(false);
        }
    }

    public void addTrusted(String name) {
        if (frequency != null) {
            GameProfile profile = ServerLifecycleHooks.getCurrentServer().getPlayerProfileCache().getGameProfileForUsername(name);
            if (profile != null) {
                frequency.addTrusted(profile.getId(), profile.getName());
                markDirty(false);
            }
        }
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        NBTUtils.setUUIDIfPresent(nbtTags, NBTConstants.OWNER_UUID, uuid -> ownerUUID = uuid);
        if (nbtTags.contains(NBTConstants.FREQUENCY, NBT.TAG_COMPOUND)) {
            frequency = new SecurityFrequency(nbtTags.getCompound(NBTConstants.FREQUENCY), false);
            frequency.valid = false;
        }
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        if (ownerUUID != null) {
            nbtTags.putUniqueId(NBTConstants.OWNER_UUID, ownerUUID);
        }
        if (frequency != null) {
            CompoundNBT frequencyTag = new CompoundNBT();
            frequency.write(frequencyTag);
            nbtTags.put(NBTConstants.FREQUENCY, frequencyTag);
        }
        return nbtTags;
    }

    @Override
    public void remove() {
        super.remove();
        if (!isRemote()) {
            if (frequency != null) {
                FrequencyManager manager = getManager(frequency);
                if (manager != null) {
                    manager.deactivate(Coord4D.get(this));
                }
            }
        }
    }

    @Override
    public void onPlace() {
        MekanismUtils.makeBoundingBlock(getWorld(), getPos().up(), getPos());
    }

    @Override
    public void onBreak() {
        World world = getWorld();
        if (world != null) {
            world.removeBlock(getPos().up(), false);
            world.removeBlock(getPos(), false);
        }
    }

    @Override
    public Frequency getFrequency(FrequencyManager manager) {
        if (manager == Mekanism.securityFrequencies) {
            return frequency;
        }
        return null;
    }

    @Nonnull
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(pos, pos.add(1, 2, 1));
    }

    @Override
    public boolean isCapabilityDisabled(@Nonnull Capability<?> capability, Direction side) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            //Even though there are inventory slots make this return none as accessible by automation, as then people could lock items to other
            // people unintentionally
            return true;
        }
        return super.isCapabilityDisabled(capability, side);
    }

    @Nonnull
    @Override
    public CompoundNBT getReducedUpdateTag() {
        CompoundNBT updateTag = super.getReducedUpdateTag();
        if (ownerUUID != null) {
            updateTag.putUniqueId(NBTConstants.OWNER_UUID, ownerUUID);
            updateTag.putString(NBTConstants.OWNER_NAME, MekanismUtils.getLastKnownUsername(ownerUUID));
        }
        if (frequency != null) {
            CompoundNBT frequencyTag = new CompoundNBT();
            frequency.writeToUpdateTag(frequencyTag);
            updateTag.put(NBTConstants.FREQUENCY, frequencyTag);
        }
        return updateTag;
    }

    @Override
    public void handleUpdateTag(@Nonnull CompoundNBT tag) {
        super.handleUpdateTag(tag);
        NBTUtils.setUUIDIfPresent(tag, NBTConstants.OWNER_UUID, uuid -> ownerUUID = uuid);
        NBTUtils.setStringIfPresent(tag, NBTConstants.OWNER_NAME, uuid -> clientOwner = uuid);
        NBTUtils.setCompoundIfPresent(tag, NBTConstants.FREQUENCY, nbt -> frequency = new SecurityFrequency(nbt, true));
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableBoolean.create(() -> frequency != null && frequency.override, value -> {
            if (frequency != null) {
                frequency.override = value;
            }
        }));
        container.track(SyncableEnum.create(SecurityMode::byIndexStatic, SecurityMode.PUBLIC, () -> frequency == null ? SecurityMode.PUBLIC : frequency.securityMode,
              value -> {
                  if (frequency != null) {
                      frequency.securityMode = value;
                  }
              }));
        container.track(SyncableStringList.create(() -> frequency == null ? Collections.emptyList() : frequency.trustedCache, value -> {
            if (frequency != null) {
                frequency.trustedCache = value;
            }
        }));
    }
}