package mekanism.common.network.to_client.configuration;

import java.util.function.Consumer;
import mekanism.common.Mekanism;
import mekanism.common.network.to_client.security.PacketBatchSecurityUpdate;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.configuration.ICustomConfigurationTask;
import org.jetbrains.annotations.NotNull;

public record SyncAllSecurityData(ServerConfigurationPacketListener listener) implements ICustomConfigurationTask {

    private static final Identifier ID = Mekanism.rl("sync_security");
    private static final Type TYPE = new Type(ID);

    @Override
    public void run(Consumer<CustomPacketPayload> sender) {
        sender.accept(new PacketBatchSecurityUpdate());
        listener().finishCurrentTask(type());
    }

    @NotNull
    @Override
    public Type type() {
        return TYPE;
    }
}