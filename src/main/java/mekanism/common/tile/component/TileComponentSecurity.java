package mekanism.common.tile.component;

import java.util.UUID;
import mekanism.api.Coord4D;
import mekanism.api.TileNetworkList;
import mekanism.common.Mekanism;
import mekanism.common.base.ITileComponent;
import mekanism.common.config.MekanismConfig;
import mekanism.common.frequency.Frequency;
import mekanism.common.frequency.FrequencyManager;
import mekanism.common.security.ISecurityTile.SecurityMode;
import mekanism.common.security.SecurityFrequency;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

public class TileComponentSecurity implements ITileComponent {

    /**
     * TileEntity implementing this component.
     */
    public TileEntityMekanism tileEntity;

    private UUID ownerUUID;
    private String clientOwner;

    private SecurityMode securityMode = SecurityMode.PUBLIC;

    private SecurityFrequency frequency;

    public TileComponentSecurity(TileEntityMekanism tile) {
        tileEntity = tile;
        tile.addComponent(this);
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
        if (MekanismConfig.general.allowProtection.get()) {
            return securityMode;
        }
        return SecurityMode.PUBLIC;
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
        if (!tileEntity.isRemote()) {
            if (frequency == null && ownerUUID != null) {
                setFrequency(ownerUUID);
            }
            FrequencyManager manager = getManager(frequency);

            if (manager != null) {
                if (frequency != null && !frequency.valid) {
                    frequency = (SecurityFrequency) manager.validateFrequency(ownerUUID, Coord4D.get(tileEntity), frequency);
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
    public void read(CompoundNBT nbtTags) {
        securityMode = EnumUtils.SECURITY_MODES[nbtTags.getInt("securityMode")];
        if (nbtTags.contains("ownerUUID")) {
            ownerUUID = UUID.fromString(nbtTags.getString("ownerUUID"));
        }
        if (nbtTags.contains("securityFreq")) {
            frequency = new SecurityFrequency(nbtTags.getCompound("securityFreq"));
            frequency.valid = false;
        }
    }

    @Override
    public void read(PacketBuffer dataStream) {
        securityMode = dataStream.readEnumValue(SecurityMode.class);

        if (dataStream.readBoolean()) {
            ownerUUID = dataStream.readUniqueId();
            clientOwner = dataStream.readString();
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
    public void write(CompoundNBT nbtTags) {
        nbtTags.putInt("securityMode", securityMode.ordinal());
        if (ownerUUID != null) {
            nbtTags.putString("ownerUUID", ownerUUID.toString());
        }
        if (frequency != null) {
            CompoundNBT frequencyTag = new CompoundNBT();
            frequency.write(frequencyTag);
            nbtTags.put("securityFreq", frequencyTag);
        }
    }

    @Override
    public void write(TileNetworkList data) {
        data.add(securityMode);

        if (ownerUUID != null) {
            data.add(true);
            data.add(ownerUUID);
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
        if (!tileEntity.isRemote()) {
            if (frequency != null) {
                FrequencyManager manager = getManager(frequency);
                if (manager != null) {
                    manager.deactivate(Coord4D.get(tileEntity));
                }
            }
        }
    }
}