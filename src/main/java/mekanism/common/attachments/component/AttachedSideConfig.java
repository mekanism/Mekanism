package mekanism.common.attachments.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import mekanism.api.RelativeSide;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.lib.codec.DroppingUnboundedMapCodec;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.component.config.IPersistentConfigInfo;
import mekanism.common.util.EnumUtils;
import net.minecraft.Util;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public record AttachedSideConfig(Map<TransmissionType, LightConfigInfo> configInfo) {

    public static final Codec<AttachedSideConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
          //TODO - 1.22: Switch this back to using Codec.unboundedMap
          new DroppingUnboundedMapCodec<>(TransmissionType.CODEC, LightConfigInfo.CODEC).fieldOf(SerializationConstants.CONFIG).forGetter(AttachedSideConfig::configInfo)
    ).apply(instance, AttachedSideConfig::new));
    public static final StreamCodec<ByteBuf, AttachedSideConfig> STREAM_CODEC = ByteBufCodecs.<ByteBuf, TransmissionType, LightConfigInfo, Map<TransmissionType, LightConfigInfo>>map(
                i -> new EnumMap<>(TransmissionType.class), TransmissionType.STREAM_CODEC, LightConfigInfo.STREAM_CODEC)
          .map(AttachedSideConfig::new, AttachedSideConfig::configInfo);

    public static final AttachedSideConfig ELECTRIC_MACHINE = Util.make(() -> {
        Map<TransmissionType, LightConfigInfo> configInfo = new EnumMap<>(TransmissionType.class);
        configInfo.put(TransmissionType.ITEM, LightConfigInfo.MACHINE);
        configInfo.put(TransmissionType.ENERGY, LightConfigInfo.INPUT_ONLY);
        return new AttachedSideConfig(configInfo);
    });
    public static final AttachedSideConfig EXTRA_MACHINE = Util.make(() -> {
        Map<TransmissionType, LightConfigInfo> configInfo = new EnumMap<>(TransmissionType.class);
        configInfo.put(TransmissionType.ITEM, LightConfigInfo.EXTRA_MACHINE);
        configInfo.put(TransmissionType.ENERGY, LightConfigInfo.INPUT_ONLY);
        return new AttachedSideConfig(configInfo);
    });

    public static final AttachedSideConfig ADVANCED_MACHINE = Util.make(() -> {
        Map<TransmissionType, LightConfigInfo> configInfo = new EnumMap<>(TransmissionType.class);
        configInfo.put(TransmissionType.ITEM, LightConfigInfo.EXTRA_MACHINE);
        //if allow extracting, set output side as right
        configInfo.put(TransmissionType.CHEMICAL, LightConfigInfo.OUT_NO_EJECT);
        configInfo.put(TransmissionType.ENERGY, LightConfigInfo.INPUT_ONLY);
        return new AttachedSideConfig(configInfo);
    });
    public static final AttachedSideConfig ADVANCED_MACHINE_INPUT_ONLY = Util.make(() -> {
        Map<TransmissionType, LightConfigInfo> configInfo = new EnumMap<>(TransmissionType.class);
        configInfo.put(TransmissionType.ITEM, LightConfigInfo.EXTRA_MACHINE);
        configInfo.put(TransmissionType.CHEMICAL, LightConfigInfo.INPUT_ONLY);
        configInfo.put(TransmissionType.ENERGY, LightConfigInfo.INPUT_ONLY);
        return new AttachedSideConfig(configInfo);
    });

    public static final AttachedSideConfig CRYSTALLIZER = Util.make(() -> {
        Map<TransmissionType, LightConfigInfo> configInfo = new EnumMap<>(TransmissionType.class);
        configInfo.put(TransmissionType.ITEM, LightConfigInfo.MACHINE);
        configInfo.put(TransmissionType.CHEMICAL, LightConfigInfo.INPUT_ONLY);
        configInfo.put(TransmissionType.ENERGY, LightConfigInfo.INPUT_ONLY);
        return new AttachedSideConfig(configInfo);
    });

    public static final AttachedSideConfig DISSOLUTION = Util.make(() -> {
        Map<TransmissionType, LightConfigInfo> configInfo = new EnumMap<>(TransmissionType.class);
        configInfo.put(TransmissionType.ITEM, LightConfigInfo.EXTRA_MACHINE);
        configInfo.put(TransmissionType.CHEMICAL, LightConfigInfo.OUT_EJECT);
        configInfo.put(TransmissionType.ENERGY, LightConfigInfo.INPUT_ONLY);
        return new AttachedSideConfig(configInfo);
    });

    public static final AttachedSideConfig CHEMICAL_INFUSING = Util.make(() -> {
        Map<TransmissionType, LightConfigInfo> configInfo = new EnumMap<>(TransmissionType.class);
        configInfo.put(TransmissionType.ITEM, LightConfigInfo.TWO_INPUT_ITEM);
        configInfo.put(TransmissionType.CHEMICAL, LightConfigInfo.TWO_INPUT_AND_OUT);
        configInfo.put(TransmissionType.ENERGY, LightConfigInfo.INPUT_ONLY);
        return new AttachedSideConfig(configInfo);
    });

    public static final AttachedSideConfig PIGMENT_MIXER = Util.make(() -> {
        Map<TransmissionType, LightConfigInfo> configInfo = new EnumMap<>(TransmissionType.class);
        configInfo.put(TransmissionType.ITEM, LightConfigInfo.TWO_INPUT_ITEM);
        configInfo.put(TransmissionType.CHEMICAL, LightConfigInfo.TWO_INPUT_AND_OUT);
        configInfo.put(TransmissionType.ENERGY, LightConfigInfo.INPUT_ONLY_NO_TOP);
        return new AttachedSideConfig(configInfo);
    });

    public static final AttachedSideConfig CHEMICAL_OUT_MACHINE = Util.make(() -> {
        Map<TransmissionType, LightConfigInfo> configInfo = new EnumMap<>(TransmissionType.class);
        configInfo.put(TransmissionType.ITEM, LightConfigInfo.MACHINE);
        configInfo.put(TransmissionType.CHEMICAL, LightConfigInfo.RIGHT_OUTPUT);
        configInfo.put(TransmissionType.ENERGY, LightConfigInfo.INPUT_ONLY);
        return new AttachedSideConfig(configInfo);
    });

    public static final AttachedSideConfig PAINTING = Util.make(() -> {
        Map<TransmissionType, LightConfigInfo> configInfo = new EnumMap<>(TransmissionType.class);
        configInfo.put(TransmissionType.ITEM, LightConfigInfo.EXTRA_MACHINE);
        configInfo.put(TransmissionType.CHEMICAL, LightConfigInfo.INPUT_ONLY);
        configInfo.put(TransmissionType.ENERGY, LightConfigInfo.INPUT_ONLY);
        return new AttachedSideConfig(configInfo);
    });

    public static final AttachedSideConfig WASHER = Util.make(() -> {
        Map<TransmissionType, LightConfigInfo> configInfo = new EnumMap<>(TransmissionType.class);
        configInfo.put(TransmissionType.ITEM, LightConfigInfo.MACHINE);
        configInfo.put(TransmissionType.CHEMICAL, LightConfigInfo.OUT_EJECT);
        configInfo.put(TransmissionType.FLUID, LightConfigInfo.INPUT_ONLY);
        configInfo.put(TransmissionType.ENERGY, LightConfigInfo.INPUT_ONLY);
        return new AttachedSideConfig(configInfo);
    });

    public static final AttachedSideConfig SEPARATOR = Util.make(() -> {
        Map<TransmissionType, LightConfigInfo> configInfo = new EnumMap<>(TransmissionType.class);
        configInfo.put(TransmissionType.ITEM, LightConfigInfo.TWO_OUTPUT_ITEM);
        configInfo.put(TransmissionType.CHEMICAL, LightConfigInfo.TWO_OUTPUT);
        configInfo.put(TransmissionType.FLUID, LightConfigInfo.INPUT_ONLY);
        configInfo.put(TransmissionType.ENERGY, LightConfigInfo.INPUT_ONLY);
        return new AttachedSideConfig(configInfo);
    });

    public static final AttachedSideConfig CENTRIFUGE = Util.make(() -> {
        Map<TransmissionType, LightConfigInfo> configInfo = new EnumMap<>(TransmissionType.class);
        configInfo.put(TransmissionType.ITEM, LightConfigInfo.without(LightConfigInfo.MACHINE, RelativeSide.TOP));
        configInfo.put(TransmissionType.CHEMICAL, LightConfigInfo.FRONT_OUT_EJECT_NO_TOP);
        configInfo.put(TransmissionType.ENERGY, LightConfigInfo.without(LightConfigInfo.INPUT_ONLY, RelativeSide.TOP));
        return new AttachedSideConfig(configInfo);
    });

    public static final AttachedSideConfig SNA = Util.make(() -> {
        Map<TransmissionType, LightConfigInfo> configInfo = new EnumMap<>(TransmissionType.class);
        configInfo.put(TransmissionType.ITEM, LightConfigInfo.without(LightConfigInfo.OUT_NO_EJECT, RelativeSide.TOP));
        configInfo.put(TransmissionType.CHEMICAL, LightConfigInfo.FRONT_OUT_EJECT_NO_TOP);
        return new AttachedSideConfig(configInfo);
    });

    public static final AttachedSideConfig LIQUIFIER = Util.make(() -> {
        Map<TransmissionType, LightConfigInfo> configInfo = new EnumMap<>(TransmissionType.class);
        configInfo.put(TransmissionType.ITEM, LightConfigInfo.MACHINE);
        configInfo.put(TransmissionType.FLUID, LightConfigInfo.RIGHT_OUTPUT);
        configInfo.put(TransmissionType.ENERGY, LightConfigInfo.INPUT_ONLY);
        return new AttachedSideConfig(configInfo);
    });

    public static final AttachedSideConfig REACTION = Util.make(() -> {
        Map<TransmissionType, LightConfigInfo> configInfo = new EnumMap<>(TransmissionType.class);
        configInfo.put(TransmissionType.ITEM, LightConfigInfo.MACHINE);
        configInfo.put(TransmissionType.FLUID, LightConfigInfo.INPUT_ONLY);
        configInfo.put(TransmissionType.CHEMICAL, LightConfigInfo.OUT_EJECT);
        configInfo.put(TransmissionType.ENERGY, LightConfigInfo.INPUT_ONLY);
        return new AttachedSideConfig(configInfo);
    });

    public static final AttachedSideConfig ROTARY = Util.make(() -> {
        Map<TransmissionType, LightConfigInfo> configInfo = new EnumMap<>(TransmissionType.class);
        configInfo.put(TransmissionType.ITEM, LightConfigInfo.MACHINE);
        configInfo.put(TransmissionType.CHEMICAL, LightConfigInfo.OUT_EJECT_LEFT);
        configInfo.put(TransmissionType.FLUID, LightConfigInfo.OUT_EJECT);
        configInfo.put(TransmissionType.ENERGY, LightConfigInfo.INPUT_ONLY);
        return new AttachedSideConfig(configInfo);
    });

    public static AttachedSideConfig create(Map<TransmissionType, ConfigInfo> configInfo) {
        Map<TransmissionType, LightConfigInfo> lightConfigInfo = new EnumMap<>(TransmissionType.class);
        for (Map.Entry<TransmissionType, ConfigInfo> entry : configInfo.entrySet()) {
            ConfigInfo info = entry.getValue();
            Map<RelativeSide, DataType> sideConfig = new EnumMap<>(RelativeSide.class);
            for (Map.Entry<RelativeSide, DataType> sideConfigEntry : info.getSideConfig()) {
                DataType type = sideConfigEntry.getValue();
                if (type != DataType.NONE) {
                    //Our defaults do not initialize nones, so to ensure we don't have a component patch. We need to
                    // skip adding none to the created side config
                    sideConfig.put(sideConfigEntry.getKey(), type);
                }
            }
            lightConfigInfo.put(entry.getKey(), new LightConfigInfo(sideConfig, info.isEjecting()));
        }
        return new AttachedSideConfig(lightConfigInfo);
    }

    public AttachedSideConfig {
        //Make the map unmodifiable to ensure we don't accidentally mutate it
        configInfo = Collections.unmodifiableMap(configInfo);
    }

    public static IPersistentConfigInfo getStoredConfigInfo(ItemStack stack, AttachedSideConfig fallback, TransmissionType transmissionType) {
        AttachedSideConfig existingData = stack.getOrDefault(MekanismDataComponents.SIDE_CONFIG, fallback);
        LightConfigInfo config = existingData.configInfo.get(transmissionType);
        if (config.sideConfig.isEmpty()) {
            if (existingData == fallback) {
                throw new IllegalStateException("Expected there to be a side config for " + transmissionType.getTransmission() + " but there wasn't");
            }
        }
        return config;
    }

    @Nullable
    public IPersistentConfigInfo getConfig(TransmissionType transmissionType) {
        return this.configInfo.get(transmissionType);
    }

    public record LightConfigInfo(Map<RelativeSide, DataType> sideConfig, boolean isEjecting) implements IPersistentConfigInfo {

        public static final Codec<LightConfigInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
              Codec.unboundedMap(RelativeSide.CODEC, DataType.CODEC).fieldOf(SerializationConstants.SIDE).forGetter(LightConfigInfo::sideConfig),
              Codec.BOOL.optionalFieldOf(SerializationConstants.EJECT, false).forGetter(LightConfigInfo::isEjecting)
        ).apply(instance, LightConfigInfo::new));
        public static final StreamCodec<ByteBuf, LightConfigInfo> STREAM_CODEC = StreamCodec.composite(
              ByteBufCodecs.map(i -> new EnumMap<>(RelativeSide.class), RelativeSide.STREAM_CODEC, DataType.STREAM_CODEC), LightConfigInfo::sideConfig,
              ByteBufCodecs.BOOL, LightConfigInfo::isEjecting,
              LightConfigInfo::new
        );

        //TODO: Do we want to try and come up with a better naming scheme?
        public static final LightConfigInfo MACHINE = create(RelativeSide.RIGHT, null, RelativeSide.BACK, false);
        public static final LightConfigInfo EXTRA_MACHINE = create(RelativeSide.RIGHT, RelativeSide.BOTTOM, RelativeSide.BACK, false);
        public static final LightConfigInfo INPUT_ONLY = create(null, null, null, false);
        public static final LightConfigInfo OUT_NO_EJECT = create(RelativeSide.RIGHT, null, null, false);
        public static final LightConfigInfo OUT_EJECT = create(RelativeSide.RIGHT, null, null, true);
        public static final LightConfigInfo OUT_EJECT_LEFT = create(RelativeSide.LEFT, null, null, true);
        public static final LightConfigInfo FRONT_OUT_NO_EJECT = create(RelativeSide.FRONT, null, null, false);
        public static final LightConfigInfo FRONT_OUT_EJECT = create(RelativeSide.FRONT, null, null, true);
        public static final LightConfigInfo RIGHT_OUTPUT = new LightConfigInfo(Map.of(RelativeSide.RIGHT, DataType.OUTPUT), true);

        public static final LightConfigInfo FRONT_OUT_EJECT_NO_TOP = without(FRONT_OUT_EJECT, RelativeSide.TOP);
        public static final LightConfigInfo INPUT_ONLY_NO_TOP = without(INPUT_ONLY, RelativeSide.TOP);

        public static final LightConfigInfo INPUT_OUT_ALL = Util.make(() -> {
            Map<RelativeSide, DataType> sideConfig = new EnumMap<>(RelativeSide.class);
            for (RelativeSide side : EnumUtils.SIDES) {
                sideConfig.put(side, DataType.INPUT_OUTPUT);
            }
            return new LightConfigInfo(sideConfig, false);
        });

        public static final LightConfigInfo TWO_OUTPUT = Util.make(() -> {
            Map<RelativeSide, DataType> sideConfig = new EnumMap<>(RelativeSide.class);
            sideConfig.put(RelativeSide.LEFT, DataType.OUTPUT_1);
            sideConfig.put(RelativeSide.RIGHT, DataType.OUTPUT_2);
            return new LightConfigInfo(sideConfig, true);
        });
        public static final LightConfigInfo TWO_OUTPUT_ITEM = Util.make(() -> {
            Map<RelativeSide, DataType> sideConfig = new EnumMap<>(RelativeSide.class);
            sideConfig.put(RelativeSide.FRONT, DataType.INPUT);
            sideConfig.put(RelativeSide.LEFT, DataType.OUTPUT_1);
            sideConfig.put(RelativeSide.RIGHT, DataType.OUTPUT_2);
            sideConfig.put(RelativeSide.BACK, DataType.ENERGY);
            return new LightConfigInfo(sideConfig, false);
        });

        public static final LightConfigInfo TWO_INPUT_AND_OUT = Util.make(() -> {
            Map<RelativeSide, DataType> sideConfig = new EnumMap<>(RelativeSide.class);
            sideConfig.put(RelativeSide.LEFT, DataType.INPUT_1);
            sideConfig.put(RelativeSide.RIGHT, DataType.INPUT_2);
            sideConfig.put(RelativeSide.FRONT, DataType.OUTPUT);
            return new LightConfigInfo(sideConfig, true);
        });
        public static final LightConfigInfo TWO_INPUT_ITEM = Util.make(() -> {
            Map<RelativeSide, DataType> sideConfig = new EnumMap<>(RelativeSide.class);
            sideConfig.put(RelativeSide.LEFT, DataType.INPUT_1);
            sideConfig.put(RelativeSide.RIGHT, DataType.INPUT_2);
            sideConfig.put(RelativeSide.FRONT, DataType.OUTPUT);
            sideConfig.put(RelativeSide.BACK, DataType.ENERGY);
            return new LightConfigInfo(sideConfig, false);
        });

        private static LightConfigInfo without(LightConfigInfo config, RelativeSide... sides) {
            Map<RelativeSide, DataType> sideConfig = new EnumMap<>(config.sideConfig());
            for (RelativeSide side : sides) {
                sideConfig.remove(side);
            }
            return new LightConfigInfo(sideConfig, config.isEjecting());
        }

        private static LightConfigInfo create(@Nullable RelativeSide output, @Nullable RelativeSide extra, @Nullable RelativeSide energy, boolean ejecting) {
            Map<RelativeSide, DataType> sideConfig = new EnumMap<>(RelativeSide.class);
            for (RelativeSide side : EnumUtils.SIDES) {
                sideConfig.put(side, DataType.INPUT);
            }
            if (output != null) {
                if (output == extra || output == energy) {
                    throw new IllegalArgumentException("Duplicate sides specified for different data types");
                }
                sideConfig.put(output, DataType.OUTPUT);
            }
            if (extra != null) {
                if (extra == energy) {
                    throw new IllegalArgumentException("Duplicate sides specified for different data types");
                }
                sideConfig.put(extra, DataType.EXTRA);
            }
            if (energy != null) {
                sideConfig.put(energy, DataType.ENERGY);
            }
            return new LightConfigInfo(sideConfig, ejecting);
        }

        public LightConfigInfo {
            //Make the map unmodifiable to ensure we don't accidentally mutate it
            sideConfig = Collections.unmodifiableMap(sideConfig);
        }

        @NotNull
        @Override
        public DataType getDataType(@NotNull RelativeSide side) {
            return sideConfig.getOrDefault(side, DataType.NONE);
        }
    }
}