package mekanism.common.attachments.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import mekanism.api.NBTConstants;
import mekanism.api.RelativeSide;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.component.config.IPersistentConfigInfo;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public record AttachedSideConfig(Map<TransmissionType, LightConfigInfo> configInfo) {

    public static final Codec<AttachedSideConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
          Codec.unboundedMap(TransmissionType.CODEC, LightConfigInfo.CODEC).fieldOf(NBTConstants.CONFIG).forGetter(AttachedSideConfig::configInfo)
    ).apply(instance, AttachedSideConfig::new));
    public static final StreamCodec<ByteBuf, AttachedSideConfig> STREAM_CODEC = ByteBufCodecs.<ByteBuf, TransmissionType, LightConfigInfo, Map<TransmissionType, LightConfigInfo>>map(
                i -> new EnumMap<>(TransmissionType.class), TransmissionType.STREAM_CODEC, LightConfigInfo.STREAM_CODEC)
          .map(AttachedSideConfig::new, AttachedSideConfig::configInfo);

    public static AttachedSideConfig create(Map<TransmissionType, ConfigInfo> configInfo) {
        Map<TransmissionType, LightConfigInfo> lightConfigInfo = new EnumMap<>(TransmissionType.class);
        for (Map.Entry<TransmissionType, ConfigInfo> entry : configInfo.entrySet()) {
            ConfigInfo info = entry.getValue();
            Map<RelativeSide, DataType> sideConfig = new EnumMap<>(RelativeSide.class);
            for (Map.Entry<RelativeSide, DataType> sideConfigEntry : info.getSideConfig()) {
                sideConfig.put(sideConfigEntry.getKey(), sideConfigEntry.getValue());
            }
            lightConfigInfo.put(entry.getKey(), new LightConfigInfo(sideConfig, info.isEjecting()));
        }
        return new AttachedSideConfig(lightConfigInfo);
    }

    public AttachedSideConfig {
        //Make the map unmodifiable to ensure we don't accidentally mutate it
        configInfo = Collections.unmodifiableMap(configInfo);
    }

    @Nullable
    public static IPersistentConfigInfo getStoredConfigInfo(ItemStack stack, TransmissionType transmissionType) {
        AttachedSideConfig existingData = stack.get(MekanismDataComponents.SIDE_CONFIG);
        if (existingData == null) {
            return null;
        }
        LightConfigInfo config = existingData.configInfo.get(transmissionType);
        return config.sideConfig.isEmpty() ? null : config;
    }

    @Nullable
    public IPersistentConfigInfo getConfig(TransmissionType transmissionType) {
        return this.configInfo.get(transmissionType);
    }

    public record LightConfigInfo(Map<RelativeSide, DataType> sideConfig, @Nullable Boolean ejecting) implements IPersistentConfigInfo {

        public static final Codec<LightConfigInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
              Codec.unboundedMap(RelativeSide.CODEC, DataType.CODEC).fieldOf(NBTConstants.SIDE).forGetter(LightConfigInfo::sideConfig),
              Codec.BOOL.optionalFieldOf(NBTConstants.EJECT).forGetter(c -> Optional.ofNullable(c.ejecting()))
        ).apply(instance, (sideConfig, ejecting) -> new LightConfigInfo(sideConfig, ejecting.orElse(null))));
        public static final StreamCodec<ByteBuf, LightConfigInfo> STREAM_CODEC = StreamCodec.composite(
              ByteBufCodecs.map(i -> new EnumMap<>(RelativeSide.class), RelativeSide.STREAM_CODEC, DataType.STREAM_CODEC), LightConfigInfo::sideConfig,
              ByteBufCodecs.optional(ByteBufCodecs.BOOL), c -> Optional.ofNullable(c.ejecting()),
              (sideConfig, ejecting) -> new LightConfigInfo(sideConfig, ejecting.orElse(null))
        );

        public LightConfigInfo {
            //Make the map unmodifiable to ensure we don't accidentally mutate it
            sideConfig = Collections.unmodifiableMap(sideConfig);
        }

        @NotNull
        @Override
        public DataType getDataType(@NotNull RelativeSide side) {
            return sideConfig.getOrDefault(side, DataType.NONE);
        }

        @Override
        public boolean isEjecting() {
            return ejecting != null && ejecting;
        }
    }
}