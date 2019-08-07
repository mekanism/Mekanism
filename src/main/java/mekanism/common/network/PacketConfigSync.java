package mekanism.common.network;

import java.util.function.Supplier;
import mekanism.common.Mekanism;
import mekanism.common.base.IModule;
import mekanism.common.config.MekanismConfig;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PacketConfigSync {

    private static final Logger LOGGER = LogManager.getLogger("Mekanism-PacketConfigSync");

    private MekanismConfig config;

    public PacketConfigSync(MekanismConfig config) {
        this.config = config;
    }

    public static void handle(PacketConfigSync message, Supplier<Context> context) {
        MekanismConfig.setSyncedConfig(message.config);
        Mekanism.proxy.onConfigSync(true);
    }

    public static void encode(PacketConfigSync pkt, PacketBuffer buf) {
        pkt.config.general.write(buf);
        pkt.config.usage.write(buf);
        pkt.config.storage.write(buf);
        try {
            for (IModule module : Mekanism.modulesLoaded) {
                module.writeConfig(buf, pkt.config);
            }
        } catch (Exception e) {
            LOGGER.fatal("Something went wrong", e);
        }
    }

    public static PacketConfigSync decode(PacketBuffer buf) {
        MekanismConfig config = new MekanismConfig();
        config.client = null;

        config.general.read(buf);
        config.usage.read(buf);
        config.storage.read(buf);
        try {
            for (IModule module : Mekanism.modulesLoaded) {
                module.readConfig(buf, config);
            }
        } catch (Exception e) {
            LOGGER.fatal("Something went wrong", e);
        }
        return new PacketConfigSync(config);
    }
}