package mekanism.common.tile.component;

import java.util.UUID;
import mekanism.api.Coord4D;
import mekanism.api.NBTConstants;
import mekanism.api.TileNetworkList;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.base.ITileComponent;
import mekanism.common.config.MekanismConfig;
import mekanism.common.frequency.Frequency;
import mekanism.common.frequency.FrequencyManager;
import mekanism.common.security.ISecurityTile.SecurityMode;
import mekanism.common.security.SecurityFrequency;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.util.Constants.NBT;

public class TileComponentSecurity implements ITileComponent {

    /**
     * TileEntity implementing this component.
     */
    public TileEntityMekanism tile;

    private UUID ownerUUID;
    private String clientOwner;

    private SecurityMode securityMode = SecurityMode.PUBLIC;

    private SecurityFrequency frequency;

    public TileComponentSecurity(TileEntityMekanism tile) {
        this.tile = tile;
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
        manager.deactivate(Coord4D.get(tile));

        for (Frequency freq : manager.getFrequencies()) {
            if (freq.ownerUUID.equals(owner)) {
                frequency = (SecurityFrequency) freq;
                frequency.activeCoords.add(Coord4D.get(tile));
                return;
            }
        }

        Frequency freq = new SecurityFrequency(owner).setPublic(true);
        freq.activeCoords.add(Coord4D.get(tile));
        manager.addFrequency(freq);
        frequency = (SecurityFrequency) freq;

        MekanismUtils.saveChunk(tile);
        tile.markDirty();
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
        if (!tile.isRemote()) {
            if (frequency == null && ownerUUID != null) {
                setFrequency(ownerUUID);
            }
            FrequencyManager manager = getManager(frequency);

            if (manager != null) {
                if (frequency != null && !frequency.valid) {
                    frequency = (SecurityFrequency) manager.validateFrequency(ownerUUID, Coord4D.get(tile), frequency);
                }
                if (frequency != null) {
                    frequency = (SecurityFrequency) manager.update(Coord4D.get(tile), frequency);
                }
            } else {
                frequency = null;
            }
        }
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        if (nbtTags.contains(NBTConstants.COMPONENT_SECURITY, NBT.TAG_COMPOUND)) {
            CompoundNBT securityNBT = nbtTags.getCompound(NBTConstants.COMPONENT_SECURITY);
            NBTUtils.setEnumIfPresent(securityNBT, NBTConstants.SECURITY_MODE, SecurityMode::byIndexStatic, mode -> securityMode = mode);
            NBTUtils.setUUIDIfPresent(securityNBT, NBTConstants.OWNER_UUID, uuid -> ownerUUID = uuid);
            if (securityNBT.contains(NBTConstants.FREQUENCY, NBT.TAG_COMPOUND)) {
                frequency = new SecurityFrequency(securityNBT.getCompound(NBTConstants.FREQUENCY));
                frequency.valid = false;
            }
        }
    }

    @Override
    public void read(PacketBuffer dataStream) {
        securityMode = dataStream.readEnumValue(SecurityMode.class);

        if (dataStream.readBoolean()) {
            ownerUUID = dataStream.readUniqueId();
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
    public void write(CompoundNBT nbtTags) {
        CompoundNBT securityNBT = new CompoundNBT();
        securityNBT.putInt(NBTConstants.SECURITY_MODE, securityMode.ordinal());
        if (ownerUUID != null) {
            nbtTags.putUniqueId(NBTConstants.OWNER_UUID, ownerUUID);
        }
        if (frequency != null) {
            CompoundNBT frequencyTag = new CompoundNBT();
            frequency.write(frequencyTag);
            securityNBT.put(NBTConstants.FREQUENCY, frequencyTag);
        }
        nbtTags.put(NBTConstants.COMPONENT_SECURITY, securityNBT);
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
        if (!tile.isRemote()) {
            if (frequency != null) {
                FrequencyManager manager = getManager(frequency);
                if (manager != null) {
                    manager.deactivate(Coord4D.get(tile));
                }
            }
        }
    }
}