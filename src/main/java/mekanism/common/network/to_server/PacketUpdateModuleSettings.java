package mekanism.common.network.to_server;

import io.netty.handler.codec.DecoderException;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.gear.ModuleData;
import mekanism.api.gear.config.ModuleConfig;
import mekanism.common.Mekanism;
import mekanism.common.content.gear.Module;
import mekanism.common.content.gear.ModuleContainer;
import mekanism.common.content.gear.ModuleHelper;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

//TODO: Eventually it would be nice to make this more generic in terms of how it can sync module data so that we can support custom types
// though given the module tweaker screen doesn't currently have a way to support custom types it isn't that big a deal to make this support it yet either
@NothingNullByDefault
public record PacketUpdateModuleSettings(int slotId, ModuleConfigTarget<?> target) implements IMekanismPacket {

    public static final CustomPacketPayload.Type<PacketUpdateModuleSettings> TYPE = new CustomPacketPayload.Type<>(Mekanism.rl("update_module"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketUpdateModuleSettings> STREAM_CODEC = StreamCodec.composite(
          ByteBufCodecs.VAR_INT, PacketUpdateModuleSettings::slotId,
          ModuleConfigTarget.STREAM_CODEC, PacketUpdateModuleSettings::target,
          PacketUpdateModuleSettings::new
    );

    public static PacketUpdateModuleSettings create(int slotId, ModuleData<?> moduleType, int installed, ModuleConfig<?> config) {
        return new PacketUpdateModuleSettings(slotId, new ModuleConfigTarget<>(moduleType, installed, config));
    }

    @NotNull
    @Override
    public CustomPacketPayload.Type<PacketUpdateModuleSettings> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        ItemStack stack = context.player().getInventory().getItem(slotId);
        ModuleContainer container = ModuleHelper.get().getModuleContainer(stack);
        if (container != null) {
            Module<?> module = container.get(target.moduleType());
            if (module != null) {
                //TODO - 1.20.5: Validate the config is at a valid level/in bounds for it, such as if it is a limited range enum value
                // maybe we want the server to do a config.with itself?
                container.replaceModuleConfig(stack, target.moduleType(), target.config());
            }
        }
    }

    public record ModuleConfigTarget<C>(ModuleData<?> moduleType, int installed, ModuleConfig<C> config) {

        private static final StreamCodec<RegistryFriendlyByteBuf, ModuleData<?>> REGISTRY_CODEC = ByteBufCodecs.registry(MekanismAPI.MODULE_REGISTRY_NAME);
        public static final StreamCodec<RegistryFriendlyByteBuf, ModuleConfigTarget<?>> STREAM_CODEC = StreamCodec.ofMember(ModuleConfigTarget::encode, ModuleConfigTarget::decode);

        private static ModuleConfigTarget<?> decode(RegistryFriendlyByteBuf buffer) {
            ModuleData<?> moduleType = REGISTRY_CODEC.decode(buffer);
            int installed = buffer.readVarInt();
            String name = buffer.readUtf();
            ModuleConfig<?> defaultConfig = moduleType.getNamedConfig(installed, name);
            if (defaultConfig == null) {
                throw new DecoderException("Unknown config " + name + " for module type: " + moduleType + " with " + installed + " modules installed");
            }
            return new ModuleConfigTarget<>(moduleType, installed, defaultConfig.namedStreamCodec(name).decode(buffer));
        }

        private void encode(RegistryFriendlyByteBuf buffer) {
            REGISTRY_CODEC.encode(buffer, moduleType);
            buffer.writeVarInt(installed);
            buffer.writeUtf(config.name());
            config.namedStreamCodec(config.name()).encode(buffer, config);
        }
    }
}