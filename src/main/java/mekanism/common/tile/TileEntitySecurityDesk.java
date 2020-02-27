package mekanism.common.tile;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.api.TileNetworkList;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.base.IBoundingBlock;
import mekanism.common.frequency.Frequency;
import mekanism.common.frequency.FrequencyManager;
import mekanism.common.inventory.slot.SecurityInventorySlot;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.network.PacketSecurityUpdate;
import mekanism.common.network.PacketSecurityUpdate.SecurityPacket;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.security.IOwnerItem;
import mekanism.common.security.ISecurityItem;
import mekanism.common.security.SecurityData;
import mekanism.common.security.SecurityFrequency;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
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
        doAutoSync = true;
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
    public void onUpdate() {
        if (!isRemote()) {
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
                return;
            }
        }

        Frequency freq = new SecurityFrequency(owner).setPublic(true);
        freq.activeCoords.add(Coord4D.get(this));
        manager.addFrequency(freq);
        frequency = (SecurityFrequency) freq;
        MekanismUtils.saveChunk(this);
        markDirty();
    }

    @Override
    public void handlePacketData(PacketBuffer dataStream) {
        if (!isRemote()) {
            int type = dataStream.readInt();
            if (type == 0) {
                if (frequency != null) {
                    GameProfile profile = ServerLifecycleHooks.getCurrentServer().getPlayerProfileCache().getGameProfileForUsername(PacketHandler.readString(dataStream));
                    if (profile != null) {
                        frequency.trusted.add(profile.getId());
                    }
                }
            } else if (type == 1) {
                if (frequency != null) {
                    frequency.trusted.remove(dataStream.readUniqueId());
                }
            } else if (type == 2) {
                if (frequency != null) {
                    frequency.override = !frequency.override;
                    Mekanism.packetHandler.sendToAll(new PacketSecurityUpdate(SecurityPacket.UPDATE, ownerUUID, new SecurityData(frequency)));
                }
            } else if (type == 3) {
                if (frequency != null) {
                    frequency.securityMode = dataStream.readEnumValue(SecurityMode.class);
                    Mekanism.packetHandler.sendToAll(new PacketSecurityUpdate(SecurityPacket.UPDATE, ownerUUID, new SecurityData(frequency)));
                }
            }
            MekanismUtils.saveChunk(this);
            return;
        }

        super.handlePacketData(dataStream);

        if (isRemote()) {
            if (dataStream.readBoolean()) {
                clientOwner = PacketHandler.readString(dataStream);
                ownerUUID = dataStream.readUniqueId();
            } else {
                clientOwner = null;
                ownerUUID = null;
            }
            if (dataStream.readBoolean()) {
                frequency = new SecurityFrequency(dataStream);
            } else {
                frequency = null;
            }
        }
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        if (nbtTags.contains("ownerUUID")) {
            ownerUUID = UUID.fromString(nbtTags.getString("ownerUUID"));
        }
        if (nbtTags.contains("frequency")) {
            frequency = new SecurityFrequency(nbtTags.getCompound("frequency"));
            frequency.valid = false;
        }
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        if (ownerUUID != null) {
            nbtTags.putString("ownerUUID", ownerUUID.toString());
        }
        if (frequency != null) {
            CompoundNBT frequencyTag = new CompoundNBT();
            frequency.write(frequencyTag);
            nbtTags.put("frequency", frequencyTag);
        }
        return nbtTags;
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);
        if (ownerUUID == null) {
            data.add(false);
        } else {
            data.add(true);
            data.add(MekanismUtils.getLastKnownUsername(ownerUUID));
            data.add(ownerUUID);
        }
        //TODO: Make it so we can sync the frequency via the container sync stuff
        if (frequency == null) {
            data.add(false);
        } else {
            data.add(true);
            frequency.write(data);
        }
        return data;
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
        return INFINITE_EXTENT_AABB;
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
}