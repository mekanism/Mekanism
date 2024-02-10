package mekanism.common.attachments.component;

import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import mekanism.api.NBTConstants;
import mekanism.api.RelativeSide;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeSideConfig;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.registries.MekanismAttachmentTypes;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.component.config.IPersistentConfigInfo;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public final class AttachedSideConfig implements IAttachedComponent<TileComponentConfig> {

    public static AttachedSideConfig create(IAttachmentHolder attachmentHolder) {
        if (attachmentHolder instanceof ItemStack stack && !stack.isEmpty() && stack.getItem() instanceof BlockItem blockItem) {
            AttributeSideConfig sideConfig = Attribute.get(blockItem.getBlock(), AttributeSideConfig.class);
            if (sideConfig != null) {
                return new AttachedSideConfig(stack, sideConfig.supportedTypes());
            }
        }
        throw new IllegalArgumentException("Attempted to attach side config awareness to an object that does not support having a side config.");
    }

    @Nullable
    public static IPersistentConfigInfo getStoredConfigInfo(ItemStack stack, TransmissionType transmissionType) {
        if (stack.hasData(MekanismAttachmentTypes.SIDE_CONFIG)) {
            return stack.getData(MekanismAttachmentTypes.SIDE_CONFIG).getConfig(transmissionType);
        }
        return null;
    }

    private final Map<TransmissionType, LightConfigInfo> configInfo = new EnumMap<>(TransmissionType.class);

    private AttachedSideConfig(ItemStack stack, Set<TransmissionType> types) {
        for (TransmissionType type : types) {
            configInfo.put(type, new LightConfigInfo());
        }
        loadLegacyData(stack);
    }

    @Deprecated//TODO - 1.21: Remove this legacy way of loading data
    private void loadLegacyData(ItemStack stack) {
        ItemDataUtils.getAndRemoveData(stack, NBTConstants.COMPONENT_CONFIG, CompoundTag::getCompound).ifPresent(this::deserializeNBT);
    }

    public boolean isCompatible(AttachedSideConfig other) {
        return other == this || configInfo.equals(other.configInfo);
    }

    @Nullable
    public IPersistentConfigInfo getConfig(TransmissionType transmissionType) {
        return this.configInfo.get(transmissionType);
    }

    @Nullable
    @Override
    public CompoundTag serializeNBT() {
        //Note: We can't just use TileComponentConfig#write as we don't want to write defaulted data so that fresh stacks don't set anything
        CompoundTag configNBT = new CompoundTag();
        for (Entry<TransmissionType, LightConfigInfo> entry : configInfo.entrySet()) {
            TransmissionType type = entry.getKey();
            LightConfigInfo info = entry.getValue();
            if (info.ejecting != null) {
                configNBT.putBoolean(NBTConstants.EJECT + type.ordinal(), info.isEjecting());
            }
            if (!info.sideConfig.isEmpty()) {
                CompoundTag sideConfig = new CompoundTag();
                for (RelativeSide side : EnumUtils.SIDES) {
                    NBTUtils.writeEnum(sideConfig, NBTConstants.SIDE + side.ordinal(), info.getDataType(side));
                }
                configNBT.put(NBTConstants.CONFIG + type.ordinal(), sideConfig);
            }
        }
        return configNBT.isEmpty() ? null : configNBT;
    }

    @Override
    public void deserializeNBT(CompoundTag configNBT) {
        TileComponentConfig.read(configNBT, configInfo);
    }

    private static class LightConfigInfo implements IPersistentConfigInfo {

        private final Map<RelativeSide, DataType> sideConfig = new EnumMap<>(RelativeSide.class);
        @Nullable
        private Boolean ejecting;

        public LightConfigInfo() {
        }

        @NotNull
        @Override
        public DataType getDataType(@NotNull RelativeSide side) {
            return sideConfig.getOrDefault(side, DataType.NONE);
        }

        @Override
        public boolean setDataType(@NotNull DataType dataType, @NotNull RelativeSide side) {
            return sideConfig.put(side, dataType) != dataType;
        }

        @Override
        public boolean isEjecting() {
            return ejecting != null && ejecting;
        }

        @Override
        public void setEjecting(boolean ejecting) {
            this.ejecting = ejecting;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            } else if (object == null || getClass() != object.getClass()) {
                return false;
            }
            LightConfigInfo other = (LightConfigInfo) object;
            return Objects.equals(ejecting, other.ejecting) && Objects.equals(sideConfig, other.sideConfig);
        }

        @Override
        public int hashCode() {
            return Objects.hash(sideConfig, ejecting);
        }
    }
}