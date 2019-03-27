package mekanism.common.network;

import io.netty.buffer.ByteBuf;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.network.PacketKey.KeyMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketKey implements IMessageHandler<KeyMessage, IMessage> {

    @Override
    public IMessage onMessage(KeyMessage message, MessageContext context) {
        if (message.add) {
            Mekanism.keyMap.add(PacketHandler.getPlayer(context), message.key);
        } else {
            Mekanism.keyMap.remove(PacketHandler.getPlayer(context), message.key);
        }

        return null;
    }

    public static class KeyMessage implements IMessage {

        public int key;
        public boolean add;

        public KeyMessage() {
        }

        public KeyMessage(int k, boolean a) {
            key = k;
            add = a;
        }

        @Override
        public void toBytes(ByteBuf dataStream) {
            dataStream.writeInt(key);
            dataStream.writeBoolean(add);
        }

        @Override
        public void fromBytes(ByteBuf dataStream) {
            key = dataStream.readInt();
            add = dataStream.readBoolean();
        }
    }
}
