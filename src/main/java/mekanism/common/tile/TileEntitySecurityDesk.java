package mekanism.common.tile;

import io.netty.buffer.ByteBuf;
import java.util.UUID;
import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.base.IBoundingBlock;
import mekanism.api.TileNetworkList;
import mekanism.common.frequency.Frequency;
import mekanism.common.frequency.FrequencyManager;
import mekanism.common.network.PacketSecurityUpdate.SecurityPacket;
import mekanism.common.network.PacketSecurityUpdate.SecurityUpdateMessage;
import mekanism.common.security.IOwnerItem;
import mekanism.common.security.ISecurityItem;
import mekanism.common.security.ISecurityTile.SecurityMode;
import mekanism.common.security.SecurityData;
import mekanism.common.security.SecurityFrequency;
import mekanism.common.tile.prefab.TileEntityContainerBlock;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntitySecurityDesk extends TileEntityContainerBlock implements IBoundingBlock {

    public UUID ownerUUID;
    public String clientOwner;

    public SecurityFrequency frequency;

    public TileEntitySecurityDesk() {
        super("SecurityDesk");

        inventory = NonNullList.withSize(2, ItemStack.EMPTY);
    }

    @Override
    public void onUpdate() {
        if (!world.isRemote) {
            if (ownerUUID != null && frequency != null) {
                if (!inventory.get(0).isEmpty() && inventory.get(0).getItem() instanceof IOwnerItem) {
                    IOwnerItem item = (IOwnerItem) inventory.get(0).getItem();

                    if (item.hasOwner(inventory.get(0)) && item.getOwnerUUID(inventory.get(0)) != null) {
                        if (item.getOwnerUUID(inventory.get(0)).equals(ownerUUID)) {
                            item.setOwnerUUID(inventory.get(0), null);

                            if (item instanceof ISecurityItem && ((ISecurityItem) item).hasSecurity(inventory.get(0))) {
                                ((ISecurityItem) item).setSecurity(inventory.get(0), SecurityMode.PUBLIC);
                            }
                        }
                    }
                }

                if (!inventory.get(1).isEmpty() && inventory.get(1).getItem() instanceof IOwnerItem) {
                    IOwnerItem item = (IOwnerItem) inventory.get(1).getItem();

                    if (item.hasOwner(inventory.get(1))) {
                        if (item.getOwnerUUID(inventory.get(1)) == null) {
                            item.setOwnerUUID(inventory.get(1), ownerUUID);
                        }

                        if (item.getOwnerUUID(inventory.get(1)).equals(ownerUUID)) {
                            if (item instanceof ISecurityItem && ((ISecurityItem) item).hasSecurity(inventory.get(1))) {
                                ((ISecurityItem) item).setSecurity(inventory.get(1), frequency.securityMode);
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
    public void handlePacketData(ByteBuf dataStream) {
        if (FMLCommonHandler.instance().getEffectiveSide().isServer()) {
            int type = dataStream.readInt();

            if (type == 0) {
                if (frequency != null) {
                    frequency.trusted.add(PacketHandler.readString(dataStream));
                }
            } else if (type == 1) {
                if (frequency != null) {
                    frequency.trusted.remove(PacketHandler.readString(dataStream));
                }
            } else if (type == 2) {
                if (frequency != null) {
                    frequency.override = !frequency.override;

                    Mekanism.packetHandler.sendToAll(
                          new SecurityUpdateMessage(SecurityPacket.UPDATE, ownerUUID, new SecurityData(frequency)));
                }
            } else if (type == 3) {
                if (frequency != null) {
                    frequency.securityMode = SecurityMode.values()[dataStream.readInt()];

                    Mekanism.packetHandler.sendToAll(
                          new SecurityUpdateMessage(SecurityPacket.UPDATE, ownerUUID, new SecurityData(frequency)));
                }
            }

            MekanismUtils.saveChunk(this);

            return;
        }

        super.handlePacketData(dataStream);

        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            if (dataStream.readBoolean()) {
                clientOwner = PacketHandler.readString(dataStream);
            } else {
                clientOwner = null;
            }

            if (dataStream.readBoolean()) {
                frequency = new SecurityFrequency(dataStream);
            } else {
                frequency = null;
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTags) {
        super.readFromNBT(nbtTags);

        if (nbtTags.hasKey("ownerUUID")) {
            ownerUUID = UUID.fromString(nbtTags.getString("ownerUUID"));
        }

        if (nbtTags.hasKey("frequency")) {
            frequency = new SecurityFrequency(nbtTags.getCompoundTag("frequency"));
            frequency.valid = false;
        }
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbtTags) {
        super.writeToNBT(nbtTags);

        if (ownerUUID != null) {
            nbtTags.setString("ownerUUID", ownerUUID.toString());
        }

        if (frequency != null) {
            NBTTagCompound frequencyTag = new NBTTagCompound();
            frequency.write(frequencyTag);
            nbtTags.setTag("frequency", frequencyTag);
        }

        return nbtTags;
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);

        if (ownerUUID != null) {
            data.add(true);
            data.add(MekanismUtils.getLastKnownUsername(ownerUUID));
        } else {
            data.add(false);
        }

        if (frequency != null) {
            data.add(true);
            frequency.write(data);
        } else {
            data.add(false);
        }

        return data;
    }

    @Override
    public void invalidate() {
        super.invalidate();

        if (!world.isRemote) {
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
        MekanismUtils.makeBoundingBlock(world, getPos().up(), Coord4D.get(this));
    }

    @Override
    public void onBreak() {
        world.setBlockToAir(getPos().up());
        world.setBlockToAir(getPos());
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
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return INFINITE_EXTENT_AABB;
    }
}
