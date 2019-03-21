package mekanism.common.tile.component;

import io.netty.buffer.ByteBuf;
import java.util.UUID;
import mekanism.api.Coord4D;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.base.ITileComponent;
import mekanism.api.TileNetworkList;
import mekanism.common.config.MekanismConfig.general;
import mekanism.common.frequency.Frequency;
import mekanism.common.frequency.FrequencyManager;
import mekanism.common.security.ISecurityTile.SecurityMode;
import mekanism.common.security.SecurityFrequency;
import mekanism.common.tile.prefab.TileEntityContainerBlock;
import mekanism.common.util.MekanismUtils;
import net.minecraft.nbt.NBTTagCompound;

public class TileComponentSecurity implements ITileComponent {

    /**
     * TileEntity implementing this component.
     */
    public TileEntityContainerBlock tileEntity;

    private UUID ownerUUID;
    private String clientOwner;

    private SecurityMode securityMode = SecurityMode.PUBLIC;

    private SecurityFrequency frequency;

    public TileComponentSecurity(TileEntityContainerBlock tile) {
        tileEntity = tile;

        tile.components.add(this);
    }

    public void readFrom(TileComponentSecurity security) {
        ownerUUID = security.ownerUUID;
        securityMode = security.securityMode;
    }

    public SecurityFrequency getFrequency() {
        return frequency;
    }

    public void setFrequency(UUID owner) {
        FrequencyManager manager = Mekanism.securityFrequencies;
        manager.deactivate(Coord4D.get(tileEntity));

        for (Frequency freq : manager.getFrequencies()) {
            if (freq.ownerUUID.equals(owner)) {
                frequency = (SecurityFrequency) freq;
                frequency.activeCoords.add(Coord4D.get(tileEntity));

                return;
            }
        }

        Frequency freq = new SecurityFrequency(owner).setPublic(true);
        freq.activeCoords.add(Coord4D.get(tileEntity));
        manager.addFrequency(freq);
        frequency = (SecurityFrequency) freq;

        MekanismUtils.saveChunk(tileEntity);
        tileEntity.markDirty();
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public void setOwnerUUID(UUID uuid) {
        frequency = null;
        ownerUUID = uuid;
    }

    public String getClientOwner() {
        return clientOwner;
    }

    public SecurityMode getMode() {
        if (general.allowProtection) {
            return securityMode;
        } else {
            return SecurityMode.PUBLIC;
        }
    }

    public void setMode(SecurityMode mode) {
        securityMode = mode;
    }

    public FrequencyManager getManager(Frequency freq) {
        if (ownerUUID == null || freq == null) {
            return null;
        }

        return Mekanism.securityFrequencies;
    }

    @Override
    public void tick() {
        if (!tileEntity.getWorld().isRemote) {
            if (frequency == null && ownerUUID != null) {
                setFrequency(ownerUUID);
            }

            FrequencyManager manager = getManager(frequency);

            if (manager != null) {
                if (frequency != null && !frequency.valid) {
                    frequency = (SecurityFrequency) manager
                          .validateFrequency(ownerUUID, Coord4D.get(tileEntity), frequency);
                }

                if (frequency != null) {
                    frequency = (SecurityFrequency) manager.update(Coord4D.get(tileEntity), frequency);
                }
            } else {
                frequency = null;
            }
        }
    }

    @Override
    public void read(NBTTagCompound nbtTags) {
        securityMode = SecurityMode.values()[nbtTags.getInteger("securityMode")];

        if (nbtTags.hasKey("ownerUUID")) {
            ownerUUID = UUID.fromString(nbtTags.getString("ownerUUID"));
        }

        if (nbtTags.hasKey("securityFreq")) {
            frequency = new SecurityFrequency(nbtTags.getCompoundTag("securityFreq"));
            frequency.valid = false;
        }
    }

    @Override
    public void read(ByteBuf dataStream) {
        securityMode = SecurityMode.values()[dataStream.readInt()];

        if (dataStream.readBoolean()) {
            ownerUUID = UUID.fromString(PacketHandler.readString(dataStream));
            clientOwner = PacketHandler.readString(dataStream);
        } else {
            ownerUUID = null;
            clientOwner = null;
        }

        if (dataStream.readBoolean()) {
            frequency = new SecurityFrequency(dataStream);
        } else {
            frequency = null;
        }
    }

    @Override
    public void write(NBTTagCompound nbtTags) {
        nbtTags.setInteger("securityMode", securityMode.ordinal());

        if (ownerUUID != null) {
            nbtTags.setString("ownerUUID", ownerUUID.toString());
        }

        if (frequency != null) {
            NBTTagCompound frequencyTag = new NBTTagCompound();
            frequency.write(frequencyTag);
            nbtTags.setTag("securityFreq", frequencyTag);
        }
    }

    @Override
    public void write(TileNetworkList data) {
        data.add(securityMode.ordinal());

        if (ownerUUID != null) {
            data.add(true);
            data.add(ownerUUID.toString());
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
    }

    @Override
    public void invalidate() {
        if (!tileEntity.getWorld().isRemote) {
            if (frequency != null) {
                FrequencyManager manager = getManager(frequency);

                if (manager != null) {
                    manager.deactivate(Coord4D.get(tileEntity));
                }
            }
        }
    }
}
