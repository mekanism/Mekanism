package mekanism.common.network.to_server;

import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.gear.ModuleData;
import mekanism.api.gear.config.ModuleBooleanData;
import mekanism.api.gear.config.ModuleColorData;
import mekanism.api.gear.config.ModuleConfigData;
import mekanism.api.gear.config.ModuleEnumData;
import mekanism.api.gear.config.ModuleIntegerData;
import mekanism.api.math.MathUtils;
import mekanism.common.Mekanism;
import mekanism.common.content.gear.Module;
import mekanism.common.content.gear.ModuleConfigItem;
import mekanism.common.content.gear.ModuleHelper;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.util.ByIdMap;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//TODO: Eventually it would be nice to make this more generic in terms of how it can sync module data so that we can support custom types
// though given the module tweaker screen doesn't currently have a way to support custom types it isn't that big a deal to make this support it yet either
@NothingNullByDefault
public record PacketUpdateModuleSettings(int slotId, ModuleData<?> moduleType, String data, TypedValue value) implements IMekanismPacket {

    public static final CustomPacketPayload.Type<PacketUpdateModuleSettings> TYPE = new CustomPacketPayload.Type<>(Mekanism.rl("update_module"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketUpdateModuleSettings> STREAM_CODEC = StreamCodec.composite(
          ByteBufCodecs.VAR_INT, PacketUpdateModuleSettings::slotId,
          ByteBufCodecs.registry(MekanismAPI.MODULE_REGISTRY_NAME), PacketUpdateModuleSettings::moduleType,
          ByteBufCodecs.STRING_UTF8, PacketUpdateModuleSettings::data,
          TypedValue.STREAM_CODEC, PacketUpdateModuleSettings::value,
          PacketUpdateModuleSettings::new
    );

    private PacketUpdateModuleSettings(int slotId, ModuleData<?> moduleType, String data, ModuleDataType dataType, Object value) {
        this(slotId, moduleType, data, new TypedValue(dataType, value));
    }

    public static PacketUpdateModuleSettings create(int slotId, ModuleData<?> moduleType, ModuleConfigItem<?> configItem) {
        ModuleConfigData<?> configData = configItem.getData();
        if (configData instanceof ModuleEnumData<?> enumData) {
            return new PacketUpdateModuleSettings(slotId, moduleType, configItem.getName(), ModuleDataType.ENUM, enumData.get().ordinal());
        }
        for (ModuleDataType type : ModuleDataType.VALUES) {
            if (type.typeMatches(configData)) {
                return new PacketUpdateModuleSettings(slotId, moduleType, configItem.getName(), type, configData.get());
            }
        }
        throw new IllegalArgumentException("Unknown config data type for config with name: " + configItem.getName());
    }

    @NotNull
    @Override
    public CustomPacketPayload.Type<PacketUpdateModuleSettings> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        if (!data.isBlank()) {
            ItemStack stack = context.player().getInventory().getItem(slotId);
            Module<?> module = ModuleHelper.get().load(stack, moduleType);
            if (module != null) {
                setValue(module.getConfigItem(data));
            }
        }
    }

    private <TYPE> void setValue(@Nullable ModuleConfigItem<TYPE> moduleConfigItem) {
        if (moduleConfigItem != null) {
            ModuleConfigData<TYPE> configData = moduleConfigItem.getData();
            if (configData instanceof ModuleEnumData && value.type() == ModuleDataType.ENUM) {
                moduleConfigItem.set((TYPE) MathUtils.getByIndexMod(((ModuleEnumData<?>) configData).getEnums(), (int) value.value()));
            } else if (value.type().typeMatches(configData)) {
                //noinspection unchecked
                moduleConfigItem.set((TYPE) value.value());
            }
        }
    }

    private enum ModuleDataType {
        BOOLEAN(data -> data instanceof ModuleBooleanData, ByteBufCodecs.BOOL),
        //Must be above integer, so it uses the color type as ModuleColorData extends ModuleIntegerData
        COLOR(data -> data instanceof ModuleColorData, ByteBufCodecs.INT),
        INTEGER(data -> data instanceof ModuleIntegerData, ByteBufCodecs.VAR_INT),
        ENUM(data -> data instanceof ModuleEnumData, ByteBufCodecs.VAR_INT);

        public static final IntFunction<ModuleDataType> BY_ID = ByIdMap.continuous(ModuleDataType::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        public static final StreamCodec<ByteBuf, ModuleDataType> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, ModuleDataType::ordinal);

        //DO NOT MODIFY
        private static final ModuleDataType[] VALUES = values();

        private final Predicate<ModuleConfigData<?>> configDataPredicate;
        private final StreamCodec<ByteBuf, Object> streamCodec;

        ModuleDataType(Predicate<ModuleConfigData<?>> configDataPredicate, StreamCodec<ByteBuf, ?> streamCodec) {
            this.configDataPredicate = configDataPredicate;
            this.streamCodec = (StreamCodec<ByteBuf, Object>) streamCodec;
        }

        public boolean typeMatches(ModuleConfigData<?> data) {
            return configDataPredicate.test(data);
        }

        public StreamCodec<ByteBuf, Object> streamCodec() {
            return streamCodec;
        }
    }

    private record TypedValue(ModuleDataType type, Object value) {

        private static final StreamCodec<ByteBuf, TypedValue> STREAM_CODEC = ModuleDataType.STREAM_CODEC.dispatch(TypedValue::type,
              type -> type.streamCodec().map(value -> new TypedValue(type, value), TypedValue::value));
    }
}