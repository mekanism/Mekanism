package mekanism.common.tile.component;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.RelativeSide;
import mekanism.api.TileNetworkList;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.base.ITileComponent;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.component.config.slot.ISlotInfo;
import mekanism.common.util.EnumUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;

public class TileComponentConfig implements ITileComponent {

    public TileEntityMekanism tileEntity;
    private Map<TransmissionType, ConfigInfo> configInfo = new EnumMap<>(TransmissionType.class);

    public TileComponentConfig(TileEntityMekanism tile, TransmissionType... types) {
        tileEntity = tile;
        for (TransmissionType type : types) {
            addSupported(type);
        }
        tile.addComponent(this);
    }

    private RelativeSide getSide(Direction direction) {
        //TODO: Instead of getDirection this should use ISideConfiguration#getOrientation
        return RelativeSide.fromDirections(tileEntity.getDirection(), direction);
    }

    public void readFrom(TileComponentConfig config) {
        configInfo = config.configInfo;
    }

    public List<TransmissionType> getTransmissions() {
        //TODO: Do this better given the side config uses this for figuring out tab order
        return new ArrayList<>(configInfo.keySet());
    }

    public void addSupported(TransmissionType type) {
        if (!configInfo.containsKey(type)) {
            //TODO: ISideConfiguration#getOrientation?
            configInfo.put(type, new ConfigInfo(() -> tileEntity.getDirection()));
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
        }
        //Energy is handled by the TileEntityElectricBlock anyways in the super clauses so no need to bother with it
        //TODO: We actually might as well deal with energy here?
        if (type != null && supports(type)) {
            ISlotInfo slotInfo = getSlotInfo(type, side);
            return slotInfo == null || !slotInfo.isEnabled();
        }
        return false;
    }

    public boolean canEject(TransmissionType type) {
        ConfigInfo info = getConfig(type);
        return info != null && info.canEject();
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
        if (nbtTags.getBoolean("sideDataStored")) {
            for (Entry<TransmissionType, ConfigInfo> entry : configInfo.entrySet()) {
                TransmissionType type = entry.getKey();
                ConfigInfo info = entry.getValue();
                info.setEjecting(nbtTags.getBoolean("ejecting" + type.ordinal()));
                CompoundNBT sideConfig = nbtTags.getCompound("config" + type.ordinal());
                for (RelativeSide side : EnumUtils.SIDES) {
                    info.setDataType(side, DataType.byIndex(sideConfig.getInt("side" + side.ordinal())));
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
                info = new ConfigInfo(() -> tileEntity.getDirection());
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
        for (Entry<TransmissionType, ConfigInfo> entry : configInfo.entrySet()) {
            TransmissionType type = entry.getKey();
            ConfigInfo info = entry.getValue();
            nbtTags.putBoolean("ejecting" + type.ordinal(), info.isEjecting());
            CompoundNBT sideConfig = new CompoundNBT();
            for (RelativeSide side : EnumUtils.SIDES) {
                sideConfig.putInt("side" + side.ordinal(), info.getDataType(side).ordinal());
            }
            nbtTags.put("config" + type.ordinal(), sideConfig);
        }
        nbtTags.putBoolean("sideDataStored", true);
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

    //TODO: Should we inline this?
    public boolean isEjecting(TransmissionType type) {
        ConfigInfo info = getConfig(type);
        return info != null && info.isEjecting();
    }
}