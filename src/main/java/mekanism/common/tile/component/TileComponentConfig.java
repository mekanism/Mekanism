package mekanism.common.tile.component;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.NBTConstants;
import mekanism.api.RelativeSide;
import mekanism.api.TileNetworkList;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.base.ITileComponent;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.component.config.slot.ISlotInfo;
import mekanism.common.util.EnumUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;

public class TileComponentConfig implements ITileComponent {

    public TileEntityMekanism tile;
    private Map<TransmissionType, ConfigInfo> configInfo = new EnumMap<>(TransmissionType.class);
    //TODO: See if we can come up with a way of not needing this. The issue is we want this to be sorted, but getting the keyset of configInfo doesn't work for us
    private List<TransmissionType> transmissionTypes = new ArrayList<>();

    public TileComponentConfig(TileEntityMekanism tile, TransmissionType... types) {
        this.tile = tile;
        for (TransmissionType type : types) {
            addSupported(type);
        }
        tile.addComponent(this);
    }

    private RelativeSide getSide(Direction direction) {
        //TODO: Instead of getDirection this should use ISideConfiguration#getOrientation
        return RelativeSide.fromDirections(tile.getDirection(), direction);
    }

    public List<TransmissionType> getTransmissions() {
        return transmissionTypes;
    }

    public void addSupported(TransmissionType type) {
        if (!configInfo.containsKey(type)) {
            //TODO: ISideConfiguration#getOrientation?
            configInfo.put(type, new ConfigInfo(() -> tile.getDirection()));
            transmissionTypes.add(type);
        }
    }

    public boolean isCapabilityDisabled(@Nonnull Capability<?> capability, Direction side) {
        TransmissionType type = null;
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            type = TransmissionType.ITEM;
        } else if (capability == Capabilities.GAS_HANDLER_CAPABILITY) {
            type = TransmissionType.GAS;
        } else if (capability == Capabilities.HEAT_TRANSFER_CAPABILITY) {
            type = TransmissionType.HEAT;
        } else if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            type = TransmissionType.FLUID;
        } else if (capability == CapabilityEnergy.ENERGY || capability == Capabilities.ENERGY_STORAGE_CAPABILITY ||
                   capability == Capabilities.ENERGY_ACCEPTOR_CAPABILITY || capability == Capabilities.ENERGY_OUTPUTTER_CAPABILITY) {
            //While we strictly speaking don't need to check this for energy because of baseline checks, we do so anyways as it makes the logic easier to follow
            type = TransmissionType.ENERGY;
        }
        if (type != null) {
            ConfigInfo info = getConfig(type);
            if (info != null && side != null) {
                //If we support this config type and we have a side so are not the read only "internal" check
                ISlotInfo slotInfo = info.getSlotInfo(getSide(side));
                //Return that it is disabled:
                // If we don't know how to handle the data type that is on that side config (such as for NONE)
                // or the slot is not enabled then return that it is disabled
                return slotInfo == null || !slotInfo.isEnabled();
            }
        }
        return false;
    }

    @Nullable
    public ConfigInfo getConfig(TransmissionType type) {
        return configInfo.get(type);
    }

    @Nullable
    public DataType getDataType(TransmissionType type, RelativeSide side) {
        ConfigInfo info = getConfig(type);
        if (info != null) {
            return info.getDataType(side);
        }
        return null;
    }

    //TODO: Use relative side where possible?
    @Nullable
    public ISlotInfo getSlotInfo(TransmissionType type, Direction direction) {
        if (direction == null) {
            return null;
        }
        ConfigInfo info = getConfig(type);
        if (info != null) {
            return info.getSlotInfo(getSide(direction));
        }
        return null;
    }

    public boolean supports(TransmissionType type) {
        return configInfo.containsKey(type);
    }

    @Override
    public void tick() {
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        if (nbtTags.contains(NBTConstants.COMPONENT_CONFIG, NBT.TAG_COMPOUND)) {
            CompoundNBT configNBT = nbtTags.getCompound(NBTConstants.COMPONENT_CONFIG);
            if (configNBT.getBoolean(NBTConstants.SIDE_DATA)) {
                for (Entry<TransmissionType, ConfigInfo> entry : configInfo.entrySet()) {
                    TransmissionType type = entry.getKey();
                    ConfigInfo info = entry.getValue();
                    info.setEjecting(configNBT.getBoolean(NBTConstants.EJECTING + type.ordinal()));
                    CompoundNBT sideConfig = configNBT.getCompound(NBTConstants.CONFIG + type.ordinal());
                    for (RelativeSide side : EnumUtils.SIDES) {
                        info.setDataType(side, DataType.byIndexStatic(sideConfig.getInt(NBTConstants.SIDE + side.ordinal())));
                    }
                }
            }
        }
    }

    @Override
    public void read(PacketBuffer dataStream) {
        //TODO: Let the ConfigInfo handle what info it sends/reads?
        int amount = dataStream.readInt();
        for (int i = 0; i < amount; i++) {
            TransmissionType type = dataStream.readEnumValue(TransmissionType.class);
            ConfigInfo info = getConfig(type);
            if (info == null) {
                //TODO: log some error?
                //TODO: ISideConfiguration#getOrientation?
                info = new ConfigInfo(() -> tile.getDirection());
                configInfo.put(type, info);
            }
            info.setEjecting(dataStream.readBoolean());
            for (RelativeSide side : EnumUtils.SIDES) {
                info.setDataType(side, dataStream.readEnumValue(DataType.class));
            }
        }
    }

    @Override
    public void write(CompoundNBT nbtTags) {
        CompoundNBT configNBT = new CompoundNBT();
        for (Entry<TransmissionType, ConfigInfo> entry : configInfo.entrySet()) {
            TransmissionType type = entry.getKey();
            ConfigInfo info = entry.getValue();
            configNBT.putBoolean(NBTConstants.EJECTING + type.ordinal(), info.isEjecting());
            CompoundNBT sideConfig = new CompoundNBT();
            for (RelativeSide side : EnumUtils.SIDES) {
                sideConfig.putInt(NBTConstants.SIDE + side.ordinal(), info.getDataType(side).ordinal());
            }
            configNBT.put(NBTConstants.CONFIG + type.ordinal(), sideConfig);
        }
        configNBT.putBoolean(NBTConstants.SIDE_DATA, true);
        nbtTags.put(NBTConstants.COMPONENT_CONFIG, configNBT);
    }

    @Override
    public void write(TileNetworkList data) {
        data.add(configInfo.size());
        for (Entry<TransmissionType, ConfigInfo> entry : configInfo.entrySet()) {
            TransmissionType type = entry.getKey();
            ConfigInfo info = entry.getValue();
            data.add(type);
            data.add(info.isEjecting());
            for (RelativeSide side : EnumUtils.SIDES) {
                data.add(info.getDataType(side));
            }
        }
    }

    @Override
    public void invalidate() {
    }

    @Override
    public void trackForMainContainer(MekanismContainer container) {
    }

    //TODO: Should we inline this?
    public boolean isEjecting(TransmissionType type) {
        ConfigInfo info = getConfig(type);
        return info != null && info.isEjecting();
    }
}