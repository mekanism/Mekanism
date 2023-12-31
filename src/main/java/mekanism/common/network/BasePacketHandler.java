package mekanism.common.network;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import mekanism.common.lib.Version;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.handling.ConfigurationPayloadContext;
import net.neoforged.neoforge.network.handling.IConfigurationPayloadHandler;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.handling.IPlayPayloadHandler;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import net.neoforged.neoforge.network.registration.IDirectionAwarePayloadHandlerBuilder;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;
import org.jetbrains.annotations.NotNull;

public abstract class BasePacketHandler {

    protected BasePacketHandler(IEventBus modEventBus, String modid, Version version) {
        modEventBus.addListener(RegisterPayloadHandlerEvent.class, event -> {
            IPayloadRegistrar registrar = event.registrar(modid)
                  .versioned(version.toString());
            registerClientToServer(new PacketRegistrar(registrar, IDirectionAwarePayloadHandlerBuilder::server));
            registerServerToClient(new PacketRegistrar(registrar, IDirectionAwarePayloadHandlerBuilder::client));
        });
    }

    protected abstract void registerClientToServer(PacketRegistrar registrar);

    protected abstract void registerServerToClient(PacketRegistrar registrar);

    @FunctionalInterface
    private interface ContextAwareHandler {

        <PAYLOAD extends CustomPacketPayload, HANDLER> IDirectionAwarePayloadHandlerBuilder<PAYLOAD, HANDLER> accept(IDirectionAwarePayloadHandlerBuilder<PAYLOAD, HANDLER> builder, HANDLER handler);
    }

    protected record PacketRegistrar(IPayloadRegistrar registrar, ContextAwareHandler contextAwareHandler) {

        private <MSG extends IMekanismPacket<IPayloadContext>> void common(ResourceLocation id, FriendlyByteBuf.Reader<MSG> reader, IPayloadHandler<MSG> handler) {
            registrar.common(id, reader, builder -> contextAwareHandler.accept(builder, handler));
        }

        public <MSG extends IMekanismPacket<IPayloadContext>> void common(ResourceLocation id, FriendlyByteBuf.Reader<MSG> reader) {
            common(id, reader, IMekanismPacket::handleMainThread);
        }

        public <MSG extends IMekanismPacket<IPayloadContext>> void commonNetworkThread(ResourceLocation id, FriendlyByteBuf.Reader<MSG> reader) {
            common(id, reader, IMekanismPacket::handle);
        }

        public IMekanismPacket<IPayloadContext> commonInstanced(ResourceLocation id, Consumer<IPayloadContext> handler) {
            return instanced(id, handler, this::common);
        }

        private <MSG extends IMekanismPacket<ConfigurationPayloadContext>> void configuration(ResourceLocation id, FriendlyByteBuf.Reader<MSG> reader, IConfigurationPayloadHandler<MSG> handler) {
            registrar.configuration(id, reader, builder -> contextAwareHandler.accept(builder, handler));
        }

        public void configuration(ResourceLocation id, FriendlyByteBuf.Reader<? extends IMekanismPacket<ConfigurationPayloadContext>> reader) {
            configuration(id, reader, IMekanismPacket::handleMainThread);
        }

        public void configurationNetworkThread(ResourceLocation id, FriendlyByteBuf.Reader<? extends IMekanismPacket<ConfigurationPayloadContext>> reader) {
            configuration(id, reader, IMekanismPacket::handle);
        }

        public IMekanismPacket<ConfigurationPayloadContext> configurationInstanced(ResourceLocation id, Consumer<ConfigurationPayloadContext> handler) {
            return instanced(id, handler, this::configuration);
        }

        private <MSG extends IMekanismPacket<PlayPayloadContext>> void play(ResourceLocation id, FriendlyByteBuf.Reader<MSG> reader, IPlayPayloadHandler<MSG> handler) {
            registrar.play(id, reader, builder -> contextAwareHandler.accept(builder, handler));
        }

        public void play(ResourceLocation id, FriendlyByteBuf.Reader<? extends IMekanismPacket<PlayPayloadContext>> reader) {
            play(id, reader, IMekanismPacket::handleMainThread);
        }

        public void playNetworkThread(ResourceLocation id, FriendlyByteBuf.Reader<? extends IMekanismPacket<PlayPayloadContext>> reader) {
            play(id, reader, IMekanismPacket::handle);
        }

        public IMekanismPacket<PlayPayloadContext> playInstanced(ResourceLocation id, Consumer<PlayPayloadContext> handler) {
            return instanced(id, handler, this::play);
        }

        private <CONTEXT extends IPayloadContext> IMekanismPacket<CONTEXT> instanced(ResourceLocation id, Consumer<CONTEXT> handler,
              BiConsumer<ResourceLocation, FriendlyByteBuf.Reader<IMekanismPacket<CONTEXT>>> registerMethod) {
            IMekanismPacket<CONTEXT> instance = new IMekanismPacket<>() {
                @Override
                public void write(@NotNull FriendlyByteBuf buf) {
                }

                @NotNull
                @Override
                public ResourceLocation id() {
                    return id;
                }

                @Override
                public void handle(CONTEXT context) {
                    handler.accept(context);
                }
            };
            registerMethod.accept(id, buf -> instance);
            return instance;
        }
    }
}