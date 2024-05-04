package mekanism.common.network.to_server;

import io.netty.handler.codec.DecoderException;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.gear.ModuleData;
import mekanism.api.gear.config.ModuleConfig;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
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
        //Validate the container still has the container, and it didn't end up somehow getting removed by the time the server received the packet
        if (container != null && container.has(target.moduleType())) {
            try {
                container.replaceModuleConfig(stack, target.moduleType(), target.config(), true);
            } catch (IllegalArgumentException | IllegalStateException e) {
                //If the packet is invalid, for example if a config got sent setting to an enum value that is not in range
                // or if a module config with the given name couldn't be found
                context.disconnect(MekanismLang.INVALID_PACKET.translate(e.getMessage()));
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