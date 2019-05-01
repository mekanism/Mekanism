package mekanism.common.network;

import io.netty.buffer.ByteBuf;
import mekanism.common.Mekanism;
import mekanism.common.base.IModule;
import mekanism.common.config.MekanismConfig;
import mekanism.common.network.PacketConfigSync.ConfigSyncMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PacketConfigSync implements IMessageHandler<ConfigSyncMessage, IMessage> {

    private static final Logger LOGGER = LogManager.getLogger("Mekanism-PacketConfigSync");

    @Override
    public IMessage onMessage(ConfigSyncMessage message, MessageContext context) {
        MekanismConfig.setSyncedConfig(message.config);
        Mekanism.proxy.onConfigSync(true);
        return null;
    }

    public static class ConfigSyncMessage implements IMessage {

        private MekanismConfig config;

        public ConfigSyncMessage() {
            config = new MekanismConfig();
            config.client = null;
        }

        public ConfigSyncMessage(MekanismConfig config) {
            this.config = config;
        }

        @Override
        public void toBytes(ByteBuf dataStream) {
            config.general.write(dataStream);
            config.usage.write(dataStream);
            config.storage.write(dataStream);

            try {
                for (IModule module : Mekanism.modulesLoaded) {
                    module.writeConfig(dataStream, config);
                }
            } catch (Exception e) {
                LOGGER.fatal("Something went wrong", e);
            }
        }

        @Override
        public void fromBytes(ByteBuf dataStream) {
            config.general.read(dataStream);
            config.usage.read(dataStream);
            config.storage.read(dataStream);

            try {
                for (IModule module : Mekanism.modulesLoaded) {
                    module.readConfig(dataStream, config);
                }
            } catch (Exception e) {
                LOGGER.fatal("Something went wrong", e);
            }
        }
    }
}
